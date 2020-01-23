package org.deviceconnect.android.deviceplugin.host.recorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

public abstract class AbstractPreviewServer implements PreviewServer {
    private final Context mContext;
    private final AbstractPreviewServerProvider mServerProvider;
    private boolean mMute;

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

    private int getDisplayRotation() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) {
            throw new RuntimeException("WindowManager is not supported.");
        }
        Display display = wm.getDefaultDisplay();
        return display.getRotation();
    }

    private boolean isSwapSize() {
        switch (getDisplayRotation()) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                return false;
            default:
                return true;
        }
    }

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

    public synchronized void registerConfigChangeReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED);
        mContext.registerReceiver(mConfigChangeReceiver, filter);
    }

    public synchronized void unregisterConfigChangeReceiver() {
        try {
            mContext.unregisterReceiver(mConfigChangeReceiver);
        } catch (Exception e) {
            // ignore.
        }
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
