package org.deviceconnect.android.rtspserver.screen;

import android.content.Context;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.util.DisplayMetrics;
import android.util.Log;

import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;

public abstract class ScreenCast {

    Context mContext;
    MediaProjection mMediaProjection;
    int mDisplayDensityDpi;

    VideoQuality mVideoQuality;
    VirtualDisplay mDisplay;

    public ScreenCast(Context context, MediaProjection mediaProjection, VideoQuality videoQuality) {
        mContext = context;
        mMediaProjection = mediaProjection;
        mVideoQuality = videoQuality;

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        mDisplayDensityDpi = metrics.densityDpi;
    }

    public boolean isCasting() {
        return mDisplay != null;
    }

    public synchronized void startCast() {
        if (mDisplay == null) {
            mDisplay = createVirtualDisplay();
        }
    }

    public synchronized void stopCast() {
        if (mDisplay != null) {
            mDisplay.release();
            mDisplay = null;
        }
    }

    public VirtualDisplay.Callback getDisplayCallback() {
        return new VirtualDisplay.Callback() {
            @Override
            public void onPaused() {
                Log.e("ABC", "$$$$$$$$$$ onPause");
            }

            @Override
            public void onResumed() {
                Log.e("ABC", "$$$$$$$$$$ onResumed");
            }

            @Override
            public void onStopped() {
                Log.e("ABC", "$$$$$$$$$$ onStopped");
            }
        };
    }

    protected abstract VirtualDisplay createVirtualDisplay();
}
