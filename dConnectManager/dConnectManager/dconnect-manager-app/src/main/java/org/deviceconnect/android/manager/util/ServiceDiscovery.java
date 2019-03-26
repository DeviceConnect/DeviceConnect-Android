package org.deviceconnect.android.manager.util;

import android.content.Context;
import android.util.Log;

import org.deviceconnect.android.manager.core.BuildConfig;
import org.deviceconnect.android.manager.core.DConnectSettings;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceDiscovery extends Authorization {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "Manager";
    public interface Callback {
        void onPreExecute();
        void onPostExecute(final List<ServiceContainer> serviceContainers);
    }
    private Callback mCallback;
    public ServiceDiscovery(final Context context, final DConnectSettings settings, final Callback callback) {
        super(context, settings);
        mCallback = callback;
    }

    @Override
    protected void onPreExecute() {
        if (mCallback != null) {
            mCallback.onPreExecute();
        }
    }
    @Override
    protected void onPostExecute(final List<ServiceContainer> serviceContainers) {
        if (mCallback != null) {
            mCallback.onPostExecute(serviceContainers);
        }
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
            String res = new String(bytes);
            return res;
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
                    case NOT_FOUND_CLIENT_ID:
                        clearAccessToken();
                        return services;
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
        Collections.sort(services, mComparator);
        return services;
    }

    private ServiceContainer parseService(final JSONObject obj) throws JSONException {
        ServiceContainer service = new ServiceContainer();
        service.setId(obj.getString(ServiceDiscoveryProfile.PARAM_ID));
        if (!obj.isNull(ServiceDiscoveryProfile.PARAM_NAME)) {
            service.setName(obj.getString(ServiceDiscoveryProfile.PARAM_NAME));
        }
        if (obj.has(ServiceDiscoveryProfile.PARAM_TYPE)) {
            service.setNetworkType(ServiceDiscoveryProfile.NetworkType.getInstance(obj.getString(ServiceDiscoveryProfile.PARAM_TYPE)));
        } else {
            service.setNetworkType(ServiceDiscoveryProfileConstants.NetworkType.UNKNOWN);
        }
        service.setOnline(obj.getBoolean(ServiceDiscoveryProfile.PARAM_ONLINE));
        return service;
    }

    private Comparator<ServiceContainer> mComparator = (lhs, rhs) -> {
        String name1 = lhs.getName();
        String name2 = rhs.getName();
        if (name1 == null || name2 == null) {
            return 0;
        }
        return name1.compareTo(name2);
    };
}
