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
import org.deviceconnect.android.deviceplugin.host.recorder.audio.HostDeviceAudioRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.camera.HostDevicePhotoRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.screen.HostDeviceScreenCast;
import org.deviceconnect.android.deviceplugin.host.recorder.video.HostDeviceVideoRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.video.VideoConst;
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
    private HostDevicePhotoRecorder mDefaultPhotoRecorder;

    /** HostDeviceAudioRecorder. */
    private HostDeviceVideoRecorder mDefaultVideoRecorder;

    private HostDeviceService mHostDeviceService;

    public HostDeviceRecorderManager(final HostDeviceService service) {
        mHostDeviceService = service;
    }

    public void createRecorders(final FileManager fileMgr) {
        List<HostDevicePhotoRecorder> photoRecorders = new ArrayList<>();
        List<HostDeviceVideoRecorder> videoRecorders = new ArrayList<>();
        for (int cameraId = 0; cameraId < Camera.getNumberOfCameras(); cameraId++) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, cameraInfo);
            HostDeviceRecorder.CameraFacing facing;
            switch (cameraInfo.facing) {
                case Camera.CameraInfo.CAMERA_FACING_BACK:
                    facing = HostDeviceRecorder.CameraFacing.BACK;
                    break;
                case Camera.CameraInfo.CAMERA_FACING_FRONT:
                    facing = HostDeviceRecorder.CameraFacing.FRONT;
                    break;
                default:
                    facing = HostDeviceRecorder.CameraFacing.UNKNOWN;
                    break;
            }
            photoRecorders.add(new HostDevicePhotoRecorder(mHostDeviceService, cameraId, facing, fileMgr));
            videoRecorders.add(new HostDeviceVideoRecorder(mHostDeviceService, cameraId, facing, fileMgr));
        }

        if (photoRecorders.size() > 0) {
            mDefaultPhotoRecorder = photoRecorders.get(0);
        }
        if (videoRecorders.size() > 0) {
            mDefaultVideoRecorder = videoRecorders.get(0);
        }

        List<HostDeviceRecorder> recorders = new ArrayList<>();
        recorders.addAll(photoRecorders);
        recorders.addAll(videoRecorders);
        recorders.add(new HostDeviceAudioRecorder(mHostDeviceService));
        if (isSupportedMediaProjection()) {
            recorders.add(new HostDeviceScreenCast(mHostDeviceService));
        }
        mRecorders = recorders.toArray(new HostDeviceRecorder[recorders.size()]);
    }

    public HostDeviceRecorder[] getRecorders() {
        return mRecorders;
    }

    public HostDeviceRecorder getRecorder(final String id) {
        if (id == null) {
            return mDefaultVideoRecorder;
        }
        for (HostDeviceRecorder recorder : mRecorders) {
            if (id.equals(recorder.getId())) {
                return recorder;
            }
        }
        return null;
    }

    public HostDevicePhotoRecorder getPhotoRecorder(final String id) {
        if (id == null) {
            return mDefaultPhotoRecorder;
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
            return mDefaultVideoRecorder;
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
            return mDefaultPhotoRecorder;
        }
        for (HostDeviceRecorder recorder : mRecorders) {
            if (id.equals(recorder.getId()) && recorder instanceof HostDevicePreviewServer) {
                return (HostDevicePreviewServer) recorder;
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

    public void forcedStopRecording() {
        for (HostDeviceRecorder hostRecorder : getRecorders()) {
            HostDeviceStreamRecorder movie = getStreamRecorder(hostRecorder.getId());
            if (movie != null && movie.getState() != HostDeviceRecorder.RecorderState.INACTTIVE) {
                movie.stop();
            }
        }
    }

    public void forcedStopPreview() {
        for (HostDeviceRecorder hostRecorder : getRecorders()) {
            HostDevicePreviewServer server = getPreviewServer(hostRecorder.getId());
            if (server != null) {
                server.stopWebServer();
            }
        }
    }

    public void stopWebServer(final String id) {
        HostDeviceRecorder recorder = getRecorder(id);
        if (recorder != null && recorder instanceof HostDevicePreviewServer) {
            ((HostDevicePreviewServer)recorder).stopWebServer();
        }
    }

    private HostDeviceVideoRecorder getVideoRecorder(final String id) {
        for (HostDeviceRecorder recorder : mRecorders) {
            if (id.equals(recorder.getId()) && recorder instanceof HostDeviceVideoRecorder) {
                return (HostDeviceVideoRecorder) recorder;
            }
        }
        return null;
    }

    private Context getContext() {
        return mHostDeviceService;
    }

    private boolean isSupportedMediaProjection() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
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
