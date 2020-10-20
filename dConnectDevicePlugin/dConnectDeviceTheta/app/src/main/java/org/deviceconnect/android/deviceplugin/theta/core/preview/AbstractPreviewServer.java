package org.deviceconnect.android.deviceplugin.theta.core.preview;



import org.deviceconnect.android.deviceplugin.theta.BuildConfig;

import javax.net.ssl.SSLContext;

/**
 * プレビュー配信サーバ.
 */
public abstract class AbstractPreviewServer implements PreviewServer {
    protected static final boolean DEBUG = BuildConfig.DEBUG;
    protected static final String TAG = "theta.preview";
    /**
     * Thetaのカメラ のイベントを通知するリスナー.
     */
    protected OnEventListener mOnEventListener;
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
     */
    public AbstractPreviewServer() {
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
    public void mute() {
        mMute = true;
    }

    @Override
    public void unMute() {
        mMute = false;
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
    public void setOnEventListener(AbstractPreviewServer.OnEventListener lister) {
        mOnEventListener = lister;
    }
    public AbstractPreviewServer.OnEventListener getOnEventListener() {
        return mOnEventListener;
    }
    /**
     /**
     * Thetaのカメラ のイベントを通知するリスナー.
     */
    public interface OnEventListener {
        /**
         * Thetaのカメラ が開始されることを通知します.
         *
         * <p>
         * Thetaのカメラ が開始されるので、他で Thetaのカメラ を使用している場合は停止処理などを行うこと。
         * </p>
         */
        void onConnect();

        /**
         * Thetaのカメラ が停止されることを通知します.
         *
         * <p>
         * Thetaのカメラ のプレビューが停止したので、他で使用していた Thetaのカメラ の再開処理などを行うこと。
         * </p>
         */
        void onDisconnect();
    }

}
