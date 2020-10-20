package org.deviceconnect.android.deviceplugin.theta.core.preview.omni.projector;

import org.deviceconnect.android.deviceplugin.theta.core.SphericalViewParam;
import org.deviceconnect.android.deviceplugin.theta.core.SphericalViewRenderer;

public interface Projector {

    SphericalViewRenderer getRenderer();

    void setRenderer(SphericalViewRenderer renderer);

    void setScreen(ProjectionScreen screen);

    boolean start();

    boolean stop();

    void setParameter(SphericalViewParam param);

    byte[] getImageCache();

}
