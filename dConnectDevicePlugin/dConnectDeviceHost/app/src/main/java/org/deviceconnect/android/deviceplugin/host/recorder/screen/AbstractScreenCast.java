package org.deviceconnect.android.deviceplugin.host.recorder.screen;


import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.util.DisplayMetrics;

import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceRecorder;


@TargetApi(21)
public abstract class AbstractScreenCast implements ScreenCast {

    final Context mContext;
    final MediaProjection mMediaProjection;
    final int mDisplayDensityDpi;

    HostDeviceRecorder.PictureSize mDisplaySize;
    VirtualDisplay mDisplay;

    AbstractScreenCast(final Context context,
                       final MediaProjection mediaProjection,
                       final HostDeviceRecorder.PictureSize size) {
        mContext = context;
        mMediaProjection = mediaProjection;

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        mDisplayDensityDpi = metrics.densityDpi;
        mDisplaySize = size;
    }

    @Override
    public boolean isCasting() {
        return mDisplay != null;
    }

    @Override
    public synchronized void startCast() {
        if (mDisplay == null) {
            mDisplay = createVirtualDisplay();
        }
    }

    @Override
    public synchronized void stopCast() {
        if (mDisplay != null) {
            mDisplay.release();
            mDisplay = null;
        }
    }

    VirtualDisplay.Callback getDisplayCallback() {
        return new VirtualDisplay.Callback() {
            @Override
            public void onPaused() {
            }

            @Override
            public void onResumed() {
            }

            @Override
            public void onStopped() {
            }
        };
    }

    protected abstract VirtualDisplay createVirtualDisplay();


}
