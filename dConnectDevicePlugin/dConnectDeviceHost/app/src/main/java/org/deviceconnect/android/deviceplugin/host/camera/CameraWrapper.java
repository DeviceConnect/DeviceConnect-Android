/*
 CameraWrapper.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
        private CameraEventListener mListener;
        private Handler mHandler;

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

    private final ImageReader mDummyPreviewReader;

    private final Options mOptions;

    private final Integer mAutoFocusMode;

    private final Integer mAutoExposureMode;

    private CameraDevice mCameraDevice;

    private CameraCaptureSession mCaptureSession;

    private boolean mIsTakingStillImage;

    private boolean mIsPreview;

    private boolean mIsRecording;

    private Surface mStillImageSurface;

    private Surface mPreviewSurface;

    private Surface mRecordingSurface;

    /**
     * プレビュー配信の画像を確認するための Surface.
     */
    private Surface mTargetSurface;

    private boolean mIsTouchOn;

    private boolean mUseTouch;

    private byte mPreviewJpegQuality = 100;

    private CameraEventListenerHolder mCameraEventListenerHolder;

    CameraWrapper(final @NonNull Context context,
                  final @NonNull String cameraId) throws CameraAccessException {
        mCameraId = cameraId;
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        mBackgroundHandler = new Handler(Looper.getMainLooper());
        mSessionConfigurationThread.start();
        mSessionConfigurationHandler = new Handler(mSessionConfigurationThread.getLooper());
        mOptions = initOptions();
        mAutoFocusMode = choiceAutoFocusMode(context, mCameraManager, cameraId);
        mAutoExposureMode = choiceAutoExposureMode(mCameraManager, cameraId);
        mDummyPreviewReader = createImageReader(mOptions.getPreviewSize(), ImageFormat.YUV_420_888);
        mDummyPreviewReader.setOnImageAvailableListener(reader -> {
            Image image = reader.acquireNextImage();
            if (image != null) {
                image.close();
            }
        }, mBackgroundHandler);

        if (DEBUG) {
            Log.d(TAG, "CameraWrapper: cameraId=" + cameraId + " autoFocus=" + mAutoFocusMode + " autoExposure=" + mAutoExposureMode);
        }
    }

    private boolean hasAutoFocus() {
        return mAutoFocusMode != null;
    }

    private boolean hasAutoExposure() {
        return mAutoExposureMode != null;
    }

    private void notifyCameraEvent(final CameraEvent event) {
        CameraEventListenerHolder holder = mCameraEventListenerHolder;
        if (holder != null) {
            holder.notifyEvent(event);
        }
    }

    public void setCameraEventListener(final CameraEventListener listener,
                                       final Handler handler) {
        mCameraEventListenerHolder = new CameraEventListenerHolder(listener, handler);
    }

    public String getId() {
        return mCameraId;
    }

    public int getPreviewJpegQuality() {
        return mPreviewJpegQuality;
    }

    public void setPreviewJpegQuality(int previewJpegQuality) {
        mPreviewJpegQuality = (byte) previewJpegQuality;
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
        mSessionConfigurationThread.quit();
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

    public ImageReader createPreviewReader(final int format) {
        return createImageReader(mOptions.getPreviewSize(), format);
    }

    private ImageReader createImageReader(final Size pictureSize, final int format) {
        return Camera2Helper.createImageReader(pictureSize.getWidth(), pictureSize.getHeight(), format);
    }

    private Options initOptions() {
        Options options = new CameraWrapper.Options();
        List<Size> supportedPictureList = Camera2Helper.getSupportedPictureSizes(mCameraManager, mCameraId);
        options.setSupportedPictureSizeList(supportedPictureList);
        options.setPictureSize(supportedPictureList.get(0));

        List<Size> supportedPreviewList = Camera2Helper.getSupportedPreviewSizes(mCameraManager, mCameraId);
        options.setSupportedPreviewSizeList(supportedPreviewList);
        options.setPreviewSize(supportedPreviewList.get(0));

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
        } catch (CameraAccessException e) {
            throw new CameraWrapperException(e);
        } catch (InterruptedException e) {
            throw new CameraWrapperException(e);
        }
    }

    private static Integer choiceAutoFocusMode(final Context context,
                                               final CameraManager cameraManager,
                                               final String cameraId) throws CameraAccessException {
        PackageManager pkgMgr = context.getPackageManager();
        if (!pkgMgr.hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS)) {
            return null;
        }

        CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
        int[] afModes = characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
        if (afModes == null) {
            return null;
        }
        for (int i = 0; i < afModes.length; i++) {
            if (afModes[i] == CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE) {
                return afModes[i];
            }
        }
        return null;
    }

    private static Integer choiceAutoExposureMode(final CameraManager cameraManager,
                                                  final String cameraId) throws CameraAccessException {
        CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
        int[] aeModes = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES);
        if (aeModes == null) {
            return null;
        }
        for (int i = 0; i < aeModes.length; i++) {
            if (aeModes[i] == CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH) {
                return aeModes[i];
            }
        }
        for (int i = 0; i < aeModes.length; i++) {
            if (aeModes[i] == CameraMetadata.CONTROL_AE_MODE_ON) {
                return aeModes[i];
            }
        }
        return null;
    }

    public void setTargetSurface(Surface surface) {
        mTargetSurface = surface;
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
            surfaceList.add(mDummyPreviewReader.getSurface());
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
        surfaceList.add(mDummyPreviewReader.getSurface());
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
            final CountDownLatch lock = new CountDownLatch(1);
            final AtomicReference<CameraCaptureSession> sessionRef = new AtomicReference<>();
            cameraDevice.createCaptureSession(targets, new CameraCaptureSession.StateCallback() {
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
        } catch (CameraAccessException e) {
            throw new CameraWrapperException(e);
        } catch (InterruptedException e) {
            throw new CameraWrapperException(e);
        }
    }

    private void setDefaultCaptureRequest(final CaptureRequest.Builder request) {
        setDefaultCaptureRequest(request, false);
    }

    private void setDefaultCaptureRequest(final CaptureRequest.Builder request,
                                          final boolean trigger) {
        if (hasAutoFocus()) {
            request.set(CaptureRequest.CONTROL_AF_MODE, mAutoFocusMode);
            if (trigger) {
                request.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
            }
        }
        if (hasAutoExposure()) {
            request.set(CaptureRequest.CONTROL_AE_MODE, mAutoExposureMode);
        }
        setWhiteBalance(request);
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
            if (mPreviewSurface != null) {
                request.addTarget(mPreviewSurface);
            }
            if (mTargetSurface != null) {
                request.addTarget(mTargetSurface);
            }
            request.set(CaptureRequest.JPEG_QUALITY, mPreviewJpegQuality);
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
            if (mIsPreview) {
                if (mPreviewSurface != null) {
                    request.addTarget(mPreviewSurface);
                }
                if (mTargetSurface != null) {
                    request.addTarget(mTargetSurface);
                }
            }
            request.addTarget(mRecordingSurface);
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
            if (DEBUG) {
                Log.d(TAG, "takeStillImage: Camera is open: cameraId=" + cameraDevice.getId());
            }
            mCaptureSession = createCaptureSession(createSurfaceListForStillImage(), cameraDevice);
            if (DEBUG) {
                Log.d(TAG, "takeStillImage: Created capture session.");
            }
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
            if (DEBUG) {
                Log.d(TAG, "takeStillImage: Started capture:");
            }
        } catch (Throwable e) {
            if (DEBUG) {
                Log.e(TAG, "Failed to take still image.", e);
            }
            resumeRepeatingRequest();
            throw new CameraWrapperException(e);
        }
    }

    private void resumeRepeatingRequest() {
        mIsTakingStillImage = false;

        try {
            if (mIsRecording) {
                startRecording(mRecordingSurface, true);
            } else if (mIsPreview) {
                startPreview(mPreviewSurface, true);
            }
        } catch (CameraWrapperException e) {
            if (DEBUG) {
                Log.e(TAG, "Failed to resume recording or preview.", e);
            }
        }
    }

    private void setWhiteBalance(final CaptureRequest.Builder request) {
        Integer whiteBalance = getWhiteBalanceOption();
        if (DEBUG) {
            Log.d(TAG, "White Balance: " + whiteBalance);
        }
        if (whiteBalance != null) {
            request.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
            request.set(CaptureRequest.CONTROL_AWB_MODE, whiteBalance);
        }
    }

    private Integer getWhiteBalanceOption() {
        String whiteBalance = getOptions().getWhiteBalance();
        Integer mode;
        if ("auto".equals(whiteBalance)) {
            mode = CameraMetadata.CONTROL_AWB_MODE_AUTO;
        } else if ("incandescent".equals(whiteBalance)) {
            mode = CameraMetadata.CONTROL_AWB_MODE_INCANDESCENT;
        } else if ("fluorescent".equals(whiteBalance)) {
            mode = CameraMetadata.CONTROL_AWB_MODE_FLUORESCENT;
        } else if ("warm-fluorescent".equals(whiteBalance)) {
            mode = CameraMetadata.CONTROL_AWB_MODE_WARM_FLUORESCENT;
        } else if ("daylight".equals(whiteBalance)) {
            mode = CameraMetadata.CONTROL_AWB_MODE_DAYLIGHT;
        } else if ("cloudy-daylight".equals(whiteBalance)) {
            mode = CameraMetadata.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT;
        } else if ("twilight".equals(whiteBalance)) {
            mode = CameraMetadata.CONTROL_AWB_MODE_TWILIGHT;
        } else if ("shade".equals(whiteBalance)) {
            mode = CameraMetadata.CONTROL_AWB_MODE_SHADE;
        } else {
            mode = null;
        }
        return mode;
    }

    private void prepareCapture(final CameraDevice cameraDevice) throws CameraWrapperException {
        if (!hasAutoFocus() && !hasAutoExposure()) {
            return;
        }
        try {
            final CountDownLatch lock = new CountDownLatch(1);
            final AtomicReference<CaptureResult> resultRef = new AtomicReference<>();
            CaptureRequest.Builder request = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            if (mIsPreview) {
                if (mPreviewSurface != null) {
                    request.addTarget(mPreviewSurface);
                }
                if (mTargetSurface != null) {
                    request.addTarget(mTargetSurface);
                }
            } else {
                request.addTarget(mDummyPreviewReader.getSurface());
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

                private boolean mIsAfReady = !hasAutoFocus();
                private boolean mIsAeReady = !hasAutoExposure();
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
                        if (DEBUG) {
                            Log.d(TAG, "prepareCapture: onCaptureCompleted: aeState=" + aeState);
                        }
                        mIsAeReady = aeState == null
                                || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED
                                || aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED;
                    }
                    mIsCaptureReady |= isCompleted;
                    if (DEBUG) {
                        Log.d(TAG, "prepareCapture: onCaptureCompleted: isAfReady=" + mIsAfReady + " isAeReady=" + mIsAeReady + " isCaptureReady=" + mIsCaptureReady);
                    }

                    if (mIsAfReady && mIsAeReady && mIsCaptureReady) {
                        resultRef.set(result);
                        lock.countDown();
                    }
                }
            }, mBackgroundHandler);
            lock.await(10, TimeUnit.SECONDS);
            mCaptureSession.stopRepeating();
            if (resultRef.get() == null) {
                throw new CameraWrapperException("Failed auto focus.");
            }
        } catch (CameraAccessException e) {
            throw new CameraWrapperException(e);
        } catch (InterruptedException e) {
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
            mCaptureSession = createCaptureSession(cameraDevice);
            final CaptureRequest.Builder requestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            if (mIsPreview) {
                if (mPreviewSurface != null) {
                    requestBuilder.addTarget(mPreviewSurface);
                }
                if (mTargetSurface != null) {
                    requestBuilder.addTarget(mTargetSurface);
                }
            } else {
                requestBuilder.addTarget(mDummyPreviewReader.getSurface());
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
                mCaptureSession = createCaptureSession(cameraDevice);
                final CaptureRequest.Builder requestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                if (mIsPreview) {
                    if (mPreviewSurface != null) {
                        requestBuilder.addTarget(mPreviewSurface);
                    }
                    if (mTargetSurface != null) {
                        requestBuilder.addTarget(mTargetSurface);
                    }
                } else {
                    requestBuilder.addTarget(mDummyPreviewReader.getSurface());
                }
                requestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                mCaptureSession.setRepeatingRequest(requestBuilder.build(),null, mBackgroundHandler);
                mIsTouchOn = false;
                mUseTouch = false;
            } catch (CameraAccessException e) {
                throw new IllegalArgumentException(e);
            } catch (CameraWrapperException e) {
                throw new IllegalArgumentException(e);
            }
            notifyTorchOffEvent(listener, handler);
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

        /**
         * デフォルトのホワイトバランス.
         */
        private static final String DEFAULT_WHITE_BALANCE = "auto";

        private Size mPictureSize;

        private Size mPreviewSize;

        private List<Size> mSupportedPictureSizeList = new ArrayList<>();

        private List<Size> mSupportedPreviewSizeList = new ArrayList<>();

        private double mPreviewMaxFrameRate = 30.0d; //fps

        private int mPreviewBitRate = 1000 * 1000; //bps

        private String mWhiteBalance = DEFAULT_WHITE_BALANCE;

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

        public Size getDefaultPictureSize() {
            return getDefaultSizeFromList(mSupportedPictureSizeList);
        }

        public Size getDefaultPreviewSize() {
            return getDefaultSizeFromList(mSupportedPreviewSizeList);
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

        public double getPreviewMaxFrameRate() {
            return mPreviewMaxFrameRate;
        }

        public void setPreviewMaxFrameRate(final double previewMaxFrameRate) {
            mPreviewMaxFrameRate = previewMaxFrameRate;
        }

        public int getPreviewBitRate() {
            return mPreviewBitRate;
        }

        public void setPreviewBitRate(final int previewBitRate) {
            mPreviewBitRate = previewBitRate;
        }

        public String getWhiteBalance() {
            return mWhiteBalance;
        }

        public void setWhiteBalance(final String whiteBalance) {
            mWhiteBalance = whiteBalance;
        }
    }

    public interface TorchOnListener {
        void onTurnOn();
    }

    public interface TorchOffListener {
        void onTurnOff();
    }
}
