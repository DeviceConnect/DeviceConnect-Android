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
import org.deviceconnect.android.deviceplugin.host.recorder.util.MovingRectThread;
import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;
import org.deviceconnect.android.libmedia.streaming.audio.MicAudioQuality;
import org.deviceconnect.android.libmedia.streaming.audio.filter.HighPassFilter;
import org.deviceconnect.android.libmedia.streaming.audio.filter.LowPassFilter;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;
import org.deviceconnect.android.libmedia.streaming.util.WeakReferenceList;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;

import javax.net.ssl.SSLContext;

/**
 * プレビュー配信サーバ.
 */
public abstract class AbstractPreviewServer implements PreviewServer, CropInterface {
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
     * プレビュー配信サーバ名.
     */
    private final String mName;

    /**
     * 切り抜き範囲移動用スレッド.
     */
    private MovingRectThread mMovingRectThread;

    /**
     * 切り抜き範囲移動用スレッドからのイベントを受け取るリスナー.
     */
    private final MovingRectThread.OnEventListener mMovingRectThreadOnEventListener = (rect) -> {
        VideoQuality videoQuality = getVideoQuality();
        if (videoQuality != null) {
            videoQuality.setCropRect(new Rect(rect));
        }
        getStreamingSettings().setCropRect(rect);
        postOnMoved(rect);
    };

    /**
     * コンストラクタ.
     *
     * <p>
     * デフォルトでは、ミュート設定は true に設定しています。
     * デフォルトでは、mUseSSL は false に設定します。
     * </p>
     *
     * @param context コンテキスト
     * @param recorder プレビューで表示するレコーダ
     * @param name サーバ名
     */
    public AbstractPreviewServer(Context context, HostMediaRecorder recorder, String name) {
        this(context, recorder, name, false);
    }

    /**
     * コンストラクタ.
     *
     * <p>
     * デフォルトでは、ミュート設定は true に設定しています。
     * </p>
     *
     * @param context コンテキスト
     * @param recorder プレビューで表示するレコーダ
     * @param name 名前
     * @param useSSL SSL使用フラグ
     */
    public AbstractPreviewServer(Context context, HostMediaRecorder recorder, String name, boolean useSSL) {
        mContext = context;
        mHostMediaRecorder = recorder;
        mUseSSL = useSSL;
        mMute = true;
        mName = name;
        startMovingRectThread();
    }

    // Implements PreviewServer methods.

    @Override
    public String getName() {
        return mName;
    }

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

    @Override
    public void release() {
        stopMovingRectThread();
    }

    // CropInterface implements

    private final WeakReferenceList<OnEventListener> mOnEventListeners = new WeakReferenceList<>();

    @Override
    public void moveCropRect(Rect start, Rect end, int duration) {
        if (end != null) {
            if (getStreamingSettings().getCropRect() == null) {
                postOnAdded(end);
            }
        } else {
            postOnRemove();
        }

        if (mMovingRectThread != null) {
            mMovingRectThread.move(start, end, duration);
        } else {
            getStreamingSettings().setCropRect(end);

            VideoQuality videoQuality = getVideoQuality();
            if (videoQuality != null) {
                videoQuality.setCropRect(end);
            }
        }
    }

    @Override
    public void setCropRect(Rect rect) {
        if (rect != null) {
            if (getStreamingSettings().getCropRect() == null) {
                postOnAdded(rect);
            }
        } else {
            postOnRemove();
        }

        if (rect == null || mMovingRectThread == null) {
            getStreamingSettings().setCropRect(rect);

            VideoQuality videoQuality = getVideoQuality();
            if (videoQuality != null) {
                videoQuality.setCropRect(rect);
            }
        } else {
            mMovingRectThread.set(rect);
        }
    }

    @Override
    public Rect getCropRect() {
        return getStreamingSettings().getCropRect();
    }

    @Override
    public void addOnEventListener(OnEventListener listener) {
        mOnEventListeners.add(listener);
    }

    @Override
    public void removeOnEventListener(OnEventListener listener) {
        mOnEventListeners.remove(listener);
    }

    private void postOnAdded(Rect rect) {
        for (OnEventListener l : mOnEventListeners.get()) {
            l.onAdded(this, rect);
        }
    }

    private void postOnRemove() {
        for (OnEventListener l : mOnEventListeners.get()) {
            l.onRemoved(this);
        }
    }

    private void postOnMoved(Rect rect) {
        for (OnEventListener l : mOnEventListeners.get()) {
            l.onMoved(this, rect);
        }
    }

    private void startMovingRectThread() {
        if (mMovingRectThread != null) {
            return;
        }

        mMovingRectThread = new MovingRectThread();
        mMovingRectThread.addOnEventListener(mMovingRectThreadOnEventListener);
        mMovingRectThread.start();
    }

    private void stopMovingRectThread() {
        if (mMovingRectThread != null) {
            mMovingRectThread.stop();
            mMovingRectThread = null;
        }
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
     * プレビューの設定を取得します.
     *
     * @return プレビュー設定
     */
    public HostMediaRecorder.StreamingSettings getStreamingSettings() {
        return mHostMediaRecorder.getSettings().getPreviewServer(getName());
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
        HostMediaRecorder.StreamingSettings settings = getStreamingSettings();

        EGLSurfaceDrawingThread d = recorder.getSurfaceDrawingThread();
        Size previewSize = settings.getPreviewSize();
        int w = d.isSwappedDimensions() ? previewSize.getHeight() : previewSize.getWidth();
        int h = d.isSwappedDimensions() ? previewSize.getWidth() : previewSize.getHeight();
        videoQuality.setVideoWidth(w);
        videoQuality.setVideoHeight(h);
        videoQuality.setCropRect(settings.getCropRect());
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
