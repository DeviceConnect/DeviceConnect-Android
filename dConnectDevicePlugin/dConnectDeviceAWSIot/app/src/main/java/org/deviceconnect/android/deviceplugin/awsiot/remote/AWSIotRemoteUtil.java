/*
 AWSIotRemoteUtil.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot.remote;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class AWSIotRemoteUtil {

    public static final String EXTRA_METHOD = "action";

    private AWSIotRemoteUtil() {
    }

    private static String parseMethod(String method) throws JSONException {
        method = method.replace("org.deviceconnect.action.", "");
        return method.toLowerCase();
    }

    public static String intentToJson(final Intent request, final ConversionIntentCallback callback) throws RuntimeException {
        Bundle extras = request.getExtras();

        JSONObject jsonObject = new JSONObject();
        for (String key: extras.keySet()) {
            try {
                if (key.equals("serviceId")) {
                    if (callback != null) {
                        jsonObject.put(key, callback.convertServiceId(extras.getString(key)));
                    }
                } else if (key.equals("uri")) {
                    if (callback != null) {
                        jsonObject.put(key, callback.convertUri(extras.getString(key)));
                    }
                } else if (key.equals("accessToken")) {
                } else if (key.equals("requestCode")) {
                } else if (key.equals("_type")) {
                } else if (key.equals("_app_type")) {
                } else if (key.equals("receiver")) {
                } else if (key.equals("version")) {
                } else if (key.equals("product")) {
                } else {
                    jsonObject.put(key, extras.get(key));
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            jsonObject.put(EXTRA_METHOD, parseMethod(request.getAction()));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return jsonObject.toString();
    }

    public static void jsonToIntent(final JSONObject jsonObject, final Bundle response, final ConversionJsonCallback callback) throws JSONException {
        Iterator<?> jsonKeys = jsonObject.keys();
        while (jsonKeys.hasNext()) {
            String key = (String) jsonKeys.next();
            if (key.equals("product") || key.equals("version")) {
                continue;
            }

            Object obj = jsonObject.get(key);
            if (obj instanceof String) {
                if (key.equals("id")) {
                    response.putString(key, callback.convertServiceId((String) obj));
                } else if (key.equals("serviceId")) {
                    response.putString(key, callback.convertServiceId((String) obj));
                } else if (key.equals("name")) {
                    response.putString(key, callback.convertName((String) obj));
                } else if (key.equals("uri")) {
                    response.putString(key, callback.convertUri((String) obj));
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
                jsonToIntent((JSONObject) obj, b, callback);
                response.putBundle(key, b);
            } else if (obj instanceof JSONArray) {
                List outArray = new ArrayList();
                JSONArray array = (JSONArray) obj;
                for (int i = 0; i < array.length(); i++) {
                    Object ooo = array.get(i);
                    if (ooo instanceof JSONObject) {
                        JSONObject obj2 = (JSONObject) ooo;
                        Bundle b = new Bundle();
                        jsonToIntent(obj2, b, callback);
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
                    }
                }
            }
        }
    }

    public interface ConversionIntentCallback {
        String convertServiceId(String id);
        String convertUri(String uri);
    }

    public interface ConversionJsonCallback {
        String convertServiceId(String id);
        String convertName(String name);
        String convertUri(String uri);
    }
}
