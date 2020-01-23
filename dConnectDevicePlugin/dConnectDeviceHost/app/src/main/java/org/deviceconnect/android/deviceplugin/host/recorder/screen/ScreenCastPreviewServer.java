package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.PreviewServer;

public abstract class ScreenCastPreviewServer implements PreviewServer {

    protected final Context mContext;
    protected final AbstractPreviewServerProvider mServerProvider;
    private boolean mMute;

    private final BroadcastReceiver mConfigChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            onConfigChange();
        }
    };

    ScreenCastPreviewServer(Context context, AbstractPreviewServerProvider serverProvider) {
        mContext = context;
        mServerProvider = serverProvider;
        mMute = true;
    }

    private int getDisplayRotation() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) {
            throw new RuntimeException("WindowManager is not supported.");
        }
        Display display = wm.getDefaultDisplay();
        return display.getRotation();
    }

    HostDeviceRecorder.PictureSize getRotatedPreviewSize() {
        HostDeviceRecorder.PictureSize size = mServerProvider.getPreviewSize();
        int w;
        int h;
        switch (getDisplayRotation()) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                w = size.getWidth();
                h = size.getHeight();
                break;
            default:
                w = size.getHeight();
                h = size.getWidth();
                break;
        }
        return new HostDeviceRecorder.PictureSize(w, h);
    }

    synchronized void registerConfigChangeReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED);
        mContext.registerReceiver(mConfigChangeReceiver, filter);
    }

    synchronized void unregisterConfigChangeReceiver() {
        try {
            mContext.unregisterReceiver(mConfigChangeReceiver);
        } catch (Exception e) {
            // ignore.
        }
    }

    protected void onConfigChange() {
        // NOP.
    }

    @Override
    public void onDisplayRotation(final int degree) {
        // NOP.
    }

    /**
     * Recorderをmute状態にする.
     */
    public void mute() {
        mMute = true;
    }

    /**
     * Recorderのmute状態を解除する.
     */
    public void unMute() {
        mMute = false;
    }

    /**
     * Recorderのmute状態を返す.
     * @return mute状態
     */
    public boolean isMuted() {
        return mMute;
    }
}
