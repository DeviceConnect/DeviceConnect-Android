package org.deviceconnect.android.deviceplugin.theta.core;


import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class SphericalImageView extends GLSurfaceView {

    protected final SphericalViewRenderer mRenderer = new SphericalViewRenderer();;

    protected SphericalViewApi mViewApi;

    protected final SphericalViewParam mParam = new SphericalViewParam();

    public SphericalImageView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        mRenderer.setFlipVertical(true);
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
        if (mViewApi != null && (mViewApi.isRunning() || mViewApi.isPaused())) {
            mViewApi.stop();
        }
    }

    public void setStereo(final boolean isStereo) {
        if (mViewApi != null) {
            mParam.setStereo(isStereo);
            mViewApi.updateImageView(mParam);
        }
    }

    public void setFOV(final double fov) {
        if (mViewApi != null) {
            mParam.setFOV(fov);
            mViewApi.updateImageView(mParam);
        }
    }

    public byte[] takeSnapshot() {
        if (mRenderer == null) {
            return null;
        }
        return mRenderer.takeSnapshot();
    }

    public void resetCameraDirection() {
        if (mViewApi != null) {
            mViewApi.resetCameraDirection();
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
