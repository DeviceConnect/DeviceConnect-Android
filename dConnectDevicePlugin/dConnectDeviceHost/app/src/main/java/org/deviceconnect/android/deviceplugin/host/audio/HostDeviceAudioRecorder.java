package org.deviceconnect.android.deviceplugin.host.audio;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;

import org.deviceconnect.android.deviceplugin.host.HostDeviceStreamRecorder;
import org.deviceconnect.android.deviceplugin.host.video.VideoConst;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HostDeviceAudioRecorder implements HostDeviceStreamRecorder {

    private static final String ID = "audio";

    private static final String NAME = "AndroidHost Audio Recorder";

    private static final String MIME_TYPE = "audio/3gp";

    private final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyyMMdd_kkmmss", Locale.JAPAN);

    private final Context mContext;

    public HostDeviceAudioRecorder(final Context context) {
        mContext = context;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public RecorderState getState() {
        String className = getClassnameOfTopActivity();
        if (AudioRecorderActivity.class.getName().equals(className)) {
            return RecorderState.RECORDING;
        } else {
            return RecorderState.INACTTIVE;
        }
    }

    private String getClassnameOfTopActivity() {
        ActivityManager activityMgr = (ActivityManager) mContext.getSystemService(Service.ACTIVITY_SERVICE);
        return activityMgr.getRunningTasks(1).get(0).topActivity.getClassName();
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public synchronized void start(final RecordingListener listener) {
        if (getState() == RecorderState.RECORDING) {
            throw new IllegalStateException();
        }

        final String filename = generateAudioFileName();
        Intent intent = new Intent();
        intent.setClass(mContext, AudioRecorderActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(AudioConst.EXTRA_FINE_NAME, filename);
        intent.putExtra(AudioConst.EXTRA_CALLBACK, new ResultReceiver(new Handler(Looper.getMainLooper())) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == Activity.RESULT_OK) {
                    listener.onRecorded(HostDeviceAudioRecorder.this, filename);
                } else {
                    String msg =
                        resultData.getString(VideoConst.EXTRA_CALLBACK_ERROR_MESSAGE, "Unknown error.");
                    listener.onFailed(HostDeviceAudioRecorder.this, msg);
                }
            }
        });
        mContext.startActivity(intent);
    }

    private String generateAudioFileName() {
        return "audio" + mSimpleDateFormat.format(new Date()) + AudioConst.FORMAT_TYPE;
    }

    @Override
    public synchronized void stop() {
        Intent intent = new Intent(AudioConst.SEND_HOSTDP_TO_AUDIO);
        intent.putExtra(AudioConst.EXTRA_NAME, AudioConst.EXTRA_NAME_AUDIO_RECORD_STOP);
        mContext.sendBroadcast(intent);
    }

    @Override
    public void pause() {
        Intent intent = new Intent(AudioConst.SEND_HOSTDP_TO_AUDIO);
        intent.putExtra(AudioConst.EXTRA_NAME, AudioConst.EXTRA_NAME_AUDIO_RECORD_PAUSE);
        mContext.sendBroadcast(intent);
    }

    @Override
    public void resume() {
        Intent intent = new Intent(AudioConst.SEND_HOSTDP_TO_AUDIO);
        intent.putExtra(AudioConst.EXTRA_NAME, AudioConst.EXTRA_NAME_AUDIO_RECORD_RESUME);
        mContext.sendBroadcast(intent);
    }

}
