package org.deviceconnect.android.deviceplugin.theta.core.preview.omni.projector;


public interface ProjectionScreen {

    void onStart(Projector projector);

    void onStop(Projector projector);

    void onProjected(Projector projector, byte[] frame);

}
