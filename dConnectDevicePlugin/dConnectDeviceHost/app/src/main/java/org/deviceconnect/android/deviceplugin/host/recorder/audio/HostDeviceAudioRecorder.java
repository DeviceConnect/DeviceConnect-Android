/*
 HostDeviceAudioRecorder.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.audio;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;

import org.deviceconnect.android.deviceplugin.host.mediaplayer.VideoConst;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceStreamRecorder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Host Device Audio Recorder.
 *
 * @author NTT DOCOMO, INC.
 */
public class HostDeviceAudioRecorder implements HostDeviceRecorder, HostDeviceStreamRecorder {

    private static final String ID = "audio";

    private static final String NAME = "AndroidHost Audio Recorder";

    private static final String MIME_TYPE = "audio/3gp";

    private final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyyMMdd_kkmmss", Locale.JAPAN);

    private final Context mContext;

    /**
     * マイムタイプ一覧を定義.
     */
    private List<String> mMimeTypes = new ArrayList<String>() {
        {
            add("audio/3gp");
        }
    };
    /** 現在録音中のファイル名. */
    private String mNowRecordingFileName;
    private RecorderState mState;
    public HostDeviceAudioRecorder(final Context context) {
        mContext = context;
    }

    @Override
    public void initialize() {
        // Nothing to do.
    }

    @Override
    public void clean() {
        stopRecording(null);
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
    public RecorderState getState() {
        String className = getClassnameOfTopActivity();
        if (AudioRecorderActivity.class.getName().equals(className)) {
            return RecorderState.RECORDING;
        } else {
            return RecorderState.INACTTIVE;
        }
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public PictureSize getPictureSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPictureSize(final PictureSize size) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PictureSize getPreviewSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPreviewSize(PictureSize size) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getMaxFrameRate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMaxFrameRate(double frameRate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getPreviewBitRate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPreviewBitRate(int bitRate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<PictureSize> getSupportedPreviewSizes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<PictureSize> getSupportedPictureSizes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSupportedPictureSize(int width, int height) {
        return false;
    }

    @Override
    public boolean isSupportedPreviewSize(int width, int height) {
        return false;
    }

    @Override
    public List<String> getSupportedMimeTypes() {
        return mMimeTypes;
    }

    @Override
    public boolean canPauseRecording() {
        return true;
    }

    @Override
    public synchronized void startRecording(final String serviceId, final RecordingListener listener) {
        if (getState() == RecorderState.RECORDING) {
            throw new IllegalStateException();
        }

        mNowRecordingFileName = generateAudioFileName();
        Intent intent = new Intent();
        intent.setClass(mContext, AudioRecorderActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(VideoConst.EXTRA_RECORDER_ID, getId());
        intent.putExtra(VideoConst.EXTRA_SERVICE_ID, serviceId);
        intent.putExtra(AudioConst.EXTRA_FILE_NAME, mNowRecordingFileName);
        intent.putExtra(AudioConst.EXTRA_CALLBACK, new ResultReceiver(new Handler(Looper.getMainLooper())) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == Activity.RESULT_OK) {
                    listener.onRecorded(HostDeviceAudioRecorder.this, mNowRecordingFileName);
                } else {
                    String msg =
                        resultData.getString(VideoConst.EXTRA_CALLBACK_ERROR_MESSAGE, "Unknown error.");
                    listener.onFailed(HostDeviceAudioRecorder.this, msg);
                }
            }
        });
        mContext.startActivity(intent);
    }

    @Override
    public synchronized void stopRecording(final StoppingListener listener) {
        Intent intent = new Intent(AudioConst.SEND_HOSTDP_TO_AUDIO);
        intent.putExtra(AudioConst.EXTRA_NAME, AudioConst.EXTRA_NAME_AUDIO_RECORD_STOP);
        mContext.sendBroadcast(intent);
        mState = RecorderState.INACTTIVE;
        if (listener != null) {
            if (mNowRecordingFileName != null) {
                listener.onStopped(this, mNowRecordingFileName);
            } else {
                listener.onFailed(this, "Failed to Stop recording.");
            }
        }
        mNowRecordingFileName = null;
    }

    @Override
    public void pauseRecording() {
        Intent intent = new Intent(AudioConst.SEND_HOSTDP_TO_AUDIO);
        intent.putExtra(AudioConst.EXTRA_NAME, AudioConst.EXTRA_NAME_AUDIO_RECORD_PAUSE);
        mContext.sendBroadcast(intent);
    }

    @Override
    public void resumeRecording() {
        Intent intent = new Intent(AudioConst.SEND_HOSTDP_TO_AUDIO);
        intent.putExtra(AudioConst.EXTRA_NAME, AudioConst.EXTRA_NAME_AUDIO_RECORD_RESUME);
        mContext.sendBroadcast(intent);
    }

    private String getClassnameOfTopActivity() {
        ActivityManager activityMgr = (ActivityManager) mContext.getSystemService(Service.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityMgr.getRunningTasks(1);
        if (tasks != null && tasks.size() > 0) {
            return tasks.get(0).topActivity.getClassName();
        }
        return null;
    }

    private String generateAudioFileName() {
        return "audio" + mSimpleDateFormat.format(new Date()) + AudioConst.FORMAT_TYPE;
    }
}
