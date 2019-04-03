/*
 CameraWrapper.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
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
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * カメラ操作クラス.
 *
 * @author NTT DOCOMO, INC.
 */
@SuppressWarnings("MissingPermission")
public class CameraWrapper {

    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final String TAG = "host.dplugin";

    private final String mCameraId;

    private final CameraManager mCameraManager;

    private final HandlerThread mBackgroundThread = new HandlerThread("background");

    private final HandlerThread mSessionConfigurationThread = new HandlerThread("session-config");

    private final Handler mBackgroundHandler;

    private final Handler mSessionConfigurationHandler;

    private final ImageReader mDummyPreviewReader;

    private final Options mOptions;

    private CameraDevice mCameraDevice;

    private CameraCaptureSession mCaptureSession;

    private boolean mIsTakingStillImage;

    private boolean mIsPreview;

    private boolean mIsRecording;

    private Surface mStillImageSurface;

    private Surface mPreviewSurface;

    private Surface mRecordingSurface;

    private boolean mIsTouchOn;

    private boolean mUseTouch;

    private byte mPreviewJpegQuality;

    public CameraWrapper(final @NonNull Context context, final @NonNull String cameraId) {
        mCameraId = cameraId;
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
        mSessionConfigurationThread.start();
        mSessionConfigurationHandler = new Handler(mSessionConfigurationThread.getLooper());
        mOptions = initOptions();

        mDummyPreviewReader = createImageReader(mOptions.getPreviewSize(), ImageFormat.YUV_420_888);
        mDummyPreviewReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(final ImageReader reader) {
                Image image = reader.acquireNextImage();
                if (image != null) {
                    image.close();
                }
            }
        }, mBackgroundHandler);
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
        mBackgroundThread.quit();
        mSessionConfigurationThread.quit();
    }

    private void close() {
        if (mCaptureSession != null) {
            mCaptureSession.close();
            mCaptureSession = null;
        }
        if (mCameraDevice != null) {
            mCameraDevice.close();
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
            final CameraDevice[] cameras = new CameraDevice[1];
            mCameraManager.openCamera(mCameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(final @NonNull CameraDevice camera) {
                    mCameraDevice = camera;
                    cameras[0] = camera;
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
            if (!lock.await(30, TimeUnit.SECONDS)) {
                throw new CameraWrapperException("Failed to open camera.");
            }
            if (cameras[0] == null) {
                throw new CameraWrapperException("Failed to open camera.");
            }
            return cameras[0];
        } catch (CameraAccessException e) {
            throw new CameraWrapperException(e);
        } catch (InterruptedException e) {
            throw new CameraWrapperException(e);
        }
    }

    private List<Surface> createSurfaceList() {
        List<Surface> surfaceList = new LinkedList<>();
        if (mIsPreview) {
            surfaceList.add(mPreviewSurface);
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
        if (mIsPreview) {
            surfaceList.add(mPreviewSurface);
        } else {
            surfaceList.add(mDummyPreviewReader.getSurface());
        }
        return surfaceList;
    }

    private synchronized CameraCaptureSession createCaptureSession(final CameraDevice cameraDevice) throws CameraWrapperException {
        return createCaptureSession(createSurfaceList(), cameraDevice);
    }

    private synchronized CameraCaptureSession createCaptureSession(final List<Surface> targets, final CameraDevice cameraDevice) throws CameraWrapperException {
        try {
            if (mCaptureSession != null) {
                mCaptureSession.close();
            }
            final CountDownLatch lock = new CountDownLatch(1);
            final CameraCaptureSession[] sessions = new CameraCaptureSession[1];
            cameraDevice.createCaptureSession(createSurfaceList(), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(final @NonNull CameraCaptureSession session) {
                    sessions[0] = session;
                    lock.countDown();
                }

                @Override
                public void onConfigureFailed(final @NonNull CameraCaptureSession session) {
                    lock.countDown();
                }
            }, mSessionConfigurationHandler);
            if (!lock.await(30, TimeUnit.SECONDS)) {
                throw new CameraWrapperException("Failed to configure capture session.");
            }
            if (sessions[0] == null) {
                throw new CameraWrapperException("Failed to configure capture session.");
            }
            return sessions[0];
        } catch (CameraAccessException e) {
            throw new CameraWrapperException(e);
        } catch (InterruptedException e) {
            throw new CameraWrapperException(e);
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
            request.addTarget(mPreviewSurface);
            request.set(CaptureRequest.JPEG_QUALITY, mPreviewJpegQuality);
            request.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            request.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            setWhiteBalance(request);
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
            mCaptureSession.close();
            mCaptureSession = null;
        }
        if (mIsRecording) {
            startRecording(mRecordingSurface, true);
        } else {
            close();
        }
    }

    public synchronized void startRecording(final Surface recordingSurface, final boolean isResume) throws CameraWrapperException {
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
                request.addTarget(mPreviewSurface);
            }
            request.addTarget(mRecordingSurface);
            request.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            request.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            captureSession.setRepeatingRequest(request.build(), null, null);
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
            mCaptureSession.close();
            mCaptureSession = null;
        }
        if (mIsPreview) {
            startPreview(mPreviewSurface, true);
        } else {
            close();
        }
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
                try {
                    autoFocus(cameraDevice);
                } catch (CameraWrapperException e) {
                    if (DEBUG) {
                        Log.w(TAG, "Failed to execute auto focus.", e);
                    }
                }
                try {
                    autoExposure(cameraDevice);
                } catch (CameraWrapperException e) {
                    if (DEBUG) {
                        Log.w(TAG, "Failed to execute auto exposure.", e);
                    }
                }
            }

            int template = mIsRecording ? CameraDevice.TEMPLATE_VIDEO_SNAPSHOT : CameraDevice.TEMPLATE_STILL_CAPTURE;
            CaptureRequest.Builder request = cameraDevice.createCaptureRequest(template);
            request.addTarget(stillImageSurface);
            request.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            request.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
            setWhiteBalance(request);
            mCaptureSession.capture(request.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
                    if (DEBUG) {
                        Log.d(TAG, "takeStillImage: onCaptureStarted");
                    }
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
            } else {
                close();
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

    private void autoFocus(final CameraDevice cameraDevice) throws CameraWrapperException {
        try {
            final CountDownLatch lock = new CountDownLatch(1);
            final CaptureResult[] results = new CaptureResult[1];
            CaptureRequest.Builder request = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            if (mIsPreview) {
                request.addTarget(mPreviewSurface);
            } else {
                request.addTarget(mDummyPreviewReader.getSurface());
            }
            request.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            request.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
            request.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
            setWhiteBalance(request);
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

                private void onCaptureResult(final CaptureResult result, final boolean isCompleted) {
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    boolean isAfReady = afState == null
                            || afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED
                            || afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED;
                    if (DEBUG) {
                        Log.d(TAG, "autoFocus: onCaptureCompleted: isAfReady=" + isAfReady);
                    }
                    if (isAfReady) {
                        results[0] = result;
                        lock.countDown();
                    }
                }
            }, mBackgroundHandler);
            lock.await(10, TimeUnit.SECONDS);
            mCaptureSession.stopRepeating();
            if (results[0] == null) {
                throw new CameraWrapperException("Failed auto focus.");
            }
        } catch (CameraAccessException e) {
            throw new CameraWrapperException(e);
        } catch (InterruptedException e) {
            throw new CameraWrapperException(e);
        }
    }

    private void autoExposure(final CameraDevice cameraDevice) throws CameraWrapperException {
        try {
            final CountDownLatch lock = new CountDownLatch(1);
            final CaptureResult[] results = new CaptureResult[1];
            CaptureRequest.Builder request = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            if (mIsPreview) {
                request.addTarget(mPreviewSurface);
            } else {
                request.addTarget(mDummyPreviewReader.getSurface());
            }
            request.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            request.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
            request.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            setWhiteBalance(request);
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

                private void onCaptureResult(final CaptureResult result, final boolean isCompleted) {
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    boolean isAeReady = aeState == null
                            || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED
                            || aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED;
                    if (DEBUG) {
                        Log.d(TAG, "autoExposure: onCaptureCompleted: isAeReady=" + isAeReady);
                    }
                    if (isAeReady) {
                        results[0] = result;
                        lock.countDown();
                    }
                }
            }, mBackgroundHandler);
            lock.await(10, TimeUnit.SECONDS);
            mCaptureSession.stopRepeating();
            if (results[0] == null) {
                throw new CameraWrapperException("Failed auto focus.");
            }
        } catch (CameraAccessException e) {
            throw new CameraWrapperException(e);
        } catch (InterruptedException e) {
            throw new CameraWrapperException(e);
        }
    }

    public synchronized void turnOnTorch() throws CameraWrapperException {
        if (mIsTouchOn) {
            return;
        }
        try {
            CameraDevice cameraDevice = openCamera();
            mCaptureSession = createCaptureSession(cameraDevice);
            final CaptureRequest.Builder requestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            requestBuilder.addTarget(mDummyPreviewReader.getSurface());
            requestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
            mCaptureSession.capture(requestBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    Integer flashMode = result.get(CaptureResult.FLASH_MODE);
                    boolean isTorchOn = flashMode == null
                            || flashMode == CaptureResult.FLASH_MODE_TORCH;
                    if (DEBUG) {
                        Log.d(TAG, "FLASH_MODE: " + isTorchOn);
                    }
                    if (isTorchOn) {
                        mIsTouchOn = true;
                    }
                }
            }, mBackgroundHandler);
            mUseTouch = true;
        } catch (CameraAccessException e) {
            throw new CameraWrapperException(e);
        }
    }

    public synchronized void turnOffTorch() {
        if (mUseTouch && mIsTouchOn) {
            mIsTouchOn = false;
            mUseTouch = false;
            close();
        }
    }

    public boolean isTorchOn() {
        return mIsTouchOn;
    }

    public boolean isUseTorch() {
        return mUseTouch;
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
}
