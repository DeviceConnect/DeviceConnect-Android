/*
 HostDeviceRecorderManager.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.os.Build;

import org.deviceconnect.android.deviceplugin.host.HostDeviceService;
import org.deviceconnect.android.deviceplugin.host.mediaplayer.VideoConst;
import org.deviceconnect.android.deviceplugin.host.recorder.audio.HostDeviceAudioRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.camera.HostDeviceCameraRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.screen.HostDeviceScreenCast;
import org.deviceconnect.android.deviceplugin.host.recorder.video.HostDeviceVideoRecorder;
import org.deviceconnect.android.provider.FileManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Host Device Recorder Manager.
 *
 * @author NTT DOCOMO, INC.
 */
@SuppressWarnings("deprecation")
public class HostDeviceRecorderManager {

    /** List of HostDeviceRecorder. */
    private HostDeviceRecorder[] mRecorders;

    /** HostDevicePhotoRecorder. */
    private HostDeviceRecorder mDefaultPhotoRecorder;

    /** HostDeviceAudioRecorder. */
    private HostDeviceRecorder mDefaultVideoRecorder;

    /** コンテキスト. */
    private HostDeviceService mHostDeviceService;

    public HostDeviceRecorderManager(final HostDeviceService service) {
        mHostDeviceService = service;
    }

    public void createRecorders(final FileManager fileMgr) {
        List<HostDeviceCameraRecorder> photoRecorders = new ArrayList<>();
        List<HostDeviceVideoRecorder> videoRecorders = new ArrayList<>();
        for (int cameraId = 0; cameraId < Camera.getNumberOfCameras(); cameraId++) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, cameraInfo);
            HostDeviceCameraRecorder.CameraFacing facing;
            switch (cameraInfo.facing) {
                case Camera.CameraInfo.CAMERA_FACING_BACK:
                    facing = HostDeviceCameraRecorder.CameraFacing.BACK;
                    break;
                case Camera.CameraInfo.CAMERA_FACING_FRONT:
                    facing = HostDeviceCameraRecorder.CameraFacing.FRONT;
                    break;
                default:
                    facing = HostDeviceCameraRecorder.CameraFacing.UNKNOWN;
                    break;
            }

            photoRecorders.add(new HostDeviceCameraRecorder(mHostDeviceService, cameraId, facing, fileMgr));
            videoRecorders.add(new HostDeviceVideoRecorder(mHostDeviceService, cameraId, facing));
        }

        if (!photoRecorders.isEmpty()) {
            mDefaultPhotoRecorder = photoRecorders.get(0);
        }

        if (!videoRecorders.isEmpty()) {
            mDefaultVideoRecorder = videoRecorders.get(0);
        }

        List<HostDeviceRecorder> recorders = new ArrayList<>();
        recorders.addAll(photoRecorders);
        recorders.addAll(videoRecorders);
        recorders.add(new HostDeviceAudioRecorder(mHostDeviceService));
        if (isSupportedMediaProjection()) {
            recorders.add(new HostDeviceScreenCast(mHostDeviceService, fileMgr));
        }
        mRecorders = recorders.toArray(new HostDeviceRecorder[recorders.size()]);
    }

    public void initialize() {
        for (HostDeviceRecorder recorder : getRecorders()) {
            recorder.initialize();
        }
    }

    public void clean() {
        for (HostDeviceRecorder recorder : getRecorders()) {
            recorder.clean();
        }
    }

    public HostDeviceRecorder[] getRecorders() {
        return mRecorders;
    }

    public HostDeviceRecorder getRecorder(final String id) {
        if (id == null) {
            return mDefaultPhotoRecorder;
        }
        for (HostDeviceRecorder recorder : mRecorders) {
            if (id.equals(recorder.getId())) {
                return recorder;
            }
        }
        return null;
    }

    public HostDevicePhotoRecorder getCameraRecorder(final String id) {
        if (id == null) {
            return (HostDeviceCameraRecorder) mDefaultPhotoRecorder;
        }
        for (HostDeviceRecorder recorder : mRecorders) {
            if (id.equals(recorder.getId()) && recorder instanceof HostDevicePhotoRecorder) {
                return (HostDevicePhotoRecorder) recorder;
            }
        }
        return null;
    }

    public HostDeviceStreamRecorder getStreamRecorder(final String id) {
        if (id == null) {
            return (HostDeviceStreamRecorder) mDefaultVideoRecorder;
        }
        for (HostDeviceRecorder recorder : mRecorders) {
            if (id.equals(recorder.getId()) && recorder instanceof HostDeviceStreamRecorder) {
                return (HostDeviceStreamRecorder) recorder;
            }
        }
        return null;
    }

    public HostDevicePreviewServer getPreviewServer(final String id) {
        if (id == null) {
            return (HostDevicePreviewServer) mDefaultPhotoRecorder;
        }
        for (HostDeviceRecorder recorder : mRecorders) {
            if (id.equals(recorder.getId()) && recorder instanceof HostDevicePreviewServer) {
                return (HostDevicePreviewServer) recorder;
            }
        }
        return null;
    }

    public HostDeviceRecorder getCameraDevice() {
        for (HostDeviceRecorder recorder : mRecorders) {
            if (recorder.isBack()) {
                return recorder;
            }
        }
        return null;
    }

    public void start() {
        IntentFilter filter = new IntentFilter(VideoConst.SEND_VIDEO_TO_HOSTDP);
        getContext().registerReceiver(mRecorderStateReceiver, filter);
    }

    public void stop() {
        getContext().unregisterReceiver(mRecorderStateReceiver);
    }

    public void stopWebServer(final String id) {
        if (id == null) {
            return;
        }
        HostDeviceRecorder recorder = getRecorder(id);
        if (recorder != null && recorder instanceof HostDevicePreviewServer) {
            ((HostDevicePreviewServer)recorder).stopWebServer();
        }
    }

    private boolean isSupportedMediaProjection() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    private Context getContext() {
        return mHostDeviceService;
    }

    private HostDeviceVideoRecorder getVideoRecorder(final String id) {
        for (HostDeviceRecorder recorder : mRecorders) {
            if (id.equals(recorder.getId()) && recorder instanceof HostDeviceVideoRecorder) {
                return (HostDeviceVideoRecorder) recorder;
            }
        }
        return null;
    }

    private final BroadcastReceiver mRecorderStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (VideoConst.SEND_VIDEO_TO_HOSTDP.equals(intent.getAction())) {
                String target = intent.getStringExtra(VideoConst.EXTRA_RECORDER_ID);
                HostDeviceRecorder.RecorderState state =
                        (HostDeviceRecorder.RecorderState) intent.getSerializableExtra(VideoConst.EXTRA_VIDEO_RECORDER_STATE);
                if (target != null && state != null) {
                    HostDeviceVideoRecorder videoRecorder = getVideoRecorder(target);
                    if (videoRecorder != null) {
                        videoRecorder.setState(state);
                    }
                }
            }
        }
    };
}
