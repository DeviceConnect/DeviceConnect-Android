package org.deviceconnect.android.deviceplugin.theta;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;

import com.theta360.lib.PtpipInitiator;
import com.theta360.lib.ThetaException;

import org.deviceconnect.android.deviceplugin.theta.profile.ThetaBatteryProfile;
import org.deviceconnect.android.deviceplugin.theta.profile.ThetaFileProfile;
import org.deviceconnect.android.deviceplugin.theta.profile.ThetaMediaStreamRecordingProfile;
import org.deviceconnect.android.deviceplugin.theta.profile.ThetaServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.theta.profile.ThetaServiceInformationProfile;
import org.deviceconnect.android.deviceplugin.theta.profile.ThetaSystemProfile;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.ServiceInformationProfile;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.provider.FileManager;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.profile.MediaStreamRecordingProfileConstants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Theta Device Service.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaDeviceService extends DConnectMessageService {

    private static final String PREFIX_SSID = "THETA";

    @Override
    public void onCreate() {
        super.onCreate();

        EventManager.INSTANCE.setController(new MemoryCacheController());

        ThetaApiClient client = new ThetaApiClient();
        FileManager fileMgr = new FileManager(this);
        addProfile(new ThetaBatteryProfile(client));
        addProfile(new ThetaFileProfile(client, fileMgr));
        addProfile(new ThetaMediaStreamRecordingProfile(client, fileMgr));
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
        return new ThetaServiceInformationProfile(this);
    }

    @Override
    protected ServiceDiscoveryProfile getServiceDiscoveryProfile() {
        return new ThetaServiceDiscoveryProfile(this);
    }

    public boolean searchDevice(final Intent request, final Intent response) {
        List<Bundle> services = new ArrayList<Bundle>();
        String ssId = getSSID();
        if (isTheta(ssId)) {
            Bundle service = new Bundle();
            service.putString(ServiceDiscoveryProfile.PARAM_ID, "theta");
            service.putString(ServiceDiscoveryProfile.PARAM_NAME, ssId);
            service.putString(ServiceDiscoveryProfile.PARAM_TYPE,
                ServiceDiscoveryProfile.NetworkType.WIFI.getValue());
            service.putBoolean(ServiceDiscoveryProfile.PARAM_ONLINE, true);
            ServiceDiscoveryProfile.setScopes(service, this);
            services.add(service);
        }
        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
        response.putExtra(ServiceDiscoveryProfile.PARAM_SERVICES, services.toArray(new Bundle[services.size()]));
        return true;
    }

    public String getSSID() {
        WifiManager wifiMgr = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        String ssId = wifiInfo.getSSID();
        ssId = ssId.replace("\"", "");
        return ssId;
    }

    private static boolean isTheta(final String ssId) {
        if (ssId == null) {
            return false;
        }
        return ssId.startsWith(PREFIX_SSID);
    }

}
