/*
 KadecotService
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.kadecot.service;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import org.deviceconnect.android.deviceplugin.kadecot.KadecotDeviceService;
import org.deviceconnect.android.deviceplugin.kadecot.kadecotdevice.ENLObject;
import org.deviceconnect.android.deviceplugin.kadecot.kadecotdevice.KadecotDevice;
import org.deviceconnect.android.deviceplugin.kadecot.kadecotdevice.KadecotHomeAirConditioner;
import org.deviceconnect.android.deviceplugin.kadecot.kadecotdevice.KadecotResult;
import org.deviceconnect.android.deviceplugin.kadecot.profile.KadecotEchonetliteProfile;
import org.deviceconnect.android.deviceplugin.kadecot.profile.KadecotHomeAirConditionerProfile;
import org.deviceconnect.android.deviceplugin.kadecot.profile.KadecotLightProfile;
import org.deviceconnect.android.deviceplugin.kadecot.profile.KadecotPowerProfile;
import org.deviceconnect.android.deviceplugin.kadecot.profile.KadecotTemperatureProfile;
import org.deviceconnect.android.deviceplugin.kadecot.profile.original.AirConditionerProfile;
import org.deviceconnect.android.deviceplugin.kadecot.profile.original.AirConditionerProfileConstants;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.LightProfile;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.message.DConnectMessage;

import static org.deviceconnect.android.profile.DConnectProfile.setResult;

public class KadecotService extends DConnectService {

    /** Kadecot prefix. */
    public static final String PREFIX_KADECOT = "kadecot";

    /** "No result" string. */
    public static final String NO_RESULT = "{}";

    /** Index of prefix. */
    public static final int IDX_PREFIX = 0;

    /** Index of kadecot deviceId. */
    public static final int IDX_DEVICEID = 1;

    /** Index of profile name. */
    public static final int IDX_PROFILENAME = 2;


    private final KadecotDevice mDevice;

    public KadecotService(final KadecotDevice kadecotDevice, final ENLObject object) {
        super(kadecotDevice.getServiceId());
        mDevice = kadecotDevice;
        if (kadecotDevice.getNickname() != null) {
            setName(kadecotDevice.getNickname() + "_" + kadecotDevice.getDeviceId());
        } else {
            setName(object.exchangeServiceId(kadecotDevice.getDeviceType()) + " (Kadecot)");
        }

        setNetworkType(NetworkType.WIFI);
        setOnline(true);

        String serviceId = kadecotDevice.getServiceId();
        if (isSupported(LightProfile.PROFILE_NAME, serviceId)) {
            addProfile(new KadecotLightProfile());
        } else if (isSupported(AirConditionerProfile.PROFILE_NAME, serviceId)) {
            addProfile(new KadecotHomeAirConditionerProfile());
            addProfile(new KadecotTemperatureProfile());
            addProfile(new KadecotPowerProfile());
        }
        addProfile(new KadecotEchonetliteProfile());
    }

    private static boolean isSupported(final String profileName, final String serviceId) {
        String[] element = KadecotDeviceService.getElementFromServiceId(serviceId);
        return profileName.equals(element[IDX_PROFILENAME]);
    }

    /**
     * Creates an error of "unknown error" for Kadecot server response.
     *
     * @param response Intent to store the response.
     */
    public static void createInvalidKadecotResponseError(final Intent response) {
        MessageUtils.setUnknownError(response, "There is a problem with the response from the Kadecot server.");
    }

    /**
     * Request Kadecot server.
     *
     * @param response Response.
     * @param serviceId Service ID.
     * @param property Request property.
     * @param value Set property value.
     * @return Request result. (Processing error is null.)
     */
     public static KadecotResult requestKadecotServer(final Context context, final Intent response, final String serviceId, final int property,
                                                      final int value) {
        String[] element = KadecotDeviceService.getElementFromServiceId(serviceId);
        if (element[IDX_PREFIX].equals(PREFIX_KADECOT) && element[IDX_DEVICEID] != null
                && element[IDX_PROFILENAME].equals(AirConditionerProfileConstants.PROFILE_NAME)) {
            KadecotHomeAirConditioner khac = new KadecotHomeAirConditioner();
            String urlstr = khac.exchangeJsonString(element[IDX_DEVICEID], property, value);
            Cursor cursor = context.getContentResolver().query(Uri.parse(urlstr), null, null, null, null);
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

    /**
     * Request Kadecot server.
     *
     * @param context Context.
     * @param response Response.
     * @param serviceId Service ID.
     * @param property Request property.
     * @return Request result. (Processing error is null.)
     */
    public static KadecotResult requestKadecotServer(final Context context, final Intent response, final String serviceId, final int property) {
        String[] element = KadecotDeviceService.getElementFromServiceId(serviceId);
        if (element[IDX_PREFIX].equals(PREFIX_KADECOT) && element[IDX_DEVICEID] != null
                && element[IDX_PROFILENAME].equals(AirConditionerProfileConstants.PROFILE_NAME)) {
            KadecotHomeAirConditioner khac = new KadecotHomeAirConditioner();
            String urlstr = khac.exchangeJsonString(element[IDX_DEVICEID], property);
            Cursor cursor = context.getContentResolver().query(Uri.parse(urlstr), null, null, null, null);
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

    /**
     * Get Power Status.
     * @param response Response Message.
     * @param result Kedecot Result.
     */
    public static void getPowerStatus(Intent response, KadecotResult result) {
        if (result != null) {
            String propertyName = result.getPropertyName();
            String propertyValue = result.getPropertyValue();
            if (propertyName != null && propertyValue != null) {
                if (propertyName.equals(KadecotHomeAirConditioner.PROP_OPERATIONSTATUS)) {
                    setResult(response, DConnectMessage.RESULT_OK);
                    switch (propertyValue) {
                        case "48":  AirConditionerProfile.setPowerStatus(response, "ON");         break;
                        case "49":  AirConditionerProfile.setPowerStatus(response, "OFF");        break;
                        default:    AirConditionerProfile.setPowerStatus(response, "UNKNOWN");    break;
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
    }

    /**
     * Set Power.
     * @param response Response Message.
     * @param result Kedecot Result.
     */
    public static void powerOn(Intent response, KadecotResult result) {
        if (result != null) {
            String propertyName = result.getPropertyName();
            String propertyValue = result.getPropertyValue();
            if (propertyName != null && propertyValue != null) {
                if (propertyName.equals(KadecotHomeAirConditioner.PROP_OPERATIONSTATUS)) {
                    switch (propertyValue) {
                        case "48":  setResult(response, DConnectMessage.RESULT_OK);     break;
                        case "49":  setResult(response, DConnectMessage.RESULT_ERROR);  break;
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
    }

    /**
     * Power Off.
     * @param response Response Message.
     * @param result Kadecot Result.
     */
    public static void powerOff(final Intent response, final KadecotResult result) {
        if (result != null) {
            String propertyName = result.getPropertyName();
            String propertyValue = result.getPropertyValue();
            if (propertyName != null && propertyValue != null) {
                if (propertyName.equals(KadecotHomeAirConditioner.PROP_OPERATIONSTATUS)) {
                    switch (propertyValue) {
                        case "48":  setResult(response, DConnectMessage.RESULT_ERROR);  break;
                        case "49":  setResult(response, DConnectMessage.RESULT_OK);     break;
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
    }

    public boolean hasDeviceId(final String deviceId) {
        String[] element = KadecotDeviceService.getElementFromServiceId(getId());
        return deviceId.equals(element[IDX_DEVICEID]);
    }

    public KadecotDevice getDevice() {
        return mDevice;
    }
}
