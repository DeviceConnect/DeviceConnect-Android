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
import org.deviceconnect.android.deviceplugin.alljoyn.BuildConfig;
import org.deviceconnect.android.deviceplugin.alljoyn.OneShotSessionHandler;
import org.deviceconnect.android.deviceplugin.alljoyn.service.AllJoynService;
import org.deviceconnect.android.deviceplugin.alljoyn.util.ColorUtil;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.LightProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.message.DConnectMessage;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private Map<String, FlashingExecutor> mFlashingMap = new HashMap<String, FlashingExecutor>();

    public AllJoynLightProfile() {
        addApi(mGetLightApi);
        addApi(mPostLightApi);
        addApi(mPutLightApi);
        addApi(mDeleteLightApi);
    }

    AllJoynDeviceApplication getApplication() {
        return (AllJoynDeviceApplication) getContext().getApplicationContext();
    }

    private final DConnectApi mGetLightApi = new DConnectApi() {
        @Override
        public Method getMethod() {
            return Method.GET;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final AllJoynServiceEntity service = ((AllJoynService) getService()).getEntity();

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
    };

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

    private final DConnectApi mPostLightApi = new DConnectApi() {
        @Override
        public Method getMethod() {
            return Method.POST;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String serviceId = getServiceID(request);
            final String lightId = getLightId(request);
            final Double brightness = getBrightness(request);
            final long[] flashing = getFlashing(request);

            final AllJoynServiceEntity service = ((AllJoynService) getService()).getEntity();


            int[] colors = getColors(request);
            if (colors == null) {
                MessageUtils.setInvalidRequestParameterError(response, "Parameter 'color' is invalid.");
                return true;
            }
            switch (getLampServiceType(service)) {
                case TYPE_SINGLE_LAMP: {
                    onPostLightForSingleLamp(request, response, service, serviceId, lightId, brightness, colors, flashing);
                    return false;
                }
                case TYPE_LAMP_CONTROLLER: {
                    onPostLightForLampController(request, response, service, serviceId, lightId, brightness, colors, flashing);
                    return false;
                }
                case TYPE_UNKNOWN:
                default: {
                    setUnsupportedError(response);
                    return true;
                }
            }
        }
    };

    private void onPostLightForSingleLamp(final Intent request, final Intent response, final AllJoynServiceEntity service,
                                          final String serviceId, final String lightId, final Double brightness, final int[] color, final long[] flashing) {
        final AllJoynDeviceApplication app = getApplication();

        if (flashing != null) {
            flashing(brightness, color, lightId, flashing); //do not check result of flashing
            setResultOK(response);
            sendResponse(response);
        }else {
            OneShotSessionHandler.SessionJoinCallback callback = new OneShotSessionHandler.SessionJoinCallback() {
                @Override
                public void onSessionJoined(final String busName, final short port, final int sessionId) {
                    final LampState proxyState = app.getInterface(busName, sessionId, LampState.class);
                    final LampDetails proxyDetails = app.getInterface(busName, sessionId, LampDetails.class);

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
                        final HashMap<String, Variant> newStates = new HashMap<>();

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
                            BigDecimal tmp = BigDecimal.valueOf(0xffffffffL);
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
                public void onSessionFailed(final String busName, final short port) {
                    MessageUtils.setUnknownError(response, "Failed to join session.");
                    sendResponse(response);
                }
            };
            OneShotSessionHandler.run(getContext(), service.busName, service.port, callback);
        }
    }

    private void onPostLightForLampController(final Intent request, final Intent response,
                                              final AllJoynServiceEntity service, final String serviceId, final String lightId,
                                              final Double brightness, final int[] color, final long[] flashing) {
        if (flashing != null) {
            flashingForController(response, brightness, color, lightId, flashing, null); //do not check result of flashing
            setResultOK(response);
            sendResponse(response);
        } else {
            final AllJoynDeviceApplication app = getApplication();
            OneShotSessionHandler.SessionJoinCallback callback = new OneShotSessionHandler.SessionJoinCallback() {
                @Override
                public void onSessionJoined(final String busName, final short port, final int sessionId) {
                    Lamp proxy = app.getInterface(busName, sessionId, Lamp.class);

                    if (proxy == null) {
                        MessageUtils.setUnknownError(response,
                                "Failed to obtain a proxy object for org.allseen.LSF.ControllerService.Lamp .");
                        sendResponse(response);
                        return;
                    }

                    try {
                        Lamp.GetAllLampIDs_return_value_uas getAllLampIDsResponse = proxy.getAllLampIDs();
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

                        String _lightId = lightId;
                        if (_lightId == null) {
                            _lightId = getAllLampIDsResponse.lampIDs[0];
                        } else if (!Arrays.asList(getAllLampIDsResponse.lampIDs).contains(_lightId)) {
                            MessageUtils.setInvalidRequestParameterError(response,
                                    "A light with ID specified by 'lightId' not found.");
                            sendResponse(response);
                            return;
                        }
                        Lamp.GetLampDetails_return_value_usa_sv lampDetailsResponse = proxy.getLampDetails(_lightId);
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
                                BigDecimal tmp = BigDecimal.valueOf(0xffffffffL);
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
                                proxy.transitionLampState(_lightId, newStates, TRANSITION_PERIOD);

                        if (transLampStateResponse == null) {
                            MessageUtils.setUnknownError(response,
                                    "Failed to change lamp states (not get transitionLampState).");
                            sendResponse(response);
                            return;
                        } else if (transLampStateResponse.responseCode != ResponseCode.OK.getValue()) {
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
    }

    private final DConnectApi mDeleteLightApi = new DConnectApi() {
        @Override
        public Method getMethod() {
            return Method.DELETE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String lightId = getLightId(request);
            final AllJoynServiceEntity service = ((AllJoynService) getService()).getEntity();

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
    };

    private void onDeleteLightForSingleLamp(@NonNull Intent request, @NonNull final Intent response
            , @NonNull final AllJoynServiceEntity service, @NonNull String lightId) {
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
                    String _lightId = lightId;
                    if (_lightId == null) {
                        _lightId = getAllLampIDsResponse.lampIDs[0];
                    } else if (!Arrays.asList(getAllLampIDsResponse.lampIDs).contains(_lightId)) {
                        MessageUtils.setInvalidRequestParameterError(response,
                                "A light with ID specified by 'lightId' not found.");
                        sendResponse(response);
                        return;
                    }

                    Map<String, Variant> newStates = new HashMap<>();
                    newStates.put("OnOff", new Variant(false, "b"));
                    Lamp.TransitionLampState_return_value_us transLampStateResponse =
                            proxy.transitionLampState(_lightId, newStates, TRANSITION_PERIOD);
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

    private final DConnectApi mPutLightApi = new DConnectApi() {
        @Override
        public Method getMethod() {
            return Method.PUT;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String serviceId = getServiceID(request);
            final String name = getName(request);
            final String lightId = getLightId(request);
            final Double brightness = getBrightness(request);
            final long[] flashing = getFlashing(request);

            final AllJoynServiceEntity service = ((AllJoynService) getService()).getEntity();

            if (service == null) {
                MessageUtils.setNotFoundServiceError(response);
                return true;
            }

            if (name == null || name.length() == 0) {
                MessageUtils.setInvalidRequestParameterError(response,
                        "name is invalid.");
                return true;
            }

            int[] colors = getColors(request);
            if (colors == null) {
                MessageUtils.setInvalidRequestParameterError(response, "Parameter 'color' is invalid.");
                return true;
            }
            switch (getLampServiceType(service)) {
                case TYPE_SINGLE_LAMP: {
                    onPutLightForSingleLamp(request, response, service, serviceId, lightId, name, brightness, colors, flashing);
                    return false;
                }
                case TYPE_LAMP_CONTROLLER: {
                    onPutLightForLampController(request, response, service, serviceId, lightId, name, brightness, colors, flashing);
                    return false;
                }
                case TYPE_UNKNOWN:
                default: {
                    setUnsupportedError(response);
                    return true;
                }
            }
        }
    };

    // TODO: Implement name change functionality using AllJoyn Config service.
    private void onPutLightForSingleLamp(@NonNull Intent request, @NonNull final Intent response
            , @NonNull AllJoynServiceEntity service, final String serviceId, @NonNull final String lightId, String name
            , final Double brightness, final int[] color, final long[] flashing) {


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

        if (flashing != null) {
            flashing(brightness, color, lightId, flashing); //do not check result of flashing
            setResultOK(response);
            sendResponse(response);
        } else {
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
                            BigDecimal tmp = BigDecimal.valueOf(0xffffffffL);
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
    }

    private void onPutLightForLampController(@NonNull Intent request, @NonNull final Intent response
            , @NonNull final AllJoynServiceEntity service, String serviceId, @NonNull final String lightId
            , final String name, final Double brightness, final int[] color, final long[] flashing) {
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

        if (flashing != null) {
            flashingForController(response, brightness, color, lightId, flashing, name); //do not check result of flashing
            setResultOK(response);
            sendResponse(response);
        } else {
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
                        String _lightId = lightId;
                        if (_lightId == null) {
                            _lightId = getAllLampIDsResponse.lampIDs[0];
                        } else if (!Arrays.asList(getAllLampIDsResponse.lampIDs).contains(_lightId)) {
                            MessageUtils.setInvalidRequestParameterError(response,
                                    "A light with ID specified by 'lightId' not found.");
                            sendResponse(response);
                            return;
                        }
                        HashMap<String, Variant> newStates = new HashMap<>();

                        // NOTE: Arithmetic operations in primitive types may lead to arithmetic
                        // overflow. To retain precision, BigDecimal objects are used.

                        Lamp.GetLampDetails_return_value_usa_sv lampDetailsResponse =
                                proxy.getLampDetails(_lightId);
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
                                BigDecimal tmp = BigDecimal.valueOf(0xffffffffL);
                                tmp = tmp.multiply(BigDecimal.valueOf(brightness));
                                long scaledVal = tmp.longValue();
                                int intScaledVal = ByteBuffer.allocate(8).putLong(scaledVal).getInt(4);
                                newStates.put("Brightness", new Variant(intScaledVal, "u"));
                            }
                        }

                        Lamp.TransitionLampState_return_value_us transLampStateResponse =
                                proxy.transitionLampState(_lightId, newStates, TRANSITION_PERIOD);

                        if (transLampStateResponse.responseCode != ResponseCode.OK.getValue()) {
                            MessageUtils.setUnknownError(response,
                                    "Failed to change lamp states (code: " + transLampStateResponse.responseCode + ").");
                            sendResponse(response);
                            return;
                        }

                        if (name != null) {
                            Lamp.SetLampName_return_value_uss lampNameResponse =
                                    proxy.setLampName(_lightId, name, service.defaultLanguage);
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
    }

    private void flashing(final Double brightness, final int[] color, String lightId, long[] flashing) {
        FlashingExecutor exe = mFlashingMap.get(lightId);
        if (exe == null) {
            exe = new FlashingExecutor();
            mFlashingMap.put(lightId, exe);
        }
        exe.setLightControllable(new FlashingExecutor.LightControllable() {
            @Override
            public void changeLight(boolean isOn, final FlashingExecutor.CompleteListener listener) {
                changeLightForSingleLamp(isOn, brightness, color);
                listener.onComplete();
            }
        });
        exe.start(flashing);
    }

    private void flashingForController(final Intent response, final Double brightness,
                                       final int[] color, final String lightId, long[] flashing, final String name) {
        FlashingExecutor exe = mFlashingMap.get(lightId);
        if (exe == null) {
            exe = new FlashingExecutor();
            mFlashingMap.put(lightId, exe);
        }
        exe.setLightControllable(new FlashingExecutor.LightControllable() {
            @Override
            public void changeLight(boolean isOn, final FlashingExecutor.CompleteListener listener) {
                changeLightForController(isOn, brightness, color, lightId, name);
                listener.onComplete();
            }
        });
        exe.start(flashing);
    }

    private void changeLightForSingleLamp(final boolean isOn, final Double brightness, final int[] color) {
        final AllJoynDeviceApplication app = getApplication();
        final AllJoynServiceEntity service = ((AllJoynService) getService()).getEntity();

        OneShotSessionHandler.SessionJoinCallback callback = new OneShotSessionHandler.SessionJoinCallback() {
            @Override
            public void onSessionJoined(final String busName, final short port, final int sessionId) {
                final LampState proxyState = app.getInterface(busName, sessionId, LampState.class);
                final LampDetails proxyDetails = app.getInterface(busName, sessionId, LampDetails.class);

                if (proxyState == null) {
                    if (BuildConfig.DEBUG) {
                        Log.w(AllJoynLightProfile.this.getClass().getSimpleName(),
                                "Failed to obtain a proxy object for org.allseen.LSF.LampState .");
                    }
                    return;
                }
                if (proxyDetails == null) {
                    if (BuildConfig.DEBUG) {
                        Log.w(AllJoynLightProfile.this.getClass().getSimpleName(),
                                "Failed to obtain a proxy object for org.allseen.LSF.LampDetails .");
                    }
                    return;
                }

                try {
                    final HashMap<String, Variant> newStates = new HashMap<>();

                    // NOTE: Arithmetic operations in primitive types may lead to arithmetic
                    // overflow. To retain precision, BigDecimal objects are used.

                    newStates.put("OnOff", new Variant(isOn, "b"));

                    if (proxyDetails.getColor() && color != null) {
                        int[] hsb = ColorUtil.convertRGB_8_8_8_To_HSB_32_32_32(color);

                        newStates.put("Hue", new Variant(hsb[0], "u"));
                        newStates.put("Saturation", new Variant(hsb[1], "u"));
                    }
                    if (proxyDetails.getDimmable() && brightness != null) {
                        // [0, 1] -> [0, 0xffffffff]
                        BigDecimal tmp = BigDecimal.valueOf(0xffffffffL);
                        tmp = tmp.multiply(BigDecimal.valueOf(brightness));
                        long scaledVal = tmp.longValue();
                        int intScaledVal = ByteBuffer.allocate(8).putLong(scaledVal).getInt(4);
                        newStates.put("Brightness", new Variant(intScaledVal, "u"));
                    }

                    int responseCode = proxyState.transitionLampState(0, newStates, TRANSITION_PERIOD);
                    if (responseCode != ResponseCode.OK.getValue()) {
                        if (BuildConfig.DEBUG) {
                            Log.w(AllJoynLightProfile.this.getClass().getSimpleName(),
                                    "Failed to change lamp states (code: "
                                            + responseCode + ").");
                        }
                    }

                } catch (BusException e) {
                    if (BuildConfig.DEBUG) {
                        Log.w(AllJoynLightProfile.this.getClass().getSimpleName(),
                                e.getLocalizedMessage());
                    }
                }
            }

            @Override
            public void onSessionFailed(final String busName, final short port) {
            }
        };
        OneShotSessionHandler.run(getContext(), service.busName, service.port, callback);
    }

    private void changeLightForController(final boolean isOn, final Double brightness,
                                          final int[] color, final String lightId, final String name) {
        final AllJoynDeviceApplication app = getApplication();
        final AllJoynServiceEntity service = ((AllJoynService) getService()).getEntity();

        OneShotSessionHandler.SessionJoinCallback callback = new OneShotSessionHandler.SessionJoinCallback() {
            @Override
            public void onSessionJoined(final String busName, final short port, final int sessionId) {
                Lamp proxy = app.getInterface(busName, sessionId, Lamp.class);

                if (proxy == null) {
                    if (BuildConfig.DEBUG) {
                        Log.w(AllJoynLightProfile.this.getClass().getSimpleName(),
                                "Failed to obtain a proxy object for org.allseen.LSF.ControllerService.Lamp .");
                    }
                    return;
                }

                try {
                    Lamp.GetAllLampIDs_return_value_uas getAllLampIDsResponse = proxy.getAllLampIDs();
                    if (getAllLampIDsResponse == null) {
                        if (BuildConfig.DEBUG) {
                            Log.w(AllJoynLightProfile.this.getClass().getSimpleName(),
                                    "Failed to obtain lamp IDs.");
                        }
                        return;
                    } else if (getAllLampIDsResponse.responseCode != ResponseCode.OK.getValue()) {
                        if (BuildConfig.DEBUG) {
                            Log.w(AllJoynLightProfile.this.getClass().getSimpleName(),
                                    "Failed to obtain lamp IDs (code: "
                                            + getAllLampIDsResponse.responseCode + ").");
                        }
                        return;
                    }

                    String _lightId = lightId;
                    if (_lightId == null) {
                        _lightId = getAllLampIDsResponse.lampIDs[0];
                    } else if (!Arrays.asList(getAllLampIDsResponse.lampIDs).contains(_lightId)) {

                        return;
                    }

                    Lamp.GetLampDetails_return_value_usa_sv lampDetailsResponse = proxy.getLampDetails(_lightId);
                    if (lampDetailsResponse == null) {
                        if (BuildConfig.DEBUG) {
                            Log.w(AllJoynLightProfile.this.getClass().getSimpleName(),
                                    "Failed to obtain lamp details.");
                        }
                        return;
                    } else if (lampDetailsResponse.responseCode != ResponseCode.OK.getValue()) {
                        if (BuildConfig.DEBUG) {
                            Log.w(AllJoynLightProfile.this.getClass().getSimpleName(),
                                    "Failed to obtain lamp details (code: " + lampDetailsResponse.responseCode + ").");
                        }
                        return;
                    }

                    final HashMap<String, Variant> newStates = new HashMap<>();

                    // NOTE: Arithmetic operations in primitive types may lead to arithmetic
                    // overflow. To retain precision, BigDecimal objects are used.

                    newStates.put("OnOff", new Variant(isOn, "b"));
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
                            BigDecimal tmp = BigDecimal.valueOf(0xffffffffL);
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
                            proxy.transitionLampState(_lightId, newStates, TRANSITION_PERIOD);
                    if (transLampStateResponse == null) {
                        if (BuildConfig.DEBUG) {
                            Log.w(AllJoynLightProfile.this.getClass().getSimpleName(),
                                    "Failed to change lamp states (not get transitionLampStatecode).");
                        }
                    } else if (transLampStateResponse.responseCode != ResponseCode.OK.getValue()) {
                        if (BuildConfig.DEBUG) {
                            Log.w(AllJoynLightProfile.this.getClass().getSimpleName(),
                                    "Failed to change lamp states (code: " + transLampStateResponse.responseCode + ").");
                        }
                    }
                    if (name != null) {
                        Lamp.SetLampName_return_value_uss lampNameResponse =
                                proxy.setLampName(_lightId, name, service.defaultLanguage);
                        if (lampNameResponse.responseCode != ResponseCode.OK.getValue()) {
                            Log.w(AllJoynLightProfile.this.getClass().getSimpleName(),
                                    "Failed to change name (code: " + lampNameResponse.responseCode + ").");
                            return;
                        }
                    }
                } catch (BusException e) {
                    if (BuildConfig.DEBUG) {
                        Log.w(AllJoynLightProfile.this.getClass().getSimpleName(),
                                e.getLocalizedMessage());
                    }
                }
            }

            @Override
            public void onSessionFailed(final String busName, final short port) {
            }
        };
        OneShotSessionHandler.run(getContext(), service.busName, service.port, callback);
    }

    /**
     * 成功レスポンス設定。
     *
     * @param response response
     */
    private void setResultOK(final Intent response) {
        setResult(response, DConnectMessage.RESULT_OK);
    }

    private boolean isSupportingInterfaces(final AllJoynServiceEntity service,
                                           final String... interfaces) {
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
    private int[] getColors(final Intent request) {
        int[] colors = new int[3];
        String colorParam = getColorString(request);
        if (colorParam != null && !parseColorParam(colorParam, colors)) {
            return null;
        }
        return colors;
    }
}
