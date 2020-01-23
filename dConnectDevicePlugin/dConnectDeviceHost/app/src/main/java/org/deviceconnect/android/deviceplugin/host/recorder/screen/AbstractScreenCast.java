package org.deviceconnect.android.deviceplugin.host.recorder.screen;


import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;


@TargetApi(21)
public abstract class AbstractScreenCast implements ScreenCast {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "ScreenCast";

    private Context mContext;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mDisplay;
    private int mDisplayDensityDpi;
    private int mWidth;
    private int mHeight;

    private final VirtualDisplay.Callback mCallback = new VirtualDisplay.Callback() {
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

    private VirtualDisplay createVirtualDisplay() {
        int w = mWidth;
        int h = mHeight;

        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) {
            throw new RuntimeException("WindowManager is not supported.");
        }

        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        if (dm.widthPixels > dm.heightPixels) {
            if (w < h) {
                w = mHeight;
                h = mWidth;
            }
        } else {
            if (w > h) {
                w = mHeight;
                h = mWidth;
            }
        }

        return mMediaProjection.createVirtualDisplay(
                "Android Host Screen",
                w,
                h,
                mDisplayDensityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                getSurface(),
                mCallback,
                new Handler(Looper.getMainLooper()));
    }

    protected abstract Surface getSurface();
}
