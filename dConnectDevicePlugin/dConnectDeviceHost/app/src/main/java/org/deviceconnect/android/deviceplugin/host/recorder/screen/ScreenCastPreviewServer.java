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
    private BroadcastReceiver mConfigChangeReceiver;

    ScreenCastPreviewServer(final Context context,
                            final AbstractPreviewServerProvider serverProvider) {
        mContext = context;
        mServerProvider = serverProvider;
    }

    private int getRotation() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        return display.getRotation();
    }

    HostDeviceRecorder.PictureSize getRotatedPreviewSize() {
        HostDeviceRecorder.PictureSize size = mServerProvider.getPreviewSize();
        int w;
        int h;
        switch (getRotation()) {
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
        mConfigChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                onConfigChange();
            }
        };
        IntentFilter filter = new IntentFilter(
                "android.intent.action.CONFIGURATION_CHANGED");
        mContext.registerReceiver(mConfigChangeReceiver, filter);
    }

    synchronized void unregisterConfigChangeReceiver() {
        if (mConfigChangeReceiver != null) {
            mContext.unregisterReceiver(mConfigChangeReceiver);
            mConfigChangeReceiver = null;
        }
    }

    protected void onConfigChange() {
        // NOP.
    }
}
