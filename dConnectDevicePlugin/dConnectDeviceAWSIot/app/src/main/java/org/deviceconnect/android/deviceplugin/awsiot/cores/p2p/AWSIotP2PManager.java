/*
 AWSIotP2PManager.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot.cores.p2p;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.awsiot.remote.BuildConfig;
import org.deviceconnect.android.deviceplugin.awsiot.udt.P2PConnection;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class AWSIotP2PManager {

    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "AWS";

    public static final String KEY_CONNECTION_ID = "connectionId";
    public static final String KEY_GLOBAL = "global";
    public static final String KEY_LOCAL = "local";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_PORT = "port";

    public void onReceivedSignaling(final String signaling) {
    }

    public void onNotifySignaling(final String signaling) {
    }

    protected P2PConnection createP2PConnection() {
        return new P2PConnection();
    }

    protected P2PConnection createP2PConnection(final String signal, final P2PConnection.OnP2PConnectionListener listener) {
        if (DEBUG) {
            Log.i(TAG, "createP2PConnection: " + signal);
        }

        try {
            JSONObject json = new JSONObject(signal);
            JSONObject global = json.getJSONObject(AWSIotP2PManager.KEY_GLOBAL);
            JSONObject local = json.getJSONObject(AWSIotP2PManager.KEY_LOCAL);

            P2PConnection connection = createP2PConnection();
            connection.setOnP2PConnectionListener(listener);

            for (int i = 0; i < 3; i++) {
                if (connect(connection, global, local)) {
                    return connection;
                }
            }
            return null;
        } catch (JSONException e) {
            if (DEBUG) {
                Log.e(TAG, "Invalid the json.", e);
            }
            return null;
        }
    }

    private boolean connect(final P2PConnection connection, final JSONObject global, final JSONObject local) throws JSONException {
        String address = global.getString(AWSIotP2PManager.KEY_ADDRESS);
        int port = global.getInt(AWSIotP2PManager.KEY_PORT);
        try {
            connection.connect(address, port);
        } catch (IOException e) {
            address = local.getString(AWSIotP2PManager.KEY_ADDRESS);
            port = local.getInt(AWSIotP2PManager.KEY_PORT);
            try {
                connection.connect(address, port);
            } catch (IOException e1) {
                try {
                    connection.close();
                } catch (IOException e2) {
                    if (DEBUG) {
                        Log.w(TAG, "", e2);
                    }
                }
                return false;
            }
        }
        return true;
    }


    protected String createSignaling(final Context context, final int connectionId, final String address, final int port) {
        try {
            JSONObject json = new JSONObject();
            json.put(KEY_CONNECTION_ID, connectionId);

            JSONObject global = new JSONObject();
            global.put(KEY_ADDRESS, address);
            global.put(KEY_PORT, port);
            json.put(KEY_GLOBAL, global);

            JSONObject local = new JSONObject();
            local.put(KEY_ADDRESS, getIPAddress(context));
            local.put(KEY_PORT, port);
            json.put(KEY_LOCAL, local);

            return json.toString();
        } catch (JSONException e) {
            if (DEBUG) {
                Log.e(TAG, "", e);
            }
        }
        return null;
    }

    protected int getConnectionId(final String signal) {
        try {
            JSONObject json = new JSONObject(signal);
            return json.getInt(AWSIotP2PManager.KEY_CONNECTION_ID);
        } catch (JSONException e) {
            return -1;
        }
    }

    protected String generateInternalServerError() {
        return generateErrorHeader("500", "<html><head><title>500 - Error</title></head><body>500 - Error</body></html>");
    }

    private String generateErrorHeader(final String status, final String body) {
        SimpleDateFormat gmtFrmt = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 " + status + " OK\r\n");
        sb.append("Server: Server\r\n");
        sb.append("Content-length: "+ body.getBytes().length + "\r\n");
        sb.append("Date: " + gmtFrmt.format(new Date()) + "\r\n");
        sb.append("Connection: close\r\n");
        sb.append("\r\n");
        sb.append(body);
        return sb.toString();
    }


    protected int findHeaderEnd(final byte[] buf, final int rlen) {
        int splitbyte = 0;
        while (splitbyte + 1 < rlen) {

            // RFC2616
            if (buf[splitbyte] == '\r' && buf[splitbyte + 1] == '\n' &&
                    splitbyte + 3 < rlen && buf[splitbyte + 2] == '\r' &&
                    buf[splitbyte + 3] == '\n') {
                return splitbyte + 4;
            }

            // tolerance
            if (buf[splitbyte] == '\n' && buf[splitbyte + 1] == '\n') {
                return splitbyte + 2;
            }
            splitbyte++;
        }
        return 0;
    }

    private static String getIPAddress(final Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        return String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
    }
}
