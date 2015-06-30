/*
 HeartRateServiceDiscoveryProfile
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.heartrate.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.heartrate.HeartRateApplication;
import org.deviceconnect.android.deviceplugin.heartrate.HeartRateDeviceService;
import org.deviceconnect.android.deviceplugin.heartrate.HeartRateManager;
import org.deviceconnect.android.deviceplugin.heartrate.ble.BleUtils;
import org.deviceconnect.android.deviceplugin.heartrate.data.HeartRateDevice;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Implement ServiceDiscoveryProfile.
 * @author NTT DOCOMO, INC.
 */
public class HeartRateServiceDiscoveryProfile extends ServiceDiscoveryProfile {
    /**
     * コンストラクタ.
     * @param provider プロファイルプロバイダ
     */
    public HeartRateServiceDiscoveryProfile(final DConnectProfileProvider provider) {
        super(provider);
    }
    @Override
    protected boolean onGetServices(final Intent request, final Intent response) {
        if (!BleUtils.isBLESupported(getContext())) {
            mLogger.warning("BLE not supported.");
            List<Bundle> services = new ArrayList<>();
            setResult(response, DConnectMessage.RESULT_OK);
            setServices(response, services);
            return true;
        }

        List<Bundle> services = new ArrayList<>();
        List<HeartRateDevice> devices = getManager().getConnectedDevices();
        synchronized (devices) {
            for (HeartRateDevice device : devices) {
                Bundle service = new Bundle();
                service.putString(PARAM_ID, device.getAddress());
                service.putString(PARAM_NAME, device.getName());
                service.putString(PARAM_TYPE, NetworkType.BLE.getValue());
                service.putBoolean(PARAM_ONLINE, true);
                service.putString(PARAM_CONFIG, "");
                setScopes(service, getProfileProvider());
                services.add(service);
            }
        }
        setResult(response, DConnectMessage.RESULT_OK);
        setServices(response, services);
        return true;
    }

    @Override
    protected boolean onPutOnServiceChange(final Intent request, final Intent response,
                                           final String serviceId, final String sessionKey) {
        return super.onPutOnServiceChange(request, response, serviceId, sessionKey);
    }

    @Override
    protected boolean onDeleteOnServiceChange(final Intent request, final Intent response,
                                              final String serviceId, final String sessionKey) {
        return super.onDeleteOnServiceChange(request, response, serviceId, sessionKey);
    }

    /**
     * Gets a instance of HeartRateManager.
     * @return instance of HeartRateManager
     */
    private HeartRateManager getManager() {
        HeartRateDeviceService service = (HeartRateDeviceService) getContext();
        HeartRateApplication app = (HeartRateApplication) service.getApplication();
        return app.getHeartRateManager();
    }
}
