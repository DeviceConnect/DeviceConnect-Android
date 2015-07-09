package org.deviceconnect.android.deviceplugin.alljoyn.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.alljoyn.bus.BusException;
import org.alljoyn.services.common.BusObjectDescription;
import org.allseen.LSF.ControllerService.Lamp;
import org.allseen.LSF.LampState;
import org.allseen.LSF.ResponseCode;
import org.deviceconnect.android.deviceplugin.alljoyn.AllJoynDeviceApplication;
import org.deviceconnect.android.deviceplugin.alljoyn.AllJoynServiceEntity;
import org.deviceconnect.android.message.MessageUtils;
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

    private enum LampServiceType {
        TYPE_SINGLE,
        TYPE_CONTROLLER,
        TYPE_UNKNOWN,
    }

    AllJoynDeviceApplication getApplication() {
        return (AllJoynDeviceApplication) getContext().getApplicationContext();
    }

    @Override
    protected boolean onGetLight(Intent request, final Intent response, final String serviceId) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

        final AllJoynDeviceApplication app = getApplication();
        final AllJoynServiceEntity service = app.getDiscoveredAlljoynServices().get(serviceId);

        switch (getLampServiceType(service)) {
            case TYPE_SINGLE: {
                app.joinSession(service.busName, service.port, app.new ResultReceiver() {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        if (resultCode != AllJoynDeviceApplication.RESULT_OK) {
                            MessageUtils.setUnknownError(response, "Failed to join session.");
                            getContext().sendBroadcast(response);
                            return;
                        }
                        int sessionId =
                                resultData.getInt(AllJoynDeviceApplication.PARAM_SESSION_ID);

                        // LampDetails details = app.getInterface(serviceId, LampDetails.class);
                        LampState state = app.getInterface(service.busName, sessionId, LampState.class);

                        List<Bundle> lights = new ArrayList<>();
                        if (state != null) {
                            Bundle light = new Bundle();
                            try {
                                light.putString(PARAM_LIGHT_ID, "self");
                                light.putString(PARAM_NAME, service.serviceName);
                                light.putString(PARAM_CONFIG, "");
                                light.putBoolean(PARAM_ON, state.getOnOff());
                                lights.add(light);
                            } catch (BusException e) {
                                MessageUtils.setUnknownError(response, e.getLocalizedMessage());
                                getContext().sendBroadcast(response);
                                return;
                            }
                        }
                        response.putExtra(PARAM_LIGHTS, lights.toArray(new Bundle[lights.size()]));
                        setResultOK(response);
                        getContext().sendBroadcast(response);

                        app.leaveSession(sessionId, app.new ResultReceiver());
                    }
                });
                return false;
            }
            case TYPE_CONTROLLER: {
                app.joinSession(service.busName, service.port, app.new ResultReceiver() {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        if (resultCode != AllJoynDeviceApplication.RESULT_OK) {
                            MessageUtils.setUnknownError(response, "Failed to join session.");
                            getContext().sendBroadcast(response);
                            return;
                        }
                        int sessionId =
                                resultData.getInt(AllJoynDeviceApplication.PARAM_SESSION_ID);

                        Lamp lamp = app.getInterface(service.busName, sessionId, Lamp.class);

                        List<Bundle> lights = new ArrayList<>();
                        if (lamp != null) {
                            try {
                                Lamp.GetAllLampIDs_return_value_uas
                                        lampIDsResponse = lamp.getAllLampIDs();
                                if (lampIDsResponse.responseCode != ResponseCode.OK.getValue()) {
                                    MessageUtils.setUnknownError(response,
                                            "Failed to obtain lamp IDs.");
                                    getContext().sendBroadcast(response);
                                    return;
                                }
                                for (String lampId : lampIDsResponse.lampIDs) {
                                    Log.i("SHIGSHIG", "lamp id: " + lampId);

                                    Bundle light = new Bundle();
                                    light.putString(PARAM_LIGHT_ID, lampId);

                                    Lamp.GetLampName_return_value_usss lampNameResponse =
                                            lamp.getLampName(lampId, service.defaultLanguage);
                                    if (lampNameResponse.responseCode != ResponseCode.OK.getValue()) {
                                        Log.w(AllJoynLightProfile.this.getClass().getSimpleName(),
                                                "Failed to obtain the lamp name. Skipping this lamp...");
                                        continue;
                                    }

                                    Log.i("SHIGSHIG", "lampNameResponse: " + lampNameResponse);

                                    Lamp.GetLampState_return_value_usa_sv lampStateResponse =
                                            lamp.getLampState(lampId);
                                    if (lampStateResponse.responseCode != ResponseCode.OK.getValue()) {
                                        Log.w(AllJoynLightProfile.this.getClass().getSimpleName(),
                                                "Failed to obtain the on/off state. Skipping this lamp...");
                                        continue;
                                    }

                                    Log.i("SHIGSHIG", "lampStateResponse: " + lampStateResponse);

                                    boolean isOn = lampStateResponse.lampState.get("OnOff")
                                            .getObject(boolean.class);

                                    light.putString(PARAM_NAME, lampNameResponse.lampName);
                                    light.putString(PARAM_CONFIG, "");
                                    light.putBoolean(PARAM_ON, isOn);
                                    lights.add(light);
                                }
                            } catch (BusException e) {
                                MessageUtils.setUnknownError(response, e.getLocalizedMessage());
                                getContext().sendBroadcast(response);
                                return;
                            }
                        }
                        response.putExtra(PARAM_LIGHTS, lights.toArray(new Bundle[lights.size()]));
                        setResultOK(response);
                        getContext().sendBroadcast(response);

                        app.leaveSession(sessionId, app.new ResultReceiver());
                    }
                });
                return false;
            }
            case TYPE_UNKNOWN:
            default: {
                setUnsupportedError(response);
                return true;
            }
        }
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

    /**
     * Error レスポンス設定。
     *
     * @param response response
     */
    private void setResultERR(final Intent response) {
        setResult(response, DConnectMessage.RESULT_ERROR);
    }

    /**
     * 成功レスポンス設定。
     *
     * @param response response
     */
    private void setResultOK(final Intent response) {
        setResult(response, DConnectMessage.RESULT_OK);
    }

    private boolean isControllerService(AllJoynServiceEntity service) {
        for (BusObjectDescription busObject : service.proxyObjects) {
            for (String iface : busObject.interfaces) {
                if (iface.equals("org.allseen.LSF.ControllerService")) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSupportingInterfaces(AllJoynServiceEntity service, String... interfaces) {
        for (String ifaceCheck : interfaces) {
            boolean found = false;
            for (BusObjectDescription busObject : service.proxyObjects) {
                for (String iface : busObject.interfaces) {
                    if (iface.equals(ifaceCheck)) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    private LampServiceType getLampServiceType(AllJoynServiceEntity service) {
        if (isSupportingInterfaces(service, "org.allseen.LSF.ControllerService.Lamp")) {
            // Can manager multiple lamps and control each of them.

            return LampServiceType.TYPE_CONTROLLER;
        } else if (isSupportingInterfaces(service, "org.allseen.LSF.LampState")) {
            // Can control a single lamp.

            return LampServiceType.TYPE_SINGLE;
        }

        return LampServiceType.TYPE_UNKNOWN;
    }
}
