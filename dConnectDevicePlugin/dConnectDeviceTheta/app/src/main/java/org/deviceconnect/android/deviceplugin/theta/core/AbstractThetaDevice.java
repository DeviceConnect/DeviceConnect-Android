package org.deviceconnect.android.deviceplugin.theta.core;


public abstract class AbstractThetaDevice implements ThetaDevice {

    private final String mName;

    protected AbstractThetaDevice(final String name) {
        mName = name;
    }

    @Override
    public String getName() {
        return mName;
    }

}
