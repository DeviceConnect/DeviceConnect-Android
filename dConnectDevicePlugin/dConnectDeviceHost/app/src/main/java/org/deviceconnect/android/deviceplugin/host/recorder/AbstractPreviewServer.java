package org.deviceconnect.android.deviceplugin.host.recorder;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;

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
     * コンストラクタ.
     *
     * <p>
     * デフォルトでは、mute は true に設定しています。
     * </p>
     *
     * @param context コンテキスト
     * @param recorder プレビューで表示するレコーダ
     */
    public AbstractPreviewServer(Context context, HostMediaRecorder recorder) {
        mContext = context;
        mHostMediaRecorder = recorder;
        mMute = true;
    }

    // PreviewServer

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
    public boolean usesSSLContext() {
        return false;
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
}
