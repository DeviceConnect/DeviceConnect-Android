/*
 HostDeviceAudioRecorder.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.audio;


import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.host.file.HostFileProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceStreamRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.PreviewServerProvider;
import org.deviceconnect.android.provider.FileManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;

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

    /** MediaRecoder. */
    private MediaRecorder mMediaRecorder;

    /** フォルダURI. */
    private File mFile;

    /**
     * マイムタイプ一覧を定義.
     */
    private List<String> mMimeTypes = new ArrayList<String>() {
        {
            add("audio/3gp");
        }
    };
    private RecorderState mState;
    public HostDeviceAudioRecorder(final Context context) {
        mContext = context;
        mState = RecorderState.INACTTIVE;
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
    public void destroy() {
        // Nothing to do.
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
        return mState;
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public String getStreamMimeType() {
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
    public void muteTrack() {
    }

    @Override
    public void unMuteTrack() {
    }

    @Override
    public boolean isMutedTrack() {
        return false;
    }

    @Override
    public PreviewServerProvider getServerProvider() {
        return null;
    }

    @Override
    public void requestPermission(PermissionCallback callback) {

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
    public synchronized void startRecording(final RecordingListener listener) {
        if (getState() == RecorderState.RECORDING) {
            throw new IllegalStateException();
        }
        requestPermissions(generateAudioFileName(), listener);
    }

    @Override
    public synchronized void stopRecording(final StoppingListener listener) {
        if (getState() == RecorderState.INACTTIVE) {
            throw new IllegalStateException();
        }
        mState = RecorderState.INACTTIVE;
        if (listener != null) {
            if (mMediaRecorder != null) {
                releaseMediaRecorder();
                listener.onStopped(this, mFile.getName());
            } else {
                listener.onFailed(this, "Failed to Stop recording.");
            }
        }
        if (mFile != null) {
            mFile = null;
        }
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

    @Override
    public void onDisplayRotation(final int degree) {
    }

    private String generateAudioFileName() {
        return "audio" + mSimpleDateFormat.format(new Date()) + AudioConst.FORMAT_TYPE;
    }

    private void requestPermissions(final String fileName, final RecordingListener listener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PermissionUtility.requestPermissions(mContext, new Handler(Looper.getMainLooper()),
                    new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    new PermissionUtility.PermissionRequestCallback() {
                        @Override
                        public void onSuccess() {
                            startRecordingInternal(fileName, listener);
                        }

                        @Override
                        public void onFail(@NonNull String deniedPermission) {
                            mState = RecorderState.ERROR;
                            listener.onFailed(HostDeviceAudioRecorder.this,
                                    "Permission " + deniedPermission + " not granted.");
                        }
                    });
        } else {
            startRecordingInternal(fileName, listener);
        }
    }

    private void startRecordingInternal(final String fileName, final RecordingListener listener) {
        try {
            initAudioContext(fileName, listener);
        } catch (Exception e) {
            releaseMediaRecorder();
            mState = RecorderState.ERROR;
            listener.onFailed(HostDeviceAudioRecorder.this,
                    e.getClass().getSimpleName() + ": " + e.getLocalizedMessage());
            return;
        }
        mState = RecorderState.RECORDING;
        listener.onRecorded(HostDeviceAudioRecorder.this, fileName);
    }

    private void initAudioContext(final String fileName, final RecordingListener listener) throws IOException {
        FileManager fileMgr = new FileManager(mContext, HostFileProvider.class.getName());

        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

        if (fileName != null) {
            mFile = new File(fileMgr.getBasePath(), fileName);
            mMediaRecorder.setOutputFile(mFile.toString());
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } else {
            mState = RecorderState.ERROR;
            listener.onFailed(HostDeviceAudioRecorder.this, "File name must be specified.");
        }
    }
    /**
     * Check the existence of file.
     *
     * @return true is exist
     */
    private boolean checkAudioFile() {
        return mFile != null && mFile.exists() && mFile.length() > 0;
    }

    /**
     * MediaRecorderを解放.
     */
    private void releaseMediaRecorder() {
        if (checkAudioFile()) {
            // Contents Providerに登録.
            ContentResolver resolver = mContext.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Video.Media.TITLE, mFile.getName());
            values.put(MediaStore.Video.Media.DISPLAY_NAME, mFile.getName());
            values.put(MediaStore.Video.Media.ARTIST, "DeviceConnect");
            values.put(MediaStore.Video.Media.MIME_TYPE, "audio/3gp");
            values.put(MediaStore.Video.Media.DATA, mFile.toString());
            resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
        }

        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }
}
