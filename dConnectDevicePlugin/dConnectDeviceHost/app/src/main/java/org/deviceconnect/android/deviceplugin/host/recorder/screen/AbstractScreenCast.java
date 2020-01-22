package org.deviceconnect.android.deviceplugin.host.recorder.screen;


import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.util.DisplayMetrics;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceRecorder;


@TargetApi(21)
public abstract class AbstractScreenCast implements ScreenCast {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "ScreenCast";

    final Context mContext;
    final MediaProjection mMediaProjection;
    final int mDisplayDensityDpi;
    int mWidth;
    int mHeight;

    HostDeviceRecorder.PictureSize mDisplaySize;
    private VirtualDisplay mDisplay;

    AbstractScreenCast(Context context, MediaProjection mediaProjection, int width, int height) {
        mContext = context;
        mMediaProjection = mediaProjection;

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        mDisplayDensityDpi = metrics.densityDpi;
        mWidth = width;
        mHeight = height;
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
                if (DEBUG) {
                    Log.d(TAG, "VirtualDisplay.Callback#onPause()");
                }
            }

            @Override
            public void onResumed() {
                if (DEBUG) {
                    Log.d(TAG, "VirtualDisplay.Callback#onResumed()");
                }
            }

            @Override
            public void onStopped() {
                if (DEBUG) {
                    Log.d(TAG, "VirtualDisplay.Callback#onStopped()");
                }
            }
        };
    }

    protected abstract VirtualDisplay createVirtualDisplay();


}
