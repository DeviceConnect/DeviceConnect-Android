package org.deviceconnect.android.deviceplugin.theta.profile;

import org.deviceconnect.android.deviceplugin.theta.core.SphericalViewParam;
import org.deviceconnect.android.deviceplugin.theta.core.SphericalViewRenderer;

interface Projector {

    SphericalViewRenderer getRenderer();

    void setRenderer(SphericalViewRenderer renderer);

    void setScreen(ProjectionScreen screen);

    boolean start();

    boolean stop();

    void setParameter(SphericalViewParam param);

    byte[] getImageCache();

}
