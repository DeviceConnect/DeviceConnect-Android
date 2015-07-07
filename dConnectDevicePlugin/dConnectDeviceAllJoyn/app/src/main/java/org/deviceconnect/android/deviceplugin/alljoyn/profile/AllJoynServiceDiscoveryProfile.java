package org.deviceconnect.android.deviceplugin.alljoyn.profile;

import android.content.Intent;
import android.os.Bundle;

import org.allseen.lsf.helper.model.ControllerDataModel;
import org.deviceconnect.android.deviceplugin.alljoyn.AllJoynDeviceApplication;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;

import java.util.LinkedList;
import java.util.List;

/**
 * AllJoynデバイスプラグイン System Discovery プロファイル.
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
        if (app.isReady(10000)) {
//            LightingDirector director = app.getLightingDirector();
//            BusAttachment bus = director.getBusAttachment();
//            Log.d("SHIGSHIG", bus.toString());

            List<ControllerDataModel> controllers = app.getDiscoveredControllers();
            List<Bundle> services = new LinkedList<>();
            for (ControllerDataModel controller : controllers) {
                Bundle service = new Bundle();
                service.putString(ServiceDiscoveryProfileConstants.PARAM_ID, controller.id);
                service.putString(ServiceDiscoveryProfileConstants.PARAM_NAME, controller.getName());
                // TODO: AllJoynリモートオブジェクトのトランスポート情報を取得できるか調査。
//                service.putString(ServiceDiscoveryProfileConstants.PARAM_TYPE, "wifi");
                service.putBoolean(ServiceDiscoveryProfileConstants.PARAM_ONLINE, controller.connected);
                services.add(service);
            }
            // レスポンスを設定
            setServices(response, services);
        } else {
            setServices(response, new Bundle[0]);
        }

        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }


}
