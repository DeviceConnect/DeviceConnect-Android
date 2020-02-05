package org.deviceconnect.android.deviceplugin.host.recorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

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
    private HostDeviceRecorder mHostDeviceRecorder;

    /**
     * プレビュー配信サーバのポート番号.
     */
    private int mPort;

    /**
     * ミュート設定.
     */
    private boolean mMute;

    /**
     * 画面回転のイベントを受信するためのレシーバー.
     */
    private final BroadcastReceiver mConfigChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            onConfigChange();
        }
    };

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
    public AbstractPreviewServer(Context context, HostDeviceRecorder recorder) {
        mContext = context;
        mHostDeviceRecorder = recorder;
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
    public  void onConfigChange() {
    }

    @Override
    public int getQuality() {
        return 0;
    }

    @Override
    public void setQuality(int quality) {
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
    public HostDeviceRecorder getRecorder() {
        return mHostDeviceRecorder;
    }

    /**
     * 画面の回転イベントを受信するレシーバーを登録します.
     */
    public synchronized void registerConfigChangeReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED);
        mContext.registerReceiver(mConfigChangeReceiver, filter);
    }

    /**
     * 画面の回転イベントを受信するレシーバーを解除します.
     */
    public synchronized void unregisterConfigChangeReceiver() {
        try {
            mContext.unregisterReceiver(mConfigChangeReceiver);
        } catch (Exception e) {
            // ignore.
        }
    }
}
