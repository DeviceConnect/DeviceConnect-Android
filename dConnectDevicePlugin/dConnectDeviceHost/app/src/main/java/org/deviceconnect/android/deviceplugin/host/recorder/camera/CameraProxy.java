package org.deviceconnect.android.deviceplugin.host.recorder.camera;

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
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.deviceconnect.android.deviceplugin.host.BuildConfig.DEBUG;


@SuppressWarnings("MissingPermission")
class CameraProxy {

    static class MessageType {
        static final int HELLO = 0;
        static final int OPEN = 1;
        static final int CLOSE = 2;
        static final int START_PREVIEW = 3;
        static final int STOP_PREVIEW = 4;
        static final int AUTO_FOCUS = 5;
        static final int AUTO_EXPOSURE = 6;
        static final int TAKE_PICTURE = 7;
        static final int CREATE_SESSION = 10;
    }

    private static final String TAG = "host.dplugin";

    private final HandlerThread mBackgroundThread = new HandlerThread("background");

    private final Handler mBackgroundHandler;

    private Handler mHandler;

    private Handler mErrorHandler;

    private CameraDevice mCameraDevice;

    private CameraCaptureSession mCaptureSession;

    private Surface mPreviewSurface;

    private ImageReader mPreviewImageReader;

    private ImageReader mPhotoImageReader;

    private ImageReader.OnImageAvailableListener mPreviewListener;

    private final ImageReader.OnImageAvailableListener mStillImageListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(final ImageReader reader) {
            if (DEBUG) {
                Log.d(TAG, "onImageAvailable: " + reader);
            }
            Image image = reader.acquireNextImage();
            if (image != null) {
                success(MessageType.TAKE_PICTURE, image);
            } else {
                error(MessageType.TAKE_PICTURE);
            }
        }
    };

    private final CameraManager mCameraManager;

    private final String mCameraId;

    private final Options mOptions = new Options();

    CameraProxy(final Context context, final String cameraId) {
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());

        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        mCameraId = cameraId;
        initOptions();
    }

    private void initOptions() {
        List<Size> supportedPictureList = Camera2Helper.getSupportedPictureSizes(mCameraManager, mCameraId);
        mOptions.setSupportedPictureSizeList(supportedPictureList);
        mOptions.setPictureSize(supportedPictureList.get(0));

        List<Size> supportedPreviewList = Camera2Helper.getSupportedPictureSizes(mCameraManager, mCameraId);
        mOptions.setSupportedPreviewSizeList(supportedPreviewList);
        mOptions.setPreviewSize(supportedPreviewList.get(0));

        Size defaultSize = mOptions.getDefaultPictureSize();
        if (defaultSize != null) {
            mOptions.setPictureSize(defaultSize);
        }
        defaultSize = mOptions.getDefaultPreviewSize();
        if (defaultSize != null) {
            mOptions.setPreviewSize(defaultSize);
        }
    }

    private void success(final int what) {
        if (mHandler != null) {
            mHandler.sendEmptyMessage(what);
        }
    }

    private void success(final int what, final Object obj) {
        if (mHandler != null) {
            Message message = Message.obtain();
            message.what = what;
            message.obj = obj;
            mHandler.sendMessage(message);
        }
    }

    private void error(final int what) {
        if (mErrorHandler != null) {
            mErrorHandler.sendEmptyMessage(what);
        }
    }

    public void start(final Handler handler, final Handler errorHandler) {
        mHandler = handler;
        mErrorHandler = errorHandler;
        handler.sendEmptyMessage(MessageType.HELLO);
    }

    public void release() {
        mHandler = null;
        mErrorHandler = null;
    }

    public synchronized void open() throws CameraAccessException {
        synchronized (this) {
            if (mCameraDevice != null) {
                success(MessageType.OPEN);
                return;
            }
        }
        mCameraManager.openCamera(mCameraId, mStateCallback, null);
    }

    public synchronized void close() {
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        success(MessageType.CLOSE);
    }

    public void setPreviewSurface(final Surface previewSurface) {
        mPreviewSurface = previewSurface;
    }

    public synchronized void createSession() throws CameraAccessException {
        if (mCameraDevice == null) {
            throw new IllegalStateException("camera is not open.");
        }
        if (mCaptureSession != null) {
            success(MessageType.CREATE_SESSION);
            return;
        }

        Options options = mOptions;
        mPreviewImageReader = createImageReader(options.getPreviewSize(), ImageFormat.JPEG);
        mPreviewImageReader.setOnImageAvailableListener(mPreviewListener, mBackgroundHandler);

        mPhotoImageReader = createImageReader(options.getPictureSize(), ImageFormat.YUV_420_888);
        mPhotoImageReader.setOnImageAvailableListener(mStillImageListener, mBackgroundHandler);

        List<Surface> outputs = new LinkedList<>();
        Surface previewSurface = mPreviewSurface;
        if (previewSurface != null) {
            outputs.add(previewSurface);
        } else {
            outputs.add(mPreviewImageReader.getSurface());
        }
        outputs.add(mPhotoImageReader.getSurface());
        mCameraDevice.createCaptureSession(outputs, mSessionStateCallback, mBackgroundHandler);
    }

    public void setPreviewListener(final ImageReader.OnImageAvailableListener listener) {
        mPreviewListener = listener;
    }

    private CaptureRequest.Builder mPreviewRequestBuilder;

    public synchronized void startPreview() throws CameraAccessException {
        if (mPreviewRequestBuilder != null) {
            success(MessageType.START_PREVIEW);
            return;
        }

        if (mCameraDevice == null) {
            throw new IllegalStateException("camera is not open.");
        }
        if (mCaptureSession == null) {
            throw new IllegalStateException("capture session is not created.");
        }
        if (mPreviewImageReader == null) {
            throw new IllegalStateException("surface is not created.");
        }

        final CaptureRequest.Builder requestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        Surface previewSurface = mPreviewSurface;
        if (previewSurface != null) {
            requestBuilder.addTarget(mPreviewSurface);
        } else {
            requestBuilder.addTarget(mPreviewImageReader.getSurface());
        }
        requestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
        requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

        CaptureRequest request = requestBuilder.build();
        mCaptureSession.setRepeatingRequest(request, new CameraCaptureSession.CaptureCallback() {

            private boolean mIsFirstFrame = true;

            @Override
            public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
                if (mIsFirstFrame) {
                    mIsFirstFrame = false;
                    mPreviewRequestBuilder = requestBuilder;
                    success(MessageType.START_PREVIEW);
                }
            }
        }, mBackgroundHandler);
    }

    public synchronized void stopPreview() {
        destroy();
    }

    public synchronized void destroy() {
        if (mCaptureSession != null) {
            mCaptureSession.close();
            mCaptureSession = null;
        }
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        if (mPhotoImageReader != null) {
            mPhotoImageReader.close();
            mPhotoImageReader = null;
        }
        if (mPreviewImageReader != null) {
            mPreviewImageReader.close();
            mPreviewImageReader = null;
        }
        mPreviewRequestBuilder = null;
        mPreviewSurface = null;
    }

    public synchronized void takePicture() throws CameraAccessException {
        if (mCameraDevice == null) {
            throw new IllegalStateException("camera is not open.");
        }
        if (mCaptureSession == null) {
            throw new IllegalStateException("capture session is not created.");
        }
        if (mPhotoImageReader == null) {
            throw new IllegalStateException("surface is not created.");
        }

        CaptureRequest.Builder requestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
        requestBuilder.addTarget(mPhotoImageReader.getSurface());
        requestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
        requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

        CaptureRequest request = requestBuilder.build();
        mCaptureSession.stopRepeating();
        mCaptureSession.capture(request, null, null);
    }

    public synchronized void autoFocus() throws CameraAccessException {
        if (DEBUG) {
            Log.d(TAG, "autoFocus");
        }
        if (mCameraDevice == null) {
            throw new IllegalStateException("camera is not open.");
        }
        if (mCaptureSession == null) {
            throw new IllegalStateException("capture session is not created.");
        }
        if (mPreviewRequestBuilder == null) {
            throw new IllegalStateException("preview is not started.");
        }

        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
        mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), new CameraCaptureSession.CaptureCallback() {

            private boolean mIsNotified;

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
                if (!mIsNotified) {
                    error(MessageType.AUTO_FOCUS);
                    mIsNotified = true;
                }
            }

            private void onCaptureResult(final CaptureResult result, final boolean isCompleted) {
                Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                boolean isAfReady = afState == null
                        || afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED
                        || afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED;
                if (DEBUG) {
                    Log.d(TAG, "autoFocus: onCaptureCompleted: isAfReady=" + isAfReady);
                }
                if (isAfReady && !mIsNotified) {
                    success(MessageType.AUTO_FOCUS);
                    mIsNotified = true;
                }
            }
        }, mBackgroundHandler);
    }

    public synchronized void autoExposure() throws CameraAccessException {
        if (mCameraDevice == null) {
            throw new IllegalStateException("camera is not open.");
        }
        if (mCaptureSession == null) {
            throw new IllegalStateException("capture session is not created.");
        }
        if (mPreviewRequestBuilder == null) {
            throw new IllegalStateException("preview is not started.");
        }

        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START);
        mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), new CameraCaptureSession.CaptureCallback() {

            private boolean mIsNotified;

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
                    Log.e(TAG, "autoExposure: onCaptureFailed");
                }
                if (!mIsNotified) {
                    error(MessageType.AUTO_EXPOSURE);
                    mIsNotified = true;
                }
            }

            private void onCaptureResult(final CaptureResult result, final boolean isCompleted) {
                Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                boolean isAeReady = aeState == null
                        || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED
                        || aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED;
                if (DEBUG) {
                    Log.e(TAG, "autoExposure: onCaptureCompleted: isAeReady=" + isAeReady);
                }
                if (isAeReady && !mIsNotified) {
                    success(MessageType.AUTO_EXPOSURE);
                    mIsNotified = true;
                }
            }
        }, mBackgroundHandler);
    }

    private boolean mIsTouchOn;
    private boolean mUseTouch;

    public synchronized void turnOnTorch() throws CameraAccessException {
        if (mIsTouchOn) {
            return;
        }
        if (mCameraDevice == null) {
            throw new IllegalStateException("camera is not open.");
        }
        if (mCaptureSession == null) {
            throw new IllegalStateException("capture session is not created.");
        }
        if (mPreviewImageReader == null) {
            throw new IllegalStateException("surface is not created.");
        }

        final CaptureRequest.Builder requestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        requestBuilder.addTarget(mPreviewImageReader.getSurface());
        requestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
        mCaptureSession.capture(requestBuilder.build(), new CameraCaptureSession.CaptureCallback() {
            @Override
            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                Integer flashMode = result.get(CaptureResult.FLASH_MODE);
                boolean isTorchOn = flashMode == null
                        || flashMode == CaptureResult.FLASH_MODE_TORCH;
                if (DEBUG) {
                    Log.d("CameraProxy", "FLASH_MODE: " + isTorchOn);
                }
                if (isTorchOn) {
                    mIsTouchOn = true;
                }
            }
        }, mBackgroundHandler);
        mUseTouch = true;
    }

    public synchronized void turnOffTorch() {
        if (mUseTouch && mIsTouchOn) {
            mIsTouchOn = false;
            mUseTouch = false;
            destroy();
        }
    }

    public synchronized boolean isTorchOn() {
        return mIsTouchOn;
    }

    public synchronized boolean isUseTorch() {
        return mUseTouch;
    }

    private final CameraCaptureSession.StateCallback mSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(final @NonNull CameraCaptureSession session) {
            mCaptureSession = session;
            success(MessageType.CREATE_SESSION);
        }

        @Override
        public void onConfigureFailed(final @NonNull CameraCaptureSession session) {
            error(MessageType.CREATE_SESSION);
        }
    };

    private ImageReader createImageReader(final Size pictureSize, final int format) {
        return Camera2Helper.createImageReader(pictureSize.getWidth(), pictureSize.getHeight(), format);
    }

    public Options getOptions() {
        return mOptions;
    }

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(final @NonNull CameraDevice camera) {
            mCameraDevice = camera;
            success(MessageType.OPEN);
        }

        @Override
        public void onDisconnected(final @NonNull CameraDevice camera) {
            if (DEBUG) {
                Log.d(TAG, "StateCallback.onError: onDisconnected=" + camera.getId());
            }
            error(MessageType.OPEN);
        }

        @Override
        public void onError(final @NonNull CameraDevice camera, final int error) {
            if (DEBUG) {
                Log.e(TAG, "StateCallback.onError: cameraId=" + camera.getId() + " error=" + error);
            }
            error(MessageType.OPEN);
        }
    };

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

        private List<Size> mSupportedPictureSizeList = new ArrayList<>();

        private List<Size> mSupportedPreviewSizeList = new ArrayList<>();

        Size getPictureSize() {
            return mPictureSize;
        }

        void setPictureSize(final Size pictureSize) {
            mPictureSize = pictureSize;
        }

        Size getPreviewSize() {
            return mPreviewSize;
        }

        void setPreviewSize(final Size previewSize) {
            mPreviewSize = previewSize;
        }

        List<Size> getSupportedPictureSizeList() {
            return mSupportedPictureSizeList;
        }

        void setSupportedPictureSizeList(final List<Size> supportedPictureSizeList) {
            mSupportedPictureSizeList = new ArrayList<>(supportedPictureSizeList);
        }

        List<Size> getSupportedPreviewSizeList() {
            return mSupportedPreviewSizeList;
        }

        void setSupportedPreviewSizeList(final List<Size> supportedPreviewSizeList) {
            mSupportedPreviewSizeList = new ArrayList<>(supportedPreviewSizeList);
        }

        Size getDefaultPictureSize() {
            return getDefaultSizeFromeList(mSupportedPictureSizeList);
        }

        Size getDefaultPreviewSize() {
            return getDefaultSizeFromeList(mSupportedPreviewSizeList);
        }

        private static Size getDefaultSizeFromeList(final List<Size> sizeList) {
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
}
