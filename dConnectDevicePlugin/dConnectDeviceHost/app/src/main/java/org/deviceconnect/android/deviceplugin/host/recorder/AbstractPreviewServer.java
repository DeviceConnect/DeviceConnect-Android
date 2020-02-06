package org.deviceconnect.android.deviceplugin.host.recorder;

import android.content.Context;

/**
 * プレビュー配信サーバ.
 */
public abstract class AbstractPreviewServer implements PreviewServer {
    /**
     * コンテキスト.
     */
    private Context mContext;

    /**
     * プレビュー再生を行うレコーダ.
     */
    private HostMediaRecorder mHostMediaRecorder;

    /**
     * プレビュー配信サーバのポート番号.
     */
    private int mPort;

    /**
     * ミュート設定.
     */
    private boolean mMute;

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
