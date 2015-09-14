/*
 KadecotLightProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.kadecot.profile;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.kadecot.kadecotdevice.KadecotGeneralLighting;
import org.deviceconnect.android.deviceplugin.kadecot.kadecotdevice.KadecotHomeAirConditioner;
import org.deviceconnect.android.deviceplugin.kadecot.kadecotdevice.KadecotResult;
import org.deviceconnect.android.deviceplugin.kadecot.KadecotDeviceService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Light Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class KadecotLightProfile extends DConnectProfile {

    /** Profile name: {@value} . */
    public static final String PROFILE_NAME = "light";
    /** Parameter: {@value} . */
    public static final String PARAM_NAME = "name";
    /** Parameter: {@value} . */
    public static final String PARAM_LIGHTS = "lights";
    /** Parameter: {@value} . */
    public static final String PARAM_LIGHT_ID = "lightId";
    /** Parameter: {@value} . */
    public static final String PARAM_ON = "on";
    /** Parameter: {@value} . */
    public static final String PARAM_CONFIG = "config";
    /** Parameter: {@value} . */
    public static final String PARAM_BRIGHTNESS = "brightness";
    /** Parameter: {@value} . */
    public static final String PARAM_COLOR = "color";
    /** Parameter: {@value} . */
    public static final String PARAM_FLASHING = "flashing";

    /** RGB string length. */
    private static final int RGB_LENGTH = 6;

    /** Kadecot prefix. */
    static final String PREFIX_KADECOT = "kadecot";

    /** "No result" string. */
    static final String NO_RESULT = "{}";

    /** Index of prefix. */
    static final int IDX_PREFIX = 0;

    /** Index of kadecot deviceId. */
    static final int IDX_DEVICEID = 1;

    /** Index of profile name. */
    static final int IDX_PROFILENAME = 2;

    @Override
    public String getProfileName() {
        return PROFILE_NAME;
    }

    @Override
    protected boolean onGetRequest(final Intent request, final Intent response) {

        if (isNullAttribute(request)) {
            return onGetLight(request, response);
        } else {
            return onGetOther(request, response);
        }
    }

    @Override
    protected boolean onPutRequest(final Intent request, final Intent response) {

        if (isNullAttribute(request)) {
            return onPutLight(request, response);
        } else {
            return onPutOther(request, response);
        }
    }

    @Override
    protected boolean onPostRequest(final Intent request, final Intent response) {

        if (isNullAttribute(request)) {
            return onPostLight(request, response);
        } else {
            return onPostOther(request, response);
        }
    }

    @Override
    protected boolean onDeleteRequest(final Intent request, final Intent response) {

        if (isNullAttribute(request)) {
            return onDeleteLight(request, response);
        } else {
            return onDeleteOther(request, response);
        }
    }

    /**
     * Kadecot server query task class.
     */
    public class KadecotServerQueryTask extends AsyncTask<Intent, Void, Void> {

        @Override
        protected Void doInBackground(final Intent... intents) {
            Intent request = intents[0];
            Intent response = intents[1];
            String action = request.getAction();
            String attr = getAttribute(request);
            if (action.endsWith(DConnectMessage.METHOD_GET)) {
                if (attr == null) {
                    getLight(request, response);
                } else {
                    MessageUtils.setNotSupportAttributeError(response);
                    getContext().sendBroadcast(response);
                }
            } else if (action.endsWith(DConnectMessage.METHOD_POST)) {
                if (attr == null) {
                    postLight(request, response);
                } else {
                    MessageUtils.setNotSupportAttributeError(response);
                    getContext().sendBroadcast(response);
                }
            } else if (action.endsWith(DConnectMessage.METHOD_PUT)) {
                MessageUtils.setNotSupportAttributeError(response);
                getContext().sendBroadcast(response);
            } else if (action.endsWith(DConnectMessage.METHOD_DELETE)) {
                if (attr == null) {
                    deleteLight(request, response);
                } else {
                    MessageUtils.setNotSupportAttributeError(response);
                    getContext().sendBroadcast(response);
                }
            }
            return null;
        }
    }

    /**
     * onGetLight method handler.
     *
     * @param request Request parameter.
     * @param response Response parameter.
     * @return Send response (true) / Not send response (false).
     */
    protected boolean onGetLight(final Intent request, final Intent response) {
        if (checkServiceId(getServiceID(request), response)) {
            new KadecotServerQueryTask().execute(request, response);
            return false;
        }
        return true;
/*
        String serviceId = getServiceID(request);
        String[] element = ((KadecotDeviceService) getContext()).getElementFromServiceId(serviceId);
        List<Bundle> lightsParam = new ArrayList<>();
        if (element[IDX_PREFIX].equals(PREFIX_KADECOT) && element[IDX_DEVICEID] != null
                && element[IDX_PROFILENAME].equals(PROFILE_NAME)) {
            Bundle lightParam = new Bundle();
            KadecotGeneralLighting kgl = new KadecotGeneralLighting();
            String urlstr = kgl.exchangeJsonString(element[IDX_DEVICEID],
                    KadecotGeneralLighting.POWERSTATE_GET);
            Cursor cursor = getContext().getContentResolver().query(Uri.parse(urlstr), null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                String result = cursor.getString(0);
                String propertyName = ((KadecotDeviceService) getContext()).getPropertyName(result);
                String propertyValue = ((KadecotDeviceService) getContext()).getPropertyValue(result);
                if (propertyName != null && propertyValue != null) {
                    if (propertyName.equals(KadecotGeneralLighting.PROP_OPERATIONSTATUS)) {
                        setResult(response, DConnectMessage.RESULT_OK);
                        switch (propertyValue) {
                            case "48":  lightParam.putBoolean(PARAM_ON, true);  break;
                            case "49":  lightParam.putBoolean(PARAM_ON, false); break;
                            default:    lightParam.putBoolean(PARAM_ON, false); break;
                        }
                        lightParam.putString(PARAM_LIGHT_ID, element[IDX_DEVICEID]);
                        lightParam.putString(PARAM_NAME, ((KadecotDeviceService) getContext()).getNickName(serviceId));
                        lightParam.putString(PARAM_CONFIG, "");
                        lightsParam.add(lightParam);
                    } else {
                        if (result.equals(NO_RESULT)) {
                            MessageUtils.setNotSupportAttributeError(response,
                                    "This device not support 'get' procedure.");
                        } else {
                            createInvalidKadecotResponseError(response);
                        }
                    }
                } else {
                    createInvalidKadecotResponseError(response);
                }
                cursor.close();
            } else {
                createInvalidKadecotResponseError(response);
            }
        } else {
            createInvalidKadecotResponseError(response);
        }
        response.putExtra(PARAM_LIGHTS, lightsParam.toArray(new Bundle[lightsParam.size()]));
        return true;
*/
    }

    /**
     * Get light status.
     *
     * @param request Request.
     * @param response Response.
     */
    protected void getLight(final Intent request, final Intent response) {
        List<Bundle> lightsParam = new ArrayList<>();
        KadecotResult result = requestKadecotServer(response, getServiceID(request),
                KadecotGeneralLighting.POWERSTATE_GET);
        if (result != null) {
            String propertyName = result.getPropertyName();
            String propertyValue = result.getPropertyValue();
            if (propertyName != null && propertyValue != null) {
                if (propertyName.equals(KadecotGeneralLighting.PROP_OPERATIONSTATUS)) {
                    Bundle lightParam = new Bundle();
                    String serviceId = getServiceID(request);
                    String[] element = ((KadecotDeviceService) getContext()).getElementFromServiceId(serviceId);
                    setResult(response, DConnectMessage.RESULT_OK);
                    switch (propertyValue) {
                        case "48":  lightParam.putBoolean(PARAM_ON, true);  break;
                        case "49":  lightParam.putBoolean(PARAM_ON, false); break;
                        default:    lightParam.putBoolean(PARAM_ON, false); break;
                    }
                    lightParam.putString(PARAM_LIGHT_ID, element[IDX_DEVICEID]);
                    lightParam.putString(PARAM_NAME, ((KadecotDeviceService) getContext()).getNickName(serviceId));
                    lightParam.putString(PARAM_CONFIG, "");
                    lightsParam.add(lightParam);
                } else if (result.getServerResult().equals(NO_RESULT)) {
                    MessageUtils.setNotSupportAttributeError(response, "This device not support 'get' procedure.");
                } else {
                    createInvalidKadecotResponseError(response);
                }
            } else {
                createInvalidKadecotResponseError(response);
            }
        }
        response.putExtra(PARAM_LIGHTS, lightsParam.toArray(new Bundle[lightsParam.size()]));
        getContext().sendBroadcast(response);
    }

    /**
     * onGetOther method handler.
     *
     * @param request Request parameter.
     * @param response Response parameter.
     * @return Send response (true).
     */
    protected boolean onGetOther(final Intent request, final Intent response) {
        String serviceId = getServiceID(request);
        String[] element = ((KadecotDeviceService) getContext()).getElementFromServiceId(serviceId);
        if (element[IDX_PREFIX].equals(PREFIX_KADECOT) && element[IDX_DEVICEID] != null
                && element[IDX_PROFILENAME].equals(PROFILE_NAME)) {
            setErrAttribute(response);
        } else {
            MessageUtils.setNotFoundServiceError(response);
        }
        return true;
    }

    /**
     * onPostLight method handler.
     *
     * @param request Request parameter.
     * @param response Response parameter.
     * @return Send response (true) / Not send response (false).
     */
    protected boolean onPostLight(final Intent request, final Intent response) {
        int[] colorParam = new int[3];
        float brightness;
        String lightId = getLightID(request);

        if (lightId == null || lightId.length() == 0) {
            MessageUtils.setInvalidRequestParameterError(response, "lightId is not specified.");
            return true;
        }

        // Kadecot plug-in not support brightness. (Parameter check only)
        brightness = getBrightnessParam(request, response);
        if (brightness == -1) {
            MessageUtils.setInvalidRequestParameterError(response, "brightness is invalid.");
            return true;
        }

        // Kadecot plug-in not support color. (Parameter check only)
        if (!(getColorParam(request, response, colorParam))) {
            MessageUtils.setInvalidRequestParameterError(response, "color is invalid.");
            return true;
        }

        // Kadecot plug-in not support flashing. (Parameter check only)
        if (!(isNullFlashingParam(request))) {
            int[] pattern = getFlashingParam(request);
            if (pattern == null) {
                MessageUtils.setInvalidRequestParameterError(response, "flashing is invalid.");
                return true;
            }
        }

        if (checkServiceId(getServiceID(request), response)) {
            new KadecotServerQueryTask().execute(request, response);
            return false;
        }
        return true;
/*
        String serviceId = getServiceID(request);
        String[] element = ((KadecotDeviceService) getContext()).getElementFromServiceId(serviceId);
        if (element[IDX_PREFIX].equals(PREFIX_KADECOT) && element[IDX_DEVICEID] != null
                && element[IDX_PROFILENAME].equals(PROFILE_NAME)) {
            KadecotGeneralLighting kgl = new KadecotGeneralLighting();
            String urlstr = kgl.exchangeJsonString(element[IDX_DEVICEID],
                    KadecotGeneralLighting.POWERSTATE_ON);
            Cursor cursor = getContext().getContentResolver().query(Uri.parse(urlstr), null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                String result = cursor.getString(0);
                String propertyName = ((KadecotDeviceService) getContext()).getPropertyName(result);
                String propertyValue = ((KadecotDeviceService) getContext()).getPropertyValue(result);
                if (propertyName != null && propertyValue != null) {
                    if (propertyName.equals("OperationStatus")) {
                        switch (propertyValue) {
                            case "48":  setResult(response, DConnectMessage.RESULT_OK);     break;
                            case "49":  setResult(response, DConnectMessage.RESULT_ERROR);  break;
                            default:    createInvalidKadecotResponseError(response);        break;
                        }
                    } else {
                        createInvalidKadecotResponseError(response);
                    }
                } else {
                    if (result.equals(NO_RESULT)) {
                        MessageUtils.setNotSupportAttributeError(response,
                                "This device not support 'set' procedure.");
                    } else {
                        createInvalidKadecotResponseError(response);
                    }
                }
                cursor.close();
            } else {
                createInvalidKadecotResponseError(response);
            }
        } else {
            createInvalidKadecotResponseError(response);
        }
        return true;
*/
    }

    /**
     * Post Light status.
     *
     * @param request Request.
     * @param response Response.
     */
    protected void postLight(final Intent request, final Intent response) {
        KadecotResult result = requestKadecotServer(response, getServiceID(request),
                KadecotGeneralLighting.POWERSTATE_ON);
        if (result != null) {
            String propertyName = result.getPropertyName();
            String propertyValue = result.getPropertyValue();
            if (propertyName != null && propertyValue != null) {
                if (propertyName.equals(KadecotGeneralLighting.PROP_OPERATIONSTATUS)) {
                    setResult(response, DConnectMessage.RESULT_OK);
                    switch (propertyValue) {
                        case "48":  setResult(response, DConnectMessage.RESULT_OK);     break;
                        case "49":  setResult(response, DConnectMessage.RESULT_ERROR);  break;
                        default:    createInvalidKadecotResponseError(response);        break;
                    }
                } else if (result.getServerResult().equals(NO_RESULT)) {
                    MessageUtils.setNotSupportAttributeError(response, "This device not support 'get' procedure.");
                } else {
                    createInvalidKadecotResponseError(response);
                }
            } else {
                createInvalidKadecotResponseError(response);
            }
        }
        getContext().sendBroadcast(response);
    }

        /**
         * onPostOther method handler.
         *
         * @param request Request parameter.
         * @param response Response parameter.
         * @return Send response (true) / Not send response (false).
         */
    protected boolean onPostOther(final Intent request, final Intent response) {
        String serviceId = getServiceID(request);
        String[] element = ((KadecotDeviceService) getContext()).getElementFromServiceId(serviceId);
        if (element[IDX_PREFIX].equals(PREFIX_KADECOT) && element[IDX_DEVICEID] != null
                && element[IDX_PROFILENAME].equals(PROFILE_NAME)) {
            setErrAttribute(response);
        } else {
            MessageUtils.setNotFoundServiceError(response);
        }
        return true;
    }

    /**
     * onPutLight method handler.
     *
     * @param request Request parameter.
     * @param response Response parameter.
     * @return Send response (true) / Not send response (false).
     */
    protected boolean onPutLight(final Intent request, final Intent response) {
        int[] colorParam = new int[3];
        float brightness;
        String serviceId = getServiceID(request);
        String lightId = getLightID(request);
        String name = getName(request);

        if (lightId == null || lightId.length() == 0) {
            MessageUtils.setInvalidRequestParameterError(response, "lightId is not specified.");
            return true;
        }

        // Kadecot plug-in not support name. (Parameter check only)
        if (name == null || name.length() == 0) {
            MessageUtils.setInvalidRequestParameterError(response, "name is not specified.");
            return true;
        }

        // Kadecot plug-in not support brightness. (Parameter check only)
        brightness = getBrightnessParam(request, response);
        if (brightness == -1) {
            MessageUtils.setInvalidRequestParameterError(response, "brightness is invalid.");
            return true;
        }

        // Kadecot plug-in not support color. (Parameter check only)
        if (!(getColorParam(request, response, colorParam))) {
            MessageUtils.setInvalidRequestParameterError(response, "color is invalid.");
            return true;
        }

        // Kadecot plug-in not support flashing. (Parameter check only)
        int[] pattern = getFlashingParam(request);
        if (pattern == null) {
            MessageUtils.setInvalidRequestParameterError(response, "flashing is invalid.");
            return true;
        }

        // Kadecot plug-in not support put method.
        String[] element = ((KadecotDeviceService) getContext()).getElementFromServiceId(serviceId);
        if (element[IDX_PREFIX].equals(PREFIX_KADECOT) && element[IDX_DEVICEID] != null
                && element[IDX_PROFILENAME].equals(PROFILE_NAME)) {
            setResult(response, DConnectMessage.RESULT_OK);
        } else {
            MessageUtils.setInvalidRequestParameterError(response, "serviceId is invalid.");
        }
        return true;
    }

    /**
     * onPutOther method handler.
     *
     * @param request Request parameter.
     * @param response Response parameter.
     * @return Send response (true) / Not send response (false).
     */
    protected boolean onPutOther(final Intent request, final Intent response) {
        String serviceId = getServiceID(request);
        String[] element = ((KadecotDeviceService) getContext()).getElementFromServiceId(serviceId);
        if (element[IDX_PREFIX].equals(PREFIX_KADECOT) && element[IDX_DEVICEID] != null
                && element[IDX_PROFILENAME].equals(PROFILE_NAME)) {
            setErrAttribute(response);
        } else {
            MessageUtils.setNotFoundServiceError(response);
        }
        return true;
    }

    /**
     * onDeleteLight method handler.
     *
     * @param request Request parameter.
     * @param response Response parameter.
     * @return Send response (true) / Not send response (false).
     */
    protected boolean onDeleteLight(final Intent request, final Intent response) {
        String lightId = getLightID(request);

        if (lightId == null || lightId.length() == 0) {
            MessageUtils.setInvalidRequestParameterError(response, "lightId is not specified.");
            return true;
        }

        if (checkServiceId(getServiceID(request), response)) {
            new KadecotServerQueryTask().execute(request, response);
            return false;
        }
        return true;
/*
        String serviceId = getServiceID(request);
        String[] element = ((KadecotDeviceService) getContext()).getElementFromServiceId(serviceId);
        if (element[IDX_PREFIX].equals(PREFIX_KADECOT) && element[IDX_DEVICEID] != null
                && element[IDX_PROFILENAME].equals(PROFILE_NAME)) {
            KadecotGeneralLighting kgl = new KadecotGeneralLighting();
            String urlstr = kgl.exchangeJsonString(element[IDX_DEVICEID],
                    KadecotGeneralLighting.POWERSTATE_OFF);
            Cursor cursor = getContext().getContentResolver().query(Uri.parse(urlstr), null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                String result = cursor.getString(0);
                String propertyName = ((KadecotDeviceService) getContext()).getPropertyName(result);
                String propertyValue = ((KadecotDeviceService) getContext()).getPropertyValue(result);
                if (propertyName != null && propertyValue != null) {
                    if (propertyName.equals("OperationStatus")) {
                        switch (propertyValue) {
                            case "48":  setResult(response, DConnectMessage.RESULT_ERROR);  break;
                            case "49":  setResult(response, DConnectMessage.RESULT_OK);     break;
                            default:    createInvalidKadecotResponseError(response);        break;
                        }
                    } else {
                        createInvalidKadecotResponseError(response);
                    }
                } else {
                    if (result.equals(NO_RESULT)) {
                        MessageUtils.setNotSupportAttributeError(response,
                                "This device not support 'set' procedure.");
                    } else {
                        createInvalidKadecotResponseError(response);
                    }
                }
                cursor.close();
            } else {
                createInvalidKadecotResponseError(response);
            }
        } else {
            createInvalidKadecotResponseError(response);
        }
        return true;
*/
    }

    /**
     * Delete Light status.
     *
     * @param request Request.
     * @param response Response.
     */
    protected void deleteLight(final Intent request, final Intent response) {
        KadecotResult result = requestKadecotServer(response, getServiceID(request),
                KadecotGeneralLighting.POWERSTATE_OFF);
        if (result != null) {
            String propertyName = result.getPropertyName();
            String propertyValue = result.getPropertyValue();
            if (propertyName != null && propertyValue != null) {
                if (propertyName.equals(KadecotGeneralLighting.PROP_OPERATIONSTATUS)) {
                    setResult(response, DConnectMessage.RESULT_OK);
                    switch (propertyValue) {
                        case "48":  setResult(response, DConnectMessage.RESULT_ERROR);  break;
                        case "49":  setResult(response, DConnectMessage.RESULT_OK);     break;
                        default:    createInvalidKadecotResponseError(response);        break;
                    }
                } else if (result.getServerResult().equals(NO_RESULT)) {
                    MessageUtils.setNotSupportAttributeError(response, "This device not support 'set' procedure.");
                } else {
                    createInvalidKadecotResponseError(response);
                }
            } else {
                createInvalidKadecotResponseError(response);
            }
        }
        getContext().sendBroadcast(response);
    }

    /**
     * onDeleteOther method handler.
     *
     * @param request Request parameter.
     * @param response Response parameter.
     * @return Send response (true) / Not send response (false).
     */
    protected boolean onDeleteOther(final Intent request, final Intent response) {
        String serviceId = getServiceID(request);
        String[] element = ((KadecotDeviceService) getContext()).getElementFromServiceId(serviceId);
        if (element[IDX_PREFIX].equals(PREFIX_KADECOT) && element[IDX_DEVICEID] != null
                && element[IDX_PROFILENAME].equals(PROFILE_NAME)) {
            setErrAttribute(response);
        } else {
            MessageUtils.setNotFoundServiceError(response);
        }
        return true;
    }


    /**
     * Attributeがnullかどうか.
     *
     * @param request リクエストパラメータ
     * @return Attributeがnullの場合はtrue
     */
    protected boolean isNullAttribute(final Intent request) {
        return getAttribute(request) == null;
    }

    /**
     * UnknownAttributeErrorのレスポンスを返す.
     *
     * @param response レスポンスパラメータ
     */
    protected void setErrAttribute(final Intent response) {
        MessageUtils.setUnknownAttributeError(response);
    }

    /**
     * Creates an error of "unknown error" for Kadecot server response.
     *
     * @param response Intent to store the response.
     */
    private void createInvalidKadecotResponseError(final Intent response) {
        MessageUtils.setUnknownError(response, "Invalid response from Kadecot server.");
    }

    /**
     * ライトID取得.
     *
     * @param request request
     * @return lightid
     */
    private static String getLightID(final Intent request) {
        return request.getStringExtra(PARAM_LIGHT_ID);
    }

    /**
     * 名前取得.
     *
     * @param request request
     * @return myName
     */
    private static String getName(final Intent request) {
        return request.getStringExtra(PARAM_NAME);
    }

    /**
     * 輝度取得.
     *
     * @param request request
     * @return PARAM_BRIGHTNESS
     */
    private static String getBrightness(final Intent request) {
        return request.getStringExtra(PARAM_BRIGHTNESS);
    }

    /**
     * リクエストからcolorパラメータを取得する.
     *
     * @param request リクエスト
     * @return colorパラメータ
     */
    private static String getColor(final Intent request) {
        return request.getStringExtra(PARAM_COLOR);
    }

    /**
     * Get flashing parameter.
     *
     * @param request request
     * @return PARAM_FLASHING
     */
    private static String getFlashing(final Intent request) {
        return request.getStringExtra(PARAM_FLASHING);
    }

    /**
     * Get brightness parameter.
     *
     * @param request request
     * @param response response
     * @return Brightness parameter, if -1, parameter error.
     */
    private static float getBrightnessParam(final Intent request, final Intent response) {
        float brightness;
        if (getBrightness(request) != null) {
            try {
                brightness = Float.valueOf(getBrightness(request));
                if (brightness > 1.0 || brightness < 0) {
                    MessageUtils.setInvalidRequestParameterError(response,
                            "brightness should be a value between 0 and 1.0");
                    return -1;
                }
            } catch (NumberFormatException e) {
                MessageUtils
                        .setInvalidRequestParameterError(response, "brightness should be a value between 0 and 1.0");
                return -1;
            }
        } else {
            brightness = 1;
        }
        return brightness;
    }

    /**
     * Get color parameter.
     *
     * @param request request
     * @param response response
     * @param color Color parameter.
     * @return true : Success, false : failure.
     */
    private static boolean getColorParam(final Intent request, final Intent response, final int[] color) {
        if (getColor(request) != null) {
            try {
                String colorParam = getColor(request);
                String rr = colorParam.substring(0, 2);
                String gg = colorParam.substring(2, 4);
                String bb = colorParam.substring(4, 6);
                if (colorParam.length() == RGB_LENGTH) {
                    color[0] = Integer.parseInt(rr, 16);
                    color[1] = Integer.parseInt(gg, 16);
                    color[2] = Integer.parseInt(bb, 16);
                } else {
                    MessageUtils.setInvalidRequestParameterError(response);
                    return false;
                }
            } catch (NumberFormatException e) {
                MessageUtils.setInvalidRequestParameterError(response);
                return false;
            } catch (IllegalArgumentException e) {
                MessageUtils.setInvalidRequestParameterError(response);
                return false;
            } catch (IndexOutOfBoundsException e) {
                MessageUtils.setInvalidRequestParameterError(response);
                return false;
            }
        } else {
            color[0] = 0xFF;
            color[1] = 0xFF;
            color[2] = 0xFF;
        }
        return true;
    }


    /**
     * Check flashing parameter.
     *
     * @param request request.
     * @return true : Not found, false : Found..
     */
    private static boolean isNullFlashingParam(final Intent request) {
        return getFlashing(request) == null;
    }

    /**
     * Get flashing parameter.
     *
     * @param request request.
     * @return flashing pattern. : Success, null : Failure.
     */
    private static int[] getFlashingParam(final Intent request) {
        if (getFlashing(request) != null) {
            Pattern p = Pattern.compile(",");
            String[] strData = p.split(getFlashing(request));
            int[] pattern = new int[strData.length];
            int count = 0;

            for (String data : strData) {
                try {
                    int value = Integer.parseInt(data);
                    if (value < 0) {
                        return null;
                    }
                    pattern[count] = value;
                    count++;
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return pattern;
        } else {
            return null;
        }
    }


    /**
     * Check serviceID.
     *
     * @param serviceId ServiceId.
     * @param response Response intent.
     * @return Normal(true) / Abnormal(false).
     */
    private boolean checkServiceId(final String serviceId, final Intent response) {
        KadecotDeviceService service = (KadecotDeviceService) getContext();
        if (serviceId == null) {
            createEmptyServiceId(response);
            return false;
        } else if (!(service.checkServiceId(serviceId))) {
            createNotFoundService(response);
            return false;
        } else {
            return true;
        }
    }

    /**
     * Creates an error of "serviceId is empty".
     *
     * @param response Intent to store the response.
     */
    private void createEmptyServiceId(final Intent response) {
        MessageUtils.setEmptyServiceIdError(response);
    }

    /**
     * Creates an error of "service not found".
     *
     * @param response Intent to store the response.
     */
    private void createNotFoundService(final Intent response) {
        MessageUtils.setNotFoundServiceError(response);
    }

    /**
     * Request Kadecot server.
     *
     * @param response Response.
     * @param serviceId Service ID.
     * @param property Request property.
     * @return Request result. (Processing error is null.)
     */
    protected KadecotResult requestKadecotServer(final Intent response, final String serviceId, final int property) {
        KadecotDeviceService service = (KadecotDeviceService) getContext();
        String[] element = service.getElementFromServiceId(serviceId);
        if (element[IDX_PREFIX].equals(PREFIX_KADECOT) && element[IDX_DEVICEID] != null
                && element[IDX_PROFILENAME].equals(PROFILE_NAME)) {
            KadecotHomeAirConditioner khac = new KadecotHomeAirConditioner();
            String urlstr = khac.exchangeJsonString(element[IDX_DEVICEID], property);
            Cursor cursor = getContext().getContentResolver().query(Uri.parse(urlstr), null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                KadecotResult result = new KadecotResult();
                String strResult = cursor.getString(0);
                result.setServerResult(strResult);
                result.setPropertyName(service.getPropertyName(strResult));
                result.setPropertyValue(service.getPropertyValue(strResult));
                cursor.close();
                return result;
            } else {
                createInvalidKadecotResponseError(response);
                return null;
            }
        } else {
            createInvalidKadecotResponseError(response);
            return null;
        }
    }
}
