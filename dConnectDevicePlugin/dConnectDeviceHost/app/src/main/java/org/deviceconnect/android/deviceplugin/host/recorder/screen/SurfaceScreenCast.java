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

import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceRecorder;


@TargetApi(21)
class SurfaceScreenCast extends AbstractScreenCast {

    private final Surface mOutputSurface;

    SurfaceScreenCast(final Context context,
                      final MediaProjection mediaProjection,
                      final Surface outputSurface,
                      final HostDeviceRecorder.PictureSize size) {
        super(context, mediaProjection, size);
        mOutputSurface = outputSurface;
    }

    @Override
    protected VirtualDisplay createVirtualDisplay() {
        HostDeviceRecorder.PictureSize size = mDisplaySize;
        int w = size.getWidth();
        int h = size.getHeight();

        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        if (dm.widthPixels > dm.heightPixels) {
            if (w < h) {
                w = size.getHeight();
                h = size.getWidth();
            }
        } else {
            if (w > h) {
                w = size.getHeight();
                h = size.getWidth();
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
