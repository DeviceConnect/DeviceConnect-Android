/*
 IRKitServiceInformationProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.irkit.profile;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.irkit.IRKitDevice;
import org.deviceconnect.android.deviceplugin.irkit.IRKitDeviceService;
import org.deviceconnect.android.deviceplugin.irkit.data.IRKitDBHelper;
import org.deviceconnect.android.deviceplugin.irkit.data.VirtualDeviceData;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceInformationProfile;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * IRKit Service Information Profile.
 * @author NTT DOCOMO, INC.
 */
public class IRKitServiceInformationProfile extends ServiceInformationProfile {


    /**
     * Lightデバイスのプロファイル.
     */
    public static final String[] LIGHT_PROFILES = {"system", "servicediscovery", "serviceinformation",
                                                    "authorization", "light"};
    /**
     * TVデバイスのプロファイル.
     */
    public static final String[] TV_PROFILES = {"system", "servicediscovery", "serviceinformation",
                                                "authorization", "tv"};

    /**
     * IRKitデバイスのプロファイル.
     */
    public static final String[] IRKIT_PROFILES = {"system", "servicediscovery", "serviceinformation",
            "authorization", "remote_controller"};

    /**
     * コンストラクタ.
     * 
     * @param provider Provider
     */
    public IRKitServiceInformationProfile(final DConnectProfileProvider provider) {
        super(provider);

    }

    @Override
    protected ConnectState getWifiState(final String serviceId) {
        
        IRKitDeviceService service = (IRKitDeviceService) getContext();
        IRKitDevice device = service.getDevice(serviceId);
        
        if (device != null) {
            return ConnectState.ON;
        } else {
            return ConnectState.OFF;
        }
    }

    @Override
    protected boolean onGetInformation(final Intent request, final Intent response, final String serviceId) {
        IRKitDBHelper dbHelper = new IRKitDBHelper(getContext());
        List<VirtualDeviceData> virtuals = dbHelper.getVirtualDevices(serviceId);
        if (virtuals.size() == 1 && virtuals.get(0).getCategoryName().equals("ライト")) {
            setDefaultServiceInformation(response, serviceId);
            ArrayList<String> profiles = new ArrayList<String>();
            for (String profile : LIGHT_PROFILES) {
                profiles.add(profile);
            }
            setSupports(response, profiles.toArray(new String[0]));
        } else if (virtuals.size() == 1 && virtuals.get(0).getCategoryName().equals("テレビ")) {
            setDefaultServiceInformation(response, serviceId);
            ArrayList<String> profiles = new ArrayList<String>();
            for (String profile : TV_PROFILES) {
                profiles.add(profile);
            }
            setSupports(response, profiles.toArray(new String[0]));
        } else {
            setDefaultServiceInformation(response, serviceId);
            ArrayList<String> profiles = new ArrayList<String>();
            for (String profile : IRKIT_PROFILES) {
                profiles.add(profile);
            }
            setSupports(response, profiles.toArray(new String[0]));
        }
        return true;
    }

    /**
     * デフォルトのServiceInformationのパラメータを設定する.
     * @param response レスポンス
     * @param serviceId サービスID
     */
    private void setDefaultServiceInformation(Intent response, String serviceId) {
        Bundle connect = new Bundle();
        setWifiState(connect, true);
        setBluetoothState(connect, getBluetoothState(serviceId));
        setNFCState(connect, getNFCState(serviceId));
        setBLEState(connect, getBLEState(serviceId));
        setConnect(response, connect);

        // version
        setVersion(response, getCurrentVersionName());
        setResult(response, DConnectMessage.RESULT_OK);
    }

    /**
     * AndroidManifest.xmlのversionNameを取得する.
     *
     * @return バージョン名
     */
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
}
