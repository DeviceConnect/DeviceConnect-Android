/*
 HostAudioRecorder.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.audio;

import android.Manifest;
import android.content.Context;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.file.HostFileProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceStreamRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.PreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.PreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.util.CapabilityUtil;
import org.deviceconnect.android.deviceplugin.host.recorder.util.MediaSharing;
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
public class HostAudioRecorder implements HostMediaRecorder, HostDeviceStreamRecorder {

    /**
     * ログ出力用フラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * ログ出力用タグ.
     */
    private static final String TAG = "host.dplugin";

    private static final String ID = "audio";

    private static final String NAME = "AndroidHost Audio Recorder";

    private static final String MIME_TYPE = "audio/aac";

    private final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyyMMdd_kkmmss", Locale.JAPAN);

    private final Context mContext;

    /**
     * MediaRecoder.
     */
    private MediaRecorder mMediaRecorder;

    /**
     * フォルダURI.
     */
    private File mFile;

    /**
     * マイムタイプ一覧を定義.
     */
    private List<String> mMimeTypes = new ArrayList<String>() {
        {
            add("audio/aac");
        }
    };

    private RecorderState mState;

    private final MediaSharing mMediaSharing = MediaSharing.getInstance();

    public HostAudioRecorder(final Context context) {
        mContext = context;
        mState = RecorderState.INACTIVE;
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
    public int getIFrameInterval() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setIFrameInterval(int interval) {
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
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSupportedPreviewSize(int width, int height) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onDisplayRotation(final int degree) {
    }

    @Override
    public void muteTrack() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unMuteTrack() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isMutedTrack() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PreviewServerProvider getServerProvider() {
        return null;
    }

    @Override
    public List<PreviewServer> startPreviews() {
        return null;
    }

    @Override
    public void stopPreviews() {
    }

    @Override
    public boolean isAudioEnabled() {
        return false;
    }

    @Override
    public int getPreviewAudioBitRate() {
        return 0;
    }

    @Override
    public int getPreviewSampleRate() {
        return 0;
    }

    @Override
    public int getPreviewChannel() {
        return 0;
    }

    @Override
    public boolean isUseAEC() {
        return false;
    }

    @Override
    public void requestPermission(PermissionCallback callback) {
        CapabilityUtil.requestPermissions(mContext, new PermissionUtility.PermissionRequestCallback() {
            @Override
            public void onSuccess() {
                callback.onAllowed();
            }

            @Override
            public void onFail(final @NonNull String deniedPermission) {
                callback.onDisallowed();
            }
        });
    }

    @Override
    public List<String> getSupportedMimeTypes() {
        return mMimeTypes;
    }

    @Override
    public synchronized void startRecording(final RecordingListener listener) {
        if (getState() == RecorderState.RECORDING) {
            listener.onFailed(this, "MediaRecorder is already recording.");
        } else {
            requestPermissions(generateAudioFileName(), listener);
        }
    }

    @Override
    public synchronized void stopRecording(final StoppingListener listener) {
        if (getState() == RecorderState.INACTIVE) {
            listener.onFailed(this, "MediaRecorder is not running.");
        } else {
            mState = RecorderState.INACTIVE;
            if (listener != null) {
                if (mMediaRecorder != null) {
                    mMediaRecorder.stop();
                    releaseMediaRecorder();
                    registerAudio(mFile);
                    listener.onStopped(this, mFile.getName());
                } else {
                    listener.onFailed(this, "Failed to Stop recording.");
                }
            }
            mFile = null;
        }
    }

    @Override
    public boolean canPauseRecording() {
        return Build.VERSION_CODES.N <= Build.VERSION.SDK_INT;
    }

    @Override
    public void pauseRecording() {
        if (mMediaRecorder == null) {
            return;
        }

        if (getState() != RecorderState.RECORDING) {
            return;
        }

        if (canPauseRecording()) {
            try {
                mMediaRecorder.pause();
                mState = RecorderState.PAUSED;
            } catch (IllegalStateException e) {
                // ignore.
            }
        }
    }

    @Override
    public void resumeRecording() {
        if (mMediaRecorder == null) {
            return;
        }

        if (getState() != RecorderState.PAUSED) {
            return;
        }

        if (canPauseRecording()) {
            try {
                mMediaRecorder.resume();
                mState = RecorderState.RECORDING;
            } catch (IllegalStateException e) {
                // ignore.
            }
        }
    }

    private String generateAudioFileName() {
        return "android_audio_" + mSimpleDateFormat.format(new Date()) + AudioConst.FORMAT_TYPE;
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
                            listener.onFailed(HostAudioRecorder.this,
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
            mState = RecorderState.RECORDING;
            listener.onRecorded(this, fileName);
        } catch (Exception e) {
            releaseMediaRecorder();
            mState = RecorderState.ERROR;
            listener.onFailed(this, e.getClass().getSimpleName() + ": " + e.getLocalizedMessage());
        }
    }

    private void initAudioContext(final String fileName, final RecordingListener listener) throws IOException {
        FileManager fileMgr = new FileManager(mContext, HostFileProvider.class.getName());
        mFile = new File(fileMgr.getBasePath(), fileName);

        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setOutputFile(mFile.toString());
        mMediaRecorder.prepare();
        mMediaRecorder.start();
    }

    /**
     * MediaRecorderを解放.
     */
    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    private void registerAudio(final File audioFile) {
        Uri uri = mMediaSharing.shareAudio(mContext, audioFile);
        if (DEBUG) {
            String filePath = audioFile.getAbsolutePath();
            if (uri != null) {
                Log.d(TAG, "Registered audio: filePath=" + filePath + ", uri=" + uri.getPath());
            } else {
                Log.e(TAG, "Failed to register audio: file=" + filePath);
            }
        }
    }
}
