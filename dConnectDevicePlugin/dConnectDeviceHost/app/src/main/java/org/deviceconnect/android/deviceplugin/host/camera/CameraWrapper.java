/*
 CameraWrapper.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * カメラ操作クラス.
 *
 * @author NTT DOCOMO, INC.
 */
@SuppressWarnings("MissingPermission")
public class CameraWrapper {

    public enum CameraEvent {
        SHUTTERED,
        STARTED_VIDEO_RECORDING,
        STOPPED_VIDEO_RECORDING
    }

    public interface CameraEventListener {
        void onEvent(CameraEvent event);
    }

    private static class CameraEventListenerHolder {
        private final CameraEventListener mListener;
        private final Handler mHandler;

        CameraEventListenerHolder(final @NonNull CameraEventListener listener,
                                  final @NonNull Handler handler) {
            mListener = listener;
            mHandler = handler;
        }

        void notifyEvent(final CameraEvent event) {
            mHandler.post(() -> mListener.onEvent(event));
        }
    }

    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "host.dplugin";

    private final String mCameraId;
    private final CameraManager mCameraManager;
    private final HandlerThread mSessionConfigurationThread = new HandlerThread("session-config");
    private final Handler mBackgroundHandler;
    private final Handler mSessionConfigurationHandler;
    private final ImageReader mPlaceHolderPreviewReader;
    private final Options mOptions;

    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCaptureSession;

    private boolean mIsTakingStillImage;
    private boolean mIsPreview;
    private boolean mIsRecording;
    private boolean mIsTouchOn;
    private boolean mUseTouch;

    private Surface mStillImageSurface;
    private Surface mPreviewSurface;
    private Surface mRecordingSurface;
    private Surface mTargetSurface;

    private List<Surface> mTargetSurfaces;

    private CameraEventListenerHolder mCameraEventListenerHolder;
    private final Context mContext;

    CameraWrapper(final @NonNull Context context,
                  final @NonNull String cameraId) throws CameraAccessException {
        mContext = context;
        mCameraId = cameraId;
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        mOptions = initOptions();
        mBackgroundHandler = new Handler(Looper.getMainLooper());
        mSessionConfigurationThread.start();
        mSessionConfigurationHandler = new Handler(mSessionConfigurationThread.getLooper());
        mPlaceHolderPreviewReader = createImageReader(mOptions.getPreviewSize(), ImageFormat.YUV_420_888);
        mPlaceHolderPreviewReader.setOnImageAvailableListener(reader -> {
            Image image = reader.acquireNextImage();
            if (image != null) {
                image.close();
            }
        }, mBackgroundHandler);
    }

    private void notifyCameraEvent(final CameraEvent event) {
        CameraEventListenerHolder holder = mCameraEventListenerHolder;
        if (holder != null) {
            holder.notifyEvent(event);
        }
    }

    public void setCameraEventListener(final CameraEventListener listener, final Handler handler) {
        mCameraEventListenerHolder = new CameraEventListenerHolder(listener, handler);
    }

    public String getId() {
        return mCameraId;
    }

    public int getFacing() {
        try {
            return Camera2Helper.getFacing(mCameraManager, mCameraId);
        } catch (CameraAccessException e) {
            return -1;
        }
    }

    public boolean isTakingStillImage() {
        return mIsTakingStillImage;
    }

    public boolean isPreview() {
        return mIsPreview;
    }

    public boolean isRecording() {
        return mIsRecording;
    }

    public synchronized void destroy() {
        close();

        try {
            mSessionConfigurationThread.quit();
            mSessionConfigurationThread.interrupt();
        } catch (Exception e) {
            // ignore.
        }
    }

    private void close() {
        if (mCaptureSession != null) {
            try {
                mCaptureSession.close();
            } catch (Exception e) {
                // ignore
            }
            mCaptureSession = null;
        }
        if (mCameraDevice != null) {
            try {
                mCameraDevice.close();
            } catch (Exception e) {
                // ignore.
            }
            mCameraDevice = null;
        }
    }

    public Options getOptions() {
        return mOptions;
    }

    public int getSensorOrientation() {
        return Camera2Helper.getSensorOrientation(mCameraManager, mCameraId);
    }

    public ImageReader createStillImageReader(final int format) {
        return createImageReader(mOptions.getPictureSize(), format);
    }

    private ImageReader createImageReader(final Size pictureSize, final int format) {
        return Camera2Helper.createImageReader(pictureSize.getWidth(), pictureSize.getHeight(), format);
    }

    private Options initOptions() {
        Options options = new CameraWrapper.Options();

        options.mAutoFocusMode = Camera2Helper.choiceAutoFocusMode(mContext, mCameraManager, mCameraId);
        options.mAutoExposureMode = Camera2Helper.choiceAutoExposureMode(mCameraManager, mCameraId);

        options.mMaxDigitalZoom = Camera2Helper.getMaxDigitalZoom(mCameraManager, mCameraId);
        options.mSupportedStabilizationList = Camera2Helper.getSupportedStabilization(mCameraManager, mCameraId);
        options.mSupportedOpticalStabilizationList = Camera2Helper.getSupportedOpticalStabilization(mCameraManager, mCameraId);
        options.mSupportedNoiseReductionList = Camera2Helper.getSupportedNoiseReductionMode(mCameraManager, mCameraId);
        options.mMaxAutoFocusRegions = Camera2Helper.getMaxMeteringArea(mCameraManager, mCameraId);

        List<Size> supportedPictureList = Camera2Helper.getSupportedPictureSizes(mCameraManager, mCameraId);
        options.setSupportedPictureSizeList(supportedPictureList);
        options.setPictureSize(supportedPictureList.get(0));

        List<Size> supportedPreviewList = Camera2Helper.getSupportedPreviewSizes(mCameraManager, mCameraId);
        options.setSupportedPreviewSizeList(supportedPreviewList);
        options.setPreviewSize(supportedPreviewList.get(0));

        List<Range<Integer>> supportedFpsList = Camera2Helper.getSupportedFps(mCameraManager, mCameraId);
        options.setSupportedFpsList(supportedFpsList);

        List<Integer> supportedWBList = Camera2Helper.getSupportedAWB(mCameraManager, mCameraId);
        options.setSupportedWhiteBalanceList(supportedWBList);

        Size defaultSize = options.getDefaultPictureSize();
        if (defaultSize != null) {
            options.setPictureSize(defaultSize);
        }
        defaultSize = options.getDefaultPreviewSize();
        if (defaultSize != null) {
            options.setPreviewSize(defaultSize);
        }
        return options;
    }

    private synchronized CameraDevice openCamera() throws CameraWrapperException {
        if (mCameraDevice != null) {
            return mCameraDevice;
        }

        try {
            final CountDownLatch lock = new CountDownLatch(1);
            final AtomicReference<CameraDevice> cameraRef = new AtomicReference<>();
            mCameraManager.openCamera(mCameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(final @NonNull CameraDevice camera) {
                    mCameraDevice = camera;
                    cameraRef.set(camera);
                    lock.countDown();
                }

                @Override
                public void onDisconnected(final @NonNull CameraDevice camera) {
                    lock.countDown();
                }

                @Override
                public void onError(final @NonNull CameraDevice camera, int error) {
                    lock.countDown();
                }
            }, mBackgroundHandler);
            if (!lock.await(5, TimeUnit.SECONDS)) {
                throw new CameraWrapperException("Failed to open camera.");
            }
            CameraDevice camera = cameraRef.get();
            if (camera == null) {
                throw new CameraWrapperException("Failed to open camera.");
            }
            return camera;
        } catch (Exception e) {
            throw new CameraWrapperException(e);
        }
    }

    public void setTargetSurface(Surface surface) {
        mTargetSurface = surface;
    }
    public void setStillImageSurface(Surface surface) {
        mStillImageSurface = surface;
    }
    public void setPreviewSurface(Surface surface) {
        mPreviewSurface = surface;
    }
    public void setRecordingSurface(Surface surface) {
        mRecordingSurface = surface;
    }

    private List<Surface> createSurfaceList() {
        List<Surface> surfaceList = new LinkedList<>();
        if (mIsPreview) {
            if (mPreviewSurface != null) {
                surfaceList.add(mPreviewSurface);
            }
            if (mTargetSurface != null) {
                surfaceList.add(mTargetSurface);
            }
        } else {
            surfaceList.add(mPlaceHolderPreviewReader.getSurface());
        }

        if (mIsRecording) {
            surfaceList.add(mRecordingSurface);
        }

        if (mIsTakingStillImage) {
            surfaceList.add(mStillImageSurface);
        }

        return surfaceList;
    }

    private List<Surface> createSurfaceListForStillImage() {
        List<Surface> surfaceList = new LinkedList<>();
        surfaceList.add(mStillImageSurface);
        surfaceList.add(mPlaceHolderPreviewReader.getSurface());
        return surfaceList;
    }

    private synchronized CameraCaptureSession createCaptureSession(final CameraDevice cameraDevice) throws CameraWrapperException {
        return createCaptureSession(createSurfaceList(), cameraDevice);
    }

    private synchronized CameraCaptureSession createCaptureSession(final List<Surface> targets, final CameraDevice cameraDevice) throws CameraWrapperException {
        try {
            if (mCaptureSession != null) {
                try {
                    mCaptureSession.close();
                } catch (Exception e) {
                    // ignore.
                }
                mCaptureSession = null;
            }

            mTargetSurfaces = targets;

            final CountDownLatch lock = new CountDownLatch(1);
            final AtomicReference<CameraCaptureSession> sessionRef = new AtomicReference<>();
            cameraDevice.createCaptureSession(mTargetSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(final @NonNull CameraCaptureSession session) {
                    sessionRef.set(session);
                    lock.countDown();
                }

                @Override
                public void onConfigureFailed(final @NonNull CameraCaptureSession session) {
                    lock.countDown();
                }
            }, mSessionConfigurationHandler);
            if (!lock.await(5, TimeUnit.SECONDS)) {
                throw new CameraWrapperException("Failed to configure capture session.");
            }
            CameraCaptureSession session = sessionRef.get();
            if (session == null) {
                throw new CameraWrapperException("Failed to configure capture session.");
            }
            return session;
        } catch (Exception e) {
            throw new CameraWrapperException(e);
        }
    }

    private void setDefaultCaptureRequest(final CaptureRequest.Builder request) {
        setDefaultCaptureRequest(request, false);
    }

    private void setDefaultCaptureRequest(final CaptureRequest.Builder request, final boolean trigger) {
        if (mOptions.hasAutoFocus()) {
            request.set(CaptureRequest.CONTROL_AF_MODE, mOptions.mAutoFocusMode);

            if (mMeteringRectangle != null) {
                request.set(CaptureRequest.CONTROL_AF_REGIONS, new MeteringRectangle[] {mMeteringRectangle});
            }

            if (trigger) {
                request.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
            }
        }

        if (mOptions.hasAutoExposure()) {
            request.set(CaptureRequest.CONTROL_AE_MODE, mOptions.mAutoExposureMode);
        }

        if (mOptions.mWhiteBalance != null) {
            request.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
            request.set(CaptureRequest.CONTROL_AWB_MODE, mOptions.mWhiteBalance);
        }

        if (mOptions.mFps != null) {
            request.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, mOptions.mFps);
        }

        if (mOptions.mDigitalZoom != null && mOptions.mDigitalZoom > 1) {
            int w = mOptions.getPreviewSize().getWidth();
            int h = mOptions.getPreviewSize().getHeight();
            int cx = w / 2;
            int cy = h / 2;
            int hw = (int) ((w >> 1) / mOptions.mDigitalZoom);
            int hh = (int) ((h >> 1) / mOptions.mDigitalZoom);
            Rect region = new Rect(cx - hw, cy - hh, cx + hw, cy + hh);
            request.set(CaptureRequest.SCALER_CROP_REGION, region);
        }

        if (mOptions.mNoiseReductionMode != null) {
            request.set(CaptureRequest.NOISE_REDUCTION_MODE, mOptions.mNoiseReductionMode);
        }

        if (mOptions.mStabilizationMode != null) {
            request.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, mOptions.mStabilizationMode);
        }

        if (mOptions.mOpticalStabilizationMode != null) {
            request.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, mOptions.mOpticalStabilizationMode);
        }

        // Light が ON の場合は、CONTROL_AE_MODE を CONTROL_AE_MODE_ON にする。
        if (mIsTouchOn) {
            mUseTouch = true;
            request.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
            request.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
        }
    }

    public synchronized void startPreview(final Surface previewSurface, final boolean isResume) throws CameraWrapperException {
        if (mIsPreview && !isResume) {
            throw new CameraWrapperException("preview is started already.");
        }
        mIsPreview = true;
        mPreviewSurface = previewSurface;
        try {
            CameraDevice cameraDevice = openCamera();
            CameraCaptureSession captureSession = createCaptureSession(cameraDevice);
            CaptureRequest.Builder request = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            for (Surface surface : mTargetSurfaces) {
                request.addTarget(surface);
            }
            setDefaultCaptureRequest(request);
            captureSession.setRepeatingRequest(request.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
                    if (DEBUG) {
                        Log.e(TAG, "onCaptureFailed: failure=" + failure.getReason());
                    }
                }
            }, mBackgroundHandler);
            mCaptureSession = captureSession;
        } catch (CameraAccessException e) {
            throw new CameraWrapperException(e);
        }
    }

    public synchronized void stopPreview() throws CameraWrapperException {
        if (!mIsPreview) {
            return;
        }
        mIsPreview = false;
        mPreviewSurface = null;
        if (mCaptureSession != null) {
            try {
                mCaptureSession.close();
            } catch (Exception e) {
                // ignore.
            }
            mCaptureSession = null;
        }
        if (mIsRecording) {
            startRecording(mRecordingSurface, true);
        } else if (mIsTouchOn) {
            mIsTouchOn = false;
            turnOnTorch();
        } else {
            close();
        }
    }
    public synchronized void startRecording(final Surface recordingSurface,
                                            final boolean isResume) throws CameraWrapperException {
        if (mIsRecording && !isResume) {
            throw new CameraWrapperException("recording is started already.");
        }
        mIsRecording = true;
        mRecordingSurface = recordingSurface;

        try {
            CameraDevice cameraDevice = openCamera();
            CameraCaptureSession captureSession = createCaptureSession(cameraDevice);
            CaptureRequest.Builder request = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            for (Surface surface : mTargetSurfaces) {
                request.addTarget(surface);
            }
            setDefaultCaptureRequest(request);
            captureSession.setRepeatingRequest(request.build(), new CameraCaptureSession.CaptureCallback() {
                private boolean mStarted;

                @Override
                public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
                    if (!mStarted && !isResume) {
                        mStarted = true;
                        notifyCameraEvent(CameraEvent.STARTED_VIDEO_RECORDING);
                    }
                }
            }, mBackgroundHandler);
            mCaptureSession = captureSession;
        } catch (CameraAccessException e) {
            throw new CameraWrapperException(e);
        }
    }

    public synchronized void stopRecording() throws CameraWrapperException {
        if (!mIsRecording) {
            return;
        }
        mIsRecording = false;
        mRecordingSurface = null;
        if (mCaptureSession != null) {
            try {
                mCaptureSession.close();
            } catch (Exception e) {
                // ignore.
            }
            mCaptureSession = null;
        }

        close();
        if (mIsPreview) {
            startPreview(mPreviewSurface, true);
        } else if (mIsTouchOn) {
            mIsTouchOn = false;
            turnOnTorch();
        }
        notifyCameraEvent(CameraEvent.STOPPED_VIDEO_RECORDING);
    }

    public synchronized void takeStillImage(final Surface stillImageSurface) throws CameraWrapperException {
        if (DEBUG) {
            Log.d(TAG, "takeStillImage: started.");
        }
        if (mIsTakingStillImage) {
            throw new CameraWrapperException("still image is taking now.");
        }
        mIsTakingStillImage = true;
        mStillImageSurface = stillImageSurface;
        try {
            CameraDevice cameraDevice = openCamera();
            mCaptureSession = createCaptureSession(createSurfaceListForStillImage(), cameraDevice);
            // 静止画を撮影する時にプレビューが開始されていない場合には
            // プレビューの表示を行います。
            if (!mIsPreview) {
                prepareCapture(cameraDevice);
            }
            int template = mIsRecording ? CameraDevice.TEMPLATE_VIDEO_SNAPSHOT : CameraDevice.TEMPLATE_STILL_CAPTURE;
            CaptureRequest.Builder request = cameraDevice.createCaptureRequest(template);
            request.addTarget(stillImageSurface);
            setDefaultCaptureRequest(request);
            request.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            mCaptureSession.capture(request.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
                    if (DEBUG) {
                        Log.d(TAG, "takeStillImage: onCaptureStarted");
                    }
                    notifyCameraEvent(CameraEvent.SHUTTERED);
                }

                @Override
                public void onCaptureCompleted(final @NonNull CameraCaptureSession session, final @NonNull CaptureRequest request, final @NonNull TotalCaptureResult result) {
                    if (DEBUG) {
                        Log.d(TAG, "takeStillImage: onCaptureCompleted");
                    }
                    resumeRepeatingRequest();
                }

                @Override
                public void onCaptureFailed(final @NonNull CameraCaptureSession session, final @NonNull CaptureRequest request, final @NonNull CaptureFailure failure) {
                    if (DEBUG) {
                        Log.e(TAG, "takeStillImage: onCaptureFailed");
                    }
                    resumeRepeatingRequest();
                }

                @Override
                public void onCaptureBufferLost(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull Surface target, long frameNumber) {
                    if (DEBUG) {
                        Log.w(TAG, "takeStillImage: onCaptureBufferLost");
                    }
                }
            }, mBackgroundHandler);
        } catch (Throwable e) {
            if (DEBUG) {
                Log.e(TAG, "Failed to take still image.", e);
            }
            resumeRepeatingRequest();
            throw new CameraWrapperException(e);
        } finally {
            mIsTakingStillImage = false;
        }
    }

    private void resumeRepeatingRequest() {
        try {
            if (mIsRecording) {
                startRecording(mRecordingSurface, true);
            } else if (mIsPreview) {
                startPreview(mPreviewSurface, true);
            } else if (mIsTouchOn) {
                mIsTouchOn = false;
                turnOnTorch();
            } else {
                close();
            }
        } catch (CameraWrapperException e) {
            if (DEBUG) {
                Log.e(TAG, "Failed to resume recording or preview.", e);
            }
        }
    }

    private void prepareCapture(final CameraDevice cameraDevice) throws CameraWrapperException {
        if (!mOptions.hasAutoFocus() && !mOptions.hasAutoExposure()) {
            return;
        }

        try {
            final CountDownLatch lock = new CountDownLatch(1);
            final AtomicReference<CaptureResult> resultRef = new AtomicReference<>();
            CaptureRequest.Builder request = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            for (Surface surface : createSurfaceList()) {
                request.addTarget(surface);
            }
            setDefaultCaptureRequest(request, true);
            mCaptureSession.setRepeatingRequest(request.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
                    onCaptureResult(partialResult, false);
                }

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    onCaptureResult(result, true);
                }

                @Override
                public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
                    if (DEBUG) {
                        Log.e(TAG, "autoFocus: onCaptureFailed");
                    }
                    lock.countDown();
                }

                private boolean mIsAfReady = !mOptions.hasAutoFocus();
                private boolean mIsAeReady = !mOptions.hasAutoExposure();
                private boolean mIsCaptureReady;

                private void onCaptureResult(final CaptureResult result, final boolean isCompleted) {
                    if (!mIsAfReady) {
                        Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                        mIsAfReady = afState == null
                                || afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED
                                || afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED;
                    }
                    if (!mIsAeReady) {
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        mIsAeReady = aeState == null
                                || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED
                                || aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED;
                    }
                    mIsCaptureReady |= isCompleted;

                    if (mIsAfReady && mIsAeReady && mIsCaptureReady) {
                        resultRef.set(result);
                        lock.countDown();
                    }
                }
            }, mBackgroundHandler);
            lock.await(5, TimeUnit.SECONDS);
            mCaptureSession.stopRepeating();
        } catch (Exception e) {
            throw new CameraWrapperException(e);
        }
    }

    /**
     * カメラのライトをONにする.
     *
     * @throws CameraWrapperException カメラに何らかの致命的なエラーが発生した場合
     */
    public synchronized void turnOnTorch() throws CameraWrapperException {
        turnOnTorch(null, null);
    }

    /**
     * カメラのライトをONにする.
     *
     * @param listener 実際にライトがONになったタイミングで実行されるリスナー
     * @param handler リスナーを実行するハンドラー. リスナーを指定する場合は必須
     * @throws CameraWrapperException カメラに何らかの致命的なエラーが発生した場合
     */
    public synchronized void turnOnTorch(final @Nullable TorchOnListener listener,
                                         final @Nullable Handler handler) throws CameraWrapperException {
        if (listener != null && handler == null) {
            throw new IllegalArgumentException("handler is mandatory if listener is specified.");
        }
        if (mIsTouchOn) {
            notifyTorchOnEvent(listener, handler);
            return;
        }
        try {
            CameraDevice cameraDevice = openCamera();
            if (mCaptureSession == null) {
                mCaptureSession = createCaptureSession(cameraDevice);
            }
            CaptureRequest.Builder requestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            for (Surface surface : mTargetSurfaces) {
                requestBuilder.addTarget(surface);
            }
            requestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
            mCaptureSession.setRepeatingRequest(requestBuilder.build(), null, mBackgroundHandler);
            mUseTouch = true;
            mIsTouchOn = true;
            notifyTorchOnEvent(listener, handler);
        } catch (CameraAccessException e) {
            throw new CameraWrapperException(e);
        }
    }

    /**
     * カメラのライトをOFFにする.
     */
    public synchronized void turnOffTorch() {
        turnOffTorch(null, null);
    }

    /**
     * カメラのライトをOFFにする.
     *
     * @param listener 実際にライトがOFFになったタイミングで実行されるリスナー
     * @param handler リスナーを実行するハンドラー. リスナーを指定する場合は必須
     */
    public synchronized void turnOffTorch(final @Nullable TorchOffListener listener,
                                          final @Nullable Handler handler) {
        if (listener != null && handler == null) {
            throw new IllegalArgumentException("handler is mandatory if listener is specified.");
        }
        if (mUseTouch && mIsTouchOn) {
            try {
                CameraDevice cameraDevice = openCamera();
                if (mCaptureSession == null) {
                    mCaptureSession = createCaptureSession(cameraDevice);
                }
                CaptureRequest.Builder requestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                for (Surface surface : mTargetSurfaces) {
                    requestBuilder.addTarget(surface);
                }
                requestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                mCaptureSession.setRepeatingRequest(requestBuilder.build(),null, mBackgroundHandler);
                mIsTouchOn = false;
                mUseTouch = false;
                notifyTorchOffEvent(listener, handler);
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            } finally {
                resumeRepeatingRequest();
            }
        }
    }

    public boolean isTorchOn() {
        return mIsTouchOn;
    }

    public boolean isUseTorch() {
        return mUseTouch;
    }

    private void notifyTorchOnEvent(final TorchOnListener listener, final Handler handler) {
        if (listener != null && handler != null) {
            handler.post(listener::onTurnOn);
        }
    }

    private void notifyTorchOffEvent(final TorchOffListener listener, final Handler handler) {
        if (listener != null && handler != null) {
            handler.post(listener::onTurnOff);
        }
    }

    private final static int AF_TOUCH_WIDTH = 300;
    private final static int AF_TOUCH_HEIGHT = 300;

    private MeteringRectangle mMeteringRectangle;

    public void setFocus(float ratioX, float ratioY, boolean isRotate) {
        mMeteringRectangle = createMeteringRectangle(ratioX, ratioY, isRotate);

        if (mCaptureSession != null) {

        }
    }

    private MeteringRectangle createMeteringRectangle(float ratioX, float ratioY, boolean isRotate) {
        Rect sensorArraySize = Camera2Helper.getActiveArraySize(mCameraManager, mCameraId);
        if (sensorArraySize == null) {
            return null;
        }

        int sensorWidth = sensorArraySize.width();
        int sensorHeight = sensorArraySize.height();
        int sensorX = (int) (ratioX * sensorWidth);
        int sensorY = (int) (ratioY * sensorHeight);
        if (isRotate) {
            sensorX = (int) (ratioY * sensorWidth);
            sensorY = (int) ((1 - ratioX) * sensorHeight);
        }

        int touchX = (int) (sensorX - AF_TOUCH_WIDTH / 2);
        int touchY = (int) (sensorY - AF_TOUCH_HEIGHT / 2);
        int origX = Math.max(touchX, 0);
        int origY = Math.max(touchY, 0);
        int meteringWeight = MeteringRectangle.METERING_WEIGHT_MAX - 1;
        return new MeteringRectangle(origX, origY, AF_TOUCH_WIDTH, AF_TOUCH_HEIGHT, meteringWeight);
    }

    /**
     * カメラオプションを保持するクラス.
     */
    public static class Options {
        /**
         * デフォルトのプレビューサイズの閾値を定義.
         */
        private static final int DEFAULT_PREVIEW_WIDTH_THRESHOLD = 640;

        /**
         * デフォルトのプレビューサイズの閾値を定義.
         */
        private static final int DEFAULT_PREVIEW_HEIGHT_THRESHOLD = 480;

        private Size mPictureSize;
        private Size mPreviewSize;
        private Integer mWhiteBalance;
        private Integer mAutoFocusMode;
        private Integer mAutoExposureMode;
        private Range<Integer> mFps;
        private Integer mStabilizationMode;
        private Integer mOpticalStabilizationMode;
        private Float mDigitalZoom;
        private Integer mNoiseReductionMode;

        private Integer mMaxAutoFocusRegions;
        private List<Size> mSupportedPictureSizeList = new ArrayList<>();
        private List<Size> mSupportedPreviewSizeList = new ArrayList<>();
        private List<Range<Integer>> mSupportedFpsList = new ArrayList<>();
        private List<Integer> mSupportedWhiteBalanceList = new ArrayList<>();
        private Float mMaxDigitalZoom;
        private List<Integer> mSupportedStabilizationList = new ArrayList<>();
        private List<Integer> mSupportedOpticalStabilizationList = new ArrayList<>();
        private List<Integer> mSupportedNoiseReductionList = new ArrayList<>();

        public Size getPictureSize() {
            return mPictureSize;
        }

        public void setPictureSize(final Size pictureSize) {
            mPictureSize = pictureSize;
        }

        public Size getPreviewSize() {
            return mPreviewSize;
        }

        public void setPreviewSize(final Size previewSize) {
            mPreviewSize = previewSize;
        }

        public Float getDigitalZoom() {
            return mDigitalZoom;
        }

        public void setDigitalZoom(Float zoom) {
            mDigitalZoom = zoom;
        }

        public Integer getNoiseReductionMode() {
            return mNoiseReductionMode;
        }

        public void setNoiseReductionMode(Integer mode) {
            mNoiseReductionMode = mode;
        }

        public void setStabilizationMode(Integer mode) {
            mStabilizationMode = mode;
        }

        public Integer getStabilizationMode() {
            return mStabilizationMode;
        }

        public void setOpticalStabilizationMode(Integer mode) {
            mOpticalStabilizationMode = mode;
        }

        public Integer getOpticalStabilizationMode() {
            return mOpticalStabilizationMode;
        }

        public void setFps(int fps) {
            // 下限と上限の fps が一致する値を探す
            for (Range<Integer> range : mSupportedFpsList) {
                if (range.getLower() == fps && range.getUpper() == fps) {
                    mFps = range;
                    return;
                }
            }

            // 上限の fps が一致する値を探す
            for (Range<Integer> range : mSupportedFpsList) {
                if (range.getUpper() == fps) {
                    mFps = range;
                    return;
                }
            }

            // 発見できなかった場合は、設定を行わない。
        }

        public Range<Integer> getFps() {
            return mFps;
        }

        public void setFps(Range<Integer> fps) {
            mFps = fps;
        }

        public Integer getWhiteBalance() {
            return mWhiteBalance;
        }

        public void setWhiteBalance(final Integer whiteBalance) {
            mWhiteBalance = whiteBalance;
        }

        public List<Size> getSupportedPictureSizeList() {
            return mSupportedPictureSizeList;
        }

        public void setSupportedPictureSizeList(final List<Size> supportedPictureSizeList) {
            mSupportedPictureSizeList = new ArrayList<>(supportedPictureSizeList);
        }

        public List<Size> getSupportedPreviewSizeList() {
            return mSupportedPreviewSizeList;
        }

        public void setSupportedPreviewSizeList(final List<Size> supportedPreviewSizeList) {
            mSupportedPreviewSizeList = new ArrayList<>(supportedPreviewSizeList);
        }

        public List<Range<Integer>> getSupportedFpsList() {
            return mSupportedFpsList;
        }

        public void setSupportedFpsList(final List<Range<Integer>> fpsList) {
            mSupportedFpsList = fpsList;
        }

        public List<Integer> getSupportedWhiteBalanceList() {
            return mSupportedWhiteBalanceList;
        }

        public void setSupportedWhiteBalanceList(List<Integer> list) {
            mSupportedWhiteBalanceList = list;
        }

        public List<Integer> getSupportedStabilizationList() {
            return mSupportedStabilizationList;
        }

        public List<Integer> getSupportedOpticalStabilizationList() {
            return mSupportedOpticalStabilizationList;
        }

        public List<Integer> getSupportedNoiseReductionList() {
            return mSupportedNoiseReductionList;
        }

        public Float getMaxDigitalZoom() {
            return mMaxDigitalZoom;
        }

        public Size getDefaultPictureSize() {
            return getDefaultSizeFromList(mSupportedPictureSizeList);
        }

        public Size getDefaultPreviewSize() {
            return getDefaultSizeFromList(mSupportedPreviewSizeList);
        }

        private boolean hasAutoFocus() {
            return mAutoFocusMode != null;
        }

        private boolean hasAutoExposure() {
            return mAutoExposureMode != null;
        }

        private static Size getDefaultSizeFromList(final List<Size> sizeList) {
            if (sizeList.size() == 0) {
                return null;
            }
            Size defaultSize = null;
            for (Size size : sizeList) {
                if (size.getWidth() == DEFAULT_PREVIEW_WIDTH_THRESHOLD &&
                        size.getHeight() == DEFAULT_PREVIEW_HEIGHT_THRESHOLD) {
                    defaultSize = size;
                }
            }
            if (defaultSize != null) {
                return defaultSize;
            }
            for (Size size : sizeList) {
                if (size.getWidth() * size.getHeight() <=
                        DEFAULT_PREVIEW_WIDTH_THRESHOLD * DEFAULT_PREVIEW_HEIGHT_THRESHOLD) {
                    defaultSize = size;
                }
            }
            if (defaultSize != null) {
                return defaultSize;
            }
            return sizeList.get(0);
        }
    }

    public interface TorchOnListener {
        void onTurnOn();
    }

    public interface TorchOffListener {
        void onTurnOff();
    }
}
