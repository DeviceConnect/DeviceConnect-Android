package org.deviceconnect.android.deviceplugin.host.recorder;

import android.graphics.Rect;
import android.media.AudioFormat;
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
    private final HostMediaRecorder mRecorder;

    public AbstractBroadcaster(HostMediaRecorder recorder, String broadcastURI) {
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

            HostMediaRecorder.Settings settings = getRecorder().getSettings();
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
    public HostMediaRecorder getRecorder() {
        return mRecorder;
    }

    /**
     * VideoEncoder の設定に、HostMediaRecorder の設定を反映します.
     *
     * @param videoQuality 設定を行う VideoEncoder の VideoQuality
     */
    public void setVideoQuality(VideoQuality videoQuality) {
        HostMediaRecorder recorder = getRecorder();
        HostMediaRecorder.Settings settings = recorder.getSettings();

        Rect rect = settings.getDrawingRange();
        if (rect != null) {
            videoQuality.setVideoWidth(rect.width());
            videoQuality.setVideoHeight(rect.height());
        } else {
            EGLSurfaceDrawingThread d = recorder.getSurfaceDrawingThread();
            Size previewSize = settings.getPreviewSize();
            int w = d.isSwappedDimensions() ? previewSize.getHeight() : previewSize.getWidth();
            int h = d.isSwappedDimensions() ? previewSize.getWidth() : previewSize.getHeight();
            videoQuality.setVideoWidth(w);
            videoQuality.setVideoHeight(h);
        }
        videoQuality.setBitRate(settings.getPreviewBitRate());
        videoQuality.setFrameRate(settings.getPreviewMaxFrameRate());
        videoQuality.setIFrameInterval(settings.getPreviewKeyFrameInterval());
        videoQuality.setUseSoftwareEncoder(settings.isUseSoftwareEncoder());
        videoQuality.setIntraRefresh(settings.getIntraRefresh());
        videoQuality.setProfile(settings.getProfile());
        videoQuality.setLevel(settings.getLevel());
        if (settings.getPreviewBitRateMode() != null) {
            switch (settings.getPreviewBitRateMode()) {
                case VBR:
                    videoQuality.setBitRateMode(VideoQuality.BitRateMode.VBR);
                    break;
                case CBR:
                    videoQuality.setBitRateMode(VideoQuality.BitRateMode.CBR);
                    break;
                case CQ:
                    videoQuality.setBitRateMode(VideoQuality.BitRateMode.CQ);
                    break;
            }
        } else {
            videoQuality.setBitRateMode(null);
        }
    }

    /**
     * AudioEncoder の設定に、HostMediaRecorder の設定を反映します.
     *
     * @param audioQuality 設定を行う AudioEncoder の AudioQuality
     */
    public void setAudioQuality(AudioQuality audioQuality) {
        HostMediaRecorder recorder = getRecorder();
        HostMediaRecorder.Settings settings = recorder.getSettings();

        audioQuality.setChannel(settings.getPreviewChannel() == 1 ?
                AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO);
        audioQuality.setSamplingRate(settings.getPreviewSampleRate());
        audioQuality.setBitRate(settings.getPreviewAudioBitRate());
        audioQuality.setUseAEC(settings.isUseAEC());
    }
}
