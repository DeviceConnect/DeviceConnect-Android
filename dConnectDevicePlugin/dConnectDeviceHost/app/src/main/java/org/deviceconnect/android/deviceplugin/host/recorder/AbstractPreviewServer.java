package org.deviceconnect.android.deviceplugin.host.recorder;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioPlaybackCaptureConfiguration;
import android.media.projection.MediaProjection;
import android.os.Build;
import android.util.Size;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.recorder.util.MediaProjectionProvider;
import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;
import org.deviceconnect.android.libmedia.streaming.audio.MicAudioQuality;
import org.deviceconnect.android.libmedia.streaming.audio.filter.HighPassFilter;
import org.deviceconnect.android.libmedia.streaming.audio.filter.LowPassFilter;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;

import javax.net.ssl.SSLContext;

/**
 * プレビュー配信サーバ.
 */
public abstract class AbstractPreviewServer implements PreviewServer {
    protected static final boolean DEBUG = BuildConfig.DEBUG;
    protected static final String TAG = "host.dplugin";

    /**
     * コンテキスト.
     */
    private final Context mContext;

    /**
     * プレビュー再生を行うレコーダ.
     */
    private final HostMediaRecorder mHostMediaRecorder;

    /**
     * プレビュー配信サーバのポート番号.
     */
    private int mPort;

    /**
     * ミュート設定.
     */
    private boolean mMute;

    /**
     * SSLContext のインスタンス.
     */
    private SSLContext mSSLContext;

    /**
     * SSL の使用フラグ.
     */
    private final boolean mUseSSL;

    /**
     * コンストラクタ.
     *
     * <p>
     * デフォルトでは、mute は true に設定しています。
     * デフォルトでは、mUseSSL は false に設定します。
     * </p>
     *
     * @param context コンテキスト
     * @param recorder プレビューで表示するレコーダ
     */
    public AbstractPreviewServer(Context context, HostMediaRecorder recorder) {
        this(context, recorder, false);
    }

    /**
     * コンストラクタ.
     *
     * <p>
     * デフォルトでは、mute は true に設定しています。
     * </p>
     *
     * @param context コンテキスト
     * @param recorder プレビューで表示するレコーダ
     * @param useSSL SSL使用フラグ
     */
    public AbstractPreviewServer(Context context, HostMediaRecorder recorder, boolean useSSL) {
        mContext = context;
        mHostMediaRecorder = recorder;
        mUseSSL = useSSL;
        mMute = true;
    }

    // Implements PreviewServer methods.

    @Override
    public int getPort() {
        return mPort;
    }

    @Override
    public void setPort(int port) {
        mPort = port;
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

    @Override
    public void setMute(boolean mute) {
        mMute = mute;
    }

    @Override
    public boolean isMuted() {
        return mMute;
    }

    @Override
    public boolean useSSLContext() {
        return mUseSSL;
    }

    @Override
    public void setSSLContext(final SSLContext sslContext) {
        mSSLContext = sslContext;
    }

    @Override
    public SSLContext getSSLContext() {
        return mSSLContext;
    }

    /**
     * コンテキストを取得します.
     *
     * @return コンテキスト
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * プレビューを表示するレコーダー.
     *
     * @return レコーダー
     */
    public HostMediaRecorder getRecorder() {
        return mHostMediaRecorder;
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
            switch (settings.getPreviewBitRateMode()) {
                default:
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

        mMute = settings.isMute();
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
