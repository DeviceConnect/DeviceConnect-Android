/*
 ChromeCastServiceInformationProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.chromecast.profile;

import java.util.ArrayList;

import org.deviceconnect.android.deviceplugin.chromecast.ChromeCastService;
import org.deviceconnect.android.deviceplugin.chromecast.core.ChromeCastApplication;
import org.deviceconnect.android.deviceplugin.chromecast.core.ChromeCastDiscovery;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceInformationProfile;
import org.deviceconnect.message.DConnectMessage;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

/**
 * Service Information プロファイル (Chromecast).
 * <p>
 * Chromecastのサービス情報を提供する
 * </p>
 * @author NTT DOCOMO, INC.
 */
public class ChromeCastServiceInformationProfile extends ServiceInformationProfile {

    /**
     * コンストラクタ.
     * 
     * @param provider プロバイダ
     */
    public ChromeCastServiceInformationProfile(final DConnectProfileProvider provider) {
        super(provider);
    }

    /**
     * バージョンを取得する.
     * 
     * @return  version バージョン
     */
    private String getCurrentVersionName() {
        PackageManager packageManager = getContext().getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getContext().getPackageName(),
                    PackageManager.GET_ACTIVITIES);
            return packageInfo.versionName;
        } catch (NameNotFoundException e) {
            return "Unknown";
        }
    }

    @Override
    protected boolean onGetInformation(final Intent request, final Intent response, final String serviceId) {
        // Select Route, launch
        ChromeCastDiscovery discovery = ((ChromeCastService) getContext()).getChromeCastDiscovery();
        ChromeCastApplication application = ((ChromeCastService) getContext()).getChromeCastApplication();
        if (discovery.getSelectedDevice() != null) {
            if (discovery.getSelectedDevice().getFriendlyName().equals(serviceId)) {
                application.connect();
            } else {
                discovery.setRouteName(serviceId);
            }
        } else {
            discovery.setRouteName(serviceId);
        }

        // connect
        Bundle connect = new Bundle();
        setWifiState(connect, getWifiState(serviceId));
        setBluetoothState(connect, getBluetoothState(serviceId));
        setNFCState(connect, getNFCState(serviceId));
        setBLEState(connect, getBLEState(serviceId));
        setConnect(response, connect);

        // version
        setVersion(response, getCurrentVersionName());

        // supports
        ArrayList<String> profiles = new ArrayList<String>();
        for (DConnectProfile profile : getProfileProvider().getProfileList()) {
            profiles.add(profile.getProfileName());
        }
        setSupports(response, profiles.toArray(new String[0]));
        setResult(response, DConnectMessage.RESULT_OK);

        return true;
    }
}
