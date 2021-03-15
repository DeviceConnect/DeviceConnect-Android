package org.deviceconnect.android.deviceplugin.uvc.recorder;

import android.content.Context;
import android.graphics.Rect;
import android.util.Size;

import org.deviceconnect.android.deviceplugin.uvc.BuildConfig;
import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;
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
    private final MediaRecorder mMediaRecorder;

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
    public AbstractPreviewServer(Context context, MediaRecorder recorder) {
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
    public AbstractPreviewServer(Context context, MediaRecorder recorder, boolean useSSL) {
        mContext = context;
        mMediaRecorder = recorder;
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

            MediaRecorder.Settings settings = getRecorder().getSettings();
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
    public MediaRecorder getRecorder() {
        return mMediaRecorder;
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
     * VideoEncoder の設定に、MediaRecorder の設定を反映します.
     *
     * @param videoQuality 設定を行う VideoEncoder の VideoQuality
     */
    public void setVideoQuality(VideoQuality videoQuality) {
        MediaRecorder recorder = getRecorder();
        MediaRecorder.Settings settings = recorder.getSettings();

        Rect rect = settings.getDrawingRange();
        if (rect != null) {
            videoQuality.setVideoWidth(rect.width());
            videoQuality.setVideoHeight(rect.height());
        } else {
            Size previewSize = settings.getPreviewSize();
            videoQuality.setVideoWidth(previewSize.getWidth());
            videoQuality.setVideoHeight(previewSize.getHeight());
        }
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
