package org.deviceconnect.android.deviceplugin.awsiot;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import org.deviceconnect.android.deviceplugin.awsiot.core.RemoteDeviceConnectManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class AWSIotRemoteUtil {

    public static final String EXTRA_METHOD = "method";
    public static final String EXTRA_REQUEST_CODE= "requestCode";

    public static final String SEPARATOR = ".";

    private static class Test {
        RemoteDeviceConnectManager mRemote;
        String mServiceId;
        String mId;
    }

    private static List<Test> mList = new ArrayList<>();

    private AWSIotRemoteUtil() {
    }

    /**
     * バイト配列を16進数の文字列に変換する.
     * @param buf 文字列に変換するバイト
     * @return 文字列
     */
    private static String hexToString(final byte[] buf) {
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < buf.length; i++) {
            hexString.append(Integer.toHexString(0xff & buf[i]));
        }
        return hexString.toString();
    }

    private static String md5(final String s) {
        try {
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes("ASCII"));
            return hexToString(digest.digest());
        } catch (UnsupportedEncodingException e) {
        } catch (NoSuchAlgorithmException e) {
        }
        return null;
    }

    public static String generateServiceId(RemoteDeviceConnectManager remote, String serviceId) {
        for (Test t : mList) {
            if (remote.equals(t.mRemote) && serviceId.equals(t.mServiceId)) {
                return t.mId;
            }
        }

        Test tt = new Test();
        tt.mRemote = remote;
        tt.mServiceId = serviceId;
        tt.mId = md5(remote.getServiceId() + SEPARATOR + serviceId);
        mList.add(tt);
        return tt.mId;
    }

    public static RemoteDeviceConnectManager getRemoteId(final String serviceId) {
        for (Test t : mList) {
            if (serviceId.equals(t.mId)) {
                return t.mRemote;
            }
        }
        return null;
    }

    public static String getServiceId(final String serviceId) {
        for (Test t : mList) {
            if (serviceId.equals(t.mId)) {
                return t.mServiceId;
            }
        }
        return null;
    }


    public static String intentToJson(final Intent request) throws RuntimeException {
        Bundle extras = request.getExtras();

        JSONObject jsonObject = new JSONObject();
        for (String key: extras.keySet()) {
            try {
                if (key.equals("serviceId")) {
                    jsonObject.put(key, AWSIotRemoteUtil.getServiceId(extras.getString(key)));
                } else if (key.equals("origin")) {
                } else if (key.equals("accessToken")) {
                } else if (key.equals("_type")) {
                } else if (key.equals("_app_type")) {
                } else if (key.equals("receiver")) {
                } else if (key.equals("version")) {
                } else if (key.equals("api")) {
                } else if (key.equals("product")) {
                } else {
                    jsonObject.put(key, extras.get(key));
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            jsonObject.put(EXTRA_METHOD, request.getAction());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return jsonObject.toString();
    }

    public static void jsonToIntent(final RemoteDeviceConnectManager remote, final JSONObject responseObj, final Bundle response) throws JSONException {
        Iterator<?> jsonKeys = responseObj.keys();
        while (jsonKeys.hasNext()) {
            String key = (String) jsonKeys.next();
            if (key.equals("product") || key.equals("version")) {
                continue;
            }

            Object obj = responseObj.get(key);
            if (obj instanceof String) {
                // ServiceDiscoveryで取得するidは、ここで、ローカルのIDにマッピングします。
                // serviceIdの頭に、適当な名前を付加します。
                if (key.equals("id")) {
                    response.putString(key, generateServiceId(remote, (String) obj));
                } else if (key.equals("name")) {
                    response.putString(key, remote.getName() + SEPARATOR + obj);
                } else {
                    response.putString(key, (String) obj);
                }
            } else if (obj instanceof Double) {
                response.putDouble(key, (Double) obj);
            } else if (obj instanceof Integer) {
                response.putInt(key, (Integer) obj);
            } else if (obj instanceof Boolean) {
                response.putBoolean(key, (Boolean) obj);
            } else if (obj instanceof JSONObject) {
                Bundle b = new Bundle();
                jsonToIntent(remote, (JSONObject) obj, b);
                response.putBundle(key, b);
            } else if (obj instanceof JSONArray) {
                List outArray = new ArrayList();
                JSONArray array = (JSONArray) obj;
                for (int i = 0; i < array.length(); i++) {
                    Object ooo = array.get(i);
                    if (ooo instanceof JSONObject) {
                        JSONObject obj2 = (JSONObject) ooo;
                        Bundle b = new Bundle();
                        jsonToIntent(remote, obj2, b);
                        outArray.add(b);
                    } else {
                        outArray.add(ooo);
                    }
                }
                if (outArray.size() > 0) {
                    if (outArray.get(0) instanceof Bundle) {
                        response.putParcelableArray(key, (Parcelable[]) outArray.toArray(new Bundle[outArray.size()]));
                    } else if (outArray.get(0) instanceof String) {
                        response.putStringArray(key, (String[]) outArray.toArray(new String[outArray.size()]));
                    } else {
                        // TODO: その他の処理
                    }
                }
            } else {
                // TODO: その他の処理
            }
        }
    }
}
