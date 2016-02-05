package org.deviceconnect.android.deviceplugin.theta.profile;


interface ProjectionScreen {

    void onStart(Projector projector);

    void onStop(Projector projector);

    void onProjected(Projector projector, byte[] frame);

}
