package org.deviceconnect.android.deviceplugin.alljoyn.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.Variant;
import org.alljoyn.services.common.BusObjectDescription;
import org.allseen.LSF.ControllerService.Lamp;
import org.allseen.LSF.ControllerService.LampGroup;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AllJoynデバイスプラグイン Light プロファイル.
 *
 * @author NTT DOCOMO, INC.
 */
public class AllJoynLightProfile extends LightProfile {

    private final static int TRANSITION_PERIOD = 10;

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

        if (service == null) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }

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
            public void onSessionJoined(@NonNull String busName, short port, int sessionId) {
                LampState state = app.getInterface(busName, sessionId, LampState.class);

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
            public void onSessionFailed(@NonNull String busName, short port) {
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
            public void onSessionJoined(@NonNull String busName, short port, int sessionId) {
                Lamp lamp = app.getInterface(busName, sessionId, Lamp.class);

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
            public void onSessionFailed(@NonNull String busName, short port) {
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

        if (service == null) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }

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
            public void onSessionJoined(@NonNull String busName, short port, int sessionId) {
                LampState state = app.getInterface(busName, sessionId, LampState.class);
                LampDetails details = app.getInterface(busName, sessionId, LampDetails.class);

                try {
                    HashMap<String, Variant> newStates = new HashMap<>();

                    // NOTE: Arithmetic operations in primitive types may lead to arithmetic
                    // overflow. To retain precision, BigDecimal objects are used.

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

                    int responseCode = state.transitionLampState(0, newStates, TRANSITION_PERIOD);
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
            public void onSessionFailed(@NonNull String busName, short port) {
                MessageUtils.setUnknownError(response, "Failed to join session.");
                getContext().sendBroadcast(response);
            }
        };
        OneShotSessionHandler.run(getContext(), service.busName, service.port, callback);
    }

    private void onPostLightForLampController(Intent request, final Intent response,
                                              AllJoynServiceEntity service, final String lightId,
                                              final Float brightness, final int[] color) {
        final AllJoynDeviceApplication app = getApplication();

        OneShotSessionHandler.SessionJoinCallback callback = new OneShotSessionHandler.SessionJoinCallback() {
            @Override
            public void onSessionJoined(@NonNull String busName, short port, int sessionId) {
                Lamp lamp = app.getInterface(busName, sessionId, Lamp.class);

                try {
                    Lamp.GetLampDetails_return_value_usa_sv lampDetailsResponse =
                            lamp.getLampDetails(lightId);
                    if (lampDetailsResponse == null ||
                            lampDetailsResponse.responseCode != ResponseCode.OK.getValue()) {
                        MessageUtils.setUnknownError(response, "Failed to obtain lamp details.");
                        getContext().sendBroadcast(response);
                        return;
                    }

                    HashMap<String, Variant> newStates = new HashMap<>();

                    // NOTE: Arithmetic operations in primitive types may lead to arithmetic
                    // overflow. To retain precision, BigDecimal objects are used.

                    newStates.put("OnOff", new Variant(true, "b"));
                    if (lampDetailsResponse.lampDetails.containsKey("Color")) {
                        if (lampDetailsResponse.lampDetails.get("Color").getObject(boolean.class)
                                && color != null) {
                            int[] hsb = ColorUtil.convertRGB_8_8_8_To_HSB_32_32_32(color);

                            newStates.put("Hue", new Variant(hsb[0], "u"));
                            newStates.put("Saturation", new Variant(hsb[1], "u"));
                        }
                    } else {
                        Log.w(AllJoynLightProfile.this.getClass().getSimpleName(),
                                "Color support is not described in the lamp details. " +
                                        "Assuming it is not supported...");
                    }
                    if (lampDetailsResponse.lampDetails.containsKey("Dimmable")) {
                        if (lampDetailsResponse.lampDetails.get("Dimmable").getObject(boolean.class)
                                && brightness != null) {
                            // [0, 1] -> [0, 0xffffffff]
                            BigDecimal tmp = BigDecimal.valueOf(0xffffffffl);
                            tmp = tmp.multiply(BigDecimal.valueOf(brightness));
                            long scaledVal = tmp.longValue();
                            int intScaledVal = ByteBuffer.allocate(8).putLong(scaledVal).getInt(4);
                            newStates.put("Brightness", new Variant(intScaledVal, "u"));
                        }
                    } else {
                        Log.w(AllJoynLightProfile.this.getClass().getSimpleName(),
                                "Dim support is not described in the lamp details. " +
                                        "Assuming it is not supported...");
                    }

                    Lamp.TransitionLampState_return_value_us transLampStateResponse =
                            lamp.transitionLampState(lightId, newStates, TRANSITION_PERIOD);
                    if (transLampStateResponse == null ||
                            transLampStateResponse.responseCode != ResponseCode.OK.getValue()) {
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
            public void onSessionFailed(@NonNull String busName, short port) {
                MessageUtils.setUnknownError(response, "Failed to join session.");
                getContext().sendBroadcast(response);
            }
        };
        OneShotSessionHandler.run(getContext(), service.busName, service.port, callback);
    }

    @Override
    protected boolean onDeleteLight(Intent request, Intent response, String serviceId, String lightId) {
        if (serviceId == null || lightId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

        final AllJoynDeviceApplication app = getApplication();
        final AllJoynServiceEntity service = app.getDiscoveredAlljoynServices().get(serviceId);

        if (service == null) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }

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
            public void onSessionJoined(@NonNull String busName, short port, int sessionId) {
                LampState state = app.getInterface(busName, sessionId, LampState.class);

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
            public void onSessionFailed(@NonNull String busName, short port) {
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
            public void onSessionJoined(@NonNull String busName, short port, int sessionId) {
                Lamp lamp = app.getInterface(busName, sessionId, Lamp.class);

                try {
                    Map<String, Variant> newStates = new HashMap<>();
                    newStates.put("OnOff", new Variant(false, "b"));
                    Lamp.TransitionLampState_return_value_us transLampStateResponse =
                            lamp.transitionLampState(lightId, newStates, TRANSITION_PERIOD);
                    if (transLampStateResponse.responseCode != ResponseCode.OK.getValue()) {
                        MessageUtils.setUnknownError(response, "Failed to turn off the light.");
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
            public void onSessionFailed(@NonNull String busName, short port) {
                MessageUtils.setUnknownError(response, "Failed to join session.");
                getContext().sendBroadcast(response);
            }
        };
        OneShotSessionHandler.run(getContext(), service.busName, service.port, callback);
    }

    @Override
    protected boolean onPutLight(Intent request, Intent response, String serviceId, String lightId,
                                 String name, Float brightness, int[] color) {
        if (serviceId == null || lightId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

        final AllJoynDeviceApplication app = getApplication();
        final AllJoynServiceEntity service = app.getDiscoveredAlljoynServices().get(serviceId);

        if (service == null) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }

        switch (getLampServiceType(service)) {
            case TYPE_SINGLE_LAMP: {
                onPutLightForSingleLamp(request, response, service, lightId, name, brightness, color);
                return false;
            }
            case TYPE_LAMP_CONTROLLER: {
                onPutLightForLampController(request, response, service, lightId, name, brightness, color);
                return false;
            }
            case TYPE_UNKNOWN:
            default: {
                setUnsupportedError(response);
                return true;
            }
        }

    }

    // TODO: Implement name change functionality using AllJoyn Config service.
    private void onPutLightForSingleLamp(Intent request, final Intent response,
                                         AllJoynServiceEntity service, String lightId,
                                         String name, final Float brightness, final int[] color) {
        if (lightId == null) {
            MessageUtils.setInvalidRequestParameterError(response,
                    "Parameter 'lightId' must be specified.");
            getContext().sendBroadcast(response);
            return;
        }
        if (name == null) {

        }
        if (brightness != null && (brightness < 0 || brightness > 1)) {
            MessageUtils.setInvalidRequestParameterError(response,
                    "Parameter 'brightness' must be within range [0, 1].");
            getContext().sendBroadcast(response);
            return;
        }
        if (color != null && color.length != 3) {
            MessageUtils.setInvalidRequestParameterError(response,
                    "Parameter 'color' must be a string representing " +
                            "an RGB hexadecimal (e.g. ff0000).");
            getContext().sendBroadcast(response);
            return;
        }

        final AllJoynDeviceApplication app = getApplication();

        OneShotSessionHandler.SessionJoinCallback callback = new OneShotSessionHandler.SessionJoinCallback() {
            @Override
            public void onSessionJoined(@NonNull String busName, short port, int sessionId) {
                LampState state = app.getInterface(busName, sessionId, LampState.class);
                LampDetails details = app.getInterface(busName, sessionId, LampDetails.class);

                try {
                    HashMap<String, Variant> newStates = new HashMap<>();

                    // NOTE: Arithmetic operations in primitive types may lead to arithmetic
                    // overflow. To retain precision, BigDecimal objects are used.

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

                    int responseCode = state.transitionLampState(0, newStates, TRANSITION_PERIOD);
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
            public void onSessionFailed(@NonNull String busName, short port) {
                MessageUtils.setUnknownError(response, "Failed to join session.");
                getContext().sendBroadcast(response);
            }
        };
        OneShotSessionHandler.run(getContext(), service.busName, service.port, callback);
    }

    private void onPutLightForLampController(Intent request, final Intent response,
                                             final AllJoynServiceEntity service, final String lightId,
                                             final String name, final Float brightness, final int[] color) {
        if (lightId == null) {
            MessageUtils.setInvalidRequestParameterError(response,
                    "Parameter 'lightId' must be specified.");
            getContext().sendBroadcast(response);
            return;
        }
        if (brightness != null && (brightness < 0 || brightness > 1)) {
            MessageUtils.setInvalidRequestParameterError(response,
                    "Parameter 'brightness' must be within range [0, 1].");
            getContext().sendBroadcast(response);
            return;
        }
        if (color != null && color.length != 3) {
            MessageUtils.setInvalidRequestParameterError(response,
                    "Parameter 'color' must be a string representing " +
                            "an RGB hexadecimal (e.g. ff0000).");
            getContext().sendBroadcast(response);
            return;
        }

        final AllJoynDeviceApplication app = getApplication();

        OneShotSessionHandler.SessionJoinCallback callback = new OneShotSessionHandler.SessionJoinCallback() {
            @Override
            public void onSessionJoined(@NonNull String busName, short port, int sessionId) {
                Lamp lamp = app.getInterface(busName, sessionId, Lamp.class);

                try {
                    HashMap<String, Variant> newStates = new HashMap<>();

                    // NOTE: Arithmetic operations in primitive types may lead to arithmetic
                    // overflow. To retain precision, BigDecimal objects are used.

                    Lamp.GetLampDetails_return_value_usa_sv lampDetailsResponse =
                            lamp.getLampDetails(lightId);
                    if (lampDetailsResponse.responseCode != ResponseCode.OK.getValue()) {
                        MessageUtils.setUnknownError(response,
                                "Failed to obtain lamp details.");
                        getContext().sendBroadcast(response);
                        return;
                    }

                    if (lampDetailsResponse.lampDetails.containsKey("Color")) {
                        if (lampDetailsResponse.lampDetails.get("Color").getObject(boolean.class)
                                && color != null) {
                            int[] hsb = ColorUtil.convertRGB_8_8_8_To_HSB_32_32_32(color);

                            newStates.put("Hue", new Variant(hsb[0], "u"));
                            newStates.put("Saturation", new Variant(hsb[1], "u"));
                        }
                    }
                    if (lampDetailsResponse.lampDetails.containsKey("Dimmable")) {
                        if (lampDetailsResponse.lampDetails.get("Dimmable").getObject(boolean.class)
                                && brightness != null) {
                            // [0, 1] -> [0, 0xffffffff]
                            BigDecimal tmp = BigDecimal.valueOf(0xffffffffl);
                            tmp = tmp.multiply(BigDecimal.valueOf(brightness));
                            long scaledVal = tmp.longValue();
                            int intScaledVal = ByteBuffer.allocate(8).putLong(scaledVal).getInt(4);
                            newStates.put("Brightness", new Variant(intScaledVal, "u"));
                        }
                    }

                    Lamp.TransitionLampState_return_value_us transitionLampStateResponse =
                            lamp.transitionLampState(lightId, newStates, TRANSITION_PERIOD);
                    if (transitionLampStateResponse.responseCode != ResponseCode.OK.getValue()) {
                        MessageUtils.setUnknownError(response, "Failed to change lamp states.");
                        getContext().sendBroadcast(response);
                        return;
                    }

                    if (name != null) {
                        Lamp.SetLampName_return_value_uss lampNameResponse =
                                lamp.setLampName(lightId, name, service.defaultLanguage);
                        if (lampNameResponse.responseCode != ResponseCode.OK.getValue()) {
                            MessageUtils.setUnknownError(response, "Failed to change name.");
                            getContext().sendBroadcast(response);
                            return;
                        }
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
            public void onSessionFailed(@NonNull String busName, short port) {
                MessageUtils.setUnknownError(response, "Failed to join session.");
                getContext().sendBroadcast(response);
            }
        };
        OneShotSessionHandler.run(getContext(), service.busName, service.port, callback);
    }

    @Override
    protected boolean onGetLightGroup(Intent request, Intent response, String serviceId) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

        final AllJoynDeviceApplication app = getApplication();
        final AllJoynServiceEntity service = app.getDiscoveredAlljoynServices().get(serviceId);

        if (service == null) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }

        switch (getLampServiceType(service)) {

            case TYPE_LAMP_CONTROLLER: {
                onGetLightGroupForLampController(request, response, service);
                return false;
            }

            case TYPE_SINGLE_LAMP:
            case TYPE_UNKNOWN:
            default: {
                setUnsupportedError(response);
                return true;
            }

        }
    }

    private void onGetLightGroupForLampController(Intent request, final Intent response,
                                                  final AllJoynServiceEntity service) {
        final AllJoynDeviceApplication app = getApplication();

        OneShotSessionHandler.SessionJoinCallback callback = new OneShotSessionHandler.SessionJoinCallback() {
            @Override
            public void onSessionJoined(@NonNull String busName, short port, int sessionId) {
                LampGroup proxyLampGroup = app.getInterface(busName, sessionId, LampGroup.class);

                if (proxyLampGroup == null) {
                    MessageUtils.setUnknownError(response,
                            "Failed to obtain a proxy object for org.allseen.LSF.ControllerService.LampGroup .");
                    getContext().sendBroadcast(response);
                    return;
                }

                try {
                    LampGroup.GetAllLampGroupIDs_return_value_uas allLampGroupIDsResponse =
                            proxyLampGroup.getAllLampGroupIDs();
                    if (allLampGroupIDsResponse.responseCode != ResponseCode.OK.getValue()) {
                        MessageUtils.setUnknownError(response,
                                "Failed to obtain lamp group IDs.");
                        getContext().sendBroadcast(response);
                        return;
                    }

                    //////////////////////////////////////////////////
                    // Obtain lamp group info.
                    //
                    HashMap<String, LampGroupInfo> lampGroups = new HashMap<>();
                    for (String lampGroupID : allLampGroupIDsResponse.lampGroupIDs) {
                        LampGroupInfo lampGroupInfo = new LampGroupInfo();

                        lampGroupInfo.ID = lampGroupID;

                        {
                            LampGroup.GetLampGroupName_return_value_usss lampGroupNameResponse =
                                    proxyLampGroup.getLampGroupName(lampGroupID, service.defaultLanguage);
                            if (lampGroupNameResponse.responseCode != ResponseCode.OK.getValue()) {
                                Log.w(AllJoynLightProfile.this.getClass().getSimpleName(),
                                        "Failed to obtain lamp group name. Skipping this lamp group...");
                                continue;
                            }
                            lampGroupInfo.name = lampGroupNameResponse.lampGroupName;
                        }

                        {
                            LampGroup.GetLampGroup_return_value_usasas lampGroupResponse =
                                    proxyLampGroup.getLampGroup(lampGroupID);
                            if (lampGroupResponse.responseCode != ResponseCode.OK.getValue()) {
                                Log.w(AllJoynLightProfile.this.getClass().getSimpleName(),
                                        "Failed to obtain IDs of lamps and lamp groups contained in a lamp group. Skipping this lamp group...");
                                continue;
                            }
                            lampGroupInfo.lampIDs =
                                    new HashSet<>(Arrays.asList(lampGroupResponse.lampID));
                            lampGroupInfo.lampGroupIDs =
                                    new HashSet<>(Arrays.asList(lampGroupResponse.lampGroupIDs));
                        }

                        lampGroupInfo.config = "";

                        lampGroups.put(lampGroupID, lampGroupInfo);
                    }

                    //////////////////////////////////////////////////
                    // Expand lamp IDs contained in lamp groups.
                    //
                    for (LampGroupInfo searchTarget : lampGroups.values()) {
                        for (LampGroupInfo expandTarget : lampGroups.values()) {
                            if (searchTarget.ID.equals(expandTarget.ID)) {
                                continue;
                            }
                            if (expandTarget.lampGroupIDs.contains(searchTarget.ID)) {
                                expandTarget.lampIDs.addAll(searchTarget.lampIDs);
                                expandTarget.lampGroupIDs.addAll(searchTarget.lampGroupIDs);
                                expandTarget.lampGroupIDs.remove(searchTarget.ID);
                            }
                        }
                    }

                    //////////////////////////////////////////////////
                    // Obtain lamp info.
                    //
                    HashMap<String, LampInfo> lamps = new HashMap<>();
                    Lamp proxyLamp = app.getInterface(busName, sessionId, Lamp.class);
                    if (proxyLamp == null) {
                        MessageUtils.setUnknownError(response,
                                "Failed to obtain a proxy object for org.allseen.LSF.ControllerService.Lamp .");
                        getContext().sendBroadcast(response);
                        return;
                    }
                    for (LampGroupInfo lampGroup : lampGroups.values()) {
                        for (String lampID : lampGroup.lampIDs) {
                            if (lamps.containsKey(lampID)) {
                                continue;
                            }

                            LampInfo lamp = new LampInfo();

                            lamp.ID = lampID;

                            {
                                Lamp.GetLampName_return_value_usss lampNameResponse =
                                        proxyLamp.getLampName(lampID, service.defaultLanguage);
                                if (lampNameResponse.responseCode == ResponseCode.OK.getValue()) {
                                    lamp.name = lampNameResponse.lampName;
                                } else {
                                    Log.w(AllJoynLightProfile.this.getClass().getSimpleName(),
                                            "Failed to obtain lamp name...");
                                }
                            }

                            {
                                Lamp.GetLampState_return_value_usa_sv lampStateResponse =
                                        proxyLamp.getLampState(lampID);
                                if (lampStateResponse.responseCode == ResponseCode.OK.getValue()) {
                                    if (lampStateResponse.lampState.containsKey("OnOff")) {
                                        lamp.on = lampStateResponse
                                                .lampState.get("OnOff").getObject(Boolean.class);
                                    } else {
                                        Log.w(AllJoynLightProfile.this.getClass().getSimpleName(),
                                                "Failed to obtain on/off state...");
                                    }
                                } else {
                                    Log.w(AllJoynLightProfile.this.getClass().getSimpleName(),
                                            "Failed to obtain lamp state...");
                                }
                            }

                            lamp.config = "";

                            lamps.put(lampID, lamp);
                        }
                    }

                    List<Bundle> lightGroupsBundle = new ArrayList<>();
                    for (LampGroupInfo lampGroup : lampGroups.values()) {
                        Bundle lightGroupBundle = new Bundle();
                        lightGroupBundle.putString(PARAM_GROUP_ID, lampGroup.ID);
                        lightGroupBundle.putString(PARAM_NAME, lampGroup.name);
                        List<Bundle> lightsBundle = new ArrayList<>();
                        for (String lampID : lampGroup.lampIDs) {
                            LampInfo lamp = lamps.get(lampID);

                            Bundle lightBundle = new Bundle();
                            lightBundle.putString(PARAM_LIGHT_ID, lamp.ID);
                            if (lamp.name != null) {
                                lightBundle.putString(PARAM_NAME, lamp.name);
                            }
                            if (lamp.on != null) {
                                lightBundle.putBoolean(PARAM_ON, lamp.on);
                            }
                            lightBundle.putString(PARAM_CONFIG, lamp.config);
                            lightsBundle.add(lightBundle);
                        }
                        lightGroupBundle.putParcelableArray(PARAM_LIGHTS,
                                lightsBundle.toArray(new Bundle[lightsBundle.size()]));
                        lightGroupBundle.putString(PARAM_CONFIG, lampGroup.config);
                        lightGroupsBundle.add(lightGroupBundle);
                    }

                    response.putExtra(PARAM_LIGHT_GROUPS,
                            lightGroupsBundle.toArray(new Bundle[lightGroupsBundle.size()]));
                    setResultOK(response);
                    getContext().sendBroadcast(response);
                } catch (BusException e) {
                    MessageUtils.setUnknownError(response, e.getLocalizedMessage());
                    getContext().sendBroadcast(response);
                    return;
                }
            }

            @Override
            public void onSessionFailed(@NonNull String busName, short port) {
                MessageUtils.setUnknownError(response, "Failed to join session.");
                getContext().sendBroadcast(response);
            }
        };
        OneShotSessionHandler.run(getContext(), service.busName, service.port, callback);
    }

    @Override
    protected boolean onPostLightGroup(final Intent request, final Intent response,
                                       final String serviceId, final String groupId,
                                       final Float brightness, final int[] color) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

        final AllJoynDeviceApplication app = getApplication();
        final AllJoynServiceEntity service = app.getDiscoveredAlljoynServices().get(serviceId);

        if (service == null) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }

        switch (getLampServiceType(service)) {

            case TYPE_LAMP_CONTROLLER: {
                onPostLightGroupForLampController(request, response, service, groupId
                        , brightness, color);
                return false;
            }

            case TYPE_SINGLE_LAMP:
            case TYPE_UNKNOWN:
            default: {
                setUnsupportedError(response);
                return true;
            }

        }
    }

    private void onPostLightGroupForLampController(Intent request, final Intent response,
                                                   AllJoynServiceEntity service,
                                                   final String groupID, final Float brightness,
                                                   final int[] color) {
        final AllJoynDeviceApplication app = getApplication();

        OneShotSessionHandler.SessionJoinCallback callback = new OneShotSessionHandler.SessionJoinCallback() {
            @Override
            public void onSessionJoined(@NonNull String busName, short port, int sessionId) {
                LampGroup proxyLampGroup = app.getInterface(busName, sessionId, LampGroup.class);

                if (proxyLampGroup == null) {
                    MessageUtils.setUnknownError(response,
                            "Failed to obtain a proxy object for org.allseen.LSF.ControllerService.LampGroup .");
                    getContext().sendBroadcast(response);
                    return;
                }

                try {
                    HashMap<String, Variant> newStates = new HashMap<>();

                    // NOTE: Arithmetic operations in primitive types may lead to arithmetic
                    // overflow. To retain precision, BigDecimal objects are used.

                    newStates.put("OnOff", new Variant(true, "b"));
                    if (color != null) {
                        int[] hsb = ColorUtil.convertRGB_8_8_8_To_HSB_32_32_32(color);

                        newStates.put("Hue", new Variant(hsb[0], "u"));
                        newStates.put("Saturation", new Variant(hsb[1], "u"));
                    }
                    if (brightness != null) {
                        // [0, 1] -> [0, 0xffffffff]
                        BigDecimal tmp = BigDecimal.valueOf(0xffffffffl);
                        tmp = tmp.multiply(BigDecimal.valueOf(brightness));
                        long scaledVal = tmp.longValue();
                        int intScaledVal = ByteBuffer.allocate(8).putLong(scaledVal).getInt(4);
                        newStates.put("Brightness", new Variant(intScaledVal, "u"));
                    }

                    LampGroup.TransitionLampGroupState_return_value_us transLampGroupStateResponse =
                            proxyLampGroup.transitionLampGroupState(groupID, newStates, TRANSITION_PERIOD);
                    if (transLampGroupStateResponse == null ||
                            transLampGroupStateResponse.responseCode != ResponseCode.OK.getValue()) {
                        MessageUtils.setUnknownError(response, "Failed to change lamp group states.");
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
            public void onSessionFailed(@NonNull String busName, short port) {
                MessageUtils.setUnknownError(response, "Failed to join session.");
                getContext().sendBroadcast(response);
            }
        };
        OneShotSessionHandler.run(getContext(), service.busName, service.port, callback);
    }

    @Override
    protected boolean onDeleteLightGroup(Intent request, Intent response, String serviceId, String groupID) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

        final AllJoynDeviceApplication app = getApplication();
        final AllJoynServiceEntity service = app.getDiscoveredAlljoynServices().get(serviceId);

        if (service == null) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }

        switch (getLampServiceType(service)) {

            case TYPE_LAMP_CONTROLLER: {
                onDeleteLightGroupForLampController(request, response, service, groupID);
                return false;
            }

            case TYPE_SINGLE_LAMP:
            case TYPE_UNKNOWN:
            default: {
                setUnsupportedError(response);
                return true;
            }

        }
    }

    private void onDeleteLightGroupForLampController(Intent request, final Intent response
            , AllJoynServiceEntity service, final String groupID) {
        final AllJoynDeviceApplication app = getApplication();

        OneShotSessionHandler.SessionJoinCallback callback = new OneShotSessionHandler.SessionJoinCallback() {
            @Override
            public void onSessionJoined(@NonNull String busName, short port, int sessionId) {
                LampGroup proxy = app.getInterface(busName, sessionId, LampGroup.class);

                try {
                    Map<String, Variant> newStates = new HashMap<>();
                    newStates.put("OnOff", new Variant(false, "b"));
                    LampGroup.TransitionLampGroupState_return_value_us transLampGroupStateResponse =
                            proxy.transitionLampGroupState(groupID, newStates, TRANSITION_PERIOD);
                    if (transLampGroupStateResponse.responseCode != ResponseCode.OK.getValue()) {
                        MessageUtils.setUnknownError(response, "Failed to turn off the light group.");
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
            public void onSessionFailed(@NonNull String busName, short port) {
                MessageUtils.setUnknownError(response, "Failed to join session.");
                getContext().sendBroadcast(response);
            }
        };
        OneShotSessionHandler.run(getContext(), service.busName, service.port, callback);
    }

    @Override
    protected boolean onPutLightGroup(Intent request, Intent response, String serviceId
            , String groupId, String name, Float brightness, int[] color) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

        final AllJoynDeviceApplication app = getApplication();
        final AllJoynServiceEntity service = app.getDiscoveredAlljoynServices().get(serviceId);

        if (service == null) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }

        switch (getLampServiceType(service)) {

            case TYPE_LAMP_CONTROLLER: {
                onPutLightGroupForLampController(request, response, service, groupId, name
                        , brightness, color);
                return false;
            }

            case TYPE_SINGLE_LAMP:
            case TYPE_UNKNOWN:
            default: {
                setUnsupportedError(response);
                return true;
            }

        }
    }

    private void onPutLightGroupForLampController(Intent request, final Intent response
            , final AllJoynServiceEntity service, final String groupID, final String name
            , final Float brightness, final int[] color) {
        final AllJoynDeviceApplication app = getApplication();

        OneShotSessionHandler.SessionJoinCallback callback = new OneShotSessionHandler.SessionJoinCallback() {
            @Override
            public void onSessionJoined(@NonNull String busName, short port, int sessionId) {
                LampGroup proxyLampGroup = app.getInterface(busName, sessionId, LampGroup.class);

                if (proxyLampGroup == null) {
                    MessageUtils.setUnknownError(response,
                            "Failed to obtain a proxy object for org.allseen.LSF.ControllerService.LampGroup .");
                    getContext().sendBroadcast(response);
                    return;
                }

                try {
                    HashMap<String, Variant> newStates = new HashMap<>();

                    // NOTE: Arithmetic operations in primitive types may lead to arithmetic
                    // overflow. To retain precision, BigDecimal objects are used.

                    if (color != null) {
                        int[] hsb = ColorUtil.convertRGB_8_8_8_To_HSB_32_32_32(color);

                        newStates.put("Hue", new Variant(hsb[0], "u"));
                        newStates.put("Saturation", new Variant(hsb[1], "u"));
                    }
                    if (brightness != null) {
                        // [0, 1] -> [0, 0xffffffff]
                        BigDecimal tmp = BigDecimal.valueOf(0xffffffffl);
                        tmp = tmp.multiply(BigDecimal.valueOf(brightness));
                        long scaledVal = tmp.longValue();
                        int intScaledVal = ByteBuffer.allocate(8).putLong(scaledVal).getInt(4);
                        newStates.put("Brightness", new Variant(intScaledVal, "u"));
                    }

                    LampGroup.TransitionLampGroupState_return_value_us transLampGroupStateResponse =
                            proxyLampGroup.transitionLampGroupState(groupID, newStates, TRANSITION_PERIOD);
                    if (transLampGroupStateResponse == null ||
                            transLampGroupStateResponse.responseCode != ResponseCode.OK.getValue()) {
                        MessageUtils.setUnknownError(response, "Failed to change lamp group states.");
                        getContext().sendBroadcast(response);
                        return;
                    }

                    if (name != null) {
                        LampGroup.SetLampGroupName_return_value_uss setLampGroupNameResponse =
                                proxyLampGroup.setLampGroupName(groupID, name, service.defaultLanguage);
                        if (setLampGroupNameResponse.responseCode != ResponseCode.OK.getValue()) {
                            MessageUtils.setUnknownError(response, "Failed to change group name.");
                            getContext().sendBroadcast(response);
                            return;
                        }
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
            public void onSessionFailed(@NonNull String busName, short port) {
                MessageUtils.setUnknownError(response, "Failed to join session.");
                getContext().sendBroadcast(response);
            }
        };
        OneShotSessionHandler.run(getContext(), service.busName, service.port, callback);
    }

    @Override
    protected boolean onPostLightGroupCreate(Intent request, Intent response, String serviceId
            , String[] lightIDs, String groupName) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

        final AllJoynDeviceApplication app = getApplication();
        final AllJoynServiceEntity service = app.getDiscoveredAlljoynServices().get(serviceId);

        if (service == null) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }

        switch (getLampServiceType(service)) {

            case TYPE_LAMP_CONTROLLER: {
                onPostLightGroupCreateForLampController(request, response, service
                        , lightIDs, groupName);
                return false;
            }

            case TYPE_SINGLE_LAMP:
            case TYPE_UNKNOWN:
            default: {
                setUnsupportedError(response);
                return true;
            }

        }
    }

    private void onPostLightGroupCreateForLampController(Intent request, final Intent response
            , final AllJoynServiceEntity service, final String[] lightIDs, final String groupName) {
        final AllJoynDeviceApplication app = getApplication();

        OneShotSessionHandler.SessionJoinCallback callback = new OneShotSessionHandler.SessionJoinCallback() {
            @Override
            public void onSessionJoined(@NonNull String busName, short port, int sessionId) {
                LampGroup proxyLampGroup = app.getInterface(busName, sessionId, LampGroup.class);

                if (proxyLampGroup == null) {
                    MessageUtils.setUnknownError(response,
                            "Failed to obtain a proxy object for org.allseen.LSF.ControllerService.LampGroup .");
                    getContext().sendBroadcast(response);
                    return;
                }

                try {
                    LampGroup.CreateLampGroup_return_value_us createLampGroupResponse =
                            proxyLampGroup.createLampGroup(lightIDs, new String[0], groupName
                                    , service.defaultLanguage);
                    if (createLampGroupResponse.responseCode != ResponseCode.OK.getValue()) {
                        MessageUtils.setUnknownError(response, "Failed to create a light group.");
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
            public void onSessionFailed(@NonNull String busName, short port) {
                MessageUtils.setUnknownError(response, "Failed to join session.");
                getContext().sendBroadcast(response);
            }
        };
        OneShotSessionHandler.run(getContext(), service.busName, service.port, callback);
    }

    @Override
    protected boolean onDeleteLightGroupClear(Intent request, Intent response
            , String serviceId, String groupID) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

        final AllJoynDeviceApplication app = getApplication();
        final AllJoynServiceEntity service = app.getDiscoveredAlljoynServices().get(serviceId);

        if (service == null) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }

        switch (getLampServiceType(service)) {

            case TYPE_LAMP_CONTROLLER: {
                onDeleteLightGroupClearForLampController(request, response, service, groupID);
                return false;
            }

            case TYPE_SINGLE_LAMP:
            case TYPE_UNKNOWN:
            default: {
                setUnsupportedError(response);
                return true;
            }

        }
    }

    private void onDeleteLightGroupClearForLampController(Intent request, final Intent response
            , final AllJoynServiceEntity service, final String groupID) {
        final AllJoynDeviceApplication app = getApplication();

        OneShotSessionHandler.SessionJoinCallback callback = new OneShotSessionHandler.SessionJoinCallback() {
            @Override
            public void onSessionJoined(@NonNull String busName, short port, int sessionId) {
                LampGroup proxyLampGroup = app.getInterface(busName, sessionId, LampGroup.class);

                if (proxyLampGroup == null) {
                    MessageUtils.setUnknownError(response,
                            "Failed to obtain a proxy object for org.allseen.LSF.ControllerService.LampGroup .");
                    getContext().sendBroadcast(response);
                    return;
                }

                try {
                    LampGroup.DeleteLampGroup_return_value_us deleteLampGroupResponse =
                            proxyLampGroup.deleteLampGroup(groupID);
                    if (deleteLampGroupResponse.responseCode != ResponseCode.OK.getValue()) {
                        MessageUtils.setUnknownError(response, "Failed to delete the light group.");
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
            public void onSessionFailed(@NonNull String busName, short port) {
                MessageUtils.setUnknownError(response, "Failed to join session.");
                getContext().sendBroadcast(response);
            }
        };
        OneShotSessionHandler.run(getContext(), service.busName, service.port, callback);
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
        if (interfaces == null || interfaces.length == 0
                || service.proxyObjects == null) {
            return false;
        }

        for (String ifaceCheck : interfaces) {
            boolean found = false;
            for (BusObjectDescription busObject : service.proxyObjects) {
                if (Arrays.asList(busObject.interfaces).contains(ifaceCheck)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    private LampServiceType getLampServiceType(@NonNull AllJoynServiceEntity service) {
        if (isSupportingInterfaces(service, "org.allseen.LSF.ControllerService.Lamp")) {
            // Can manage and control multiple lamps.

            return LampServiceType.TYPE_LAMP_CONTROLLER;
        } else if (isSupportingInterfaces(service, "org.allseen.LSF.LampState")) {
            // Can control a single lamp.

            return LampServiceType.TYPE_SINGLE_LAMP;
        }

        return LampServiceType.TYPE_UNKNOWN;
    }

    private static class LampGroupInfo {
        public String ID;
        public String name;
        public Set<String> lampIDs;
        public Set<String> lampGroupIDs;
        public String config;
    }

    private static class LampInfo {
        public String ID;
        public String name;
        public Boolean on;
        public String config;
    }

}
