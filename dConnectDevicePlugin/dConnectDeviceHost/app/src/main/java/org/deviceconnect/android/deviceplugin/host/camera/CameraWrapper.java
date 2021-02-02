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
import android.hardware.camera2.params.RggbChannelVector;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.WindowManager;

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
        STOPPED_VIDEO_RECORDING,
        STARTED_PREVIEW,
        STOPPED_PREVIEW
    }

    public interface CameraEventListener {
        void onEvent(CameraEvent event);
    }

    private static class CameraEventListenerHolder {
        private final CameraEventListener mListener;
        private final Handler mHandler;

        CameraEventListenerHolder(final CameraEventListener listener,
                                  final Handler handler) {
            mListener = listener;
            mHandler = handler;
        }

        void notifyEvent(final CameraEvent event) {
            if (mHandler != null) {
                mHandler.post(() -> mListener.onEvent(event));
            } else {
                mListener.onEvent(event);
            }
        }
    }

    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "host.dplugin";

    private final String mCameraId;
    private final CameraManager mCameraManager;
    private final HandlerThread mSessionConfigurationThread;
    private final Handler mBackgroundHandler;
    private final Handler mSessionConfigurationHandler;
    private final ImageReader mPlaceHolderPreviewReader;
    private final Options mOptions;

    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCaptureSession;
    private ImageReader mStillImageReader;

    private boolean mIsTakingStillImage;
    private boolean mIsTorchOn;

    private Surface mPreviewSurface;
    private Surface mRecordingSurface;
    private int mTemplateType;

    private CameraEventListenerHolder mCameraEventListenerHolder;
    private final Context mContext;

    CameraWrapper(final @NonNull Context context, final @NonNull String cameraId) {
        mContext = context;
        mCameraId = cameraId;
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        mOptions = createOptions();
        mBackgroundHandler = new Handler(Looper.getMainLooper());
        mSessionConfigurationThread = new HandlerThread("session-config");
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
        if (listener != null) {
            mCameraEventListenerHolder = new CameraEventListenerHolder(listener, handler);
        } else {
            mCameraEventListenerHolder = null;
        }
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

    public boolean isPreview() {
        return mPreviewSurface != null;
    }

    public void destroy() {
        closeCameraDevice();

        try {
            mSessionConfigurationThread.quit();
            mSessionConfigurationThread.interrupt();
        } catch (Exception e) {
            // ignore.
        }
    }

    public Options getOptions() {
        return mOptions;
    }

    public int getSensorOrientation() {
        return Camera2Helper.getSensorOrientation(mCameraManager, mCameraId);
    }

    private ImageReader createStillImageReader() {
        return createImageReader(mOptions.getPictureSize(), ImageFormat.JPEG);
    }

    private ImageReader createImageReader(final Size pictureSize, final int format) {
        return Camera2Helper.createImageReader(pictureSize.getWidth(), pictureSize.getHeight(), format);
    }

    private Options createOptions() {
        Options options = new CameraWrapper.Options();

        // カメラがサポートしている値を設定
        options.mSupportedPictureSizeList = Camera2Helper.getSupportedPictureSizes(mCameraManager, mCameraId);
        options.mSupportedPreviewSizeList = Camera2Helper.getSupportedPreviewSizes(mCameraManager, mCameraId);
        options.mSupportedFpsList = Camera2Helper.getSupportedFps(mCameraManager, mCameraId);
        options.mSupportedAutoFocusModeList = Camera2Helper.getSupportedAutoFocusMode(mCameraManager, mCameraId);
        options.mSupportedWhiteBalanceList = Camera2Helper.getSupportedAWB(mCameraManager, mCameraId);
        options.mSupportedAutoExposureList = Camera2Helper.getSupportedAutoExposureMode(mCameraManager, mCameraId);
        options.mSupportedExposureTimeRange = Camera2Helper.getSupportedSensorExposureTime(mCameraManager, mCameraId);
        options.mSupportedSensitivityRange = Camera2Helper.getSupportedSensorSensitivity(mCameraManager, mCameraId);
        options.mMaxFrameDuration = Camera2Helper.getMaxSensorFrameDuration(mCameraManager, mCameraId);
        options.mMaxAutoFocusRegions = Camera2Helper.getMaxMeteringArea(mCameraManager, mCameraId);
        options.mSupportedStabilizationList = Camera2Helper.getSupportedStabilization(mCameraManager, mCameraId);
        options.mSupportedOpticalStabilizationList = Camera2Helper.getSupportedOpticalStabilization(mCameraManager, mCameraId);
        options.mSupportedNoiseReductionList = Camera2Helper.getSupportedNoiseReductionMode(mCameraManager, mCameraId);
        options.mMaxDigitalZoom = Camera2Helper.getMaxDigitalZoom(mCameraManager, mCameraId);
        options.mSupportedFocalLengthList = Camera2Helper.getSupportedFocalLengthList(mCameraManager, mCameraId);

        // デフォルト設定
        options.mAutoFocusMode = Camera2Helper.choiceAutoFocusMode(mContext, mCameraManager, mCameraId);
        options.mAutoExposureMode = Camera2Helper.choiceAutoExposureMode(mCameraManager, mCameraId);

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

    private synchronized CameraDevice openCameraDevice() throws CameraWrapperException {
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

    private synchronized void closeCameraDevice() {
        destroyCaptureSession();

        if (mCameraDevice != null) {
            try {
                mCameraDevice.close();
            } catch (Exception e) {
                // ignore.
            }
            mCameraDevice = null;
        }
        mIsTakingStillImage = false;
    }

    private List<Surface> createSurfaceList() {
        List<Surface> surfaceList = new LinkedList<>();
        if (mPreviewSurface != null) {
            surfaceList.add(mPreviewSurface);
        } else {
            surfaceList.add(mPlaceHolderPreviewReader.getSurface());
        }

        mStillImageReader = createStillImageReader();
        surfaceList.add(mStillImageReader.getSurface());

        return surfaceList;
    }

    private synchronized CameraCaptureSession createCaptureSession(final CameraDevice cameraDevice) throws CameraWrapperException {
        try {
            final CountDownLatch lock = new CountDownLatch(1);
            final AtomicReference<CameraCaptureSession> sessionRef = new AtomicReference<>();
            cameraDevice.createCaptureSession(createSurfaceList(), new CameraCaptureSession.StateCallback() {
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

    private synchronized void destroyCaptureSession() {
        if (mCaptureSession != null) {
            try {
                mCaptureSession.stopRepeating();
            } catch (Exception e) {
                // ignore.
            }
            try {
                mCaptureSession.close();
            } catch (Exception e) {
                // igore.
            }
            mCaptureSession = null;
        }
    }

    private void setDefaultCaptureRequest(final CaptureRequest.Builder requestBuilder) {
        if (mOptions.mAutoFocusMode != null) {
            requestBuilder.set(CaptureRequest.CONTROL_AF_MODE, mOptions.mAutoFocusMode);
        }

        if (mOptions.mAutoExposureMode != null) {
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, mOptions.mAutoExposureMode);

            // 自動露出を無効のみ設定できる
            if (mOptions.mAutoExposureMode == CaptureRequest.CONTROL_AE_MODE_OFF) {
                // 露出時間、感度(ISO)、およびフレーム期間を手動で制御
                if (mOptions.getSensorExposureTime() != null) {
                    requestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, mOptions.getSensorExposureTime());
                }
                if (mOptions.getSensorSensitivity() != null) {
                    requestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, mOptions.getSensorSensitivity());
                }
                if (mOptions.getSensorFrameDuration() != null) {
                    requestBuilder.set(CaptureRequest.SENSOR_FRAME_DURATION, mOptions.getSensorFrameDuration());
                }
            }
        }

        if (mOptions.mAutoWhiteBalanceMode != null) {
            requestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
            requestBuilder.set(CaptureRequest.CONTROL_AWB_MODE, mOptions.mAutoWhiteBalanceMode);
            if (mOptions.mAutoWhiteBalanceMode == CameraMetadata.CONTROL_AWB_MODE_OFF && mOptions.mWhiteBalanceTemperature != null) {
                requestBuilder.set(CaptureRequest.COLOR_CORRECTION_MODE, CaptureRequest.COLOR_CORRECTION_MODE_TRANSFORM_MATRIX);
                requestBuilder.set(CaptureRequest.COLOR_CORRECTION_GAINS, convertTemperatureToRggb(mOptions.mWhiteBalanceTemperature));
            }
        }

        if (mOptions.mFps != null) {
            requestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, mOptions.mFps);
        }

        if (mOptions.mDigitalZoom != null && mOptions.mDigitalZoom > 1) {
            int w = mOptions.getPreviewSize().getWidth();
            int h = mOptions.getPreviewSize().getHeight();
            int cx = w / 2;
            int cy = h / 2;
            int hw = (int) ((w >> 1) / mOptions.mDigitalZoom);
            int hh = (int) ((h >> 1) / mOptions.mDigitalZoom);
            Rect region = new Rect(cx - hw, cy - hh, cx + hw, cy + hh);
            requestBuilder.set(CaptureRequest.SCALER_CROP_REGION, region);
        }

        if (mOptions.mNoiseReductionMode != null) {
            requestBuilder.set(CaptureRequest.NOISE_REDUCTION_MODE, mOptions.mNoiseReductionMode);
        }

        if (mOptions.mStabilizationMode != null) {
            requestBuilder.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, mOptions.mStabilizationMode);
        }

        if (mOptions.mOpticalStabilizationMode != null) {
            requestBuilder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, mOptions.mOpticalStabilizationMode);
        }

        if (mOptions.mFocalLength != null) {
            requestBuilder.set(CaptureRequest.LENS_FOCAL_LENGTH, mOptions.mFocalLength);
        }

        // Torch が ON の場合は、CONTROL_AE_MODE を CONTROL_AE_MODE_ON にする。
        if (mIsTorchOn) {
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
            requestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
        } else {
            requestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
        }
    }

    private CaptureRequest.Builder createPreviewCaptureRequest() throws CameraAccessException {
        return createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
    }

    private CaptureRequest.Builder createRecordingCaptureRequest() throws CameraAccessException {
        return createCaptureRequest(CameraDevice.TEMPLATE_VIDEO_SNAPSHOT);
    }

    private CaptureRequest.Builder createCaptureRequest(int templateType) throws CameraAccessException {
        mTemplateType = templateType;

        CaptureRequest.Builder requestBuilder = mCameraDevice.createCaptureRequest(templateType);
        if (mPreviewSurface != null) {
            requestBuilder.addTarget(mPreviewSurface);
        } else {
            requestBuilder.addTarget(mPlaceHolderPreviewReader.getSurface());
        }
        setDefaultCaptureRequest(requestBuilder);
        return requestBuilder;
    }

    public synchronized void startPreview(final Surface surface) throws CameraWrapperException {
        if (mPreviewSurface != null) {
            throw new CameraWrapperException("preview is already running.");
        }
        mPreviewSurface = surface;
        startPreview(false);
    }

    private boolean mIsPreview;
    private synchronized void startPreview(final boolean resume) throws CameraWrapperException {
        try {
            if (!resume) {
                // プレビューが開始される前に CaptureSession が存在する場合は、
                // Torch でライトが ON されている。その場合にはターゲットとして
                // 登録されている Surface がダミー用なので一旦セッションを削除する。
                destroyCaptureSession();
            }
            if (mCaptureSession == null) {
                mCaptureSession = createCaptureSession(openCameraDevice());
            }
            mCaptureSession.setRepeatingRequest(createPreviewCaptureRequest().build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureStarted(@NonNull CameraCaptureSession session,
                                             @NonNull CaptureRequest request, long timestamp, long frameNumber) {
                    notifyCameraEvent(CameraEvent.STARTED_PREVIEW);
                    mIsPreview = true;
                }
            }, mBackgroundHandler);
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Failed to start a preview.", e);
            }
            stopPreview();
            throw new CameraWrapperException(e);
        }
    }

    public synchronized void stopPreview() {
        if (mIsPreview) {
            notifyCameraEvent(CameraEvent.STOPPED_PREVIEW);
            mIsPreview = false;
        }
        mPreviewSurface = null;
        destroyCaptureSession();
        resumeRepeatingRequest();
    }

    public synchronized void startRecording(Surface surface) throws CameraWrapperException {
        if (mRecordingSurface != null) {
            throw new CameraWrapperException("recording is already running.");
        }
        mRecordingSurface = surface;
        startRecording(false);
    }

    private boolean mIsRecording;
    private synchronized void startRecording(boolean resume) throws CameraWrapperException {
        try {
            if (!resume) {
                destroyCaptureSession();
            }
            if (mCaptureSession == null) {
                mCaptureSession = createCaptureSession(openCameraDevice());
            }
            mCaptureSession.setRepeatingRequest(createRecordingCaptureRequest().build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureStarted(@NonNull CameraCaptureSession session,
                                             @NonNull CaptureRequest request, long timestamp, long frameNumber) {
                    notifyCameraEvent(CameraEvent.STARTED_VIDEO_RECORDING);
                    mIsRecording = true;
                }
            }, mBackgroundHandler);
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Failed to start recording.", e);
            }
            stopRecording();
            throw new CameraWrapperException(e);
        }
    }

    public synchronized void stopRecording() {
        if (mIsRecording) {
            notifyCameraEvent(CameraEvent.STOPPED_VIDEO_RECORDING);
            mIsRecording = false;
        }
        mRecordingSurface = null;
        destroyCaptureSession();
        resumeRepeatingRequest();
    }

    public synchronized void takeStillImage(ImageReader.OnImageAvailableListener listener, Handler handler) throws CameraWrapperException {
        if (mIsTakingStillImage) {
            throw new CameraWrapperException("still image is taking now.");
        }
        mIsTakingStillImage = true;

        try {
            // プレビューが起動していない場合は、ダミーのプレビューを開始
            if (mCaptureSession == null) {
                mCaptureSession = createCaptureSession(openCameraDevice());
                prepareCapture();
            } else {
                mCaptureSession.stopRepeating();
            }

            mStillImageReader.setOnImageAvailableListener(listener, handler);

            CaptureRequest.Builder requestBuilder = createPreviewCaptureRequest();
            requestBuilder.addTarget(mStillImageReader.getSurface());
            mCaptureSession.capture(requestBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureStarted(@NonNull CameraCaptureSession session,
                                             @NonNull CaptureRequest request, long timestamp, long frameNumber) {
                    notifyCameraEvent(CameraEvent.SHUTTERED);
                }

                @Override
                public void onCaptureCompleted(final @NonNull CameraCaptureSession session,
                                               final @NonNull CaptureRequest request, final @NonNull TotalCaptureResult result) {
                    mIsTakingStillImage = false;
                    resumeRepeatingRequest();
                }

                @Override
                public void onCaptureFailed(final @NonNull CameraCaptureSession session,
                                            final @NonNull CaptureRequest request, final @NonNull CaptureFailure failure) {
                    if (DEBUG) {
                        Log.e(TAG, "takeStillImage: onCaptureFailed " + failure);
                    }
                    mIsTakingStillImage = false;
                    resumeRepeatingRequest();
                }
            }, mBackgroundHandler);
        } catch (Throwable e) {
            if (DEBUG) {
                Log.e(TAG, "Failed to take still image.", e);
            }
            mIsTakingStillImage = false;
            resumeRepeatingRequest();
            throw new CameraWrapperException(e);
        }
    }

    private void resumeRepeatingRequest() {
        try {
            if (mRecordingSurface != null) {
                startRecording(mTemplateType == CameraDevice.TEMPLATE_VIDEO_SNAPSHOT);
            } else if (mPreviewSurface != null) {
                startPreview(mTemplateType == CameraDevice.TEMPLATE_PREVIEW);
            } else if (mIsTorchOn) {
                turnOnTorch();
            } else {
                closeCameraDevice();
            }
        } catch (CameraWrapperException e) {
            if (DEBUG) {
                Log.e(TAG, "Failed to resume recording or preview.", e);
            }
        }
    }

    private void prepareCapture() throws CameraWrapperException {
        if (mOptions.mAutoFocusMode == null && mOptions.mAutoExposureMode == null) {
            return;
        }

        try {
            final CountDownLatch lock = new CountDownLatch(1);
            final AtomicReference<CaptureResult> resultRef = new AtomicReference<>();
            CaptureRequest.Builder requestBuilder = createPreviewCaptureRequest();
            requestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
            mCaptureSession.setRepeatingRequest(requestBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                                @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
                    onCaptureResult(partialResult, false);
                }

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    onCaptureResult(result, true);
                }

                @Override
                public void onCaptureFailed(@NonNull CameraCaptureSession session,
                                            @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
                    lock.countDown();
                }

                private boolean mIsAfReady = (mOptions.mAutoFocusMode == null);
                private boolean mIsAeReady = (mOptions.mAutoExposureMode == null);
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
            // 自動フォーカスと自動露出の処理が完了するのを待つ。
            // 5 秒待っても完了しない場合には、動作していない可能性があるので
            // タイムアウトして、処理を続ける。
            lock.await(5, TimeUnit.SECONDS);
            mCaptureSession.stopRepeating();
        } catch (Exception e) {
            throw new CameraWrapperException(e);
        }
    }

    public synchronized void startFocus(float x, float y, int width, int height) {
        if (mCaptureSession == null) {
            return;
        }

        if (mOptions.mAutoFocusMode != CaptureRequest.CONTROL_AF_MODE_AUTO) {
            return;
        }

        try {
            MeteringRectangle meteringRectangle = createMeteringRectangle(x, y, width, height);
            if (meteringRectangle == null) {
                return;
            }

            CaptureRequest.Builder requestBuilder = createPreviewCaptureRequest();
            requestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            requestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
            requestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
            requestBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, new MeteringRectangle[]{meteringRectangle});

            mCaptureSession.stopRepeating();
            mCaptureSession.setRepeatingRequest(requestBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    if (afState != null) {
                        switch (afState) {
                            case CaptureResult.CONTROL_AF_STATE_INACTIVE:
                            case CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED:
                            case CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED:
                                try {
                                    requestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
                                    mCaptureSession.setRepeatingRequest(requestBuilder.build(), null, mBackgroundHandler);
                                } catch (Exception e) {
                                    // ignore.
                                }
                                break;
                            default:
                                break;
                        }
                    }
                    super.onCaptureCompleted(session, request, result);
                }
            }, mBackgroundHandler);
        } catch (Exception e) {
            // ignore.
        }
    }

    private MeteringRectangle createMeteringRectangle(float px, float py, int width, int height) {
        Rect sensorArraySize = Camera2Helper.getActiveArraySize(mCameraManager, mCameraId);
        if (sensorArraySize == null) {
            return null;
        }

        // 画面の向きとセンサーの向きからタッチされた座標とサイズを変換
        float x = px;
        float y = py;
        int w = width;
        int h = height;
        switch (getCameraRotation()) {
            case 90:
                x = py;
                y = width - px;
                w = height;
                h = width;
                break;
            case 180:
                x = width - px;
                y = height - py;
                w = width;
                h = height;
                break;
            case 270:
                x = height - py;
                y = px;
                w = height;
                h = width;
                break;
        }

        // センサーサイズと表示する View サイズからセンサーの描画領域を計算。
        Rect previewRegion = calculatePreviewRegion(sensorArraySize.width(), sensorArraySize.height(), w, h);

        int afAreaSize = 300;

        // 画面がタッチされた座標をセンサーの座標に変換
        float ratioX = x / w;
        float ratioY = y / h;
        int sensorX = previewRegion.left + (int) (previewRegion.width() * ratioX);
        int sensorY = previewRegion.top + (int) (previewRegion.height() * ratioY);
        int focusX = (sensorX - afAreaSize / 2);
        int focusY = (sensorY - afAreaSize / 2);
        int focusW = sensorX + afAreaSize / 2;
        int focusH = sensorY + afAreaSize / 2;
        int meteringWeight = MeteringRectangle.METERING_WEIGHT_MAX;
        Rect focusRegion = new Rect(Math.max(focusX, 0), Math.max(focusY, 0),
                Math.min(focusW, sensorArraySize.width()), Math.min(focusH, sensorArraySize.height()));
        return new MeteringRectangle(focusRegion, meteringWeight);
    }

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private int getCameraRotation() {
        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
        // We have to take that into account and rotate JPEG properly.
        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
        return (ORIENTATIONS.get(getDisplayRotation(mContext)) + getSensorOrientation() + 270) % 360;
    }

    private static int getDisplayRotation(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) {
            throw new RuntimeException("WindowManager is not supported.");
        }
        return wm.getDefaultDisplay().getRotation();
    }

    private static Rect calculatePreviewRegion(int sensorWidth, int sensorHeight, int width, int height) {
        float ratioX;
        float ratioY;

        double baseAspect = ((double) sensorHeight / sensorWidth);
        double targetAspect = ((double) height / width);
        if (Math.abs(baseAspect - targetAspect) < 0.001) {
            ratioX = 1.0f;
            ratioY = 1.0f;
        } else {
            int widthLcm = getLCM(sensorWidth, width);
            int baseRatio = widthLcm / sensorWidth;
            int outputRatio = widthLcm / width;
            if (targetAspect - baseAspect < 0) {
                ratioX = 1.0f;
                ratioY = (float) (height * outputRatio) / (float) (sensorHeight * baseRatio);
            } else {
                ratioX = (float) (sensorHeight * baseRatio) / (float) (height * outputRatio);
                ratioY = 1.0f;
            }
        }

        int previewWidth = (int) (sensorWidth * ratioX);
        int previewHeight = (int) (sensorHeight * ratioY);
        int left = (sensorWidth - previewWidth) / 2;
        int top = (sensorHeight - previewHeight) / 2;
        int right = left + previewWidth;
        int bottom = top + previewHeight;
        return new Rect(left, top, right, bottom);
    }

    /**
     * 最大公約数（greatest common divisor）.
     * @param a 最大公約数を出したい値
     * @param b 最大公約数を出したい値
     * @return 最大公約数
     */
    private static int getGCD(int a, int b) {
        if (a > b) {
            int temp = a;
            a = b;
            b = temp;
        }

        while (a != 0) {
            int temp = a;
            a = b % a;
            b = temp;
        }
        return b;
    }

    /**
     * 最小公倍数（least common multiple）.
     * @param a 最小公倍数を出したい値
     * @param b 最小公倍数を出したい値
     * @return 最小公倍数
     */
    private static int getLCM(int a, int b) {
        return (a * b) / getGCD(a, b);
    }

    private RggbChannelVector convertTemperatureToRggb(int whiteBalanceTemperature) {
        float temperature = whiteBalanceTemperature / 100.0f;
        float red;
        float green;
        float blue;

        if (temperature <= 66) {
            red = 255;
        } else {
            red = temperature - 60;
            red = (float) (329.698727446 * (Math.pow(red, -0.1332047592)));
            red = Math.max(red, 0);
            red = Math.min(red, 255);
        }

        if (temperature <= 66) {
            green = temperature;
            green = (float) (99.4708025861 * Math.log(green) - 161.1195681661);
        } else {
            green = temperature - 60;
            green = (float) (288.1221695283 * (Math.pow(green, -0.0755148492)));
        }
        green = Math.max(green, 0);
        green = Math.min(green, 255);

        if (temperature >= 66) {
            blue = 255;
        } else if (temperature <= 19) {
            blue = 0;
        } else {
            blue = temperature - 10;
            blue = (float) (138.5177312231 * Math.log(blue) - 305.0447927307);
            blue = Math.max(blue, 0);
            blue = Math.min(blue, 255);
        }

        return new RggbChannelVector((red / 255) * 2, (green / 255), (green / 255), (blue / 255) * 2);
    }

    /**
     * カメラのライトをONにする.
     *
     * @throws CameraWrapperException カメラに何らかの致命的なエラーが発生した場合
     */
    public void turnOnTorch() throws CameraWrapperException {
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

        try {
            if (mCaptureSession == null) {
                mCaptureSession = createCaptureSession(openCameraDevice());
            } else {
                mCaptureSession.stopRepeating();
            }
            CaptureRequest.Builder requestBuilder = createPreviewCaptureRequest();
            // AutoExposure が CONTROL_AE_MODE_ON 以外だと動作しない。
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
            requestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
            mCaptureSession.setRepeatingRequest(requestBuilder.build(), null, mBackgroundHandler);
            mIsTorchOn = true;
            notifyTorchOnEvent(listener, handler);
        } catch (CameraAccessException e) {
            throw new CameraWrapperException(e);
        }
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

        if (!mIsTorchOn) {
           return;
        }

        try {
            // Torch フラグを OFF にして、resumeRepeatingRequest を呼び出します
            // プレビューがある場合には、Torch を OFF で再開され、プレビューがない場合
            // には close されるので消灯します。
            mIsTorchOn = false;
            notifyTorchOffEvent(listener, handler);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        } finally {
            resumeRepeatingRequest();
        }
    }

    public synchronized boolean isTorchOn() {
        return mIsTorchOn;
    }

    public synchronized boolean isUseTorch() {
        return mIsTorchOn;
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

        private Size mPictureSize;
        private Size mPreviewSize;
        private Integer mAutoFocusMode;
        private Integer mAutoWhiteBalanceMode;
        private Integer mAutoExposureMode;
        private Range<Integer> mFps;
        private Integer mStabilizationMode;
        private Integer mOpticalStabilizationMode;
        private Float mDigitalZoom;
        private Integer mNoiseReductionMode;
        private Long mSensorExposureTime;
        private Integer mSensorSensitivity;
        private Long mSensorFrameDuration;
        private Float mFocalLength;
        private Integer mWhiteBalanceTemperature;

        private Integer mMaxAutoFocusRegions;
        private List<Size> mSupportedPictureSizeList = new ArrayList<>();
        private List<Size> mSupportedPreviewSizeList = new ArrayList<>();
        private List<Range<Integer>> mSupportedFpsList = new ArrayList<>();
        private List<Integer> mSupportedAutoFocusModeList = new ArrayList<>();
        private List<Integer> mSupportedWhiteBalanceList = new ArrayList<>();
        private List<Integer> mSupportedAutoExposureList = new ArrayList<>();
        private Float mMaxDigitalZoom;
        private List<Integer> mSupportedStabilizationList = new ArrayList<>();
        private List<Integer> mSupportedOpticalStabilizationList = new ArrayList<>();
        private List<Integer> mSupportedNoiseReductionList = new ArrayList<>();
        private Range<Long> mSupportedExposureTimeRange;
        private Range<Integer> mSupportedSensitivityRange;
        private Long mMaxFrameDuration;
        private List<Float> mSupportedFocalLengthList = new ArrayList<>();

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

        public Float getFocalLength() {
            return mFocalLength;
        }

        public void setFocalLength(Float focalLength) {
            mFocalLength = focalLength;
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

        public Integer getAutoFocusMode() {
            return mAutoFocusMode;
        }

        public void setAutoFocusMode(Integer mode) {
            mAutoFocusMode = mode;
        }

        public Integer getAutoWhiteBalanceMode() {
            return mAutoWhiteBalanceMode;
        }

        public void setAutoWhiteBalanceMode(final Integer whiteBalance) {
            mAutoWhiteBalanceMode = whiteBalance;
        }

        public Integer getWhiteBalanceTemperature() {
            return mWhiteBalanceTemperature;
        }

        public void setWhiteBalanceTemperature(Integer whiteBalanceTemperature) {
            mWhiteBalanceTemperature = whiteBalanceTemperature;
        }

        public Integer getAutoExposureMode() {
            return mAutoExposureMode;
        }

        public void setAutoExposureMode(Integer mode) {
            mAutoExposureMode = mode;
        }

        public Long getSensorExposureTime() {
            return mSensorExposureTime;
        }

        public void setSensorExposureTime(Long exposureTime) {
            mSensorExposureTime = exposureTime;
        }

        public Integer getSensorSensitivity() {
            return mSensorSensitivity;
        }

        public void setSensorSensitivity(Integer sensitivity) {
            mSensorSensitivity = sensitivity;
        }

        public Long getSensorFrameDuration() {
            return mSensorFrameDuration;
        }

        public void setSensorFrameDuration(Long frameDuration) {
            mSensorFrameDuration = frameDuration;
        }

        public List<Size> getSupportedPictureSizeList() {
            return mSupportedPictureSizeList;
        }

        public List<Size> getSupportedPreviewSizeList() {
            return mSupportedPreviewSizeList;
        }

        public List<Range<Integer>> getSupportedFpsList() {
            return mSupportedFpsList;
        }

        public List<Integer> getSupportedAutoFocusModeList() {
            return mSupportedAutoFocusModeList;
        }

        public List<Integer> getSupportedWhiteBalanceList() {
            return mSupportedWhiteBalanceList;
        }

        public List<Integer> getSupportedAutoExposureModeList() {
            return mSupportedAutoExposureList;
        }

        public Range<Long> getSupportedExposureTimeRange() {
            return mSupportedExposureTimeRange;
        }

        public Range<Integer> getSupportedSensitivityRange() {
            return mSupportedSensitivityRange;
        }

        public Long getMaxFrameDuration() {
            return mMaxFrameDuration;
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

        public List<Float> getSupportedFocalLengthList() {
            return mSupportedFocalLengthList;
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

        private Size getDefaultSizeFromList(final List<Size> sizeList) {
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
