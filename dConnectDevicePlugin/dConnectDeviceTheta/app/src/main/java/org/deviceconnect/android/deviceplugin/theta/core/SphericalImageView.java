package org.deviceconnect.android.deviceplugin.theta.core;


import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class SphericalImageView extends GLSurfaceView {

    private final SphericalViewRenderer mRenderer = new SphericalViewRenderer();;

    private SphericalViewApi mViewApi;

    private final SphericalViewParam mParam = new SphericalViewParam();

    public SphericalImageView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        setRenderer(mRenderer);

        mParam.setVRMode(true);
    }

    public void setViewApi(final SphericalViewApi viewApi) {
        mViewApi = viewApi;
    }

    public void start(final byte[] picture) {
        if (mViewApi != null && !mViewApi.isRunning()) {
            mViewApi.startImageView(picture, mParam, mRenderer);
        }
    }

    public void stop() {
        if (mViewApi != null && mViewApi.isRunning()) {
            mViewApi.stop();
        }
    }

    public void setStereo(final boolean isStereo) {
        if (mViewApi != null) {
            mParam.setStereo(isStereo);
            mViewApi.updateImageView(mParam);
        }
    }

    @Override
    public void onPause() {
        if (mViewApi != null) {
            mViewApi.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mViewApi != null) {
            mViewApi.resume();
        }
    }
}
