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

import org.deviceconnect.android.deviceplugin.kadecot.KadecotDeviceService;
import org.deviceconnect.android.deviceplugin.kadecot.kadecotdevice.KadecotGeneralLighting;
import org.deviceconnect.android.deviceplugin.kadecot.kadecotdevice.KadecotHomeAirConditioner;
import org.deviceconnect.android.deviceplugin.kadecot.kadecotdevice.KadecotResult;
import org.deviceconnect.android.deviceplugin.kadecot.service.KadecotService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.LightProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.List;

import static org.deviceconnect.android.deviceplugin.kadecot.service.KadecotService.IDX_DEVICEID;
import static org.deviceconnect.android.deviceplugin.kadecot.service.KadecotService.IDX_PREFIX;
import static org.deviceconnect.android.deviceplugin.kadecot.service.KadecotService.IDX_PROFILENAME;
import static org.deviceconnect.android.deviceplugin.kadecot.service.KadecotService.NO_RESULT;
import static org.deviceconnect.android.deviceplugin.kadecot.service.KadecotService.PREFIX_KADECOT;

/**
 * Light Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class KadecotLightProfile extends LightProfile {


    public KadecotLightProfile() {
        addApi(mGetLightApi);
        addApi(mPostLightApi);
        addApi(mDeleteLightApi);
    }

    /**
     * Kadecot server query task class.
     */
    private class KadecotServerQueryTask extends AsyncTask<Intent, Void, Void> {

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
                    sendResponse(response);
                }
            } else if (action.endsWith(DConnectMessage.METHOD_POST)) {
                if (attr == null) {
                    postLight(request, response);
                } else {
                    MessageUtils.setNotSupportAttributeError(response);
                    sendResponse(response);
                }
            } else if (action.endsWith(DConnectMessage.METHOD_PUT)) {
                MessageUtils.setNotSupportAttributeError(response);
                sendResponse(response);
            } else if (action.endsWith(DConnectMessage.METHOD_DELETE)) {
                if (attr == null) {
                    deleteLight(request, response);
                } else {
                    MessageUtils.setNotSupportAttributeError(response);
                    sendResponse(response);
                }
            }
            return null;
        }
    }

    private final DConnectApi mGetLightApi = new GetApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            new KadecotServerQueryTask().execute(request, response);
            return false;
        }
    };

    /**
     * Get light status.
     *
     * @param request Request.
     * @param response Response.
     */
    private void getLight(final Intent request, final Intent response) {
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
                    String[] element = KadecotDeviceService.getElementFromServiceId(serviceId);
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
        sendResponse(response);
    }

    private final DConnectApi mPostLightApi = new PostApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            KadecotService service = (KadecotService) getService();
            String lightId = getLightId(request);

            // Kadecot plug-in not support brightness, color, flashing.
            if (lightId != null && !service.hasDeviceId(lightId)) {
                MessageUtils.setInvalidRequestParameterError(response, "lightId is not match.");
            } else {
                new KadecotServerQueryTask().execute(request, response);
                return false;
            }
            return true;
        }
    };

    /**
     * Post Light status.
     *
     * @param request Request.
     * @param response Response.
     */
    private void postLight(final Intent request, final Intent response) {
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
        sendResponse(response);
    }

    private final DConnectApi mDeleteLightApi = new DeleteApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            KadecotService service = (KadecotService) getService();
            String lightId = getLightId(request);

            if (lightId != null && !service.hasDeviceId(lightId)) {
                MessageUtils.setInvalidRequestParameterError(response, "lightId is not match.");
            } else {
                new KadecotServerQueryTask().execute(request, response);
                return false;
            }
            return true;
        }
    };

    /**
     * Delete Light status.
     *
     * @param request Request.
     * @param response Response.
     */
    private void deleteLight(final Intent request, final Intent response) {
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
        sendResponse(response);
    }

    /**
     * Creates an error of "unknown error" for Kadecot server response.
     *
     * @param response Intent to store the response.
     */
    private void createInvalidKadecotResponseError(final Intent response) {
        MessageUtils.setUnknownError(response, "There is a problem with the response from the Kadecot server.");
    }

    /**
     * Request Kadecot server.
     *
     * @param response Response.
     * @param serviceId Service ID.
     * @param property Request property.
     * @return Request result. (Processing error is null.)
     */
    private KadecotResult requestKadecotServer(final Intent response, final String serviceId, final int property) {
        String[] element = KadecotDeviceService.getElementFromServiceId(serviceId);
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
                result.setPropertyName(KadecotDeviceService.getPropertyName(strResult));
                result.setPropertyValue(KadecotDeviceService.getPropertyValue(strResult));
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
