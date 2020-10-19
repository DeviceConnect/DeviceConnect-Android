package org.deviceconnect.android.deviceplugin.theta.core.preview.omni.projector;


import org.deviceconnect.android.deviceplugin.theta.core.SphericalViewParam;
import org.deviceconnect.android.deviceplugin.theta.core.SphericalViewRenderer;
import org.deviceconnect.android.deviceplugin.theta.core.sensor.HeadTracker;
import org.deviceconnect.android.deviceplugin.theta.core.sensor.HeadTrackingListener;
import org.deviceconnect.android.deviceplugin.theta.utils.Quaternion;

public abstract class Viewer implements HeadTrackingListener {

    private String mId;

    protected Projector mProjector;

    protected HeadTracker mHeadTracker;

    protected SphericalViewParam mCurrentParam = new SphericalViewParam();

    public void setId(final String id) {
        mId = id;
    }

    public String getId() {
        return mId;
    }

    public void setProjector(final Projector projector) {
        mProjector = projector;
    }
    public void setHeadTracker(final HeadTracker tracker) {
        mHeadTracker = tracker;
    }

    public void start() {
        mProjector.start();
    }

    public void stop() {
        mProjector.stop();
        mHeadTracker.unregisterTrackingListener(this);
    }

    public void setParameter(final SphericalViewParam param) {
        if (!mCurrentParam.isVRMode() && param.isVRMode()) {
            mHeadTracker.registerTrackingListener(this);
        } else if (mCurrentParam.isVRMode() && !param.isVRMode()) {
            mHeadTracker.unregisterTrackingListener(this);
        }
        mProjector.setParameter(param);
        mCurrentParam = param;
    }

    public byte[] getImageCache() {
        return mProjector.getImageCache();
    }

    @Override
    public void onHeadRotated(final Quaternion rotation) {
        if (mProjector == null) {
            return;
        }
        SphericalViewRenderer renderer = mProjector.getRenderer();
        if (renderer == null) {
            return;
        }
        SphericalViewRenderer.Camera currentCamera = renderer.getCamera();
        SphericalViewRenderer.CameraBuilder newCamera = new SphericalViewRenderer.CameraBuilder(currentCamera);
        newCamera.rotate(rotation);
        renderer.setCamera(newCamera.create());
    }
}
