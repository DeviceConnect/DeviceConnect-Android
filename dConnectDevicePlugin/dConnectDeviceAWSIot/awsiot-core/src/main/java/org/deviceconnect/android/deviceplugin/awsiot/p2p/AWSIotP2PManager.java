package org.deviceconnect.android.deviceplugin.awsiot.p2p;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.awsiot.core.BuildConfig;
import org.deviceconnect.android.deviceplugin.awsiot.udt.P2PConnection;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class AWSIotP2PManager {

    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "ABC";

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

    protected P2PConnection parseSignal(final String signal) {
        if (DEBUG) {
            Log.i(TAG, "parseSignal: " + signal);
        }

        try {
            JSONObject json = new JSONObject(signal);
            int connectionId = json.getInt(AWSIotP2PManager.KEY_CONNECTION_ID);
            JSONObject global = json.getJSONObject(AWSIotP2PManager.KEY_GLOBAL);
            JSONObject local = json.getJSONObject(AWSIotP2PManager.KEY_LOCAL);

            P2PConnection connection = createP2PConnection();

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
                    if (DEBUG) {
                        Log.w(TAG, "Failed to connect the p2p.", e1);
                    }
                    try {
                        connection.close();
                    } catch (IOException e2) {
                        if (DEBUG) {
                            Log.w(TAG, "", e2);
                        }
                    }
                    return null;
                }
            }
            return connection;
        } catch (JSONException e) {
            if (DEBUG) {
                Log.e(TAG, "Invalid the json.", e);
            }
            return null;
        }
    }

    private static String getIPAddress(final Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        return String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
    }
}
