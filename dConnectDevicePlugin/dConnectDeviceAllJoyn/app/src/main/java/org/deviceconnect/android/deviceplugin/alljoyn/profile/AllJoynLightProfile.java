package org.deviceconnect.android.deviceplugin.alljoyn.profile;

import android.content.Intent;
import android.os.Bundle;

import org.allseen.lsf.helper.facade.Lamp;
import org.allseen.lsf.helper.model.LampDataModel;
import org.deviceconnect.android.deviceplugin.alljoyn.AllJoynDeviceApplication;
import org.deviceconnect.android.profile.LightProfile;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * AllJoynデバイスプラグイン Light プロファイル.
 *
 * @author NTT DOCOMO, INC.
 */
public class AllJoynLightProfile extends LightProfile {

    AllJoynDeviceApplication getApplication() {
        return (AllJoynDeviceApplication) getContext().getApplicationContext();
    }

    /**
     * Error レスポンス設定.
     *
     * @param response response
     */
    private void setResultERR(final Intent response) {
        setResult(response, DConnectMessage.RESULT_ERROR);
    }

    /**
     * 成功レスポンス送信.
     *
     * @param response response
     */
    private void sendResultOK(final Intent response) {
        setResult(response, DConnectMessage.RESULT_OK);
        getContext().sendBroadcast(response);
    }

    @Override
    protected boolean onGetLight(Intent request, Intent response, String serviceId) {
        // TODO: 特定のサービスとの紐付け
        List<Bundle> lights = new ArrayList<>();
        AllJoynDeviceApplication app = getApplication();
        for (Lamp lamp : app.getLightingDirector().getLamps()) {
            LampDataModel lampData = lamp.getLampDataModel();
            Bundle light = new Bundle();
            light.putString(PARAM_LIGHT_ID, lampData.id);
            light.putString(PARAM_NAME, lampData.getName());
            light.putString(PARAM_CONFIG, "");
            light.putBoolean(PARAM_ON, lampData.state.getOnOff());
            lights.add(light);
        }
        response.putExtra(PARAM_LIGHTS, lights.toArray(new Bundle[lights.size()]));
        sendResultOK(response);
        return true;
    }

    @Override
    protected boolean onPostLight(Intent request, Intent response, String serviceId, String lightId,
                                  float brightness, int[] color) {
        return false;
    }

    @Override
    protected boolean onDeleteLight(Intent request, Intent response, String serviceId) {
        return false;
    }

    @Override
    protected boolean onPutLight(Intent request, Intent response, String serviceId) {
        return false;
    }

    @Override
    protected boolean onGetLightGroup(Intent request, Intent response, String serviceId) {
        return false;
    }

    @Override
    protected boolean onPostLightGroup(Intent request, Intent response, String serviceId) {
        return false;
    }

    @Override
    protected boolean onDeleteLightGroup(Intent request, Intent response, String serviceId) {
        return false;
    }

    @Override
    protected boolean onPutLightGroup(Intent request, Intent response, String serviceId) {
        return false;
    }

    @Override
    protected boolean onPostLightGroupCreate(Intent request, Intent response, String serviceId) {
        return false;
    }

    @Override
    protected boolean onDeleteLightGroupClear(Intent request, Intent response, String serviceId) {
        return false;
    }
}
