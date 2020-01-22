package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.WindowManager;

@TargetApi(21)
class SurfaceScreenCast extends AbstractScreenCast {

    private Surface mOutputSurface;

    SurfaceScreenCast(Context context, MediaProjection mediaProjection,
                      Surface outputSurface, int width, int height) {
        super(context, mediaProjection, width, height);
        mOutputSurface = outputSurface;
    }

    @Override
    protected VirtualDisplay createVirtualDisplay() {
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
                mOutputSurface,
                getDisplayCallback(),
                new Handler(Looper.getMainLooper()));
    }
}
