/*
 HostAudioRecorder.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.audio;

import android.content.Context;
import android.os.Build;
import android.os.Handler;

import androidx.annotation.NonNull;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.recorder.AbstractMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.BroadcasterProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.PreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.util.AudioMP4Recorder;
import org.deviceconnect.android.deviceplugin.host.recorder.util.CapabilityUtil;
import org.deviceconnect.android.deviceplugin.host.recorder.util.MP4Recorder;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;
import org.deviceconnect.android.provider.FileManager;

import java.io.File;
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
public class HostAudioRecorder extends AbstractMediaRecorder {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "host.dplugin";
    private static final String ID = "audio";
    private static final String NAME = "AndroidHost Audio Recorder";
    private static final String MIME_TYPE = "audio/aac";

    /**
     * マイムタイプ一覧を定義.
     */
    private List<String> mMimeTypes = new ArrayList<String>() {
        {
            add("audio/aac");
        }
    };

    private final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyyMMdd_kkmmss", Locale.JAPAN);
    private final Context mContext;

    /**
     * MP4レコーダ.
     */
    private MP4Recorder mMP4Recorder;

    private Settings mSettings = new Settings();

    private AudioPreviewServerProvider mAudioPreviewServerProvider;
    private AudioBroadcasterProvider mAudioBroadcasterProvider;

    private State mState = State.INACTIVE;

    public HostAudioRecorder(final Context context, FileManager fileManager) {
        super(context, 3, fileManager);
        mContext = context;

        initSettings();
    }

    private void initSettings() {
    }

    @Override
    public EGLSurfaceDrawingThread getSurfaceDrawingThread() {
        return null;
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
    public State getState() {
        return mState;
    }

    @Override
    public Settings getSettings() {
        return null;
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public List<String> getSupportedMimeTypes() {
        return mMimeTypes;
    }

    @Override
    public void onDisplayRotation(final int degree) {
    }

    @Override
    public PreviewServerProvider getServerProvider() {
        return mAudioPreviewServerProvider;
    }

    @Override
    public BroadcasterProvider getBroadcasterProvider() {
        return mAudioBroadcasterProvider;
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

    // HostDevicePhotoRecorder

    @Override
    public void takePhoto(OnPhotoEventListener listener) {
    }

    @Override
    public void turnOnFlashLight(@NonNull TurnOnFlashLightListener listener, @NonNull Handler handler) {
    }

    @Override
    public void turnOffFlashLight(@NonNull TurnOffFlashLightListener listener, @NonNull Handler handler) {
    }

    @Override
    public boolean isFlashLightState() {
        return false;
    }

    @Override
    public boolean isUseFlashLight() {
        return false;
    }

    // HostDeviceStreamRecorder

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
    public synchronized void startRecording(final RecordingListener listener) {
        if (getState() != State.INACTIVE) {
            listener.onFailed(this, "MediaRecorder is already recording.");
        } else {
            requestPermission(new PermissionCallback() {
                @Override
                public void onAllowed() {
                    startRecordingInternal(listener);
                }

                @Override
                public void onDisallowed() {
                    mState = State.ERROR;
                    listener.onFailed(HostAudioRecorder.this, "Permission not granted.");
                }
            });
        }
    }

    @Override
    public synchronized void stopRecording(final StoppingListener listener) {
        if (getState() == State.INACTIVE) {
            if (listener != null) {
                listener.onFailed(this, "MediaRecorder is not running.");
            }
        } else {
            stopRecordingInternal(listener);
        }
    }

    @Override
    public boolean canPauseRecording() {
        return Build.VERSION_CODES.N <= Build.VERSION.SDK_INT;
    }

    @Override
    public void pauseRecording() {
        if (mMP4Recorder == null) {
            return;
        }

        if (getState() != State.RECORDING) {
            return;
        }

        if (canPauseRecording()) {
            mMP4Recorder.pause();
            mState = State.PAUSED;
        }
    }

    @Override
    public void resumeRecording() {
        if (mMP4Recorder == null) {
            return;
        }

        if (getState() != State.PAUSED) {
            return;
        }

        if (canPauseRecording()) {
            mMP4Recorder.resume();
            mState = State.RECORDING;
        }
    }

    @Override
    public String getStreamMimeType() {
        return MIME_TYPE;
    }

    // private method.

    private String generateAudioFileName() {
        return "android_audio_" + mSimpleDateFormat.format(new Date()) + AudioConst.FORMAT_TYPE;
    }

    /**
     * 録画を行います.
     *
     * @param listener 録画開始結果を通知するリスナー
     */
    private void startRecordingInternal(final RecordingListener listener) {
        if (mMP4Recorder != null) {
            listener.onFailed(this, "Recording has started already.");
            return;
        }

        File filePath = new File(getFileManager().getBasePath(), generateAudioFileName());
        mMP4Recorder = new AudioMP4Recorder(filePath, mSettings);
        mMP4Recorder.start(new MP4Recorder.OnRecordingStartListener() {
            @Override
            public void onRecordingStart() {
                mState = State.RECORDING;

                if (listener != null) {
                    listener.onRecorded(HostAudioRecorder.this, mMP4Recorder.getOutputFile().getAbsolutePath());
                }
            }

            @Override
            public void onRecordingStartError(Throwable e) {
                if (mMP4Recorder != null) {
                    mMP4Recorder.release();
                    mMP4Recorder = null;
                }

                if (listener != null) {
                    listener.onFailed(HostAudioRecorder.this,
                            "Failed to start recording because of camera problem: " + e.getMessage());
                }
            }
        });
    }

    /**
     * 録画停止を行います.
     *
     * @param listener 録画停止結果を通知するリスナー
     */
    private void stopRecordingInternal(final StoppingListener listener) {
        if (mMP4Recorder == null) {
            listener.onFailed(this, "Recording has stopped already.");
            return;
        }

        mState = State.INACTIVE;

        mMP4Recorder.stop(new MP4Recorder.OnRecordingStopListener() {
            @Override
            public void onRecordingStop() {
                File videoFile = mMP4Recorder.getOutputFile();
                registerVideo(videoFile);
                mMP4Recorder.release();
                mMP4Recorder = null;

                if (listener != null) {
                    listener.onStopped(HostAudioRecorder.this, videoFile.getAbsolutePath());
                }
            }

            @Override
            public void onRecordingStopError(Throwable e) {
                if (mMP4Recorder != null) {
                    mMP4Recorder.release();
                    mMP4Recorder = null;
                }

                if (listener != null) {
                    listener.onFailed(HostAudioRecorder.this,
                            "Failed to stop recording for unexpected error: " + e.getMessage());
                }
            }
        });
    }
}
