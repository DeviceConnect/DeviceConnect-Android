package org.deviceconnect.android.deviceplugin.kadecot.profile;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.kadecot.KadecotDeviceService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


import static org.deviceconnect.android.deviceplugin.kadecot.profile.original.AirConditionerProfile.getEpc;
import static org.deviceconnect.android.deviceplugin.kadecot.profile.original.AirConditionerProfile.getEpcValue;
import static org.deviceconnect.android.deviceplugin.kadecot.profile.original.AirConditionerProfile.setEpc;
import static org.deviceconnect.android.deviceplugin.kadecot.profile.original.AirConditionerProfile.setValue;
import static org.deviceconnect.android.deviceplugin.kadecot.profile.original.AirConditionerProfileConstants.PARAM_EPC;
import static org.deviceconnect.android.deviceplugin.kadecot.profile.original.AirConditionerProfileConstants.PARAM_PROPERTIES;
import static org.deviceconnect.android.deviceplugin.kadecot.profile.original.AirConditionerProfileConstants.PARAM_VALUE;
import static org.deviceconnect.android.deviceplugin.kadecot.service.KadecotService.IDX_DEVICEID;
import static org.deviceconnect.android.deviceplugin.kadecot.service.KadecotService.IDX_PREFIX;
import static org.deviceconnect.android.deviceplugin.kadecot.service.KadecotService.NO_RESULT;
import static org.deviceconnect.android.deviceplugin.kadecot.service.KadecotService.PREFIX_KADECOT;
import static org.deviceconnect.android.deviceplugin.kadecot.service.KadecotService.createInvalidKadecotResponseError;

public class KadecotEchonetliteProfile extends DConnectProfile {

    public KadecotEchonetliteProfile() {

        // GET /gotapi/echonetLite/property
        addApi(new GetApi() {
            @Override
            public String getAttribute() {
                return "property";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                getECHONETLiteProperty(request, response);
                return false;
            }
        });

        // PUT /gotapi/echonetLite/property
        addApi(new PutApi() {
            @Override
            public String getAttribute() {
                return "property";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                putECHONETLiteProperty(request, response);
                return false;
            }
        });

    }

    @Override
    public String getProfileName() {
        return "echonetLite";
    }


    /**
     * Get ECHONET Lite property.
     *
     * @param request Request.
     * @param response Response.
     */
    private void getECHONETLiteProperty(final Intent request, final Intent response) {
        String[] element = KadecotDeviceService.getElementFromServiceId(getServiceID(request));
        if (element[IDX_PREFIX].equals(PREFIX_KADECOT) && element[IDX_DEVICEID] != null) {
            String strEpcs = getEpc(request);
            if (strEpcs == null) {
                MessageUtils.setInvalidRequestParameterError(response);
                sendResponse(response);
                return;
            }

            Pattern p = Pattern.compile(",");
            String[] epcs = p.split(strEpcs);
            List<Bundle> dataList = new ArrayList<>();
            Bundle resultData = new Bundle();

            for (int i = 0; i < epcs.length; i++) {
                String strValue = epcs[i].trim();
                try {
                    int checkInt = Integer.decode(strValue);
                    if (checkInt < 0x0 || checkInt >= 0x100) {
                        MessageUtils.setInvalidRequestParameterError(response);
                        sendResponse(response);
                        return;
                    }
                    epcs[i] = "0x" + Integer.toHexString(checkInt);
                } catch (NumberFormatException e) {
                    MessageUtils.setInvalidRequestParameterError(response);
                    sendResponse(response);
                    return;
                }
            }

            for (String epc : epcs) {
                String urlstr = "content://com.sonycsl.kadecot.json.provider/jsonp/v1/devices/"
                        + element[IDX_DEVICEID] + "?procedure=get&params={\"propertyName\":\"" + epc + "\"}";
                Cursor cursor = getContext().getContentResolver().query(Uri.parse(urlstr), null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    String result = cursor.getString(0);
                    String propertyName = KadecotDeviceService.getPropertyName(result);
                    String propertyValue = KadecotDeviceService.getPropertyValue(result);
                    if (propertyName != null && propertyValue != null) {
                        if (result.equals(NO_RESULT)) {
                            cursor.close();
                            MessageUtils.setNotSupportAttributeError(response,
                                    "This device not support 'get' procedure.");
                            sendResponse(response);
                            return;
                        } else {
                            resultData.putString(PARAM_EPC, propertyName);
                            resultData.putString(PARAM_VALUE, propertyValue);
                            dataList.add((Bundle) resultData.clone());
                        }
                    } else {
                        cursor.close();
                        createInvalidKadecotResponseError(response);
                        sendResponse(response);
                        return;
                    }
                    cursor.close();
                } else {
                    createInvalidKadecotResponseError(response);
                    sendResponse(response);
                    return;
                }
            }
            setResult(response, DConnectMessage.RESULT_OK);
            response.putExtra(PARAM_PROPERTIES, dataList.toArray(new Bundle[dataList.size()]));
        } else {
            createInvalidKadecotResponseError(response);
        }
        sendResponse(response);
    }

    /**
     * Put  ECHONET Lite property.
     *
     * @param request Request.
     * @param response Response.
     */
    private void putECHONETLiteProperty(final Intent request, final Intent response) {
        String[] element = KadecotDeviceService.getElementFromServiceId(getServiceID(request));
        if (element[IDX_PREFIX].equals(PREFIX_KADECOT) && element[IDX_DEVICEID] != null) {
            String epc = getEpc(request);
            String value = getEpcValue(request);
            if (epc == null || value == null) {
                MessageUtils.setInvalidRequestParameterError(response);
                sendResponse(response);
                return;
            }

            try {
                int checkInt = Integer.decode(epc);
                if (checkInt < 0x0 || checkInt >= 0x100) {
                    MessageUtils.setInvalidRequestParameterError(response);
                    sendResponse(response);
                    return;
                }
                epc = "0x" + Integer.toHexString(checkInt);
            } catch (NumberFormatException e) {
                MessageUtils.setInvalidRequestParameterError(response);
                sendResponse(response);
                return;
            }

            Pattern p = Pattern.compile(",");
            String[] values = p.split(value);
            for (int i = 0; i < values.length; i++) {
                String strValue = values[i].trim();
                try {
                    int checkInt = Integer.decode(strValue);
                    if (checkInt < 0x0 || checkInt >= 0x100) {
                        MessageUtils.setInvalidRequestParameterError(response);
                        sendResponse(response);
                        return;
                    }
                    values[i] = "0x" + Integer.toHexString(checkInt);
                } catch (NumberFormatException e) {
                    MessageUtils.setInvalidRequestParameterError(response);
                    sendResponse(response);
                    return;
                }
            }
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                if (i != 0) {
                    buf.append(",");
                }
                buf.append(values[i]);
            }

            String urlstr = "content://com.sonycsl.kadecot.json.provider/jsonp/v1/devices/"
                    + element[IDX_DEVICEID] + "?procedure=set&params={\"propertyName\":\"" + epc
                    + "\",\"propertyValue\":[" + buf.toString() + "]}";

            Cursor cursor = getContext().getContentResolver().query(Uri.parse(urlstr), null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                String result = cursor.getString(0);
                String propertyName = KadecotDeviceService.getPropertyName(result);
                String propertyValue = KadecotDeviceService.getPropertyValue(result);
                if (propertyName != null && propertyValue != null) {
                    if (result.equals(NO_RESULT)) {
                        MessageUtils.setNotSupportAttributeError(response,
                                "This device not support 'get' procedure.");
                    } else {
                        setResult(response, DConnectMessage.RESULT_OK);
                        setEpc(response, propertyName);
                        setValue(response, propertyValue);
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
        sendResponse(response);
    }

}