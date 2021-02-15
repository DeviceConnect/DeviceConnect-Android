package org.deviceconnect.android.deviceplugin.uvc.recorder;

import android.util.Size;

import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;

public abstract class AbstractBroadcaster implements Broadcaster {
    /**
     * 配信先の URI.
     */
    private final String mBroadcastURI;

    /**
     * カメラを操作するレコーダ.
     */
    private final MediaRecorder mRecorder;

    public AbstractBroadcaster(MediaRecorder recorder, String broadcastURI) {
        mRecorder = recorder;
        mBroadcastURI = broadcastURI;
    }

    @Override
    public String getMimeType() {
        return "";
    }

    @Override
    public String getBroadcastURI() {
        return mBroadcastURI;
    }

    @Override
    public void onConfigChange() {
        VideoQuality videoQuality = getVideoQuality();
        if (videoQuality != null) {
            setVideoQuality(videoQuality);
        }

        AudioQuality audioQuality = getAudioQuality();
        if (audioQuality != null) {
            setAudioQuality(audioQuality);

            MediaRecorder.Settings settings = getRecorder().getSettings();
            setMute(settings.isMute());
        }
    }

    /**
     * 映像の設定を取得します.
     *
     * 映像が使用されていない場合は null を返却すること。
     *
     * @return 映像の設定
     */
    protected VideoQuality getVideoQuality() {
        return null;
    }

    /**
     * 音声の設定を取得します.
     *
     * 音声が使用されていない場合は null を返却すること。
     *
     * @return 音声の設定
     */
    protected AudioQuality getAudioQuality() {
        return null;
    }

    /**
     * Broadcaster で使用するレコーダを取得します.
     *
     * @return Broadcaster で使用するレコーダ
     */
    public MediaRecorder getRecorder() {
        return mRecorder;
    }

    /**
     * VideoEncoder の設定に、MediaRecorder の設定を反映します.
     *
     * @param videoQuality 設定を行う VideoEncoder の VideoQuality
     */
    public void setVideoQuality(VideoQuality videoQuality) {
        MediaRecorder recorder = getRecorder();
        MediaRecorder.Settings settings = recorder.getSettings();

        Size previewSize = settings.getPreviewSize();
        videoQuality.setVideoWidth(previewSize.getWidth());
        videoQuality.setVideoHeight(previewSize.getHeight());
        videoQuality.setBitRate(settings.getPreviewBitRate());
        videoQuality.setFrameRate(settings.getPreviewMaxFrameRate());
        videoQuality.setIFrameInterval(settings.getPreviewKeyFrameInterval());
        videoQuality.setUseSoftwareEncoder(settings.isUseSoftwareEncoder());
        videoQuality.setIntraRefresh(settings.getIntraRefresh());
        videoQuality.setProfile(settings.getProfile());
        videoQuality.setLevel(settings.getLevel());
    }

    /**
     * AudioEncoder の設定に、MediaRecorder の設定を反映します.
     *
     * @param audioQuality 設定を行う AudioEncoder の AudioQuality
     */
    public void setAudioQuality(AudioQuality audioQuality) {
    }
}
