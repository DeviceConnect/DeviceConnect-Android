package org.deviceconnect.android.deviceplugin.alljoyn.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.Variant;
import org.alljoyn.services.common.BusObjectDescription;
import org.allseen.LSF.ControllerService.Lamp;
import org.allseen.LSF.LampDetails;
import org.allseen.LSF.LampState;
import org.allseen.LSF.ResponseCode;
import org.deviceconnect.android.deviceplugin.alljoyn.AllJoynDeviceApplication;
import org.deviceconnect.android.deviceplugin.alljoyn.AllJoynServiceEntity;
import org.deviceconnect.android.deviceplugin.alljoyn.OneShotSessionHandler;
import org.deviceconnect.android.deviceplugin.alljoyn.util.ColorUtil;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.LightProfile;
import org.deviceconnect.message.DConnectMessage;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AllJoynデバイスプラグイン Light プロファイル.
 *
 * @author NTT DOCOMO, INC.
 */
public class AllJoynLightProfile extends LightProfile {

    private enum LampServiceType {
        TYPE_SINGLE_LAMP,
        TYPE_LAMP_CONTROLLER,
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
            case TYPE_SINGLE_LAMP: {
                onGetLightForSingleLamp(request, response, service);
                return false;
            }
            case TYPE_LAMP_CONTROLLER: {
                onGetLightForLampController(request, response, service);
                return false;
            }
            case TYPE_UNKNOWN:
            default: {
                setUnsupportedError(response);
                return true;
            }
        }
    }

    private void onGetLightForSingleLamp(Intent request, final Intent response,
                                         final AllJoynServiceEntity service) {
        final AllJoynDeviceApplication app = getApplication();

        OneShotSessionHandler.SessionJoinCallback callback = new OneShotSessionHandler.SessionJoinCallback() {
            @Override
            public void onSessionJoined(String busName, short port, int sessionId) {
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
            }

            @Override
            public void onSessionFailed(String busName, short port) {
                MessageUtils.setUnknownError(response, "Failed to join session.");
                getContext().sendBroadcast(response);
            }
        };
        OneShotSessionHandler.run(getContext(), service.busName, service.port, callback);
    }

    private void onGetLightForLampController(Intent request, final Intent response,
                                             final AllJoynServiceEntity service) {
        final AllJoynDeviceApplication app = getApplication();

        OneShotSessionHandler.SessionJoinCallback callback = new OneShotSessionHandler.SessionJoinCallback() {
            @Override
            public void onSessionJoined(String busName, short port, int sessionId) {
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
                            Bundle light = new Bundle();
                            light.putString(PARAM_LIGHT_ID, lampId);

                            Lamp.GetLampName_return_value_usss lampNameResponse =
                                    lamp.getLampName(lampId, service.defaultLanguage);
                            if (lampNameResponse.responseCode != ResponseCode.OK.getValue()) {
                                Log.w(AllJoynLightProfile.this.getClass().getSimpleName(),
                                        "Failed to obtain the lamp name. Skipping this lamp...");
                                continue;
                            }

                            Lamp.GetLampState_return_value_usa_sv lampStateResponse =
                                    lamp.getLampState(lampId);
                            if (lampStateResponse.responseCode != ResponseCode.OK.getValue()) {
                                Log.w(AllJoynLightProfile.this.getClass().getSimpleName(),
                                        "Failed to obtain the on/off state. Skipping this lamp...");
                                continue;
                            }

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
            }

            @Override
            public void onSessionFailed(String busName, short port) {
                MessageUtils.setUnknownError(response, "Failed to join session.");
                getContext().sendBroadcast(response);
            }
        };
        OneShotSessionHandler.run(getContext(), service.busName, service.port, callback);
    }

    @Override
    protected boolean onPostLight(Intent request, Intent response, String serviceId, String lightId,
                                  Float brightness, int[] color) {
        if (serviceId == null || lightId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

        final AllJoynDeviceApplication app = getApplication();
        final AllJoynServiceEntity service = app.getDiscoveredAlljoynServices().get(serviceId);

        switch (getLampServiceType(service)) {
            case TYPE_SINGLE_LAMP: {
                onPostLightForSingleLamp(request, response, service, lightId, brightness, color);
                return false;
            }
            case TYPE_LAMP_CONTROLLER: {
                onPostLightForLampController(request, response, service, lightId, brightness, color);
                return false;
            }
            case TYPE_UNKNOWN:
            default: {
                setUnsupportedError(response);
                return true;
            }
        }
    }

    private void onPostLightForSingleLamp(Intent request, final Intent response,
                                          final AllJoynServiceEntity service, String lightId,
                                          final Float brightness, final int[] color) {
        final AllJoynDeviceApplication app = getApplication();

        OneShotSessionHandler.SessionJoinCallback callback = new OneShotSessionHandler.SessionJoinCallback() {
            @Override
            public void onSessionJoined(String busName, short port, int sessionId) {
                LampState state = app.getInterface(service.busName, sessionId, LampState.class);
                LampDetails details = app.getInterface(service.busName, sessionId, LampDetails.class);

                try {
                    HashMap<String, Variant> newStates = new HashMap<>();

                    // NOTE: Arithmetic operations in primitive types may lead to arithmetic
                    // overflow. To retain precision, BigDecimal objects are used.

                    state.getOnOff();
                    newStates.put("OnOff", new Variant(true, "b"));
                    if (details.getColor() && color != null) {
                        int[] hsb = ColorUtil.convertRGB_8_8_8_To_HSB_32_32_32(color);

                        newStates.put("Hue", new Variant(hsb[0], "u"));
                        newStates.put("Saturation", new Variant(hsb[1], "u"));
                    }
                    if (details.getDimmable() && brightness != null) {
                        // [0, 1] -> [0, 0xffffffff]
                        BigDecimal tmp = BigDecimal.valueOf(0xffffffffl);
                        tmp = tmp.multiply(BigDecimal.valueOf(brightness));
                        long scaledVal = tmp.longValue();
                        int intScaledVal = ByteBuffer.allocate(8).putLong(scaledVal).getInt(4);
                        newStates.put("Brightness", new Variant(intScaledVal, "u"));
                    }

                    int responseCode = state.transitionLampState(0, newStates, 10);
                    if (responseCode != ResponseCode.OK.getValue()) {
                        MessageUtils.setUnknownError(response, "Failed to change lamp states.");
                        getContext().sendBroadcast(response);
                        return;
                    }
                } catch (BusException e) {
                    MessageUtils.setUnknownError(response, e.getLocalizedMessage());
                    getContext().sendBroadcast(response);
                    return;
                }

                setResultOK(response);
                getContext().sendBroadcast(response);
            }

            @Override
            public void onSessionFailed(String busName, short port) {
                MessageUtils.setUnknownError(response, "Failed to join session.");
                getContext().sendBroadcast(response);
            }
        };
        OneShotSessionHandler.run(getContext(), service.busName, service.port, callback);
    }

    private void onPostLightForLampController(Intent request, Intent response,
                                              AllJoynServiceEntity service, String lightId,
                                              Float brightness, int[] color) {

    }

    @Override
    protected boolean onDeleteLight(Intent request, Intent response, String serviceId, String lightId) {
        if (serviceId == null || lightId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

        final AllJoynDeviceApplication app = getApplication();
        final AllJoynServiceEntity service = app.getDiscoveredAlljoynServices().get(serviceId);

        switch (getLampServiceType(service)) {
            case TYPE_SINGLE_LAMP: {
                onDeleteLightForSingleLamp(request, response, service, lightId);
                return false;
            }
            case TYPE_LAMP_CONTROLLER: {
                onDeleteLightForLampController(request, response, service, lightId);
                return false;
            }
            case TYPE_UNKNOWN:
            default: {
                setUnsupportedError(response);
                return true;
            }
        }
    }

    private void onDeleteLightForSingleLamp(Intent request, final Intent response,
                                            final AllJoynServiceEntity service, String lightId) {
        final AllJoynDeviceApplication app = getApplication();

        OneShotSessionHandler.SessionJoinCallback callback = new OneShotSessionHandler.SessionJoinCallback() {
            @Override
            public void onSessionJoined(String busName, short port, int sessionId) {
                LampState state = app.getInterface(service.busName, sessionId, LampState.class);

                try {
                    state.setOnOff(false);
                } catch (BusException e) {
                    MessageUtils.setUnknownError(response, e.getLocalizedMessage());
                    getContext().sendBroadcast(response);
                    return;
                }

                setResultOK(response);
                getContext().sendBroadcast(response);
            }

            @Override
            public void onSessionFailed(String busName, short port) {
                MessageUtils.setUnknownError(response, "Failed to join session.");
                getContext().sendBroadcast(response);
            }
        };
        OneShotSessionHandler.run(getContext(), service.busName, service.port, callback);
    }

    private void onDeleteLightForLampController(Intent request, final Intent response,
                                                final AllJoynServiceEntity service,
                                                final String lightId) {
        final AllJoynDeviceApplication app = getApplication();

        OneShotSessionHandler.SessionJoinCallback callback = new OneShotSessionHandler.SessionJoinCallback() {
            @Override
            public void onSessionJoined(String busName, short port, int sessionId) {
                Lamp lamp = app.getInterface(service.busName, sessionId, Lamp.class);

                try {
                    Map<String, Variant> newStates = new HashMap<>();
                    newStates.put("OnOff", new Variant(false, "b"));
                    lamp.transitionLampState(lightId, newStates, 10);
                } catch (BusException e) {
                    MessageUtils.setUnknownError(response, e.getLocalizedMessage());
                    getContext().sendBroadcast(response);
                    return;
                }

                setResultOK(response);
                getContext().sendBroadcast(response);
            }

            @Override
            public void onSessionFailed(String busName, short port) {
                MessageUtils.setUnknownError(response, "Failed to join session.");
                getContext().sendBroadcast(response);
            }
        };
        OneShotSessionHandler.run(getContext(), service.busName, service.port, callback);
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

    private boolean isSupportingInterfaces(@NonNull AllJoynServiceEntity service,
                                           String... interfaces) {
        if (service.proxyObjects == null) {
            return false;
        }

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

            return LampServiceType.TYPE_LAMP_CONTROLLER;
        } else if (isSupportingInterfaces(service, "org.allseen.LSF.LampState")) {
            // Can control a single lamp.

            return LampServiceType.TYPE_SINGLE_LAMP;
        }

        return LampServiceType.TYPE_UNKNOWN;
    }
}
