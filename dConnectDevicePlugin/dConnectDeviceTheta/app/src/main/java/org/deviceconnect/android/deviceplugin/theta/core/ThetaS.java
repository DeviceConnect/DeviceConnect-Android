package org.deviceconnect.android.deviceplugin.theta.core;


import java.util.List;

class ThetaS extends AbstractThetaDevice {

    ThetaS(final String name) {
        super(name);
    }

    @Override
    public ThetaDeviceModel getModel() {
        return ThetaDeviceModel.THETA_S;
    }

    @Override
    public List<ThetaObject> fetchAllObjectList() throws ThetaDeviceException {
        return null;
    }

    @Override
    public List<ThetaObject> fetchObjectList(int offset, int maxLength) throws ThetaDeviceException {
        return null;
    }

    @Override
    public ThetaObject takePicture() throws ThetaDeviceException {
        return null;
    }

    @Override
    public void startVideoRecording() throws ThetaDeviceException {

    }

    @Override
    public ThetaObject stopVideoRecording() throws ThetaDeviceException {
        return null;
    }

    @Override
    public double getBatteryLevel() throws ThetaDeviceException {
        return 0;
    }

    @Override
    public void changeShootingMode(ShootingMode mode) throws ThetaDeviceException {

    }

}
