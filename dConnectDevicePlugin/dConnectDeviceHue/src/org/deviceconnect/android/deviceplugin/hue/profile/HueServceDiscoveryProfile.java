/*
HueServceDiscoveryProfile
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
*/

package org.deviceconnect.android.deviceplugin.hue.profile;

import java.util.ArrayList;
import java.util.List;

import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;

import android.content.Intent;
import android.os.Bundle;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHHueSDK;

/**
 * スマートデバイス検索機能を提供するAPI.
 * @author NTT DOCOMO, INC.
 */
public class HueServceDiscoveryProfile extends ServiceDiscoveryProfile {

    /**
     * コンストラクタ.
     * 
     * @param provider プロファイルプロバイダ
     */
    public HueServceDiscoveryProfile(final DConnectProfileProvider provider) {
        super(provider);
    }

    @Override
    protected boolean onGetServices(final Intent request, final Intent response) {
        PHHueSDK hueSDK = PHHueSDK.getInstance();
        List<PHAccessPoint> allAccessPointList = hueSDK.getAccessPointsFound();
        List<Bundle> services = new ArrayList<Bundle>();
        for (PHAccessPoint accessPoint : allAccessPointList) {
            if (!hueSDK.isAccessPointConnected(accessPoint)) {
                continue;
            }
            Bundle service = new Bundle();
            service.putString(ServiceDiscoveryProfileConstants.PARAM_ID, accessPoint.getIpAddress());
            service.putString(ServiceDiscoveryProfileConstants.PARAM_NAME, "hue " + accessPoint.getMacAddress());
            service.putString(ServiceDiscoveryProfileConstants.PARAM_TYPE, "wifi");
            service.putBoolean(ServiceDiscoveryProfileConstants.PARAM_ONLINE, true);
            setScopes(service, getProfileProvider());
            services.add(service);
        }
        // レスポンスを設定
        setServices(response, services);
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

}
