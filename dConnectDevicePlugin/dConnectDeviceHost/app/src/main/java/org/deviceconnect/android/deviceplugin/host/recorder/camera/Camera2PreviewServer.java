package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.content.Context;
import android.media.AudioFormat;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;

/**
 * Camera2 のプレビュー配信サーバの基底クラス.
 */
abstract class Camera2PreviewServer extends AbstractPreviewServer {

    Camera2PreviewServer(Context context, HostMediaRecorder recorder) {
        super(context, recorder);
    }

    Camera2PreviewServer(Context context, HostMediaRecorder recorder, boolean useSSL) {
        super(context, recorder, useSSL);
    }

    /**
     * カメラの再起動を要求します.
     */
    abstract void restartCamera();

    /**
     * VideoEncoder の設定に、Camera2Recorder の設定を反映します.
     *
     * @param videoQuality 設定を行う VideoEncoder の VideoQuality
     */
    void setVideoQuality(VideoQuality videoQuality) {
        Camera2Recorder recorder = (Camera2Recorder) getRecorder();
        HostMediaRecorder.Settings settings = recorder.getSettings();

        videoQuality.setVideoWidth(settings.getPreviewSize().getWidth());
        videoQuality.setVideoHeight(settings.getPreviewSize().getHeight());
        videoQuality.setBitRate(settings.getPreviewBitRate());
        videoQuality.setFrameRate(settings.getPreviewMaxFrameRate());
        videoQuality.setIFrameInterval(settings.getPreviewKeyFrameInterval());
        videoQuality.setUseSoftwareEncoder(settings.isUseSoftwareEncoder());
        videoQuality.setIntraRefresh(settings.getIntraRefresh());
    }

    /**
     * AudioEncoder の設定に、Camera2Recorder の設定を反映します.
     *
     * @param audioQuality 設定を行う AudioEncoder の AudioQuality
     */
    void setAudioQuality(AudioQuality audioQuality) {
        Camera2Recorder recorder = (Camera2Recorder) getRecorder();
        HostMediaRecorder.Settings settings = recorder.getSettings();

        audioQuality.setChannel(settings.getPreviewChannel() == 1 ?
                AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO);
        audioQuality.setSamplingRate(settings.getPreviewSampleRate());
        audioQuality.setBitRate(settings.getPreviewAudioBitRate());
        audioQuality.setUseAEC(settings.isUseAEC());
    }
}
