package org.deviceconnect.android.rtspserver.screen;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.WindowManager;

import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;

public class SurfaceScreenCast extends ScreenCast {

    private Surface mOutputSurface;

    public SurfaceScreenCast(Context context, MediaProjection mediaProjection, Surface surface, VideoQuality videoQuality) {
        super(context, mediaProjection, videoQuality);
        mOutputSurface = surface;
    }

    @Override
    protected VirtualDisplay createVirtualDisplay() {
        int w = mVideoQuality.getVideoWidth();
        int h = mVideoQuality.getVideoHeight();

        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) {
            throw new RuntimeException("");
        }

        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        if (dm.widthPixels > dm.heightPixels) {
            if (w < h) {
                w = mVideoQuality.getVideoHeight();
                h = mVideoQuality.getVideoWidth();
            }
        } else {
            if (w > h) {
                w = mVideoQuality.getVideoWidth();
                h = mVideoQuality.getVideoWidth();
            }
        }

        return mMediaProjection.createVirtualDisplay(
                "Screen Cast",
                w, h,
                mDisplayDensityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mOutputSurface,
                getDisplayCallback(),
                new Handler(Looper.getMainLooper()));
    }
}
