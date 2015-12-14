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
import org.deviceconnect.android.deviceplugin.alljoyn.BuildConfig;
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
 * Light profile for AllJoyn.
 *
 * @author NTT DOCOMO, INC.
 */
public class AllJoynLightProfile extends LightProfile {

    private final static int TRANSITION_PERIOD = 10;

    // TODO: Use property LampID in org.allseen.LSF.LampDetails instead.
    private static final String LIGHT_ID_SELF = "self";

    /**
     * RGBの文字列の長さ.
     */
    private static final int RGB_LENGTH = 6;

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

    private void onGetLightForSingleLamp(@NonNull Intent request, @NonNull final Intent response
            , @NonNull final AllJoynServiceEntity service) {
        final AllJoynDeviceApplication app = getApplication();

        OneShotSessionHandler.SessionJoinCallback callback = new OneShotSessionHandler.SessionJoinCallback() {
            @Override
            public void onSessionJoined(@NonNull String busName, short port, int sessionId) {
                LampState proxy = app.getInterface(busName, sessionId, LampState.class);

                if (proxy == null) {
                    MessageUtils.setUnknownError(response,
                            "Failed to obtain a proxy object for org.allseen.LSF.LampState .");
                    sendResponse(response);
                    return;
                }

                List<Bundle> lights = new ArrayList<>();
                Bundle light = new Bundle();
                try {
                    light.putString(PARAM_LIGHT_ID, LIGHT_ID_SELF);
                    light.putString(PARAM_NAME, service.serviceName);
                    light.putString(PARAM_CONFIG, "");
                    light.putBoolean(PARAM_ON, proxy.getOnOff());
                    lights.add(light);
                } catch (BusException e) {
                    MessageUtils.setUnknownError(response, e.getLocalizedMessage());
                    sendResponse(response);
                    return;
                }
                response.putExtra(PARAM_LIGHTS, lights.toArray(new Bundle[lights.size()]));
                setResultOK(response);
                sendResponse(response);
            }

            @Override
            public void onSessionFailed(@NonNull String busName, short port) {
                MessageUtils.setUnknownError(response, "Failed to join session.");
                sendResponse(response);
            }
        };
        OneShotSessionHandler.run(getContext(), service.busName, service.port, callback);
    }

    private void onGetLightForLampController(@NonNull Intent request, @NonNull final Intent response
            , @NonNull final AllJoynServiceEntity service) {
        final AllJoynDeviceApplication app = getApplication();

        OneShotSessionHandler.SessionJoinCallback callback = new OneShotSessionHandler.SessionJoinCallback() {
            @Override
            public void onSessionJoined(@NonNull String busName, short port, int sessionId) {
                Lamp proxy = app.getInterface(busName, sessionId, Lamp.class);

                if (proxy == null) {
                    MessageUtils.setUnknownError(response,
                            "Failed to obtain a proxy object for org.allseen.LSF.ControllerService.Lamp .");
                    sendResponse(response);
                    return;
                }

                List<Bundle> lights = new ArrayList<>();
                try {
                    Lamp.GetAllLampIDs_return_value_uas
                            lampIDsResponse = proxy.getAllLampIDs();
                    if (lampIDsResponse.responseCode != ResponseCode.OK.getValue()) {
                        MessageUtils.setUnknownError(response,
                                "Failed to obtain lamp IDs (code: " + lampIDsResponse.responseCode + ").");
                        sendResponse(response);
                        return;
                    }
                    for (String lampId : lampIDsResponse.lampIDs) {
                        Bundle light = new Bundle();
                        light.putString(PARAM_LIGHT_ID, lampId);

                        Lamp.GetLampName_return_value_usss lampNameResponse =
                                proxy.getLampName(lampId, service.defaultLanguage);
                        if (lampNameResponse.responseCode != ResponseCode.OK.getValue()) {
                            if (BuildConfig.DEBUG) {
                                Log.w(AllJoynLightProfile.this.getClass().getSimpleName(),
                                        "Failed to obtain the lamp name (code: "
                                                + lampNameResponse.responseCode + "). Skipping this lamp...");
                            }
                            continue;
                        }

                        Lamp.GetLampState_return_value_usa_sv lampStateResponse =
                                proxy.getLampState(lampId);
                        if (lampStateResponse.responseCode != ResponseCode.OK.getValue()) {
                            if (BuildConfig.DEBUG) {
                                Log.w(AllJoynLightProfile.this.getClass().getSimpleName(),
                                        "Failed to obtain the on/off state (code: "
                                                + lampStateResponse.responseCode + "). Skipping this lamp...");
                            }
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
                    sendResponse(response);
                    return;
                }
                response.putExtra(PARAM_LIGHTS, lights.toArray(new Bundle[lights.size()]));
                setResultOK(response);
                sendResponse(response);
            }

            @Override
            public void onSessionFailed(@NonNull String busName, short port) {
                MessageUtils.setUnknownError(response, "Failed to join session.");
                sendResponse(response);
            }
        };
        OneShotSessionHandler.run(getContext(), service.busName, service.port, callback);
    }

    @Override
    protected boolean onPostLight(final Intent request, final Intent response, final String serviceId,
                                  final String lightId, final Integer color, final Double brightness,
                                  final long[] flashing) {
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

        if (lightId == null) {
            MessageUtils.setInvalidRequestParameterError(response,
                    "Parameter 'lightId' must be specified.");
            return true;
        }

        if (flashing != null) {
            MessageUtils.setNotSupportActionError(response
                    , "Parameter 'flashing' is not supported.");
            return true;
        }
        int[] colors = new int[3];
        String colorParam = getColorString(request);
        if (colorParam != null) {
            if (!parseColorParam(colorParam, colors)) {
                MessageUtils.setInvalidRequestParameterError(response,
                        "Parameter 'color' is invalid.");
                return true;
            }
        }
        switch (getLampServiceType(service)) {
            case TYPE_SINGLE_LAMP: {
                onPostLightForSingleLamp(request, response, service, lightId, brightness, colors);
                return false;
            }
            case TYPE_LAMP_CONTROLLER: {
                onPostLightForLampController(request, response, service, lightId, brightness, colors);
                return false;
            }
            case TYPE_UNKNOWN:
            default: {
                setUnsupportedError(response);
                return true;
            }
        }
    }

    private void onPostLightForSingleLamp(@NonNull Intent request, @NonNull final Intent response
            , @NonNull final AllJoynServiceEntity service, @NonNull String lightId
            , final Double brightness, final int[] color) {
        final AllJoynDeviceApplication app = getApplication();

        if (!lightId.equals(LIGHT_ID_SELF)) {
            MessageUtils.setInvalidRequestParameterError(response,
                    "A light with ID specified by 'lightId' not found.");
            sendResponse(response);
            return;
        }

        OneShotSessionHandler.SessionJoinCallback callback = new OneShotSessionHandler.SessionJoinCallback() {
            @Override
            public void onSessionJoined(@NonNull String busName, short port, int sessionId) {
                LampState proxyState = app.getInterface(busName, sessionId, LampState.class);
                LampDetails proxyDetails = app.getInterface(busName, sessionId, LampDetails.class);

                if (proxyState == null) {
                    MessageUtils.setUnknownError(response,
                            "Failed to obtain a proxy object for org.allseen.LSF.LampState .");
                    sendResponse(response);
                    return;
                }
                if (proxyDetails == null) {
                    MessageUtils.setUnknownError(response,
                            "Failed to obtain a proxy object for org.allseen.LSF.LampDetails .");
                    sendResponse(response);
                    return;
                }

                try {
                    HashMap<String, Variant> newStates = new HashMap<>();

                    // NOTE: Arithmetic operations in primitive types may lead to arithmetic
                    // overflow. To retain precision, BigDecimal objects are used.

                    newStates.put("OnOff", new Variant(true, "b"));
                    if (proxyDetails.getColor() && color != null) {
                        int[] hsb = ColorUtil.convertRGB_8_8_8_To_HSB_32_32_32(color);

                        newStates.put("Hue", new Variant(hsb[0], "u"));
                        newStates.put("Saturation", new Variant(hsb[1], "u"));
                    }
                    if (proxyDetails.getDimmable() && brightness != null) {
                        // [0, 1] -> [0, 0xffffffff]
                        BigDecimal tmp = BigDecimal.valueOf(0xffffffffl);
                        tmp = tmp.multiply(BigDecimal.valueOf(brightness));
                        long scaledVal = tmp.longValue();
                        int intScaledVal = ByteBuffer.allocate(8).putLong(scaledVal).getInt(4);
                        newStates.put("Brightness", new Variant(intScaledVal, "u"));
                    }

                    int responseCode = proxyState.transitionLampState(0, newStates, TRANSITION_PERIOD);
                    if (responseCode != ResponseCode.OK.getValue()) {
                        MessageUtils.setUnknownError(response,
                                "Failed to change lamp states (code: " + responseCode + ").");
                        sendResponse(response);
                        return;
                    }
                } catch (BusException e) {
                    MessageUtils.setUnknownError(response, e.getLocalizedMessage());
                    sendResponse(response);
                    return;
                }

                setResultOK(response);
                sendResponse(response);
            }

            @Override
            public void onSessionFailed(@NonNull String busName, short port) {
                MessageUtils.setUnknownError(response, "Failed to join session.");
                sendResponse(response);
            }
        };
        OneShotSessionHandler.run(getContext(), service.busName, service.port, callback);
    }

    private void onPostLightForLampController(@NonNull Intent request, @NonNull final Intent response
            , @NonNull AllJoynServiceEntity service, @NonNull final String lightId
            , final Double brightness, final int[] color) {
        final AllJoynDeviceApplication app = getApplication();

        OneShotSessionHandler.SessionJoinCallback callback = new OneShotSessionHandler.SessionJoinCallback() {
            @Override
            public void onSessionJoined(@NonNull String busName, short port, int sessionId) {
                Lamp proxy = app.getInterface(busName, sessionId, Lamp.class);

                if (proxy == null) {
                    MessageUtils.setUnknownError(response,
                            "Failed to obtain a proxy object for org.allseen.LSF.ControllerService.Lamp .");
                    sendResponse(response);
                    return;
                }

                try {
                    Lamp.GetAllLampIDs_return_value_uas getAllLampIDsResponse =
                            proxy.getAllLampIDs();
                    if (getAllLampIDsResponse == null) {
                        MessageUtils.setUnknownError(response, "Failed to obtain lamp IDs.");
                        sendResponse(response);
                        return;
                    } else if (getAllLampIDsResponse.responseCode != ResponseCode.OK.getValue()) {
                        MessageUtils.setUnknownError(response,
                                "Failed to obtain lamp IDs (code: "
                                        + getAllLampIDsResponse.responseCode + ").");
                        sendResponse(response);
                        return;
                    }
                    if (!Arrays.asList(getAllLampIDsResponse.lampIDs).contains(lightId)) {
                        MessageUtils.setInvalidRequestParameterError(response,
                                "A light with ID specified by 'lightId' not found.");
                        sendResponse(response);
                        return;
                    }

                    Lamp.GetLampDetails_return_value_usa_sv lampDetailsResponse =
                            proxy.getLampDetails(lightId);
                    if (lampDetailsResponse == null) {
                        MessageUtils.setUnknownError(response,
                                "Failed to obtain lamp details.");
                        sendResponse(response);
                        return;
                    } else if (lampDetailsResponse.responseCode != ResponseCode.OK.getValue()) {
                        MessageUtils.setUnknownError(response,
                                "Failed to obtain lamp details (code: " + lampDetailsResponse.responseCode + ").");
                        sendResponse(response);
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
                        if (BuildConfig.DEBUG) {
                            Log.w(AllJoynLightProfile.this.getClass().getSimpleName(),
                                    "Color support is not described in the lamp details. " +
                                            "Assuming it is not supported...");
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
                    } else {
                        if (BuildConfig.DEBUG) {
                            Log.w(AllJoynLightProfile.this.getClass().getSimpleName(),
                                    "Dim support is not described in the lamp details. " +
                                            "Assuming it is not supported...");
                        }
                    }

                    Lamp.TransitionLampState_return_value_us transLampStateResponse =
                            proxy.transitionLampState(lightId, newStates, TRANSITION_PERIOD);
                    if (transLampStateResponse == null ||
                            transLampStateResponse.responseCode != ResponseCode.OK.getValue()) {
                        MessageUtils.setUnknownError(response,
                                "Failed to change lamp states (code: " + transLampStateResponse.responseCode + ").");
                        sendResponse(response);
                        return;
                    }
                } catch (BusException e) {
                    MessageUtils.setUnknownError(response, e.getLocalizedMessage());
                    sendResponse(response);
                    return;
                }

                setResultOK(response);
                sendResponse(response);
            }

            @Override
            public void onSessionFailed(@NonNull String busName, short port) {
                MessageUtils.setUnknownError(response, "Failed to join session.");
                sendResponse(response);
            }
        };
        OneShotSessionHandler.run(getContext(), service.busName, service.port, callback);
    }

    @Override
    protected boolean onDeleteLight(Intent request, Intent response, String serviceId, String lightId) {
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

        if (lightId == null) {
            MessageUtils.setInvalidRequestParameterError(response,
                    "Parameter 'lightId' must be specified.");
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

    private void onDeleteLightForSingleLamp(@NonNull Intent request, @NonNull final Intent response
            , @NonNull final AllJoynServiceEntity service, @NonNull String lightId) {
        final AllJoynDeviceApplication app = getApplication();

        if (!lightId.equals(LIGHT_ID_SELF)) {
            MessageUtils.setInvalidRequestParameterError(response,
                    "A light with ID specified by 'lightId' not found.");
            sendResponse(response);
            return;
        }

        OneShotSessionHandler.SessionJoinCallback callback = new OneShotSessionHandler.SessionJoinCallback() {
            @Override
            public void onSessionJoined(@NonNull String busName, short port, int sessionId) {
                LampState proxy = app.getInterface(busName, sessionId, LampState.class);

                if (proxy == null) {
                    MessageUtils.setUnknownError(response,
                            "Failed to obtain a proxy object for org.allseen.LSF.LampState .");
                    sendResponse(response);
                    return;
                }

                try {
                    proxy.setOnOff(false);
                } catch (BusException e) {
                    MessageUtils.setUnknownError(response, e.getLocalizedMessage());
                    sendResponse(response);
                    return;
                }

                setResultOK(response);
                sendResponse(response);
            }

            @Override
            public void onSessionFailed(@NonNull String busName, short port) {
                MessageUtils.setUnknownError(response, "Failed to join session.");
                sendResponse(response);
            }
        };
        OneShotSessionHandler.run(getContext(), service.busName, service.port, callback);
    }

    private void onDeleteLightForLampController(@NonNull Intent request
            , @NonNull final Intent response, @NonNull final AllJoynServiceEntity service
            , @NonNull final String lightId) {
        final AllJoynDeviceApplication app = getApplication();

        OneShotSessionHandler.SessionJoinCallback callback = new OneShotSessionHandler.SessionJoinCallback() {
            @Override
            public void onSessionJoined(@NonNull String busName, short port, int sessionId) {
                Lamp proxy = app.getInterface(busName, sessionId, Lamp.class);

                if (proxy == null) {
                    MessageUtils.setUnknownError(response,
                            "Failed to obtain a proxy object for org.allseen.LSF.ControllerService.Lamp .");
                    sendResponse(response);
                    return;
                }

                try {
                    Lamp.GetAllLampIDs_return_value_uas getAllLampIDsResponse =
                            proxy.getAllLampIDs();
                    if (getAllLampIDsResponse == null) {
                        MessageUtils.setUnknownError(response, "Failed to obtain lamp IDs.");
                        sendResponse(response);
                        return;
                    } else if (getAllLampIDsResponse.responseCode != ResponseCode.OK.getValue()) {
                        MessageUtils.setUnknownError(response,
                                "Failed to obtain lamp IDs (code: "
                                        + getAllLampIDsResponse.responseCode + ").");
                        sendResponse(response);
                        return;
                    }
                    if (!Arrays.asList(getAllLampIDsResponse.lampIDs).contains(lightId)) {
                        MessageUtils.setInvalidRequestParameterError(response,
                                "A light with ID specified by 'lightId' not found.");
                        sendResponse(response);
                        return;
                    }

                    Map<String, Variant> newStates = new HashMap<>();
                    newStates.put("OnOff", new Variant(false, "b"));
                    Lamp.TransitionLampState_return_value_us transLampStateResponse =
                            proxy.transitionLampState(lightId, newStates, TRANSITION_PERIOD);
                    if (transLampStateResponse.responseCode != ResponseCode.OK.getValue()) {
                        MessageUtils.setUnknownError(response,
                                "Failed to turn off the light (code: " + transLampStateResponse.responseCode + ").");
                        sendResponse(response);
                        return;
                    }
                } catch (BusException e) {
                    MessageUtils.setUnknownError(response, e.getLocalizedMessage());
                    sendResponse(response);
                    return;
                }

                setResultOK(response);
                sendResponse(response);
            }

            @Override
            public void onSessionFailed(@NonNull String busName, short port) {
                MessageUtils.setUnknownError(response, "Failed to join session.");
                sendResponse(response);
            }
        };
        OneShotSessionHandler.run(getContext(), service.busName, service.port, callback);
    }

    @Override
    protected boolean onPutLight(final Intent request, final Intent response, final String serviceId,
                                 final String lightId, final String name, final Integer color,
                                 final Double brightness, final long[] flashing) {
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

        if (lightId == null) {
            MessageUtils.setInvalidRequestParameterError(response,
                    "Parameter 'lightId' must be specified.");
            return true;
        }

        if (flashing != null) {
            MessageUtils.setNotSupportActionError(response
                    , "Parameter 'flashing' is not supported.");
            return true;
        }
        int[] colors = new int[3];
        String colorParam = getColorString(request);
        if (colorParam != null) {
            if (!parseColorParam(colorParam, colors)) {
                MessageUtils.setInvalidRequestParameterError(response,
                        "Parameter 'color' is invalid.");
                return true;
            }
        }
        switch (getLampServiceType(service)) {
            case TYPE_SINGLE_LAMP: {
                onPutLightForSingleLamp(request, response, service, lightId, name, brightness, colors);
                return false;
            }
            case TYPE_LAMP_CONTROLLER: {
                onPutLightForLampController(request, response, service, lightId, name, brightness, colors);
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
    private void onPutLightForSingleLamp(@NonNull Intent request, @NonNull final Intent response
            , @NonNull AllJoynServiceEntity service, @NonNull String lightId, String name
            , final Double brightness, final int[] color) {
        if (!lightId.equals(LIGHT_ID_SELF)) {
            MessageUtils.setInvalidRequestParameterError(response,
                    "A light with ID specified by 'lightId' not found.");
            sendResponse(response);
            return;
        }
        if (name == null) {

        }
        if (brightness != null && (brightness < 0 || brightness > 1)) {
            MessageUtils.setInvalidRequestParameterError(response,
                    "Parameter 'brightness' must be within range [0, 1].");
            sendResponse(response);
            return;
        }
        if (color != null && color.length != 3) {
            MessageUtils.setInvalidRequestParameterError(response,
                    "Parameter 'color' must be a string representing " +
                            "an RGB hexadecimal (e.g. ff0000).");
            sendResponse(response);
            return;
        }

        final AllJoynDeviceApplication app = getApplication();

        OneShotSessionHandler.SessionJoinCallback callback = new OneShotSessionHandler.SessionJoinCallback() {
            @Override
            public void onSessionJoined(@NonNull String busName, short port, int sessionId) {
                LampState proxyState = app.getInterface(busName, sessionId, LampState.class);
                LampDetails proxyDetails = app.getInterface(busName, sessionId, LampDetails.class);

                if (proxyState == null) {
                    MessageUtils.setUnknownError(response,
                            "Failed to obtain a proxy object for org.allseen.LSF.LampState .");
                    sendResponse(response);
                    return;
                }
                if (proxyDetails == null) {
                    MessageUtils.setUnknownError(response,
                            "Failed to obtain a proxy object for org.allseen.LSF.LampDetails .");
                    sendResponse(response);
                    return;
                }

                try {
                    HashMap<String, Variant> newStates = new HashMap<>();

                    // NOTE: Arithmetic operations in primitive types may lead to arithmetic
                    // overflow. To retain precision, BigDecimal objects are used.

                    if (proxyDetails.getColor() && color != null) {
                        int[] hsb = ColorUtil.convertRGB_8_8_8_To_HSB_32_32_32(color);

                        newStates.put("Hue", new Variant(hsb[0], "u"));
                        newStates.put("Saturation", new Variant(hsb[1], "u"));
                    }
                    if (proxyDetails.getDimmable() && brightness != null) {
                        // [0, 1] -> [0, 0xffffffff]
                        BigDecimal tmp = BigDecimal.valueOf(0xffffffffl);
                        tmp = tmp.multiply(BigDecimal.valueOf(brightness));
                        long scaledVal = tmp.longValue();
                        int intScaledVal = ByteBuffer.allocate(8).putLong(scaledVal).getInt(4);
                        newStates.put("Brightness", new Variant(intScaledVal, "u"));
                    }

                    int responseCode = proxyState.transitionLampState(0, newStates, TRANSITION_PERIOD);
                    if (responseCode != ResponseCode.OK.getValue()) {
                        MessageUtils.setUnknownError(response,
                                "Failed to change lamp states (code: "
                                        + responseCode + ").");
                        sendResponse(response);
                        return;
                    }
                } catch (BusException e) {
                    MessageUtils.setUnknownError(response, e.getLocalizedMessage());
                    sendResponse(response);
                    return;
                }

                setResultOK(response);
                sendResponse(response);
            }

            @Override
            public void onSessionFailed(@NonNull String busName, short port) {
                MessageUtils.setUnknownError(response, "Failed to join session.");
                sendResponse(response);
            }
        };
        OneShotSessionHandler.run(getContext(), service.busName, service.port, callback);
    }

    private void onPutLightForLampController(@NonNull Intent request, @NonNull final Intent response
            , @NonNull final AllJoynServiceEntity service, @NonNull final String lightId
            , final String name, final Double brightness, final int[] color) {
        if (brightness != null && (brightness < 0 || brightness > 1)) {
            MessageUtils.setInvalidRequestParameterError(response,
                    "Parameter 'brightness' must be within range [0, 1].");
            sendResponse(response);
            return;
        }
        if (color != null && color.length != 3) {
            MessageUtils.setInvalidRequestParameterError(response,
                    "Parameter 'color' must be a string representing " +
                            "an RGB hexadecimal (e.g. ff0000).");
            sendResponse(response);
            return;
        }

        final AllJoynDeviceApplication app = getApplication();

        OneShotSessionHandler.SessionJoinCallback callback = new OneShotSessionHandler.SessionJoinCallback() {
            @Override
            public void onSessionJoined(@NonNull String busName, short port, int sessionId) {
                Lamp proxy = app.getInterface(busName, sessionId, Lamp.class);

                if (proxy == null) {
                    MessageUtils.setUnknownError(response,
                            "Failed to obtain a proxy object for org.allseen.LSF.ControllerService.Lamp .");
                    sendResponse(response);
                    return;
                }

                try {
                    Lamp.GetAllLampIDs_return_value_uas getAllLampIDsResponse =
                            proxy.getAllLampIDs();
                    if (getAllLampIDsResponse == null) {
                        MessageUtils.setUnknownError(response, "Failed to obtain lamp IDs.");
                        sendResponse(response);
                        return;
                    } else if (getAllLampIDsResponse.responseCode != ResponseCode.OK.getValue()) {
                        MessageUtils.setUnknownError(response,
                                "Failed to obtain lamp IDs (code: "
                                        + getAllLampIDsResponse.responseCode + ").");
                        sendResponse(response);
                        return;
                    }
                    if (!Arrays.asList(getAllLampIDsResponse.lampIDs).contains(lightId)) {
                        MessageUtils.setInvalidRequestParameterError(response,
                                "A light with ID specified by 'lightId' not found.");
                        sendResponse(response);
                        return;
                    }

                    HashMap<String, Variant> newStates = new HashMap<>();

                    // NOTE: Arithmetic operations in primitive types may lead to arithmetic
                    // overflow. To retain precision, BigDecimal objects are used.

                    Lamp.GetLampDetails_return_value_usa_sv lampDetailsResponse =
                            proxy.getLampDetails(lightId);
                    if (lampDetailsResponse.responseCode != ResponseCode.OK.getValue()) {
                        MessageUtils.setUnknownError(response,
                                "Failed to obtain lamp details (code: " + lampDetailsResponse.responseCode + ").");
                        sendResponse(response);
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

                    Lamp.TransitionLampState_return_value_us transLampStateResponse =
                            proxy.transitionLampState(lightId, newStates, TRANSITION_PERIOD);
                    if (transLampStateResponse.responseCode != ResponseCode.OK.getValue()) {
                        MessageUtils.setUnknownError(response,
                                "Failed to change lamp states (code: " + transLampStateResponse.responseCode + ").");
                        sendResponse(response);
                        return;
                    }

                    if (name != null) {
                        Lamp.SetLampName_return_value_uss lampNameResponse =
                                proxy.setLampName(lightId, name, service.defaultLanguage);
                        if (lampNameResponse.responseCode != ResponseCode.OK.getValue()) {
                            MessageUtils.setUnknownError(response,
                                    "Failed to change name (code: " + lampNameResponse.responseCode + ").");
                            sendResponse(response);
                            return;
                        }
                    }
                } catch (BusException e) {
                    MessageUtils.setUnknownError(response, e.getLocalizedMessage());
                    sendResponse(response);
                    return;
                }

                setResultOK(response);
                sendResponse(response);
            }

            @Override
            public void onSessionFailed(@NonNull String busName, short port) {
                MessageUtils.setUnknownError(response, "Failed to join session.");
                sendResponse(response);
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

    private void onGetLightGroupForLampController(@NonNull Intent request
            , @NonNull final Intent response, @NonNull final AllJoynServiceEntity service) {
        final AllJoynDeviceApplication app = getApplication();

        OneShotSessionHandler.SessionJoinCallback callback = new OneShotSessionHandler.SessionJoinCallback() {
            @Override
            public void onSessionJoined(@NonNull String busName, short port, int sessionId) {
                LampGroup proxyLampGroup = app.getInterface(busName, sessionId, LampGroup.class);

                if (proxyLampGroup == null) {
                    MessageUtils.setUnknownError(response,
                            "Failed to obtain a proxy object for org.allseen.LSF.ControllerService.LampGroup .");
                    sendResponse(response);
                    return;
                }

                try {
                    LampGroup.GetAllLampGroupIDs_return_value_uas allLampGroupIDsResponse =
                            proxyLampGroup.getAllLampGroupIDs();
                    if (allLampGroupIDsResponse.responseCode != ResponseCode.OK.getValue()) {
                        MessageUtils.setUnknownError(response,
                                "Failed to obtain lamp group IDs (code: " + allLampGroupIDsResponse.responseCode + ").");
                        sendResponse(response);
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
                                if (BuildConfig.DEBUG) {
                                    Log.w(AllJoynLightProfile.this.getClass().getSimpleName(),
                                            "Failed to obtain lamp group name (code: "
                                                    + lampGroupNameResponse.responseCode + "). Skipping this lamp group...");
                                }
                                continue;
                            }
                            lampGroupInfo.name = lampGroupNameResponse.lampGroupName;
                        }

                        {
                            LampGroup.GetLampGroup_return_value_usasas lampGroupResponse =
                                    proxyLampGroup.getLampGroup(lampGroupID);
                            if (lampGroupResponse.responseCode != ResponseCode.OK.getValue()) {
                                if (BuildConfig.DEBUG) {
                                    Log.w(AllJoynLightProfile.this.getClass().getSimpleName(),
                                            "Failed to obtain IDs of lamps and lamp groups contained"
                                                    + " in a lamp group (code: " + lampGroupResponse.responseCode
                                                    + "). Skipping this lamp group...");
                                }
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
                        sendResponse(response);
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
                                    if (BuildConfig.DEBUG) {
                                        Log.w(AllJoynLightProfile.this.getClass().getSimpleName(),
                                                "Failed to obtain lamp name (code: " + lampNameResponse.responseCode + ").");
                                    }
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
                                        if (BuildConfig.DEBUG) {
                                            Log.w(AllJoynLightProfile.this.getClass().getSimpleName(),
                                                    "Failed to obtain on/off state...");
                                        }
                                    }
                                } else {
                                    if (BuildConfig.DEBUG) {
                                        Log.w(AllJoynLightProfile.this.getClass().getSimpleName(),
                                                "Failed to obtain lamp state (code: " + lampStateResponse.responseCode + ").");
                                    }
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
                    sendResponse(response);
                } catch (BusException e) {
                    MessageUtils.setUnknownError(response, e.getLocalizedMessage());
                    sendResponse(response);
                    return;
                }
            }

            @Override
            public void onSessionFailed(@NonNull String busName, short port) {
                MessageUtils.setUnknownError(response, "Failed to join session.");
                sendResponse(response);
            }
        };
        OneShotSessionHandler.run(getContext(), service.busName, service.port, callback);
    }

    @Override
    protected boolean onPostLightGroup(final Intent request, final Intent response, final String serviceId,
                                       final String groupId, final Integer color, final Double brightness,
                                       final long[] flashing) {
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

        if (groupId == null) {
            MessageUtils.setInvalidRequestParameterError(response,
                    "Parameter 'groupId' must be specified.");
            return true;
        }

        if (flashing != null) {
            MessageUtils.setNotSupportActionError(response
                    , "Parameter 'flashing' is not supported.");
            return true;
        }
        int[] colors = new int[3];
        String colorParam = getColorString(request);
        if (colorParam != null) {
            if (!parseColorParam(colorParam, colors)) {
                MessageUtils.setInvalidRequestParameterError(response,
                        "Parameter 'color' is invalid.");
                return true;
            }
        }
        switch (getLampServiceType(service)) {

            case TYPE_LAMP_CONTROLLER: {
                onPostLightGroupForLampController(request, response, service, groupId
                        , brightness, colors);
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

    private void onPostLightGroupForLampController(@NonNull Intent request
            , @NonNull final Intent response, @NonNull AllJoynServiceEntity service
            , @NonNull final String groupID, final Double brightness, final int[] color) {
        final AllJoynDeviceApplication app = getApplication();

        OneShotSessionHandler.SessionJoinCallback callback = new OneShotSessionHandler.SessionJoinCallback() {
            @Override
            public void onSessionJoined(@NonNull String busName, short port, int sessionId) {
                LampGroup proxy = app.getInterface(busName, sessionId, LampGroup.class);

                if (proxy == null) {
                    MessageUtils.setUnknownError(response,
                            "Failed to obtain a proxy object for org.allseen.LSF.ControllerService.LampGroup .");
                    sendResponse(response);
                    return;
                }

                try {
                    LampGroup.GetAllLampGroupIDs_return_value_uas getAllLampGroupIDsResponse =
                            proxy.getAllLampGroupIDs();
                    if (getAllLampGroupIDsResponse == null) {
                        MessageUtils.setUnknownError(response, "Failed to obtain lamp group IDs.");
                        sendResponse(response);
                        return;
                    } else if (getAllLampGroupIDsResponse.responseCode != ResponseCode.OK.getValue()) {
                        MessageUtils.setUnknownError(response,
                                "Failed to obtain lamp IDs (code: "
                                        + getAllLampGroupIDsResponse.responseCode + ").");
                        sendResponse(response);
                        return;
                    }
                    if (!Arrays.asList(getAllLampGroupIDsResponse.lampGroupIDs).contains(groupID)) {
                        MessageUtils.setInvalidRequestParameterError(response,
                                "A light group with ID specified by 'groupId' not found.");
                        sendResponse(response);
                        return;
                    }

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
                            proxy.transitionLampGroupState(groupID, newStates, TRANSITION_PERIOD);
                    if (transLampGroupStateResponse == null) {
                        MessageUtils.setUnknownError(response,
                                "Failed to change lamp group states.");
                        sendResponse(response);
                        return;
                    } else if (transLampGroupStateResponse.responseCode != ResponseCode.OK.getValue()) {
                        MessageUtils.setUnknownError(response,
                                "Failed to change lamp group states (code: " + transLampGroupStateResponse.responseCode + ").");
                        sendResponse(response);
                        return;
                    }
                } catch (BusException e) {
                    MessageUtils.setUnknownError(response, e.getLocalizedMessage());
                    sendResponse(response);
                    return;
                }

                setResultOK(response);
                sendResponse(response);
            }

            @Override
            public void onSessionFailed(@NonNull String busName, short port) {
                MessageUtils.setUnknownError(response, "Failed to join session.");
                sendResponse(response);
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

        if (groupID == null) {
            MessageUtils.setInvalidRequestParameterError(response,
                    "Parameter 'groupId' must be specified.");
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

    private void onDeleteLightGroupForLampController(@NonNull Intent request
            , @NonNull final Intent response, @NonNull AllJoynServiceEntity service
            , @NonNull final String groupID) {
        final AllJoynDeviceApplication app = getApplication();

        OneShotSessionHandler.SessionJoinCallback callback = new OneShotSessionHandler.SessionJoinCallback() {
            @Override
            public void onSessionJoined(@NonNull String busName, short port, int sessionId) {
                LampGroup proxy = app.getInterface(busName, sessionId, LampGroup.class);

                if (proxy == null) {
                    MessageUtils.setUnknownError(response,
                            "Failed to obtain a proxy object for org.allseen.LSF.ControllerService.LampGroup .");
                    sendResponse(response);
                    return;
                }

                try {
                    LampGroup.GetAllLampGroupIDs_return_value_uas getAllLampGroupIDsResponse =
                            proxy.getAllLampGroupIDs();
                    if (getAllLampGroupIDsResponse == null) {
                        MessageUtils.setUnknownError(response, "Failed to obtain lamp group IDs.");
                        sendResponse(response);
                        return;
                    } else if (getAllLampGroupIDsResponse.responseCode != ResponseCode.OK.getValue()) {
                        MessageUtils.setUnknownError(response,
                                "Failed to obtain lamp IDs (code: "
                                        + getAllLampGroupIDsResponse.responseCode + ").");
                        sendResponse(response);
                        return;
                    }
                    if (!Arrays.asList(getAllLampGroupIDsResponse.lampGroupIDs).contains(groupID)) {
                        MessageUtils.setInvalidRequestParameterError(response,
                                "A light group with ID specified by 'groupId' not found.");
                        sendResponse(response);
                        return;
                    }

                    Map<String, Variant> newStates = new HashMap<>();
                    newStates.put("OnOff", new Variant(false, "b"));
                    LampGroup.TransitionLampGroupState_return_value_us transLampGroupStateResponse =
                            proxy.transitionLampGroupState(groupID, newStates, TRANSITION_PERIOD);
                    if (transLampGroupStateResponse.responseCode != ResponseCode.OK.getValue()) {
                        MessageUtils.setUnknownError(response,
                                "Failed to turn off the light group (code: " + transLampGroupStateResponse.responseCode + ").");
                        sendResponse(response);
                        return;
                    }
                } catch (BusException e) {
                    MessageUtils.setUnknownError(response, e.getLocalizedMessage());
                    sendResponse(response);
                    return;
                }

                setResultOK(response);
                sendResponse(response);
            }

            @Override
            public void onSessionFailed(@NonNull String busName, short port) {
                MessageUtils.setUnknownError(response, "Failed to join session.");
                sendResponse(response);
            }
        };
        OneShotSessionHandler.run(getContext(), service.busName, service.port, callback);
    }

    @Override
    protected boolean onPutLightGroup(final Intent request, final Intent response, final String serviceId,
                                      final String groupId, final String name, final Integer color,
                                      final Double brightness, final long[] flashing) {
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

        if (groupId == null) {
            MessageUtils.setInvalidRequestParameterError(response,
                    "Parameter 'groupId' must be specified.");
            return true;
        }

        if (flashing != null) {
            MessageUtils.setNotSupportActionError(response
                    , "Parameter 'flashing' is not supported.");
            return true;
        }
        int[] colors = new int[3];
        String colorParam = getColorString(request);
        if (colorParam != null) {
            if (!parseColorParam(colorParam, colors)) {
                MessageUtils.setInvalidRequestParameterError(response,
                        "Parameter 'color' is invalid.");
                return true;
            }
        }
        switch (getLampServiceType(service)) {

            case TYPE_LAMP_CONTROLLER: {
                onPutLightGroupForLampController(request, response, service, groupId, name
                        , brightness, colors);
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

    private void onPutLightGroupForLampController(@NonNull Intent request
            , @NonNull final Intent response, @NonNull final AllJoynServiceEntity service
            , @NonNull final String groupID, final String name, final Double brightness
            , final int[] color) {
        final AllJoynDeviceApplication app = getApplication();

        OneShotSessionHandler.SessionJoinCallback callback = new OneShotSessionHandler.SessionJoinCallback() {
            @Override
            public void onSessionJoined(@NonNull String busName, short port, int sessionId) {
                LampGroup proxy = app.getInterface(busName, sessionId, LampGroup.class);

                if (proxy == null) {
                    MessageUtils.setUnknownError(response,
                            "Failed to obtain a proxy object for org.allseen.LSF.ControllerService.LampGroup .");
                    sendResponse(response);
                    return;
                }

                try {
                    LampGroup.GetAllLampGroupIDs_return_value_uas getAllLampGroupIDsResponse =
                            proxy.getAllLampGroupIDs();
                    if (getAllLampGroupIDsResponse == null) {
                        MessageUtils.setUnknownError(response, "Failed to obtain lamp group IDs.");
                        sendResponse(response);
                        return;
                    } else if (getAllLampGroupIDsResponse.responseCode != ResponseCode.OK.getValue()) {
                        MessageUtils.setUnknownError(response,
                                "Failed to obtain lamp IDs (code: "
                                        + getAllLampGroupIDsResponse.responseCode + ").");
                        sendResponse(response);
                        return;
                    }
                    if (!Arrays.asList(getAllLampGroupIDsResponse.lampGroupIDs).contains(groupID)) {
                        MessageUtils.setInvalidRequestParameterError(response,
                                "A light group with ID specified by 'groupId' not found.");
                        sendResponse(response);
                        return;
                    }

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
                            proxy.transitionLampGroupState(groupID, newStates, TRANSITION_PERIOD);
                    if (transLampGroupStateResponse == null) {
                        MessageUtils.setUnknownError(response,
                                "Failed to change lamp group states.");
                        sendResponse(response);
                        return;
                    }
                    if (transLampGroupStateResponse.responseCode != ResponseCode.OK.getValue()) {
                        MessageUtils.setUnknownError(response,
                                "Failed to change lamp group states (code: " + transLampGroupStateResponse.responseCode + ").");
                        sendResponse(response);
                        return;
                    }

                    if (name != null) {
                        LampGroup.SetLampGroupName_return_value_uss setLampGroupNameResponse =
                                proxy.setLampGroupName(groupID, name, service.defaultLanguage);
                        if (setLampGroupNameResponse.responseCode != ResponseCode.OK.getValue()) {
                            MessageUtils.setUnknownError(response,
                                    "Failed to change group name (code: " + setLampGroupNameResponse.responseCode + ").");
                            sendResponse(response);
                            return;
                        }
                    }
                } catch (BusException e) {
                    MessageUtils.setUnknownError(response, e.getLocalizedMessage());
                    sendResponse(response);
                    return;
                }

                setResultOK(response);
                sendResponse(response);
            }

            @Override
            public void onSessionFailed(@NonNull String busName, short port) {
                MessageUtils.setUnknownError(response, "Failed to join session.");
                sendResponse(response);
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

        if (lightIDs == null) {
            MessageUtils.setInvalidRequestParameterError(response,
                    "Parameter 'lightIds' must be specified.");
            return true;
        }
        if (groupName == null) {
            MessageUtils.setInvalidRequestParameterError(response,
                    "Parameter 'groupName' must be specified.");
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

    private void onPostLightGroupCreateForLampController(@NonNull Intent request
            , @NonNull final Intent response, @NonNull final AllJoynServiceEntity service
            , @NonNull final String[] lightIDs, @NonNull final String groupName) {
        final AllJoynDeviceApplication app = getApplication();

        OneShotSessionHandler.SessionJoinCallback callback = new OneShotSessionHandler.SessionJoinCallback() {
            @Override
            public void onSessionJoined(@NonNull String busName, short port, int sessionId) {
                LampGroup proxyLampGroup = app.getInterface(busName, sessionId, LampGroup.class);

                if (proxyLampGroup == null) {
                    MessageUtils.setUnknownError(response,
                            "Failed to obtain a proxy object for org.allseen.LSF.ControllerService.LampGroup .");
                    sendResponse(response);
                    return;
                }

                try {
                    LampGroup.CreateLampGroup_return_value_us createLampGroupResponse =
                            proxyLampGroup.createLampGroup(lightIDs, new String[0], groupName
                                    , service.defaultLanguage);
                    if (createLampGroupResponse.responseCode != ResponseCode.OK.getValue()) {
                        MessageUtils.setUnknownError(response,
                                "Failed to create a light group (code: " + createLampGroupResponse.responseCode + ").");
                        sendResponse(response);
                        return;
                    }
                } catch (BusException e) {
                    MessageUtils.setUnknownError(response, e.getLocalizedMessage());
                    sendResponse(response);
                    return;
                }

                setResultOK(response);
                sendResponse(response);
            }

            @Override
            public void onSessionFailed(@NonNull String busName, short port) {
                MessageUtils.setUnknownError(response, "Failed to join session.");
                sendResponse(response);
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

        if (groupID == null) {
            MessageUtils.setInvalidRequestParameterError(response,
                    "Parameter 'groupId' must be specified.");
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

    private void onDeleteLightGroupClearForLampController(@NonNull Intent request
            , @NonNull final Intent response, @NonNull final AllJoynServiceEntity service
            , @NonNull final String groupID) {
        final AllJoynDeviceApplication app = getApplication();

        OneShotSessionHandler.SessionJoinCallback callback = new OneShotSessionHandler.SessionJoinCallback() {
            @Override
            public void onSessionJoined(@NonNull String busName, short port, int sessionId) {
                LampGroup proxy = app.getInterface(busName, sessionId, LampGroup.class);

                if (proxy == null) {
                    MessageUtils.setUnknownError(response,
                            "Failed to obtain a proxy object for org.allseen.LSF.ControllerService.LampGroup .");
                    sendResponse(response);
                    return;
                }

                try {
                    LampGroup.GetAllLampGroupIDs_return_value_uas getAllLampGroupIDsResponse =
                            proxy.getAllLampGroupIDs();
                    if (getAllLampGroupIDsResponse == null) {
                        MessageUtils.setUnknownError(response, "Failed to obtain lamp group IDs.");
                        sendResponse(response);
                        return;
                    } else if (getAllLampGroupIDsResponse.responseCode != ResponseCode.OK.getValue()) {
                        MessageUtils.setUnknownError(response,
                                "Failed to obtain lamp IDs (code: "
                                        + getAllLampGroupIDsResponse.responseCode + ").");
                        sendResponse(response);
                        return;
                    }
                    if (!Arrays.asList(getAllLampGroupIDsResponse.lampGroupIDs).contains(groupID)) {
                        MessageUtils.setInvalidRequestParameterError(response,
                                "A light group with ID specified by 'groupId' not found.");
                        sendResponse(response);
                        return;
                    }

                    LampGroup.DeleteLampGroup_return_value_us deleteLampGroupResponse =
                            proxy.deleteLampGroup(groupID);
                    if (deleteLampGroupResponse.responseCode != ResponseCode.OK.getValue()) {
                        MessageUtils.setUnknownError(response,
                                "Failed to delete the light group (code: " + deleteLampGroupResponse.responseCode + ").");
                        sendResponse(response);
                        return;
                    }
                } catch (BusException e) {
                    MessageUtils.setUnknownError(response, e.getLocalizedMessage());
                    sendResponse(response);
                    return;
                }

                setResultOK(response);
                sendResponse(response);
            }

            @Override
            public void onSessionFailed(@NonNull String busName, short port) {
                MessageUtils.setUnknownError(response, "Failed to join session.");
                sendResponse(response);
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
    /**
     * リクエストからcolorパラメータを取得する.
     *
     * @param request リクエスト
     * @return colorパラメータ
     */
    private String getColorString(final Intent request) {
        return request.getStringExtra(PARAM_COLOR);
    }

    /**
     * Get color parameter.
     *
     * @param colorParam color in string expression
     * @param color      Color parameter.
     * @return true : Success, false : failure.
     */
    private static boolean parseColorParam(final String colorParam, final int[] color) {
        try {
            String rr = colorParam.substring(0, 2);
            String gg = colorParam.substring(2, 4);
            String bb = colorParam.substring(4, 6);
            if (colorParam.length() == RGB_LENGTH) {
                if (rr == null || gg == null || bb == null) {
                    return false;
                }
                color[0] = Integer.parseInt(rr, 16);
                color[1] = Integer.parseInt(gg, 16);
                color[2] = Integer.parseInt(bb, 16);
            } else {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    /**
     * フラッシュパターンを文字列から解析し、数値の配列に変換する.<br/>
     * 数値の前後の半角のスペースは無視される。その他の半角、全角のスペースは不正なフォーマットとして扱われる。
     *
     * @param pattern フラッシュパターン文字列。
     * @return 鳴動パターンの配列。解析できないフォーマットの場合nullを返す。
     */
    private static long[] parseFlashingParam(final String pattern) {

        if (pattern.length() == 0) {
            return null;
        }

        long[] result = null;

        if (pattern.contains(",")) {
            String[] times = pattern.split(",");
            ArrayList<Long> values = new ArrayList<Long>();
            for (String time : times) {
                try {
                    String valueStr = time.trim();
                    if (valueStr.length() == 0) {
                        if (values.size() != times.length - 1) {
                            // 数値の間にスペースがある場合はフォーマットエラー
                            // ex. 100, , 100
                            values.clear();
                        }
                        break;
                    }
                    long value = Long.parseLong(time.trim());
                    values.add(value);
                } catch (NumberFormatException ignored) {
                    values.clear();
                    break;
                }
            }

            if (values.size() != 0) {
                result = new long[values.size()];
                for (int i = 0; i < values.size(); ++i) {
                    result[i] = values.get(i);
                }
            }
        } else {
            try {
                long time = Long.parseLong(pattern);
                result = new long[]{time};
            } catch (NumberFormatException ignored) {
            }
        }

        return result;
    }

}
