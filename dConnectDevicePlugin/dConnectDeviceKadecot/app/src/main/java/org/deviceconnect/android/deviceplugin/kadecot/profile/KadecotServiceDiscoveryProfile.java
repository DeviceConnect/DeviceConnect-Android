/*
 KadecotServiceDiscoveryProfile
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.kadecot.profile;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import org.deviceconnect.android.deviceplugin.kadecot.KadecotDeviceApplication;
import org.deviceconnect.android.deviceplugin.kadecot.KadecotDeviceService;
import org.deviceconnect.android.deviceplugin.kadecot.kadecotdevice.ENLObject;
import org.deviceconnect.android.deviceplugin.kadecot.kadecotdevice.KadecotDevice;
import org.deviceconnect.android.deviceplugin.kadecot.service.KadecotService;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.android.service.DConnectServiceProvider;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Kadecot Service Discovery Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class KadecotServiceDiscoveryProfile extends ServiceDiscoveryProfile {

    /** Kadecot device list. */
    private ArrayList<KadecotDevice> mKadecotDevices = new ArrayList<>();

    /**
     * Constructor.
     * @param provider an instance of {@link DConnectServiceProvider}
     */
    public KadecotServiceDiscoveryProfile(final DConnectServiceProvider provider) {
        super(provider);
        addApi(mServiceDiscoveryApi);
    }

    private final DConnectApi mServiceDiscoveryApi = new GetApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            new DeviceSearchAsyncTask().execute(response);
            return false;
        }
    };

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
     *  Device search async task.
     */
    private class DeviceSearchAsyncTask extends AsyncTask<Intent, Void, Void> {

        private final ContentResolver mResolver = getContext().getContentResolver();

        private final KadecotDeviceApplication mApp =
            ((KadecotDeviceService) getContext()).getKadecotDeviceApplication();

        @Override
        protected Void doInBackground(final Intent... intents) {
            Intent response = intents[0];
            String urlstr = "content://com.sonycsl.kadecot.json.provider/jsonp/v1/devices/";

            Cursor cursor = mResolver.query(Uri.parse(urlstr), null, null, null, null);
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

                        if (kadecotDevice.getProtocol().equals("echonetlite")) {
                            String serviceId = "";
                            ENLObject object = mApp.getENLObject();
                            String deviceType = object.exchangeServiceId(kadecotDevice.getDeviceType());
                            if (deviceType != null) {
                                serviceId += "kadecot_" + kadecotDevice.getDeviceId() + "." + deviceType;
                                kadecotDevice.setServiceId(serviceId);

                                DConnectService service = getServiceProvider().getService(serviceId);
                                if (service == null) {
                                    service = new KadecotService(kadecotDevice, object);
                                    getServiceProvider().addService(service);
                                }
                                service.setOnline(kadecotDevice.getStatus().equals("true"));
                            }
                        }
                    }

                    appendServiceList(response);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    cursor.close();
                    sendResponse(response);
                }
            }
            return null;
        }
    }
}
