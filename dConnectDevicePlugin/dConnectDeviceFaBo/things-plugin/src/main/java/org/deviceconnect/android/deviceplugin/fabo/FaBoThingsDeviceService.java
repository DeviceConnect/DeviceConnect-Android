package org.deviceconnect.android.deviceplugin.fabo;

import org.deviceconnect.android.deviceplugin.fabo.device.FaBoDeviceControl;
import org.deviceconnect.android.deviceplugin.fabo.device.things.FaBoThingsDeviceControl;

public class FaBoThingsDeviceService extends FaBoDeviceService {

    @Override
    public void onCreate() {
        super.onCreate();

        setUseLocalOAuth(false);
    }

    @Override
    protected FaBoDeviceControl createFaBoDeviceControl() {
        return new FaBoThingsDeviceControl(this);
    }
}
