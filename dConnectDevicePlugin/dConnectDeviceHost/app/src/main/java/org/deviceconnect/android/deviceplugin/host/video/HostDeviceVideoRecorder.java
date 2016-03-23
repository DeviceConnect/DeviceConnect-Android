/*
 HostDeviceVideoRecorder.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.video;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;

import org.deviceconnect.android.deviceplugin.host.HostDeviceStreamRecorder;
import org.deviceconnect.android.deviceplugin.host.camera.HostDeviceCameraRecorder;
import org.deviceconnect.android.provider.FileManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Host Device Video Recorder.
 *
 * @author NTT DOCOMO, INC.
 */
public class HostDeviceVideoRecorder extends HostDeviceCameraRecorder
    implements HostDeviceStreamRecorder {

    private static final String ID_BASE = "video";

    private static final String NAME_BASE = "AndroidHost Video Recorder";

    private static final String MIME_TYPE = "video/3gp";

    private final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyyMMdd_kkmmss", Locale.JAPAN);

    private RecorderState mState = RecorderState.INACTTIVE;

    private PictureSize mPictureSize;

    public HostDeviceVideoRecorder(final Context context, final int cameraId,
                                   final CameraFacing facing, final FileManager fileMgr) {
        super(context, createId(cameraId), createName(facing), facing, cameraId, fileMgr);
    }

    @Override
    @SuppressWarnings("deprecation")
    protected List<Camera.Size> getSupportedSizes(final Camera.Parameters params) {
        return params.getSupportedVideoSizes();
    }

    private static String createId(final int cameraId) {
        return ID_BASE + "_" + cameraId;
    }

    private static String createName(final CameraFacing facing) {
        return NAME_BASE + " - " + facing.getName();
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public String[] getSupportedMimeTypes() {
        return new String[] {MIME_TYPE};
    }

    public void setState(final RecorderState state) {
        mState = state;
    }

    @Override
    public RecorderState getState() {
        return mState;
    }

    @Override
    public boolean mutablePictureSize() {
        return true;
    }

    @Override
    public boolean canPause() {
        return false;
    }

    @Override
    public synchronized void start(final RecordingListener listener) {
        if (getState() == RecorderState.RECORDING) {
            throw new IllegalStateException();
        }

        final String filename = generateVideoFileName();
        Intent intent = new Intent();
        intent.setClass(mContext, VideoRecorderActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(VideoConst.EXTRA_RECORDER_ID, getId());
        intent.putExtra(VideoConst.EXTRA_CAMERA_ID, mCameraId);
        intent.putExtra(VideoConst.EXTRA_FILE_NAME, filename);
        intent.putExtra(VideoConst.EXTRA_PICTURE_SIZE, getPictureSize());
        intent.putExtra(VideoConst.EXTRA_CALLBACK, new ResultReceiver(new Handler(Looper.getMainLooper())) {
            @Override
            protected void onReceiveResult(final int resultCode, final Bundle resultData) {
                if (resultCode == Activity.RESULT_OK) {
                    listener.onRecorded(HostDeviceVideoRecorder.this, filename);
                } else {
                    String msg =
                        resultData.getString(VideoConst.EXTRA_CALLBACK_ERROR_MESSAGE, "Unknown error.");
                    listener.onFailed(HostDeviceVideoRecorder.this, msg);
                }
            }
        });
        mContext.startActivity(intent);
    }

    private String generateVideoFileName() {
        return "video" + mSimpleDateFormat.format(new Date()) + VideoConst.FORMAT_TYPE;
    }

    @Override
    public synchronized void stop() {
        Intent intent = new Intent(VideoConst.SEND_HOSTDP_TO_VIDEO);
        intent.putExtra(VideoConst.EXTRA_NAME, VideoConst.EXTRA_VALUE_VIDEO_RECORD_STOP);
        mContext.sendBroadcast(intent);
    }

    @Override
    public void pause() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resume() {
        throw new UnsupportedOperationException();
    }

}
