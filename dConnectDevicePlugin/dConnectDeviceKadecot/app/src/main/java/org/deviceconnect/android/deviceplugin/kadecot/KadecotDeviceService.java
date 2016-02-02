/*
 KadecotDeviceService
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.kadecot;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import org.deviceconnect.android.deviceplugin.kadecot.kadecotdevice.ENLObject;
import org.deviceconnect.android.deviceplugin.kadecot.kadecotdevice.KadecotDevice;
import org.deviceconnect.android.deviceplugin.kadecot.profile.KadecotHomeAirConditionerProfile;
import org.deviceconnect.android.deviceplugin.kadecot.profile.KadecotLightProfile;
import org.deviceconnect.android.deviceplugin.kadecot.profile.KadecotServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.kadecot.profile.KadecotServiceInformationProfile;
import org.deviceconnect.android.deviceplugin.kadecot.profile.KadecotSystemProfile;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.ServiceInformationProfile;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Kadecot Device Service.
 *
 * @author NTT DOCOMO, INC.
 */
public class KadecotDeviceService extends DConnectMessageService {

    /** Application instance. */
    private KadecotDeviceApplication mApp;
    /** Logger. */
    private final Logger mLogger = Logger.getLogger("kadecot.dplugin");
    /** Kadecot device list. */
    private ArrayList<KadecotDevice> mKadecotDevices = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();

        mApp = (KadecotDeviceApplication) this.getApplication();

        addProfile(new KadecotHomeAirConditionerProfile());
        addProfile(new KadecotLightProfile());
        addProfile(new KadecotServiceInformationProfile(this));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        super.onStartCommand(intent, flags, startId);

        if (BuildConfig.DEBUG) {
            if (intent != null) {
                String action = intent.getAction();
                mLogger.info("onStartCommand: action=" + action);
            }
        }

        return START_STICKY;
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new KadecotSystemProfile();
    }

    @Override
    protected ServiceInformationProfile getServiceInformationProfile() {
        return new ServiceInformationProfile(this) { };
    }

    @Override
    protected ServiceDiscoveryProfile getServiceDiscoveryProfile() {
        return new KadecotServiceDiscoveryProfile(this);
    }

    /**
     *  Device search async task.
     */
    public class DeviceSearchAsyncTask extends AsyncTask<Intent, Void, Void> {

        @Override
        protected Void doInBackground(final Intent... intents) {
            Intent response = intents[0];
            List<Bundle> services = new ArrayList<>();
            String urlstr = "content://com.sonycsl.kadecot.json.provider/jsonp/v1/devices/";

            Cursor cursor = getContentResolver().query(Uri.parse(urlstr), null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();

                try {
                    JSONObject rootObject = new JSONObject(cursor.getString(0));
                    JSONArray resultArray = rootObject.getJSONArray("deviceList");

                    int count = resultArray.length();
                    JSONObject[] kadecotObject = new JSONObject[count];
                    for (int i = 0; i < count; i++) {
                        kadecotObject[i] = resultArray.getJSONObject(i);
                    }

                    mKadecotDevices.clear();

                    for (JSONObject obj : kadecotObject) {
                        KadecotDevice kadecotDevice = new KadecotDevice();

                        kadecotDevice.setDeviceId(obj.getString("deviceId"));
                        kadecotDevice.setProtocol(obj.getString("protocol"));
                        kadecotDevice.setDeviceType(obj.getString("deviceType"));
                        kadecotDevice.setDescription(obj.getString("description"));
                        kadecotDevice.setStatus(obj.getString("status"));
                        kadecotDevice.setNickname(obj.getString("nickname"));
                        kadecotDevice.setIpAddr(obj.getString("ip_addr"));
                        kadecotDevice.setLocation(obj.getString("location"));
                        mKadecotDevices.add(kadecotDevice);

                        if (kadecotDevice.getStatus().equals("true")
                                && kadecotDevice.getProtocol().equals("echonetlite")) {
                            String serviceId = "";
                            ENLObject object = mApp.getENLObject();
                            String deviceType = object.exchangeServiceId(kadecotDevice.getDeviceType());
                            if (deviceType != null) {
                                serviceId += "kadecot_" + kadecotDevice.getDeviceId() + "." + deviceType;
                                kadecotDevice.setServiceId(serviceId);

                                Bundle service = new Bundle();
                                service.putString(ServiceDiscoveryProfile.PARAM_ID, kadecotDevice.getServiceId());
                                String deviceName = kadecotDevice.getNickname();
                                if (deviceName != null) {
                                    service.putString(ServiceDiscoveryProfile.PARAM_NAME, deviceName + "_"
                                            + kadecotDevice.getDeviceId());
                                } else {
                                    service.putString(ServiceDiscoveryProfile.PARAM_NAME,
                                            object.exchangeServiceId(kadecotDevice.getDeviceType()) + " (Kadecot)");
                                }
                                service.putString(ServiceDiscoveryProfile.PARAM_TYPE,
                                        ServiceDiscoveryProfile.NetworkType.WIFI.getValue());
                                service.putBoolean(ServiceDiscoveryProfile.PARAM_ONLINE, true);
                                ArrayList<String> scopes = object.getScopesFromClassName(kadecotDevice.getDeviceType());
                                service.putStringArray(ServiceDiscoveryProfileConstants.PARAM_SCOPES,
                                        scopes.toArray(new String[scopes.size()]));
                                services.add(service);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cursor.close();
            }

            response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
            response.putExtra(ServiceDiscoveryProfile.PARAM_SERVICES, services.toArray(new Bundle[services.size()]));
            sendResponse(response);
            return null;
        }
    }
    /**
     * Search device.
     *
     * @param response Response.
     * @return If <code>serviceId</code> is equal to test for serviceId, true.
     *         Otherwise false.
     */
    public boolean searchDevice(final Intent response) {
        new DeviceSearchAsyncTask().execute(response);
        return false;
    }

    /**
     * Check ServiceID.
     * @param serviceId ServiceID.
     * @return If <code>serviceId</code> is equal to test for serviceId, true.
     *         Otherwise false.
     */
    public boolean checkServiceId(final String serviceId) {
        for (int  i = 0; i < mKadecotDevices.size(); i++) {
            String id = mKadecotDevices.get(i).getServiceId();
            if (id != null && id.equals(serviceId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get element from ServiceID.
     *
     * @param serviceId ServiceId.
     * @return Elements.
     */
    public String[] getElementFromServiceId(final String serviceId) {
        Pattern p = Pattern.compile("[_\\.]+");
        return p.split(serviceId);
    }

    /**
     * Get property name.
     *
     * @param response JSON response.
     * @return Property name(Success) / null (Failure).
     */
    public String getPropertyName(final String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            return jsonObject.getString("propertyName");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get property value.
     *
     * @param response JSON response.
     * @return Property value(Success) / null (Failure).
     */
    public String getPropertyValue(final String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            String propertyValue = jsonObject.getString("propertyValue");
            if (propertyValue != null) {
                propertyValue = propertyValue.replace("[", "").replace("]", "");
            }
            return propertyValue;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get NickName.
     *
     * @param serviceId ServiceId.
     * @return Nickname(Success) / null(not found)
     */
    public String getNickName(final String serviceId) {
        for (KadecotDevice device : mKadecotDevices) {
            String id = device.getServiceId();
            if (id != null && id.equals(serviceId)) {
                return device.getNickname();
            }
        }
        return null;
    }


    /**
     * Get application instance.
     *
     * @return Application instance.
     */
    public KadecotDeviceApplication getKadecotDeviceApplication() {
        return mApp;
    }

}
