package org.deviceconnect.android.deviceplugin.host.recorder;

import android.graphics.Rect;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioPlaybackCaptureConfiguration;
import android.media.projection.MediaProjection;
import android.os.Build;
import android.util.Size;

import org.deviceconnect.android.deviceplugin.host.recorder.util.MediaProjectionProvider;
import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;
import org.deviceconnect.android.libmedia.streaming.audio.MicAudioQuality;
import org.deviceconnect.android.libmedia.streaming.audio.filter.HighPassFilter;
import org.deviceconnect.android.libmedia.streaming.audio.filter.LowPassFilter;
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
        return "video/x-rtmp";
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

        EGLSurfaceDrawingThread d = recorder.getSurfaceDrawingThread();
        Size previewSize = settings.getPreviewSize(getMimeType());
        if (previewSize == null) {
            previewSize = settings.getPreviewSize();
        }
        int w = d.isSwappedDimensions() ? previewSize.getHeight() : previewSize.getWidth();
        int h = d.isSwappedDimensions() ? previewSize.getWidth() : previewSize.getHeight();
        videoQuality.setVideoWidth(w);
        videoQuality.setVideoHeight(h);
        videoQuality.setDrawingRange(settings.getDrawingRange(getMimeType()));
        videoQuality.setBitRate(settings.getPreviewBitRate(getMimeType()));
        videoQuality.setFrameRate(settings.getPreviewMaxFrameRate(getMimeType()));
        videoQuality.setIFrameInterval(settings.getPreviewKeyFrameInterval(getMimeType()));
        videoQuality.setUseSoftwareEncoder(settings.isUseSoftwareEncoder(getMimeType()));
        videoQuality.setIntraRefresh(settings.getIntraRefresh(getMimeType()));
        videoQuality.setProfile(settings.getProfile(getMimeType()));
        videoQuality.setLevel(settings.getLevel(getMimeType()));
        if (settings.getPreviewBitRateMode(getMimeType()) != null) {
            switch (settings.getPreviewBitRateMode(getMimeType())) {
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

        if (settings.getAudioFilter() != null) {
            float coeff = settings.getAudioCoefficient();
            switch (settings.getAudioFilter()) {
                case LOW_PASS:
                    audioQuality.setFilter(new LowPassFilter(audioQuality, coeff));
                    break;
                case HIGH_PASS:
                    audioQuality.setFilter(new HighPassFilter(audioQuality, coeff));
                    break;
                default:
                    audioQuality.setFilter(null);
                    break;
            }
        } else {
            audioQuality.setFilter(null);
        }

        MicAudioQuality quality = (MicAudioQuality) audioQuality;
        if (settings.getPreviewAudioSource() == HostMediaRecorder.AudioSource.APP) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // アプリの録音機能
                MediaProjectionProvider provider = recorder.getMediaProjectionProvider();
                if (provider != null && provider.getMediaProjection() != null) {
                    MediaProjection mediaProjection = provider.getMediaProjection();
                    AudioPlaybackCaptureConfiguration configuration =
                            new AudioPlaybackCaptureConfiguration.Builder(mediaProjection)
                                    .addMatchingUsage(AudioAttributes.USAGE_GAME)
                                    .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                                    .addMatchingUsage(AudioAttributes.USAGE_UNKNOWN)
                                    .build();
                    quality.setCaptureConfig(configuration);
                }
            }
            quality.setSource(MicAudioQuality.Source.APP);
        }
    }
}
