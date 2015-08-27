/*
 ThetaDeviceService
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import com.theta360.lib.PtpipInitiator;
import com.theta360.lib.ThetaException;

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
import java.util.logging.Logger;

/**
 * Theta Device Service.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaDeviceService extends DConnectMessageService {

    private final Logger mLogger = Logger.getLogger("theta.dplugin");

    private final ThetaApiClient mClient = new ThetaApiClient();

    @Override
    public void onCreate() {
        super.onCreate();

        EventManager.INSTANCE.setController(new MemoryCacheController());

        FileManager fileMgr = new FileManager(this);
        addProfile(new ThetaBatteryProfile(mClient));
        addProfile(new ThetaFileProfile(mClient, fileMgr));
        addProfile(new ThetaMediaStreamRecordingProfile(mClient, fileMgr));
        addProfile(new ThetaOmnidirectionalImageProfile());

        WifiManager wifiMgr = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
        fetchThetaDevice(wifiMgr.getConnectionInfo());
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
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return super.onStartCommand(intent, flags, startId);
        }

        String action = intent.getAction();
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                fetchThetaDevice(wifiInfo);
            }
        } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
            switch (state) {
                case WifiManager.WIFI_STATE_DISABLED:
                    mLogger.info("WiFi state: disabled.");
                    mClient.disposeDevice();
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                    mLogger.info("WiFi state: enabled.");
                    break;
                default:
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
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
        ThetaDeviceInfo deviceInfo = mClient.getDevice();
        if (deviceInfo != null) {
            Bundle service = new Bundle();
            service.putString(ServiceDiscoveryProfile.PARAM_ID, deviceInfo.mServiceId);
            service.putString(ServiceDiscoveryProfile.PARAM_NAME, deviceInfo.mName);
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

    private void fetchThetaDevice(final WifiInfo wifiInfo) {
        if (wifiInfo == null) {
            return;
        }
        String ssId = wifiInfo.getSSID();
        if (ssId == null) {
            return;
        }
        ssId = ssId.replace("\"", "");
        if (isTheta(ssId)) {
            mClient.fetchDevice(wifiInfo);
        } else {
            mClient.disposeDevice();
        }
    }

    private boolean isTheta(final String ssId) {
        if (ssId == null) {
            return false;
        }
        return ssId.startsWith(getString(R.string.theta_ssid_prefix));
    }

}
