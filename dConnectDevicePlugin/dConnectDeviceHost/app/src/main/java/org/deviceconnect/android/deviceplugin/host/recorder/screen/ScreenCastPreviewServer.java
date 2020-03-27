package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.content.Context;
import android.media.AudioFormat;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;

abstract class ScreenCastPreviewServer extends AbstractPreviewServer {

    ScreenCastPreviewServer(Context context, HostMediaRecorder recorder) {
        super(context, recorder);
    }

    /**
     * VideoQuality にスクリーンキャストの設定を行います.
     *
     * @param videoQuality 設定を行う VideoQuality
     */
    void setVideoQuality(VideoQuality videoQuality) {
        ScreenCastRecorder recorder = (ScreenCastRecorder) getRecorder();

        HostMediaRecorder.PictureSize size = recorder.getPreviewSize();

        videoQuality.setVideoWidth(size.getWidth());
        videoQuality.setVideoHeight(size.getHeight());
        videoQuality.setBitRate(recorder.getPreviewBitRate());
        videoQuality.setFrameRate((int) recorder.getMaxFrameRate());
        videoQuality.setIFrameInterval(recorder.getIFrameInterval());
    }

    /**
     * AudioQuality の設定を行います.
     *
     * @param audioQuality 設定を行う AudioQuality
     */
    void setAudioQuality(AudioQuality audioQuality) {
        ScreenCastRecorder recorder = (ScreenCastRecorder) getRecorder();

        audioQuality.setChannel(recorder.getPreviewChannel() == 1 ?
                AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO);
        audioQuality.setSamplingRate(recorder.getPreviewSampleRate());
        audioQuality.setBitRate(recorder.getPreviewAudioBitRate());
        audioQuality.setUseAEC(recorder.isUseAEC());
    }
}
