package org.deviceconnect.android.deviceplugin.theta.profile;


import org.deviceconnect.android.deviceplugin.theta.core.SphericalViewParam;
import org.deviceconnect.android.deviceplugin.theta.core.SphericalViewRenderer;
import org.deviceconnect.android.deviceplugin.theta.utils.Vector3D;

abstract class AbstractProjector implements Projector {

    protected SphericalViewRenderer mRenderer;

    protected ProjectionScreen mScreen;

    @Override
    public SphericalViewRenderer getRenderer() {
        return mRenderer;
    }

    @Override
    public void setRenderer(final SphericalViewRenderer renderer) {
        mRenderer = renderer;
    }

    @Override
    public void setScreen(final ProjectionScreen screen) {
        mScreen = screen;
    }

    @Override
    public void setParameter(final SphericalViewParam param) {
        SphericalViewRenderer renderer = getRenderer();
        SphericalViewRenderer.CameraBuilder camera
            = new SphericalViewRenderer.CameraBuilder(renderer.getCamera());
        camera.setFov((float) param.getFOV());
        camera.setPosition(new Vector3D((float) param.getCameraX(),
            (float) param.getCameraY(),
            (float) param.getCameraZ()));
        camera.rotateByEulerAngle(
            (float) param.getCameraRoll(),
            (float) param.getCameraYaw(),
            (float) param.getCameraPitch()
        );
        renderer.setCamera(camera.create());
        renderer.setSphereRadius((float) param.getSphereSize());
        renderer.setScreenSettings(param.getWidth(), param.getHeight(), param.isStereo());
    }

}
