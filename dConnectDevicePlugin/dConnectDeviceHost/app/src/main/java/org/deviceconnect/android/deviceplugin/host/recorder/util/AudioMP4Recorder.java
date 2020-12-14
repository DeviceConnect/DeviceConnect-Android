package org.deviceconnect.android.deviceplugin.host.recorder.util;

import android.media.MediaRecorder;

import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;

import java.io.File;
import java.io.IOException;

public class AudioMP4Recorder extends MP4Recorder {

    private HostMediaRecorder.Settings mSettings;

    public AudioMP4Recorder(File filePath, HostMediaRecorder.Settings settings) {
        super(filePath);
        mSettings = settings;
    }

    @Override
    public MediaRecorder setUpMediaRecorder(File outputFile) throws IOException {

        // TODO 録音するときの設定を可変にすること。

        MediaRecorder mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setOutputFile(outputFile.getAbsolutePath());
        mMediaRecorder.prepare();
        mMediaRecorder.start();
        return mMediaRecorder;
    }

    @Override
    public void tearDownMediaRecorder() {
    }
}
