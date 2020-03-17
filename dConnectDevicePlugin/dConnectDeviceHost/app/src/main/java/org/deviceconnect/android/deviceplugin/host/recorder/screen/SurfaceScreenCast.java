package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.projection.MediaProjection;
import android.os.Build;
import android.view.Surface;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class SurfaceScreenCast extends AbstractScreenCast {

    private Surface mOutputSurface;

    SurfaceScreenCast(Context context, MediaProjection mediaProjection,
                      Surface outputSurface, int width, int height) {
        super(context, mediaProjection, width, height);
        mOutputSurface = outputSurface;
    }

    @Override
    protected Surface getSurface() {
        return mOutputSurface;
    }
}
