package org.deviceconnect.android.deviceplugin.theta.core;


public abstract class AbstractThetaDevice implements ThetaDevice {

    protected final String mSSID;

    protected AbstractThetaDevice(final String ssId) {
        mSSID = ssId;
    }

    @Override
    public String getName() {
        return mSSID;
    }

}
