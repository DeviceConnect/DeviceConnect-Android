package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioPlaybackCaptureConfiguration;
import android.media.projection.MediaProjection;
import android.os.Build;
import android.util.Size;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.util.MediaProjectionProvider;
import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;
import org.deviceconnect.android.libmedia.streaming.audio.MicAudioQuality;
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
        Size size = settings.getPreviewSize();

        videoQuality.setVideoWidth(size.getWidth());
        videoQuality.setVideoHeight(size.getHeight());
        videoQuality.setBitRate(settings.getPreviewBitRate());
        videoQuality.setFrameRate(settings.getPreviewMaxFrameRate());
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


        // アプリの録音機能
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaProjectionProvider client = recorder.getMediaProjectionClient();
            if (client != null && client.getMediaProjection() != null) {
                MediaProjection mediaProjection = client.getMediaProjection();
                AudioPlaybackCaptureConfiguration configuration =
                        new AudioPlaybackCaptureConfiguration.Builder(mediaProjection)
                                .addMatchingUsage(AudioAttributes.USAGE_GAME)
                                .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                                .addMatchingUsage(AudioAttributes.USAGE_UNKNOWN)
                                .build();
                ((MicAudioQuality) audioQuality).setCaptureConfig(configuration);
            }
        }
    }
}
