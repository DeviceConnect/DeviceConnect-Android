package org.deviceconnect.android.deviceplugin.theta.core;


import org.deviceconnect.android.deviceplugin.theta.core.preview.PreviewServer;
import org.deviceconnect.android.deviceplugin.theta.core.preview.PreviewServerProvider;
import org.deviceconnect.android.deviceplugin.theta.core.preview.camera.ThetaCameraPreviewServerProvider;

import java.util.List;

public abstract class AbstractThetaDevice implements ThetaDevice {

    protected final String mSSID;

    protected ThetaCameraPreviewServerProvider mProvider;

    protected AbstractThetaDevice(final String ssId) {
        mSSID = ssId;
        mProvider = new ThetaCameraPreviewServerProvider(this);
    }

    @Override
    public String getName() {
        return mSSID;
    }


    @Override
    public PreviewServerProvider getServerProvider() {
        return mProvider;
    }
    @Override
    public List<PreviewServer> startPreviews() {
        return mProvider.startServers();
    }

    @Override
    public void stopPreviews() {
        mProvider.stopServers();
    }
}
