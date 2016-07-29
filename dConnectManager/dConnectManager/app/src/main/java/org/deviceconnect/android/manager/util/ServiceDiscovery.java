package org.deviceconnect.android.manager.util;

import android.content.Context;
import android.util.Log;

import org.deviceconnect.android.manager.BuildConfig;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.message.DConnectMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceDiscovery extends Authorization {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "Manager";

    public ServiceDiscovery(final Context context) {
        super(context);
    }

    @Override
    protected List<ServiceContainer> doInBackground(final Void... params) {
        return parseServiceList(executeServiceDiscovery(getAccessToken()));
    }

    private String executeServiceDiscovery(final String accessToken) {
        Map<String, String> params = new HashMap<String, String>() {
            {put("accessToken", accessToken);}
        };
        String uri = getUri(ServiceDiscoveryProfile.PATH_PROFILE, params);
        byte[] bytes = HttpUtil.get(uri);
        if (bytes != null) {
            return new String(bytes);
        }
        return null;
    }

    private List<ServiceContainer> parseServiceList(final String jsonString) {
        List<ServiceContainer> services = new ArrayList<>();
        if (jsonString == null) {
            return services;
        }

        try {
            JSONObject json = new JSONObject(jsonString);

            int result = json.getInt("result");
            if (result == DConnectMessage.RESULT_OK) {
                JSONArray serviceArray = json.getJSONArray(ServiceDiscoveryProfile.PARAM_SERVICES);
                for (int i = 0; i < serviceArray.length(); i++) {
                    services.add(parseService(serviceArray.getJSONObject(i)));
                }
            } else {
                int code = json.getInt("errorCode");
                switch (DConnectMessage.ErrorCode.getInstance(code)) {
                    case AUTHORIZATION:
                    case EXPIRED_ACCESS_TOKEN:
                        clearAccessToken();
                        return parseServiceList(executeServiceDiscovery(getAccessToken()));
                    default:
                        if (DEBUG) {
                            Log.w(TAG, "");
                        }
                        break;
                }
            }
        } catch (JSONException e) {
            if (DEBUG) {
                Log.e(TAG, "", e);
            }
        }
        return services;
    }

    private ServiceContainer parseService(final JSONObject obj) throws JSONException {
        ServiceContainer service = new ServiceContainer();
        service.setId(obj.getString(ServiceDiscoveryProfile.PARAM_ID));
        service.setName(obj.getString(ServiceDiscoveryProfile.PARAM_NAME));
        if (obj.has(ServiceDiscoveryProfile.PARAM_TYPE)) {
            service.setNetworkType(ServiceDiscoveryProfile.NetworkType.getInstance(obj.getString(ServiceDiscoveryProfile.PARAM_TYPE)));
        }
        service.setOnline(obj.getBoolean(ServiceDiscoveryProfile.PARAM_ONLINE));
        return service;
    }
}
