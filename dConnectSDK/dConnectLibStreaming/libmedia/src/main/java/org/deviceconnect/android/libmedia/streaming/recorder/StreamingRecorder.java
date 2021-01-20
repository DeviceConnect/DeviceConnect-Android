package org.deviceconnect.android.libmedia.streaming.recorder;

import org.deviceconnect.android.libmedia.streaming.MediaEncoderException;
import org.deviceconnect.android.libmedia.streaming.MediaStreamer;
import org.deviceconnect.android.libmedia.streaming.audio.AudioEncoder;
import org.deviceconnect.android.libmedia.streaming.muxer.Mpeg4Muxer;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;

import java.io.File;

public class StreamingRecorder {

    private final MediaStreamer mMediaStreamer;

    public StreamingRecorder(File file) {
        mMediaStreamer = new MediaStreamer(new Mpeg4Muxer(file.getPath()));
        mMediaStreamer.setOnEventListener(new MediaStreamer.OnEventListener() {
            @Override
            public void onStarted() {
            }

            @Override
            public void onStopped() {
            }

            @Override
            public void onError(MediaEncoderException e) {
            }
        });
    }

    /**
     * SRT のセッションを開始します.
     */
    public void start() {
        mMediaStreamer.start();
    }

    /**
     * SRT のセッションを停止します.
     */
    public void stop() {
        mMediaStreamer.stop();
    }

    /**
     * 映像エンコーダーを設定します.
     *
     * @param videoEncoder 映像エンコーダー
     */
    public void setVideoEncoder(VideoEncoder videoEncoder) {
        mMediaStreamer.setVideoEncoder(videoEncoder);
    }

    /**
     * 音声エンコーダーを設定します.
     *
     * @param audioEncoder 音声エンコーダー
     */
    public void setAudioEncoder(AudioEncoder audioEncoder) {
        mMediaStreamer.setAudioEncoder(audioEncoder);
    }

    /**
     * 映像エンコーダーを取得します.
     *
     * @return 映像エンコーダー
     */
    public VideoEncoder getVideoEncoder() {
        return mMediaStreamer.getVideoEncoder();
    }

    /**
     * 音声エンコーダーを取得します.
     *
     * @return 音声エンコーダー
     */
    public AudioEncoder getAudioEncoder() {
        return mMediaStreamer.getAudioEncoder();
    }
}
