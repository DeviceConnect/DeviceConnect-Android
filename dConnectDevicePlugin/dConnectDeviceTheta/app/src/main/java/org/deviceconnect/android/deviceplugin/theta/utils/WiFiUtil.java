/*
WiFiUtil
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
*/

package org.deviceconnect.android.deviceplugin.theta.utils;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.basic.message.DConnectResponseMessage;
import org.deviceconnect.message.client.DConnectClient;
import org.deviceconnect.message.http.impl.client.HttpDConnectClient;
import org.deviceconnect.message.http.impl.factory.HttpMessageFactory;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;

import java.io.IOException;

/**
 * WiFi Utility.
 * @author NTT DOCOMO, INC.
 */
public final class WiFiUtil {
    /** WiFi prefix of Theta. */
    private static final String WIFI_PREFIX = "THETA";

    /** dConnectManager's URI. */
    private static final String BASE_URI = "http://localhost:4035";

    /** Service Discovery ProfileのURI. */
    private static final String DISCOVERY_URI = BASE_URI + "/" + ServiceDiscoveryProfileConstants.PROFILE_NAME;

    /**
     * Constructor.
     */
    private WiFiUtil() {
    }

    /**
     * Specified SSID to check whether the SSID of Wifi of SonyCamera device.
     * 
     * @param ssid SSID
     * @return True if the SSID of Theta device, otherwise false
     */
    public static boolean checkSSID(final String ssid) {
        if (ssid == null) {
            return false;
        }
        String id = ssid.replace("\"", "");
        return id.startsWith(WIFI_PREFIX);
    }

    /**
     * Search the device to asynchronous.
     * 
     * @param listener Listener to notify the results
     */
    public static void asyncSearchDevice(final DConnectMessageHandler listener) {
        AsyncTask<Void, Void, DConnectMessage> task = new AsyncTask<Void, Void, DConnectMessage>() {
            @Override
            protected DConnectMessage doInBackground(final Void... params) {
                try {
                    DConnectClient client = new HttpDConnectClient();
                    HttpGet request = new HttpGet(DISCOVERY_URI);
                    HttpResponse response = client.execute(request);
                    return (new HttpMessageFactory()).newDConnectMessage(response);
                } catch (IOException e) {
                    return new DConnectResponseMessage(DConnectMessage.RESULT_ERROR);
                }
            }

            @Override
            protected void onPostExecute(final DConnectMessage message) {
                if (listener != null) {
                    listener.handleMessage(message);
                }
            }
        };
        task.execute();
    }
}
