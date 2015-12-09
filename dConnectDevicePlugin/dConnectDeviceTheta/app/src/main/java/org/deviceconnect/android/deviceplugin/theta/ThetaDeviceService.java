/*
 ThetaDeviceService
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta;

import android.content.Intent;
import android.os.Bundle;

import com.theta360.lib.PtpipInitiator;
import com.theta360.lib.ThetaException;

import org.deviceconnect.android.deviceplugin.theta.core.ThetaDevice;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceClient;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceManager;
import org.deviceconnect.android.deviceplugin.theta.profile.ThetaBatteryProfile;
import org.deviceconnect.android.deviceplugin.theta.profile.ThetaFileProfile;
import org.deviceconnect.android.deviceplugin.theta.profile.ThetaMediaStreamRecordingProfile;
import org.deviceconnect.android.deviceplugin.theta.profile.ThetaOmnidirectionalImageProfile;
import org.deviceconnect.android.deviceplugin.theta.profile.ThetaServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.theta.profile.ThetaSystemProfile;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.OmnidirectionalImageProfile;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.ServiceInformationProfile;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.provider.FileManager;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Theta Device Service.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaDeviceService extends DConnectMessageService {

    private ThetaDeviceManager mDeviceMgr;
    private ThetaDeviceClient mClient;

    @Override
    public void onCreate() {
        super.onCreate();

        ThetaDeviceApplication app = (ThetaDeviceApplication) getApplication();
        mDeviceMgr = app.getDeviceManager();
        mClient = new ThetaDeviceClient(mDeviceMgr);

        EventManager.INSTANCE.setController(new MemoryCacheController());

        FileManager fileMgr = new FileManager(this);
        addProfile(new ThetaBatteryProfile(mClient));
        addProfile(new ThetaFileProfile(mClient, fileMgr));
        addProfile(new ThetaMediaStreamRecordingProfile(mClient, fileMgr));
        addProfile(new ThetaOmnidirectionalImageProfile(app.getHeadTracker()));
    }

    @Override
    public void onDestroy() {
        try {
            PtpipInitiator.close();
        } catch (ThetaException e) {
            // Nothing to do.
        }
        super.onDestroy();
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new ThetaSystemProfile();
    }

    @Override
    protected ServiceInformationProfile getServiceInformationProfile() {
        return new ServiceInformationProfile(this) {
        };
    }

    @Override
    protected ServiceDiscoveryProfile getServiceDiscoveryProfile() {
        return new ThetaServiceDiscoveryProfile(this);
    }

    public boolean searchDevice(final Intent request, final Intent response) {
        List<Bundle> services = new ArrayList<Bundle>();
        ThetaDevice device = mDeviceMgr.getConnectedDevice();
        if (device != null) {
            Bundle service = new Bundle();
            service.putString(ServiceDiscoveryProfile.PARAM_ID, device.getId());
            service.putString(ServiceDiscoveryProfile.PARAM_NAME, device.getName());
            service.putString(ServiceDiscoveryProfile.PARAM_TYPE,
                ServiceDiscoveryProfile.NetworkType.WIFI.getValue());
            service.putBoolean(ServiceDiscoveryProfile.PARAM_ONLINE, true);
            ServiceDiscoveryProfile.setScopes(service, this);
            services.add(service);
        }

        Bundle service = new Bundle();
        service.putString(ServiceDiscoveryProfile.PARAM_ID,
            ThetaOmnidirectionalImageProfile.SERVICE_ID);
        service.putString(ServiceDiscoveryProfile.PARAM_NAME,
            ThetaOmnidirectionalImageProfile.SERVICE_NAME);
        service.putBoolean(ServiceDiscoveryProfile.PARAM_ONLINE, true);
        service.putStringArray(ServiceDiscoveryProfile.PARAM_SCOPES,
            new String[] {OmnidirectionalImageProfile.PROFILE_NAME});
        services.add(service);

        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
        response.putExtra(ServiceDiscoveryProfile.PARAM_SERVICES, services.toArray(new Bundle[services.size()]));
        return true;
    }

}
