package org.deviceconnect.android.deviceplugin.theta.core;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.deviceconnect.android.deviceplugin.theta.core.sensor.DefaultHeadTracker;
import org.deviceconnect.android.deviceplugin.theta.core.sensor.HeadTracker;
import org.deviceconnect.android.deviceplugin.theta.core.sensor.HeadTrackingListener;
import org.deviceconnect.android.deviceplugin.theta.utils.BitmapUtils;
import org.deviceconnect.android.deviceplugin.theta.utils.Quaternion;

/**
 * Spherical View API.
 *
 * <h2>Usage</h2>
 * <code>
 *     // Obtain Spherical View API.
 *     ThetaDeviceApplication app = (ThetaDeviceApplication) getApplication();
 *     SphericalViewApi api = app.getSphericalViewApi();
 *
 *     // Start Image View.
 *     api.startImageView(picture, param, renderer);
 *
 *     // Change Image View Settings.
 *     api.updateImageView(newParam);
 *
 *     // Stop Image View.
 *     api.stopImageView();
 * </code>
 */
public class SphericalViewApi implements HeadTrackingListener {

    private State mState;

    private SphericalViewParam mParam;

    private SphericalViewRenderer mRenderer;

    private final HeadTracker mHeadTracker;

    private Bitmap mTexture;

    public SphericalViewApi(final Context context) {
        mHeadTracker = new DefaultHeadTracker(context);
    }

    @Override
    public void onHeadRotated(final Quaternion rotation) {
        synchronized (this) {
            if (isRunning()) {
                SphericalViewRenderer.Camera currentCamera = mRenderer.getCamera();
                SphericalViewRenderer.CameraBuilder newCamera = new SphericalViewRenderer.CameraBuilder(currentCamera);
                newCamera.rotate(rotation);
                mRenderer.setCamera(newCamera.create());
            }
        }
    }

    public synchronized void startImageView(final byte[] picture,
                                            final SphericalViewParam param,
                                            final SphericalViewRenderer renderer) {
        if (isRunning()) {
            throw new IllegalStateException("SphericalViewApi is already running.");
        }

        mParam = param;
        mRenderer = renderer;

        Bitmap texture = BitmapFactory.decodeByteArray(picture, 0, picture.length);
        mTexture = BitmapUtils.resize(texture, 2048, 1024);
        renderer.setTexture(mTexture);

        if (param.isVRMode()) {
            mHeadTracker.registerTrackingListener(this);
            mHeadTracker.start();
        }

        mRenderer.setStereoMode(param.isStereo());

        mState = State.RUNNING;
    }

    public synchronized void updateImageView(final SphericalViewParam param) {
        if (!isRunning()) {
            throw new IllegalStateException("SphericalViewApi is not running.");
        }

        mParam = param;

        if (param.isVRMode()) {
            mHeadTracker.start();
        } else {
            mHeadTracker.stop();
            mHeadTracker.unregisterTrackingListener(this);
        }

        mRenderer.setStereoMode(param.isStereo());
    }

    public synchronized void stop() {
        if (isState(State.STOPPED)) {
            throw new IllegalStateException("SphericalViewApi has already stopped.");
        }

        mTexture.recycle();

        mHeadTracker.stop();
        mHeadTracker.unregisterTrackingListener(this);

        mState = State.STOPPED;
    }

    public synchronized void pause() {
        mState = State.PAUSED;
        mHeadTracker.stop();
    }

    public synchronized void resume() {
        mState = State.RUNNING;
        mHeadTracker.start();
    }

    public boolean isRunning() {
        return isState(State.RUNNING);
    }

    public byte[] takeSnapshot() {
        return null;
    }

    private boolean isState(State state) {
        return mState == state;
    }

    private enum State {

        STOPPED,

        RUNNING,

        PAUSED

    }

}
