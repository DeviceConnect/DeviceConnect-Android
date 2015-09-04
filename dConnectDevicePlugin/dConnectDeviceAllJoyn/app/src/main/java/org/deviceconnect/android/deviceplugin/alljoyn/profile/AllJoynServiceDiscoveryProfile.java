package org.deviceconnect.android.deviceplugin.alljoyn.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.alljoyn.AllJoynDeviceApplication;
import org.deviceconnect.android.deviceplugin.alljoyn.AllJoynServiceEntity;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;

import java.util.LinkedList;
import java.util.List;

/**
 * System Discovery profile for AllJoyn.
 *
 * @author NTT DOCOMO, INC.
 */
public class AllJoynServiceDiscoveryProfile extends ServiceDiscoveryProfile {

    public AllJoynServiceDiscoveryProfile(DConnectProfileProvider provider) {
        super(provider);
    }

    @Override
    protected boolean onGetServices(final Intent request, final Intent response) {
        AllJoynDeviceApplication app =
                (AllJoynDeviceApplication) getContext().getApplicationContext();

        app.performDiscovery();
        List<Bundle> services = new LinkedList<>();
        for (AllJoynServiceEntity serviceEntity : app.getDiscoveredAlljoynServices().values()) {
            Bundle service = new Bundle();
            service.putString(ServiceDiscoveryProfileConstants.PARAM_ID, serviceEntity.appId);
            service.putString(ServiceDiscoveryProfileConstants.PARAM_NAME, serviceEntity.serviceName);
            // TODO: AllJoynリモートオブジェクトのトランスポート情報を取得できるか調査。
//                service.putString(ServiceDiscoveryProfileConstants.PARAM_TYPE, "wifi");
            service.putBoolean(ServiceDiscoveryProfileConstants.PARAM_ONLINE, true);
            services.add(service);
        }

        // レスポンスを設定
        setServices(response, services);
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

}
