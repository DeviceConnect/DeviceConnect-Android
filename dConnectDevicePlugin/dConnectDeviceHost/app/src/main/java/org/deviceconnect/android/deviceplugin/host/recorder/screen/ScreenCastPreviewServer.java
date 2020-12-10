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
        HostMediaRecorder.Settings settings = recorder.getSettings();

        HostMediaRecorder.Size size = settings.getPreviewSize();

        videoQuality.setVideoWidth(size.getWidth());
        videoQuality.setVideoHeight(size.getHeight());
        videoQuality.setBitRate(settings.getPreviewBitRate());
        videoQuality.setFrameRate((int) settings.getPreviewMaxFrameRate());
        videoQuality.setIFrameInterval(settings.getPreviewKeyFrameInterval());
    }

    /**
     * AudioQuality の設定を行います.
     *
     * @param audioQuality 設定を行う AudioQuality
     */
    void setAudioQuality(AudioQuality audioQuality) {
        ScreenCastRecorder recorder = (ScreenCastRecorder) getRecorder();
        HostMediaRecorder.Settings settings = recorder.getSettings();

        audioQuality.setChannel(settings.getPreviewChannel() == 1 ?
                AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO);
        audioQuality.setSamplingRate(settings.getPreviewSampleRate());
        audioQuality.setBitRate(settings.getPreviewAudioBitRate());
        audioQuality.setUseAEC(settings.isUseAEC());
    }
}
