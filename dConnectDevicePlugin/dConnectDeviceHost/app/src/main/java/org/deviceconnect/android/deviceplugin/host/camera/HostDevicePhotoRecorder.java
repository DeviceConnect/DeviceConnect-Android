/*
 HostDevicePhotoRecorder.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.camera;


import android.Manifest;
import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.provider.FileManager;

import java.util.List;

/**
 * Host Device Photo Recorder.
 *
 * @author NTT DOCOMO, INC.
 */
public class HostDevicePhotoRecorder extends HostDeviceCameraRecorder {

    private static final String ID_BASE = "photo";

    private static final String NAME_BASE = "AndroidHost Photo Recorder";

    private static final String MIME_TYPE = "image/png";

    public HostDevicePhotoRecorder(final Context context, final int cameraId,
                                   final CameraFacing facing, final FileManager fileMgr) {
        super(context, createId(cameraId), createName(facing), facing, cameraId, fileMgr);
    }

    private static String createId(final int cameraId) {
        return ID_BASE + "_" + cameraId;
    }

    private static String createName(final CameraFacing facing) {
        return NAME_BASE + " - " + facing.getName();
    }

    @Override
    @SuppressWarnings("deprecation")
    protected List<Camera.Size> getSupportedSizes(final Camera.Parameters params) {
        return params.getSupportedPictureSizes();
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

    @Override
    public boolean mutablePictureSize() {
        return true;
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
}
