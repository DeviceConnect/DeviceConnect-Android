package org.deviceconnect.android.deviceplugin.host.recorder;

import android.content.Context;
import android.graphics.Rect;
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
