package org.deviceconnect.android.deviceplugin.theta.core;


import android.content.Context;
import android.util.AttributeSet;

public class SphericalImageLiveView extends SphericalImageView {

    private ThetaDeviceManager mDeviceMgr;

    public SphericalImageLiveView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        mRenderer.setDestroyTextureOnUpdate(true);
    }

    public void setDeviceManager(final ThetaDeviceManager deviceMgr) {
        mDeviceMgr = deviceMgr;
    }

    public synchronized void startLivePreview() throws ThetaDeviceException {
        if (mDeviceMgr == null) {
            throw new IllegalStateException("Device Manager is not set.");
        }
        final ThetaDevice device = mDeviceMgr.getConnectedDevice();
        if (device == null) {
            throw new ThetaDeviceException(ThetaDeviceException.NOT_FOUND_THETA);
        }
        if (device.getModel() == ThetaDeviceModel.THETA_M15) {
            throw new ThetaDeviceException(ThetaDeviceException.NOT_SUPPORTED_FEATURE);
        }
        mViewApi.startLiveView(device, mParam, mRenderer);
    }

    public void destroy() {
        stop();
    }

}
