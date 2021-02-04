/*
 HostAudioRecorder.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.audio;

import android.Manifest;
import android.content.Context;
import android.os.Handler;
import android.util.Size;

import androidx.annotation.NonNull;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.BroadcasterProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.PreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.util.AudioMP4Recorder;
import org.deviceconnect.android.deviceplugin.host.recorder.util.MP4Recorder;
import org.deviceconnect.android.deviceplugin.host.recorder.util.MediaProjectionProvider;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;
import org.deviceconnect.android.provider.FileManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Host Device Audio Recorder.
 *
 * @author NTT DOCOMO, INC.
 */
public class HostAudioRecorder extends AbstractMediaRecorder {
    private static final String ID = "audio";
    private static final String NAME = "AndroidHost Audio Recorder";
    private static final String MIME_TYPE = "audio/aac";

    /**
     * マイムタイプ一覧を定義.
     */
    private final List<String> mMimeTypes = new ArrayList<String>() {
        {
            add("audio/aac");
        }
    };

    private final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyyMMdd_kkmmss", Locale.JAPAN);
    private final AudioSettings mSettings;
    private final AudioPreviewServerProvider mAudioPreviewServerProvider;
    private final AudioBroadcasterProvider mAudioBroadcasterProvider;

    private final TempEGLSurfaceDrawingThread mTempEGLSurfaceDrawingThread;

    public HostAudioRecorder(final Context context, FileManager fileManager, MediaProjectionProvider provider) {
        super(context, fileManager, provider);
        mSettings = new AudioSettings(context, this);

        initSettings();

        mAudioPreviewServerProvider = new AudioPreviewServerProvider(context, this);
        mAudioBroadcasterProvider = new AudioBroadcasterProvider(context, this);

        // RTMP 配信の時に映像がないと正常に動作しないことを考慮
        mTempEGLSurfaceDrawingThread = new TempEGLSurfaceDrawingThread();
        mTempEGLSurfaceDrawingThread.setSize(320, 240);
    }

    private void initSettings() {
        if (!mSettings.isInitialized()) {
            mSettings.setPreviewSize(new Size(320, 240));
            mSettings.setPreviewBitRate(512 * 1024);
            mSettings.setPreviewMaxFrameRate(30);
            mSettings.setPreviewKeyFrameInterval(1);

            mSettings.setPreviewAudioSource(AudioSource.DEFAULT);
            mSettings.setPreviewAudioBitRate(64 * 1024);
            mSettings.setPreviewSampleRate(16000);
            mSettings.setPreviewChannel(1);
            mSettings.setUseAEC(true);

            mSettings.setRtspPort(32000);
            mSettings.setSrtPort(33000);

            mSettings.finishInitialization();
        }
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
    public Settings getSettings() {
        return mSettings;
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
    public void onConfigChange() {
        mAudioPreviewServerProvider.onConfigChange();
        mAudioBroadcasterProvider.onConfigChange();
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
    public EGLSurfaceDrawingThread getSurfaceDrawingThread() {
        return mTempEGLSurfaceDrawingThread;
    }

    @Override
    public void requestPermission(PermissionCallback callback) {
        requestPermission(new String[]{
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, new PermissionCallback() {
            @Override
            public void onAllowed() {
                if (mSettings.getPreviewAudioSource() == AudioSource.APP) {
                    requestMediaProjection(callback);
                } else {
                    callback.onAllowed();
                }
            }

            @Override
            public void onDisallowed() {
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
    public String getStreamMimeType() {
        return MIME_TYPE;
    }

//    @Override
//    public boolean canPauseRecording() {
//        return Build.VERSION_CODES.N <= Build.VERSION.SDK_INT;
//    }
//
//    @Override
//    public void pauseRecording() {
//        if (mMP4Recorder == null) {
//            return;
//        }
//
//        if (getState() != State.RECORDING) {
//            return;
//        }
//
//        if (canPauseRecording()) {
//            mMP4Recorder.pause();
//            setState(State.PAUSED);
//        }
//    }
//
//    @Override
//    public void resumeRecording() {
//        if (mMP4Recorder == null) {
//            return;
//        }
//
//        if (getState() != State.PAUSED) {
//            return;
//        }
//
//        if (canPauseRecording()) {
//            mMP4Recorder.resume();
//            setState(State.RECORDING);
//        }
//    }

    public boolean hasVideo() {
        return false;
    }

    // private method.

    private String generateAudioFileName() {
        return "android_audio_" + mSimpleDateFormat.format(new Date()) + AudioConst.FORMAT_TYPE;
    }

    protected MP4Recorder createMP4Recorder() {
        File filePath = new File(getFileManager().getBasePath(), generateAudioFileName());
        return new AudioMP4Recorder(filePath, mSettings);
    }

    private static class AudioSettings extends Settings {
        AudioSettings(Context context, HostMediaRecorder recorder) {
            super(context, recorder);
        }

        @Override
        public List<Size> getSupportedPreviewSizes() {
            return Collections.singletonList(new Size(320, 240));
        }
    }

    private static class TempEGLSurfaceDrawingThread extends EGLSurfaceDrawingThread {

    }
}
