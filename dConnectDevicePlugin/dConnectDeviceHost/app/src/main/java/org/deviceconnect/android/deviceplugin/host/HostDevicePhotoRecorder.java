/*
 HostDevicePhotoRecorder.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host;


import android.Manifest;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.host.camera.CameraOverlay;
import org.deviceconnect.android.deviceplugin.host.camera.MixedReplaceMediaServer;
import org.deviceconnect.android.provider.FileManager;

/**
 * Host Device Photo Recorder.
 *
 * @author NTT DOCOMO, INC.
 */
public class HostDevicePhotoRecorder implements HostDeviceRecorder, HostDevicePreviewServer {

    private static final String ID_BASE = "photo";

    private static final String NAME_BASE = "AndroidHost Recorder";

    private static final String MIME_TYPE = "image/png";

    private final Context mContext;

    private final int mCameraId;

    private final String mId;

    private final String mName;

    private final Object mLockObj = new Object();

    private MixedReplaceMediaServer mServer;

    private final CameraOverlay mCameraOverlay;

    public HostDevicePhotoRecorder(final Context context, final int cameraId,
                                   final CameraFacing facing, final FileManager fileMgr) {
        mContext = context;
        mCameraId = cameraId;
        mId = ID_BASE + "_" + cameraId;
        mName = NAME_BASE + " - " + facing.getName();

        mCameraOverlay = new CameraOverlay(context, cameraId);
        mCameraOverlay.setFileManager(fileMgr);
    }

    @Override
    public String getId() {
        return mId;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public String[] getSupportedMimeTypes() {
        return new String[] {MIME_TYPE};
    }

    @Override
    public RecorderState getState() {
        return isShowCamera() ? RecorderState.RECORDING : RecorderState.INACTTIVE;
    }

    /**
     * カメラが使用されているか確認する.
     *
     * @return カメラが使用されている場合はtrue、それ以外はfalse
     */
    private boolean isShowCamera() {
        return mCameraOverlay != null && mCameraOverlay.isShow();
    }

    @Override
    public boolean mutableInputPictureSize() {
        return true;
    }

    @Override
    public boolean usesCamera() {
        return true;
    }

    @Override
    public int getCameraId() {
        return mCameraId;
    }

    @Override
    public PictureSize getInputPictureSize() {
        return mCameraOverlay.getCameraPictureSize();
    }

    @Override
    public void setInputPictureSize(final PictureSize size) {
        mCameraOverlay.setCameraPictureSize(size);
    }

    /**
     * 写真撮影を行う.
     *
     * @param listener 写真撮影の結果を通知するリスナー
     */
    public void takePhoto(final CameraOverlay.OnTakePhotoListener listener) {
        PermissionUtility.requestPermissions(mContext, new Handler(Looper.getMainLooper()),
            new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
            new PermissionUtility.PermissionRequestCallback() {
                @Override
                public void onSuccess() {
                    if (!mCameraOverlay.isShow()) {
                        mCameraOverlay.show(new CameraOverlay.Callback() {
                            @Override
                            public void onSuccess() {
                                mCameraOverlay.setFinishFlag(true);
                                mCameraOverlay.takePicture(listener);
                            }

                            @Override
                            public void onFail() {
                                listener.onFailedTakePhoto();
                            }
                        });
                    } else {
                        mCameraOverlay.takePicture(listener);
                    }
                }

                @Override
                public void onFail(@NonNull String deniedPermission) {
                    listener.onFailedTakePhoto();
                }
            });
    }

    /**
     * Start a web server.
     *
     * @param callback a callback to return the result.
     */
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
                            mCameraOverlay.setFinishFlag(false);
                            mCameraOverlay.setServer(mServer);
                            callback.onStart(ip);
                        }

                        @Override
                        public void onFail() {
                            callback.onFail();
                        }
                    });
                } else {
                    mCameraOverlay.setFinishFlag(false);
                    mCameraOverlay.setServer(mServer);
                    callback.onStart(ip);
                }
            } else {
                callback.onStart(mServer.getUrl());
            }
        }
    }

    /**
     * Stop a web server.
     */
    @Override
    public void stopWebServer() {
        synchronized (mLockObj) {
            if (mServer != null) {
                mServer.stop();
                mServer = null;
            }
            if (isShowCamera()) {
                mCameraOverlay.hide();
            }
        }
    }
}
