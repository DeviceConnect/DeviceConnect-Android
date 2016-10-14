/*
 AWSIotUtil.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot.cores.util;

import android.content.Context;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.awsiot.cores.core.AWSIotDBHelper;
import org.deviceconnect.android.deviceplugin.awsiot.cores.core.AWSIotPrefUtil;
import org.deviceconnect.android.deviceplugin.awsiot.cores.core.RemoteDeviceConnectManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public final class AWSIotUtil {


    public static final String KEY_DCONNECT_SHADOW_NAME = "DeviceConnect";

    public static final String KEY_REQUEST_CODE = "requestCode";
    public static final String KEY_REQUEST = "request";
    public static final String KEY_RESPONSE = "response";
    public static final String KEY_P2P_REMOTE = "p2p_remote";
    public static final String KEY_P2P_LOCAL = "p2p_local";

    public static final String PARAM_SELF_FLAG = "_selfOnly";

    private AWSIotUtil() {
    }

    public static String hexToString(final byte[] buf) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : buf) {
            hexString.append(Integer.toHexString(b & 0xfF));
        }
        return hexString.toString();
    }

    public static String md5(final String s) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes("ASCII"));
            return hexToString(digest.digest());
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            Log.e("AWS", "AWSIotUtil#md5", e);
        }
        return null;
    }

    public static List<RemoteDeviceConnectManager> parseDeviceShadow(final Context context, final String message) {
        if (context == null || message == null) {
            return new ArrayList<>();
        }

        List<RemoteDeviceConnectManager> managers = new ArrayList<>();
        AWSIotDBHelper dbHelper = new AWSIotDBHelper(context);

        AWSIotPrefUtil util = new AWSIotPrefUtil(context);
        String myUuid = util.getManagerUuid();

        // JSON解析、Id, Manager名取得。
        try {
            JSONObject json = new JSONObject(message);
            JSONObject obj1 = json.getJSONObject("state");
            JSONObject obj = obj1.getJSONObject("reported");

            Iterator<String> ids = obj.keys();
            while (ids.hasNext()) {
                String id = ids.next();

                // 自分自身のUUIDの場合にはリストに含めない
                if (id.equals(myUuid)) {
                    continue;
                }

                JSONObject manager = obj.getJSONObject(id);
                Boolean online = manager.getBoolean("online");
                String name = manager.getString("name");
                Double timeStamp = manager.getDouble("timeStamp");

                // DB登録チェック
                RemoteDeviceConnectManager findInfo = dbHelper.findManagerById(id);
                if (findInfo != null) {
                    findInfo.setName(name);
                    dbHelper.updateManager(findInfo);
                } else {
                    findInfo = new RemoteDeviceConnectManager(name, id);
                    dbHelper.addManager(findInfo);
                }
                findInfo.setOnline(online);
                findInfo.setTimeStamp(timeStamp);
                managers.add(findInfo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return managers;
    }

    public static String createRequest(final long requestCode, final String request) {
        return "{\"" + KEY_REQUEST + "\":" + request + ",\"" + KEY_REQUEST_CODE + "\":" + requestCode + "}";
    }

    public static String createResponse(final long requestCode, final String response) {
        return "{\"" + KEY_RESPONSE + "\":" + response + ",\"" + KEY_REQUEST_CODE + "\":" + requestCode + "}";
    }

    public static String createRemoteP2P(final String p2p) {
        return createRemoteP2P(generateRequestCode(), p2p);
    }

    public static String createRemoteP2P(final long requestCode, final String p2p) {
        return "{\"" + KEY_P2P_REMOTE + "\":" + p2p + ",\"" + KEY_REQUEST_CODE + "\":" + requestCode + "}";
    }

    public static String createLocalP2P(final String p2p) {
        return createLocalP2P(generateRequestCode(), p2p);
    }

    public static String createLocalP2P(final long requestCode, final String p2p) {
        return "{\"" + KEY_P2P_LOCAL + "\":" + p2p + ",\"" + KEY_REQUEST_CODE + "\":" + requestCode + "}";
    }

    public static int generateRequestCode() {
        return Math.abs(UUID.randomUUID().hashCode());
    }
}
