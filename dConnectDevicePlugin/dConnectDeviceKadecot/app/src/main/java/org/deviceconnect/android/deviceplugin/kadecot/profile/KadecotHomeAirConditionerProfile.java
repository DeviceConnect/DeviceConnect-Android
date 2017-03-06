/*
 KadecotHomeAirConditionerProfile.java
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
import org.deviceconnect.android.deviceplugin.kadecot.kadecotdevice.KadecotHomeAirConditioner;
import org.deviceconnect.android.deviceplugin.kadecot.kadecotdevice.KadecotResult;
import org.deviceconnect.android.deviceplugin.kadecot.profile.original.AirConditionerProfile;
import org.deviceconnect.android.deviceplugin.kadecot.service.KadecotService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.deviceconnect.android.deviceplugin.kadecot.service.KadecotService.IDX_DEVICEID;
import static org.deviceconnect.android.deviceplugin.kadecot.service.KadecotService.IDX_PREFIX;
import static org.deviceconnect.android.deviceplugin.kadecot.service.KadecotService.IDX_PROFILENAME;
import static org.deviceconnect.android.deviceplugin.kadecot.service.KadecotService.NO_RESULT;
import static org.deviceconnect.android.deviceplugin.kadecot.service.KadecotService.PREFIX_KADECOT;


/**
 * Home Air Conditioner Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class KadecotHomeAirConditionerProfile extends AirConditionerProfile {

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
                    getAirConditioner(request, response);
                } else {
                    if (isEqual(KadecotHomeAirConditionerProfile.ATTRIBUTE_POWER_SAVING, attr)) {
                        getAirConditionerOperationPowerSaving(request, response);
                    } else if (isEqual(KadecotHomeAirConditionerProfile.ATTRIBUTE_MODE_SETTING, attr)) {
                        getAirConditionerOperationModeSetting(request, response);
                    } else if (isEqual(KadecotHomeAirConditionerProfile.ATTRIBUTE_ROOM_TEMPERATURE, attr)) {
                        getAirConditionerRoomTemperature(request, response);
                    } else if (isEqual(KadecotHomeAirConditionerProfile.ATTRIBUTE_TEMPERATURE, attr)) {
                        getAirConditionerTemperatureValue(request, response);
                    } else if (isEqual(KadecotHomeAirConditionerProfile.ATTRIBUTE_AIR_FLOW, attr)) {
                        getAirConditionerAirFlowValue(request, response);
                    } else if (isEqual(KadecotHomeAirConditionerProfile.ATTRIBUTE_ENL_PROPERTY, attr)) {
                        getAirConditionerECHONETLiteProperty(request, response);
                    } else {
                        MessageUtils.setNotSupportAttributeError(response);
                        sendResponse(response);
                    }
                }
            } else if (action.endsWith(DConnectMessage.METHOD_POST)) {
                MessageUtils.setNotSupportAttributeError(response);
                sendResponse(response);
            } else if (action.endsWith(DConnectMessage.METHOD_PUT)) {
                if (attr == null) {
                    putAirConditioner(request, response);
                } else {
                    if (isEqual(KadecotHomeAirConditionerProfile.ATTRIBUTE_POWER_SAVING, attr)) {
                        putAirConditionerOperationPowerSaving(request, response);
                    } else if (isEqual(KadecotHomeAirConditionerProfile.ATTRIBUTE_MODE_SETTING, attr)) {
                        putAirConditionerOperationModeSetting(request, response);
                    } else if (isEqual(KadecotHomeAirConditionerProfile.ATTRIBUTE_TEMPERATURE, attr)) {
                        putAirConditionerTemperatureValue(request, response);
                    } else if (isEqual(KadecotHomeAirConditionerProfile.ATTRIBUTE_AIR_FLOW, attr)) {
                        putAirConditionerAirFlowValue(request, response);
                    } else if (isEqual(KadecotHomeAirConditionerProfile.ATTRIBUTE_ENL_PROPERTY, attr)) {
                        putAirConditionerECHONETLiteProperty(request, response);
                    } else {
                        MessageUtils.setNotSupportAttributeError(response);
                        sendResponse(response);
                    }
                }
            } else if (action.endsWith(DConnectMessage.METHOD_DELETE)) {
                if (attr == null) {
                    deleteAirConditioner(request, response);
                } else {
                    MessageUtils.setNotSupportAttributeError(response);
                    sendResponse(response);
                }
            }
            return null;
        }
    }

    public KadecotHomeAirConditionerProfile() {
        addApi(mGetAirConditionerApi);
        addApi(mGetOperationPowerSavingApi);
        addApi(mGetOperationModeSettingApi);
        addApi(mGetRoomTemperatureApi);
        addApi(mGetTemperatureValueApi);
        addApi(mGetAirFlowValueApi);
        addApi(mGetECHONETLitePropertyApi);
        addApi(mPutAirConditionerApi);
        addApi(mPutOperationPowerSavingApi);
        addApi(mPutOperationModeSettingApi);
        addApi(mPutTemperatureValueApi);
        addApi(mPutAirFlowValueApi);
        addApi(mPutECHONETLitePropertyApi);
        addApi(mDeleteAirConditionerApi);
    }

    private final DConnectApi mGetAirConditionerApi = new GetApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            new KadecotServerQueryTask().execute(request, response);
            return false;
        }
    };

    /**
     * Get air conditioner power status.
     *
     * @param request Request.
     * @param response Response.
     */
    private void getAirConditioner(final Intent request, final Intent response) {
        KadecotResult result = KadecotService.requestKadecotServer(getContext(),response, getServiceID(request),
                KadecotHomeAirConditioner.POWERSTATE_GET);
        KadecotService.getPowerStatus(response, result);
        sendResponse(response);
    }



    private final DConnectApi mGetOperationPowerSavingApi = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_POWER_SAVING;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            new KadecotServerQueryTask().execute(request, response);
            return false;
        }
    };

    /**
     * Get air conditioner operation power saving status.
     *
     * @param request Request.
     * @param response Response.
     */
    protected void getAirConditionerOperationPowerSaving(final Intent request, final Intent response) {
        KadecotResult result = KadecotService.requestKadecotServer(getContext(),response, getServiceID(request),
                KadecotHomeAirConditioner.POWERSAVING_GET);
        if (result != null) {
            String propertyName = result.getPropertyName();
            String propertyValue = result.getPropertyValue();
            if (propertyName != null && propertyValue != null) {
                if (propertyName.equals(KadecotHomeAirConditioner.PROP_POWERSAVINGOPERATIONSETTING)) {
                    setResult(response, DConnectMessage.RESULT_OK);
                    switch (propertyValue) {
                        case "65":  setPowerSaving(response, "PowerSaving");   break;
                        case "66":  setPowerSaving(response, "Normal");        break;
                        default:    setPowerSaving(response, "UNKNOWN");       break;
                    }
                } else if (result.getServerResult().equals(NO_RESULT)) {
                    MessageUtils.setNotSupportAttributeError(response, "This device not support 'get' procedure.");
                } else {
                    KadecotService.createInvalidKadecotResponseError(response);
                }
            } else {
                KadecotService.createInvalidKadecotResponseError(response);
            }
        }
        sendResponse(response);
    }

    private final DConnectApi mGetOperationModeSettingApi = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_MODE_SETTING;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            new KadecotServerQueryTask().execute(request, response);
            return false;
        }
    };

    /**
     * Get air conditioner operation mode setting status.
     *
     * @param request Request.
     * @param response Response.
     */
    protected void getAirConditionerOperationModeSetting(final Intent request, final Intent response) {
        KadecotResult result = KadecotService.requestKadecotServer(getContext(),response, getServiceID(request),
                KadecotHomeAirConditioner.OPERATIONMODE_GET);
        if (result != null) {
            String propertyName = result.getPropertyName();
            String propertyValue = result.getPropertyValue();
            if (propertyName != null && propertyValue != null) {
                if (propertyName.equals(KadecotHomeAirConditioner.PROP_OPERATIONMODESETTING)) {
                    setResult(response, DConnectMessage.RESULT_OK);
                    switch (propertyValue) {
                        case "64":  setModeSetting(response, "Other");             break;
                        case "65":  setModeSetting(response, "Automatic");         break;
                        case "66":  setModeSetting(response, "Cooling");           break;
                        case "67":  setModeSetting(response, "Heating");           break;
                        case "68":  setModeSetting(response, "Dehumidification");  break;
                        case "69":  setModeSetting(response, "AirCirculator");     break;
                        default:    setModeSetting(response, "UNKNOWN");           break;
                    }
                } else if (result.getServerResult().equals(NO_RESULT)) {
                    MessageUtils.setNotSupportAttributeError(response, "This device not support 'get' procedure.");
                } else {
                    KadecotService.createInvalidKadecotResponseError(response);
                }
            } else {
                KadecotService.createInvalidKadecotResponseError(response);
            }
        }
        sendResponse(response);
    }

    private final DConnectApi mGetRoomTemperatureApi = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ROOM_TEMPERATURE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            new KadecotServerQueryTask().execute(request, response);
            return false;
        }
    };

    /**
     * Get air conditioner room temperature.
     *
     * @param request Request.
     * @param response Response.
     */
    protected void getAirConditionerRoomTemperature(final Intent request, final Intent response) {
        KadecotResult result = KadecotService.requestKadecotServer(getContext(),response, getServiceID(request),
                KadecotHomeAirConditioner.ROOMTEMPERATURE_GET);
        if (result != null) {
            String propertyName = result.getPropertyName();
            String propertyValue = result.getPropertyValue();
            if (propertyName != null && propertyValue != null) {
                if (propertyName.equals(KadecotHomeAirConditioner.PROP_MEASUREDVALUEOFROOMTEMPERATURE)) {
                    setResult(response, DConnectMessage.RESULT_OK);
                    int value = Integer.parseInt(propertyValue);
                    if ((value & 0x80) == 0x80) {
                        value = ((~(value) & 0x7F) + 1) * -1;
                    }
                    setRoomTemperature(response, String.valueOf(value));
                } else if (result.getServerResult().equals(NO_RESULT)) {
                    MessageUtils.setNotSupportAttributeError(response, "This device not support 'get' procedure.");
                } else {
                    KadecotService.createInvalidKadecotResponseError(response);
                }
            } else {
                KadecotService.createInvalidKadecotResponseError(response);
            }
        }
        sendResponse(response);
    }

    private final DConnectApi mGetTemperatureValueApi = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_TEMPERATURE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            new KadecotServerQueryTask().execute(request, response);
            return false;
        }
    };

    /**
     * Get air conditioner temperature value.
     *
     * @param request Request.
     * @param response Response.
     */
    protected void getAirConditionerTemperatureValue(final Intent request, final Intent response) {
        KadecotResult result = KadecotService.requestKadecotServer(getContext(),response, getServiceID(request),
                KadecotHomeAirConditioner.TEMPERATUREVALUE_GET);
        if (result != null) {
            String propertyName = result.getPropertyName();
            String propertyValue = result.getPropertyValue();
            if (propertyName != null && propertyValue != null) {
                if (propertyName.equals(KadecotHomeAirConditioner.PROP_SETTEMPERATUREVALUE)) {
                    setResult(response, DConnectMessage.RESULT_OK);
                    setTemperature(response, propertyValue);
                } else if (result.getServerResult().equals(NO_RESULT)) {
                    MessageUtils.setNotSupportAttributeError(response, "This device not support 'get' procedure.");
                } else {
                    KadecotService.createInvalidKadecotResponseError(response);
                }
            } else {
                KadecotService.createInvalidKadecotResponseError(response);
            }
        }
        sendResponse(response);
    }

    private final DConnectApi mGetAirFlowValueApi = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_AIR_FLOW;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            new KadecotServerQueryTask().execute(request, response);
            return false;
        }
    };

    /**
     * Get air conditioner air flow value.
     *
     * @param request Request.
     * @param response Response.
     */
    protected void getAirConditionerAirFlowValue(final Intent request, final Intent response) {
        KadecotResult result = KadecotService.requestKadecotServer(getContext(),response, getServiceID(request),
                KadecotHomeAirConditioner.AIRFLOW_GET);
        if (result != null) {
            String propertyName = result.getPropertyName();
            String propertyValue = result.getPropertyValue();
            if (propertyName != null && propertyValue != null) {
                if (propertyName.equals(KadecotHomeAirConditioner.PROP_AIRFLOWRATESETTING)) {
                    setResult(response, DConnectMessage.RESULT_OK);
                    setAirFlowAuto(response, "false");
                    switch (propertyValue) {
                        case "49":  setAirFlow(response, "0.11");  break;
                        case "50":  setAirFlow(response, "0.24");  break;
                        case "51":  setAirFlow(response, "0.37");  break;
                        case "52":  setAirFlow(response, "0.50");  break;
                        case "53":  setAirFlow(response, "0.63");  break;
                        case "54":  setAirFlow(response, "0.76");  break;
                        case "55":  setAirFlow(response, "0.89");  break;
                        case "56":  setAirFlow(response, "1.0");   break;
                        case "65":
                            setAirFlow(response, "0.0");
                            setAirFlowAuto(response, "true");
                            break;
                        default:    setAirFlow(response, "-1.0");  break;
                    }
                } else if (result.getServerResult().equals(NO_RESULT)) {
                    MessageUtils.setNotSupportAttributeError(response, "This device not support 'get' procedure.");
                } else {
                    KadecotService.createInvalidKadecotResponseError(response);
                }
            } else {
                KadecotService.createInvalidKadecotResponseError(response);
            }
        }
        sendResponse(response);
    }

    private final DConnectApi mGetECHONETLitePropertyApi = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ENL_PROPERTY;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            new KadecotServerQueryTask().execute(request, response);
            return false;
        }
    };

    /**
     * Get air conditioner ECHONET Lite property.
     *
     * @param request Request.
     * @param response Response.
     */
    protected void getAirConditionerECHONETLiteProperty(final Intent request, final Intent response) {
        String[] element = KadecotDeviceService.getElementFromServiceId(getServiceID(request));
        if (element[IDX_PREFIX].equals(PREFIX_KADECOT) && element[IDX_DEVICEID] != null
                && element[IDX_PROFILENAME].equals(PROFILE_NAME)) {
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
                        KadecotService.createInvalidKadecotResponseError(response);
                        sendResponse(response);
                        return;
                    }
                    cursor.close();
                } else {
                    KadecotService.createInvalidKadecotResponseError(response);
                    sendResponse(response);
                    return;
                }
            }
            setResult(response, DConnectMessage.RESULT_OK);
            response.putExtra(PARAM_PROPERTIES, dataList.toArray(new Bundle[dataList.size()]));
        } else {
            KadecotService.createInvalidKadecotResponseError(response);
        }
        sendResponse(response);
    }

    private final DConnectApi mPutAirConditionerApi = new PutApi() {

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            new KadecotServerQueryTask().execute(request, response);
            return false;
        }
    };

    /**
     * Put air conditioner power status.
     *
     * @param request Request.
     * @param response Response.
     */
    private void putAirConditioner(final Intent request, final Intent response) {
        KadecotResult result = KadecotService.requestKadecotServer(getContext(),response, getServiceID(request),
                KadecotHomeAirConditioner.POWERSTATE_ON);
        KadecotService.powerOn(response, result);
        sendResponse(response);
    }



    private final DConnectApi mPutOperationPowerSavingApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_POWER_SAVING;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            new KadecotServerQueryTask().execute(request, response);
            return false;
        }
    };

    /**
     * Put air conditioner operation power saving status.
     *
     * @param request Request.
     * @param response Response.
     */
    protected void putAirConditionerOperationPowerSaving(final Intent request, final Intent response) {
        int index;
        String status = getPowerSaving(request);
        if (status == null) {
            MessageUtils.setInvalidRequestParameterError(response);
            sendResponse(response);
            return;
        }

        switch (status) {
            case "Normal":      index = KadecotHomeAirConditioner.POWERSAVING_OFF; break;
            case "PowerSaving": index = KadecotHomeAirConditioner.POWERSAVING_ON;  break;
            default:
                MessageUtils.setInvalidRequestParameterError(response);
                sendResponse(response);
                return;
        }

        KadecotResult result = KadecotService.requestKadecotServer(getContext(),response, getServiceID(request), index);
        if (result != null) {
            String propertyName = result.getPropertyName();
            String propertyValue = result.getPropertyValue();
            if (propertyName != null && propertyValue != null) {
                if (propertyName.equals(KadecotHomeAirConditioner.PROP_POWERSAVINGOPERATIONSETTING)) {
                    String powersaving = getPowerSaving(request);
                    switch (propertyValue) {
                        case "65":  checkResult(response, powersaving, "PowerSaving");  break;
                        case "66":  checkResult(response, powersaving, "Normal");       break;
                        default:    KadecotService.createInvalidKadecotResponseError(response);        break;
                    }
                } else if (result.getServerResult().equals(NO_RESULT)) {
                    MessageUtils.setNotSupportAttributeError(response, "This device not support 'get' procedure.");
                } else {
                    KadecotService.createInvalidKadecotResponseError(response);
                }
            } else {
                KadecotService.createInvalidKadecotResponseError(response);
            }
        }
        sendResponse(response);
    }

    private final DConnectApi mPutOperationModeSettingApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_MODE_SETTING;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            new KadecotServerQueryTask().execute(request, response);
            return false;
        }
    };

    /**
     * Put air conditioner operation mode setting status.
     *
     * @param request Request.
     * @param response Response.
     */
    protected void putAirConditionerOperationModeSetting(final Intent request, final Intent response) {
        int index;
        String status = getModeSetting(request);
        if (status == null) {
            MessageUtils.setInvalidRequestParameterError(response);
            sendResponse(response);
            return;
        }

        switch (getModeSetting(request)) {
            case "Other":               index = KadecotHomeAirConditioner.OPERATIONMODE_OTHER; break;
            case "Automatic":           index = KadecotHomeAirConditioner.OPERATIONMODE_AUTO;  break;
            case "Cooling":             index = KadecotHomeAirConditioner.OPERATIONMODE_COOL;  break;
            case "Heating":             index = KadecotHomeAirConditioner.OPERATIONMODE_HEAT;  break;
            case "Dehumidification":    index = KadecotHomeAirConditioner.OPERATIONMODE_DRY;   break;
            case "AirCirculator":       index = KadecotHomeAirConditioner.OPERATIONMODE_WIND;  break;
            default:
                MessageUtils.setInvalidRequestParameterError(response);
                sendResponse(response);
                return;
        }

        KadecotResult result = KadecotService.requestKadecotServer(getContext(),response, getServiceID(request), index);
        if (result != null) {
            String propertyName = result.getPropertyName();
            String propertyValue = result.getPropertyValue();
            if (propertyName != null && propertyValue != null) {
                if (propertyName.equals(KadecotHomeAirConditioner.PROP_OPERATIONMODESETTING)) {
                    String mode = getModeSetting(request);
                    switch (propertyValue) {
                        case "64":  checkResult(response, mode, "Other");               break;
                        case "65":  checkResult(response, mode, "Automatic");           break;
                        case "66":  checkResult(response, mode, "Cooling");             break;
                        case "67":  checkResult(response, mode, "Heating");             break;
                        case "68":  checkResult(response, mode, "Dehumidification");    break;
                        case "69":  checkResult(response, mode, "AirCirculator");       break;
                        default:    KadecotService.createInvalidKadecotResponseError(response);        break;
                    }
                } else if (result.getServerResult().equals(NO_RESULT)) {
                    MessageUtils.setNotSupportAttributeError(response, "This device not support 'get' procedure.");
                } else {
                    KadecotService.createInvalidKadecotResponseError(response);
                }
            } else {
                KadecotService.createInvalidKadecotResponseError(response);
            }
        }
        sendResponse(response);
    }

    private final DConnectApi mPutTemperatureValueApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_TEMPERATURE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            new KadecotServerQueryTask().execute(request, response);
            return false;
        }
    };

    /**
     * Put air conditioner temperature value.
     *
     * @param request Request.
     * @param response Response.
     */
    protected void putAirConditionerTemperatureValue(final Intent request, final Intent response) {
        int value = getTemperature(request);
        if (value == -1 || value < 0 || value > 50) {
            MessageUtils.setInvalidRequestParameterError(response);
            sendResponse(response);
            return;
        }

        KadecotResult result = KadecotService.requestKadecotServer(getContext(),
                response, getServiceID(request),
                KadecotHomeAirConditioner.TEMPERATUREVALUE_SET, value);
        if (result != null) {
            String propertyName = result.getPropertyName();
            String propertyValue = result.getPropertyValue();
            if (propertyName != null && propertyValue != null) {
                if (propertyName.equals(KadecotHomeAirConditioner.PROP_SETTEMPERATUREVALUE)) {
                    if (Integer.parseInt(propertyValue) == value) {
                        setResult(response, DConnectMessage.RESULT_OK);
                    } else {
                        setResult(response, DConnectMessage.RESULT_ERROR);
                    }
                } else if (result.getServerResult().equals(NO_RESULT)) {
                    MessageUtils.setNotSupportAttributeError(response, "This device not support 'get' procedure.");
                } else {
                    KadecotService.createInvalidKadecotResponseError(response);
                }
            } else {
                KadecotService.createInvalidKadecotResponseError(response);
            }
        }
        sendResponse(response);
    }

    private final DConnectApi mPutAirFlowValueApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_AIR_FLOW;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            new KadecotServerQueryTask().execute(request, response);
            return false;
        }
    };

    /**
     * Put air conditioner air flow value.
     *
     * @param request Request.
     * @param response Response.
     */
    protected void putAirConditionerAirFlowValue(final Intent request, final Intent response) {
        if (isNullAirFlow(request) && isNullAirFlowAuto(request)) {
            MessageUtils.setInvalidRequestParameterError(response);
            sendResponse(response);
            return;
        }

        boolean auto = getAirFlowAuto(request);
        int param;

        if (auto) {
            param = KadecotHomeAirConditioner.AIRFLOW_AUTO;
        } else {
            int value = (int) (getAirFlow(request) * 100);
            if (value >= 0 && value < 12) {
                param = KadecotHomeAirConditioner.AIRFLOW_LV1;
            } else if (value >= 12 && value < 25) {
                param = KadecotHomeAirConditioner.AIRFLOW_LV2;
            } else if (value >= 25 && value < 38) {
                param = KadecotHomeAirConditioner.AIRFLOW_LV3;
            } else if (value >= 38 && value < 51) {
                param = KadecotHomeAirConditioner.AIRFLOW_LV4;
            } else if (value >= 51 && value < 64) {
                param = KadecotHomeAirConditioner.AIRFLOW_LV5;
            } else if (value >= 64 && value < 77) {
                param = KadecotHomeAirConditioner.AIRFLOW_LV6;
            } else if (value >= 77 && value < 90) {
                param = KadecotHomeAirConditioner.AIRFLOW_LV7;
            } else if (value >= 90 && value <= 100) {
                param = KadecotHomeAirConditioner.AIRFLOW_LV8;
            } else {
                MessageUtils.setInvalidRequestParameterError(response);
                sendResponse(response);
                return;
            }
        }

        KadecotResult result = KadecotService.requestKadecotServer(getContext(),response, getServiceID(request), param);
        if (result != null) {
            String propertyName = result.getPropertyName();
            String propertyValue = result.getPropertyValue();
            if (propertyName != null && propertyValue != null) {
                if (propertyName.equals(KadecotHomeAirConditioner.PROP_AIRFLOWRATESETTING)) {
                    if (propertyName.equals(KadecotHomeAirConditioner.PROP_AIRFLOWRATESETTING)) {
                        switch (propertyValue) {
                            case "49":
                                checkResult(response, param, KadecotHomeAirConditioner.AIRFLOW_LV1);
                                break;
                            case "50":
                                checkResult(response, param, KadecotHomeAirConditioner.AIRFLOW_LV2);
                                break;
                            case "51":
                                checkResult(response, param, KadecotHomeAirConditioner.AIRFLOW_LV3);
                                break;
                            case "52":
                                checkResult(response, param, KadecotHomeAirConditioner.AIRFLOW_LV4);
                                break;
                            case "53":
                                checkResult(response, param, KadecotHomeAirConditioner.AIRFLOW_LV5);
                                break;
                            case "54":
                                checkResult(response, param, KadecotHomeAirConditioner.AIRFLOW_LV6);
                                break;
                            case "55":
                                checkResult(response, param, KadecotHomeAirConditioner.AIRFLOW_LV7);
                                break;
                            case "56":
                                checkResult(response, param, KadecotHomeAirConditioner.AIRFLOW_LV8);
                                break;
                            case "65":
                                checkResult(response, param, KadecotHomeAirConditioner.AIRFLOW_AUTO);
                                break;
                            default:
                                KadecotService.createInvalidKadecotResponseError(response);
                                break;
                        }
                    } else {
                        KadecotService.createInvalidKadecotResponseError(response);
                    }
                } else if (result.getServerResult().equals(NO_RESULT)) {
                    MessageUtils.setNotSupportAttributeError(response, "This device not support 'get' procedure.");
                } else {
                    KadecotService.createInvalidKadecotResponseError(response);
                }
            } else {
                KadecotService.createInvalidKadecotResponseError(response);
            }
        }
        sendResponse(response);
    }

    private final DConnectApi mPutECHONETLitePropertyApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ENL_PROPERTY;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            new KadecotServerQueryTask().execute(request, response);
            return false;
        }
    };

    /**
     * Put air conditioner ECHONET Lite property.
     *
     * @param request Request.
     * @param response Response.
     */
    protected void putAirConditionerECHONETLiteProperty(final Intent request, final Intent response) {
        String[] element = KadecotDeviceService.getElementFromServiceId(getServiceID(request));
        if (element[IDX_PREFIX].equals(PREFIX_KADECOT) && element[IDX_DEVICEID] != null
                && element[IDX_PROFILENAME].equals(PROFILE_NAME)) {
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
                    KadecotService.createInvalidKadecotResponseError(response);
                }
                cursor.close();
            } else {
                KadecotService.createInvalidKadecotResponseError(response);
            }
        } else {
            KadecotService.createInvalidKadecotResponseError(response);
        }
        sendResponse(response);
    }

    private final DConnectApi mDeleteAirConditionerApi = new DeleteApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            new KadecotServerQueryTask().execute(request, response);
            return false;
        }
    };

    /**
     * Delete air conditioner power status.
     *
     * @param request Request.
     * @param response Response.
     */
    protected void deleteAirConditioner(final Intent request, final Intent response) {
        KadecotResult result = KadecotService.requestKadecotServer(getContext(),response, getServiceID(request),
                KadecotHomeAirConditioner.POWERSTATE_OFF);
        KadecotService.powerOff(response, result);
        sendResponse(response);
    }



    /**
     * Check result (Int).
     *
     * @param response Response intent.
     * @param param1 parameter 1.
     * @param param2 parameter 2.
     */
    private void checkResult(final Intent response, final int param1, final int param2) {
        if (param1 == param2) {
            setResult(response, DConnectMessage.RESULT_OK);
        } else {
            setResult(response, DConnectMessage.RESULT_ERROR);
        }
    }

    /**
     * Check result (String).
     *
     * @param response Response intent.
     * @param param1 parameter 1.
     * @param param2 parameter 2.
     */
    private void checkResult(final Intent response, final String param1, final String param2) {
        if (param1.equals(param2)) {
            setResult(response, DConnectMessage.RESULT_OK);
        } else {
            setResult(response, DConnectMessage.RESULT_ERROR);
        }
    }


}
