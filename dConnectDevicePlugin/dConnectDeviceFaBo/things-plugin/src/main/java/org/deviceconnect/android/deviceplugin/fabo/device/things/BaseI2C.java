package org.deviceconnect.android.deviceplugin.fabo.device.things;

class BaseI2C {

    /**
     * Android Thingsを管理するクラス.
     */
    FaBoThingsDeviceControl mFaBoThingsDeviceControl;

    public void setFaBoThingsDeviceControl(final FaBoThingsDeviceControl control) {
        mFaBoThingsDeviceControl = control;
    }

}
