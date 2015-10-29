package org.deviceconnect.android.deviceplugin.theta.core;


public interface SphericalViewRenderer {

    void onSurfaceCreated();

    void onSurfaceChanged(int width, int height);

    void onDrawFrame();

}
