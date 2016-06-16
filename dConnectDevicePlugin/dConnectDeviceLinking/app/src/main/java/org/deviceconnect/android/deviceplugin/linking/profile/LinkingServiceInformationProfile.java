/*
 LinkingServiceInformationProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.profile;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.linking.LinkingApplication;
import org.deviceconnect.android.deviceplugin.linking.LinkingDeviceService;
import org.deviceconnect.android.deviceplugin.linking.beacon.LinkingBeaconManager;
import org.deviceconnect.android.deviceplugin.linking.beacon.LinkingBeaconUtil;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.LinkingBeacon;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDeviceManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceInformationProfile;
import org.deviceconnect.message.DConnectMessage;

/**
 * Linking Service Information Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class LinkingServiceInformationProfile extends ServiceInformationProfile {

    public LinkingServiceInformationProfile(final DConnectProfileProvider provider) {
        super(provider);
    }

    @Override
    protected boolean onGetInformation(final Intent request, final Intent response, final String serviceId) {
        switch (Util.getServiceType(serviceId)) {
            case Util.LINKING_DEVICE:
                return createLinkingDevice(request, response, serviceId);
            case Util.LINKING_BEACON:
                return createLinkingBeacon(request, response, serviceId);
            case Util.LINKING_APP:
                return createLinkingApp(request, response, serviceId);
            default:
                MessageUtils.setNotFoundServiceError(response);
                break;
        }
        return true;
    }

    private boolean createLinkingDevice(final Intent request, final Intent response, final String serviceId) {
        LinkingDevice device = getLinkingDeviceManager().findDeviceByBdAddress(serviceId);
        if (device == null) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }

        Bundle connect = new Bundle();
        setWifiState(connect, getWifiState(serviceId));
        setBluetoothState(connect, getBluetoothState(device));
        setNFCState(connect, getNFCState(serviceId));
        setBLEState(connect, getBLEState(serviceId));
        setConnect(response, connect);
        setVersion(response, getCurrentVersionName());
        setSupports(response, Util.createLinkingDeviceScopes(device));
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    private boolean createLinkingBeacon(final Intent request, final Intent response, final String serviceId) {
        LinkingBeaconManager mgr = getLinkingBeaconManager();
        LinkingBeacon beacon = LinkingBeaconUtil.findLinkingBeacon(mgr, serviceId);
        if (beacon == null) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }

        Bundle connect = new Bundle();
        setWifiState(connect, getWifiState(serviceId));
        setBluetoothState(connect, getBluetoothState(beacon));
        setNFCState(connect, getNFCState(serviceId));
        setBLEState(connect, getBLEState(serviceId));
        setConnect(response, connect);
        setVersion(response, getCurrentVersionName());
        setSupports(response, Util.createLinkingBeaconScopes());
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    private boolean createLinkingApp(final Intent request, final Intent response, final String serviceId) {
        Bundle connect = new Bundle();
        setWifiState(connect, getWifiState(serviceId));
        setBluetoothState(connect, getBluetoothState(serviceId));
        setNFCState(connect, getNFCState(serviceId));
        setBLEState(connect, getBLEState(serviceId));
        setConnect(response, connect);
        setVersion(response, getCurrentVersionName());
        setSupports(response, Util.createLinkingAppScopes());
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    private ConnectState getBluetoothState(final LinkingDevice device) {
        return device.isConnected() ? ConnectState.ON : ConnectState.OFF;
    }

    private ConnectState getBluetoothState(final LinkingBeacon beacon) {
        return beacon.isOnline() ? ConnectState.ON : ConnectState.OFF;
    }

    private String getCurrentVersionName() {
        PackageManager packageManager = getContext().getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getContext().getPackageName(),
                    PackageManager.GET_ACTIVITIES);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "Unknown";
        }
    }

    private LinkingDeviceManager getLinkingDeviceManager() {
        LinkingApplication app = getLinkingApplication();
        return app.getLinkingDeviceManager();
    }

    private LinkingBeaconManager getLinkingBeaconManager() {
        LinkingApplication app = getLinkingApplication();
        return app.getLinkingBeaconManager();
    }

    private LinkingApplication getLinkingApplication() {
        LinkingDeviceService service = (LinkingDeviceService) getContext();
        return (LinkingApplication) service.getApplication();
    }
}
