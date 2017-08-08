/*
 HostDeviceCameraRecorder.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.camera;


import android.content.Context;
import android.hardware.Camera;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDevicePhotoRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDevicePreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.util.MixedReplaceMediaServer;
import org.deviceconnect.android.provider.FileManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Host Device Camera Recorder.
 *
 * @author NTT DOCOMO, INC.
 */
@SuppressWarnings("deprecation")
public class HostDeviceCameraRecorder extends HostDevicePreviewServer implements HostDevicePhotoRecorder {

    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "HOST";

    /**
     * カメラターゲットIDの定義.
     */
    private static final String ID_BASE = "photo";

    /**
     * カメラ名の定義.
     */
    private static final String NAME_BASE = "AndroidHost Camera Recorder";

    /**
     * NotificationIDを定義.
     */
    private static final int NOTIFICATION_ID = 1010;

    /**
     * デフォルトのプレビューサイズの閾値を定義.
     */
    private static final int DEFAULT_PREVIEW_WIDTH_THRESHOLD = 640;

    /**
     * デフォルトのプレビューサイズの閾値を定義.
     */
    private static final int DEFAULT_PREVIEW_HEIGHT_THRESHOLD = 480;

    /**
     * マイムタイプ一覧を定義.
     */
    private List<String> mMimeTypes = new ArrayList<String>() {
        {
            add("image/png");
        }
    };

    private CameraOverlay mCameraOverlay;

    private String mMimeType;

    private CameraFacing mFacing;

    private int mCameraId;

    private boolean mIsInitialized;

    private final List<PictureSize> mSupportedPictureSizes = new ArrayList<>();

    private final List<PictureSize> mSupportedPreviewSizes = new ArrayList<>();

    private final Object mLockObj = new Object();

    private MixedReplaceMediaServer mServer;

    private RecorderState mState;

    public HostDeviceCameraRecorder(final Context context, final int cameraId,
                                    final CameraFacing facing, final FileManager fileMgr) {
        super(context, NOTIFICATION_ID + cameraId);

        mFacing = facing;
        mCameraId = cameraId;
        mMimeType = mMimeTypes.get(0);
        mState = RecorderState.INACTTIVE;

        if (mCameraOverlay == null) {
            mCameraOverlay = new CameraOverlay(context, cameraId);
        } else {
            if (DEBUG) {
                Log.d(TAG, "CameraOverlay create fail.");
            }
        }
        mCameraOverlay.setFileManager(fileMgr);
        mCameraOverlay.setFacingDirection(facing == CameraFacing.FRONT ? -1 : 1);
    }

    @Override
    public void initialize() {
        if (mIsInitialized) {
            return;
        }

        try {
            Camera camera = Camera.open(mCameraId);
            Camera.Parameters params = camera.getParameters();
            Camera.Size picture = params.getPictureSize();
            setPictureSize(new PictureSize(picture.width, picture.height));
            for (Camera.Size size : params.getSupportedPictureSizes()) {
                mSupportedPictureSizes.add(new PictureSize(size.width, size.height));
            }
            Camera.Size preview = params.getPreviewSize();
            for (Camera.Size size : params.getSupportedPreviewSizes()) {
                mSupportedPreviewSizes.add(new PictureSize(size.width, size.height));
            }
            PictureSize defaultSize = getDefaultPreviewSize();
            if (defaultSize != null) {
                setPreviewSize(defaultSize);
            } else {
                setPreviewSize(new PictureSize(preview.width, preview.height));
            }
            camera.release();

            mIsInitialized = true;
        } catch (Exception e) {
            if (DEBUG) {
                Log.w(TAG, "", e);
            }
        }
    }

    @Override
    public void clean() {
        stopWebServer();
    }

    @Override
    public String getId() {
        return ID_BASE + "_" + mCameraId;
    }

    @Override
    public String getName() {
        return NAME_BASE + " - " + mFacing.getName();
    }

    @Override
    public String getMimeType() {
        return mMimeType;
    }

    @Override
    public RecorderState getState() {
        return mState;
    }

    @Override
    public List<String> getSupportedMimeTypes() {
        return mMimeTypes;
    }

    @Override
    public PictureSize getPictureSize() {
        return mCameraOverlay.getPictureSize();
    }

    @Override
    public void setPictureSize(final PictureSize size) {
        mCameraOverlay.setPictureSize(size);
    }

    @Override
    public PictureSize getPreviewSize() {
        return mCameraOverlay.getPreviewSize();
    }

    @Override
    public void setPreviewSize(final PictureSize size) {
        mCameraOverlay.setPreviewSize(size);
    }

    @Override
    public double getMaxFrameRate() {
        return mCameraOverlay.getPreviewMaxFrameRate();
    }

    @Override
    public void setMaxFrameRate(final double max) {
        mCameraOverlay.setPreviewFrameRate(max);
    }

    @Override
    public List<PictureSize> getSupportedPictureSizes() {
        return mSupportedPictureSizes;
    }

    @Override
    public List<PictureSize> getSupportedPreviewSizes() {
        return mSupportedPreviewSizes;
    }

    @Override
    public boolean isSupportedPictureSize(final int width, final int height) {
        if (mSupportedPictureSizes != null) {
            for (PictureSize size : mSupportedPictureSizes) {
                if (size.getWidth() == width && size.getHeight() == height) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isSupportedPreviewSize(final int width, final int height) {
        if (mSupportedPreviewSizes != null) {
            for (PictureSize size : mSupportedPreviewSizes) {
                if (size.getWidth() == width && size.getHeight() == height) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isBack() {
        return mFacing == CameraFacing.BACK;
    }

    @Override
    public void turnOnFlashLight() {
        if (mCameraOverlay != null) {
            mCameraOverlay.turnOnFlashLight();
        }
    }

    @Override
    public void turnOffFlashLight() {
        if (mCameraOverlay != null) {
            mCameraOverlay.turnOffFlashLight();
        }
    }

    @Override
    public boolean isFlashLightState() {
        return mCameraOverlay != null && mCameraOverlay.isFlashLightState();
    }

    @Override
    public boolean isUseFlashLight() {
        return mCameraOverlay != null && mCameraOverlay.isUseFlashLight();
    }


    @Override
    public void startWebServer(final OnWebServerStartCallback callback) {
        synchronized (mLockObj) {
            if (mServer == null) {
                mServer = new MixedReplaceMediaServer();
                mServer.setServerName("HostDevicePlugin Server");
                mServer.setContentType("image/jpg");
                final String ip = mServer.start();

                if (!mCameraOverlay.isShow()) {
                    mCameraOverlay.show(new CameraOverlay.Callback() {
                        @Override
                        public void onSuccess() {
                            sendNotification();
                            mCameraOverlay.setPreviewMode(true);
                            mCameraOverlay.setServer(mServer);
                            callback.onStart(ip);
                        }

                        @Override
                        public void onFail() {
                            callback.onFail();
                        }
                    });
                } else {
                    mCameraOverlay.setPreviewMode(true);
                    mCameraOverlay.setServer(mServer);
                    callback.onStart(ip);
                }
            } else {
                callback.onStart(mServer.getUrl());
            }
        }
    }

    @Override
    public void stopWebServer() {
        synchronized (mLockObj) {
            if (mServer != null) {
                mServer.stop();
                mServer = null;
            }
            mCameraOverlay.hide();
            hideNotification();
        }
    }

    /**
     * 写真撮影を行う.
     *
     * @param listener 写真撮影の結果を通知するリスナー
     */
    @Override
    public void takePhoto(final OnPhotoEventListener listener) {
        if (!mIsInitialized) {
            listener.onFailedTakePhoto("Camera is not initialized.");
            return;
        }

        if (mState != RecorderState.INACTTIVE) {
            listener.onFailedTakePhoto("Failed to camera state.");
            return;
        }

        mState = RecorderState.RECORDING;
        mCameraOverlay.takePicture(new CameraOverlay.OnTakePhotoListener() {
            @Override
            public void onTakenPhoto(String uri, String filePath) {
                listener.onTakePhoto(uri, filePath);
                mState = RecorderState.INACTTIVE;
            }

            @Override
            public void onFailedTakePhoto(final String errorMessage) {
                listener.onFailedTakePhoto(errorMessage);
                mState = RecorderState.INACTTIVE;
            }
        });
    }

    /**
     * デフォルトのプレビューサイズを取得します.
     * @return デフォルトのプレビューサイズ
     */
    private PictureSize getDefaultPreviewSize() {
        if (mSupportedPreviewSizes.size() == 0) {
            return null;
        }
        PictureSize defaultSize = null;
        for (PictureSize size : mSupportedPreviewSizes) {
            if (size.getWidth() == DEFAULT_PREVIEW_WIDTH_THRESHOLD &&
                    size.getHeight() == DEFAULT_PREVIEW_HEIGHT_THRESHOLD) {
                defaultSize = size;
            }
        }
        if (defaultSize != null) {
            return defaultSize;
        }
        for (PictureSize size : mSupportedPreviewSizes) {
            if (size.getWidth() * size.getHeight() <=
                    DEFAULT_PREVIEW_WIDTH_THRESHOLD * DEFAULT_PREVIEW_HEIGHT_THRESHOLD) {
                defaultSize = size;
            }
        }
        if (defaultSize != null) {
            return defaultSize;
        }
        return mSupportedPreviewSizes.get(0);
    }

    public enum CameraFacing {
        BACK("back"),
        FRONT("front"),
        UNKNOWN("unknown");

        private final String mName;

        CameraFacing(final String name) {
            mName = name;
        }

        public String getName() {
            return mName;
        }
    }
}
