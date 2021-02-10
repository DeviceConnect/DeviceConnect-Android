package org.deviceconnect.android.deviceplugin.host.recorder.util;

import android.media.MediaRecorder;
import android.util.Log;

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

        MediaRecorder mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setOutputFile(outputFile.getAbsolutePath());
        mediaRecorder.prepare();
        return mediaRecorder;
    }

    @Override
    public void tearDownMediaRecorder() {
    }
}
