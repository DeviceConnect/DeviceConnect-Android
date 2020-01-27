package org.deviceconnect.android.deviceplugin.host.recorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

/**
 * プレビュー配信サーバ.
 */
public abstract class AbstractPreviewServer implements PreviewServer {
    private final Context mContext;
    private final AbstractPreviewServerProvider mServerProvider;

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

    public AbstractPreviewServer(Context context, AbstractPreviewServerProvider serverProvider) {
        mContext = context;
        mServerProvider = serverProvider;
        mMute = true;
    }

    public Context getContext() {
        return mContext;
    }

    public AbstractPreviewServerProvider getServerProvider() {
        return mServerProvider;
    }

    /**
     * プレビュー配信サーバのポート番号を取得します.
     *
     * @return ポート番号
     */
    public int getPort() {
        return mPort;
    }

    /**
     * 画面の回転を取得します.
     * @return 画面の回転
     */
    private int getDisplayRotation() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) {
            // WindowManager がサポートされていないので、回転は無し。
            return Surface.ROTATION_0;
        }
        Display display = wm.getDefaultDisplay();
        return display.getRotation();
    }

    /**
     * 画面の回転から解像度の縦横をスワップする必要があるか確認します.
     *
     * @return 回転する場合はtrue、それ以外はfalse
     */
    private boolean isSwapSize() {
        switch (getDisplayRotation()) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                return false;
            default:
                return true;
        }
    }

    /**
     * 画面の回転に合わせたプレビューの解像度を取得します.
     *
     * @return プレビューの解像度
     */
    public HostDeviceRecorder.PictureSize getRotatedPreviewSize() {
        HostDeviceRecorder.PictureSize size = mServerProvider.getPreviewSize();
        int w = size.getWidth();
        int h = size.getHeight();
        if (isSwapSize()) {
            w = size.getHeight();
            h = size.getWidth();
        }
        return new HostDeviceRecorder.PictureSize(w, h);
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
}
