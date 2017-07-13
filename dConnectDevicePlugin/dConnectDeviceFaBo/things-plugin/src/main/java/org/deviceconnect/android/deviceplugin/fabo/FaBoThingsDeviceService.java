package org.deviceconnect.android.deviceplugin.fabo;

import org.deviceconnect.android.deviceplugin.fabo.device.FaBoDeviceControl;
import org.deviceconnect.android.deviceplugin.fabo.device.things.FaBoThingsDeviceControl;

/**
 * Android Things用のサービス.
 */
public class FaBoThingsDeviceService extends FaBoDeviceService {

    @Override
    public void onCreate() {
        super.onCreate();
        // 画面がないのでLocal OAuthはfalseにしておく
        setUseLocalOAuth(false);
    }

    @Override
    protected FaBoDeviceControl createFaBoDeviceControl() {
        return new FaBoThingsDeviceControl();
    }
}
