package org.deviceconnect.android.deviceplugin.alljoyn.profile;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.alljoyn.AllJoynDeviceApplication;
import org.deviceconnect.android.deviceplugin.alljoyn.AllJoynServiceEntity;
import org.deviceconnect.android.deviceplugin.alljoyn.AllJoynServiceSupportChecker;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceInformationProfile;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AllJoynServiceInformationProfile extends ServiceInformationProfile {
    public AllJoynServiceInformationProfile(DConnectProfileProvider provider) {
        super(provider);
    }

    @Override
    protected boolean onGetInformation(Intent request, Intent response, String serviceId) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

        AllJoynDeviceApplication app =
                (AllJoynDeviceApplication)getContext().getApplicationContext();
        AllJoynServiceEntity service = app.getDiscoveredAlljoynServices().get(serviceId);
        if (service == null){
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }

        Bundle connect = new Bundle();
        setWifiState(connect, this.getWifiState(serviceId));
        setBluetoothState(connect, this.getBluetoothState(serviceId));
        setNFCState(connect, this.getNFCState(serviceId));
        setBLEState(connect, this.getBLEState(serviceId));
        setConnect(response, connect);
        setVersion(response, app.getCurrentVersionName());

        List<String> profiles =
                AllJoynServiceSupportChecker.getSupportedDCProfiles(getProfileProvider(), service);

        setSupports(response, profiles.toArray(new String[profiles.size()]));
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }
}
