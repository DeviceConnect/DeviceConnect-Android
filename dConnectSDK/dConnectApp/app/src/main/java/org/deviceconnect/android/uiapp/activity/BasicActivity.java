package org.deviceconnect.android.uiapp.activity;

import android.app.Activity;

import org.deviceconnect.android.uiapp.DConnectApplication;
import org.deviceconnect.android.uiapp.R;
import org.deviceconnect.android.uiapp.data.DCApi;
import org.deviceconnect.android.uiapp.data.DCDevicePlugin;
import org.deviceconnect.android.uiapp.data.DCParam;
import org.deviceconnect.android.uiapp.data.DCProfile;
import org.deviceconnect.android.uiapp.utils.Settings;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectResponseMessage;
import org.deviceconnect.message.DConnectSDK;
import org.deviceconnect.profile.ServiceInformationProfileConstants;
import org.deviceconnect.profile.SystemProfileConstants;

import java.util.ArrayList;
import java.util.List;

public class BasicActivity extends Activity {

    protected DConnectSDK getSDK() {
        DConnectApplication app = (DConnectApplication) getApplication();
        return app.getDConnectSK();
    }

    protected void getSystem(final OnReceivedDevicePluginListener listener) {
        DConnectSDK.URIBuilder builder = getSDK().createURIBuilder();
        builder.setProfile(SystemProfileConstants.PROFILE_NAME);

        getSDK().get(builder.build(), new DConnectSDK.OnResponseListener() {
            @Override
            public void onResponse(final DConnectResponseMessage response) {
                if (response.getResult() == DConnectMessage.RESULT_OK) {
                    if (listener != null) {
                        listener.onReceived(getDevicePlugin(response));
                    }
                } else {
                    int errorCode = response.getErrorCode();
                    switch (DConnectMessage.ErrorCode.getInstance(errorCode)) {
                        case SCOPE:
                        case EXPIRED_ACCESS_TOKEN:
                        case EMPTY_ACCESS_TOKEN:
                        case NOT_FOUND_CLIENT_ID:
                            String[] profiles = new String[DConnectApplication.SCOPES.size()];
                            DConnectApplication.SCOPES.toArray(profiles);
                            String appName = getString(R.string.app_name);
                            getSDK().authorization(appName, profiles, new DConnectSDK.OnAuthorizationListener() {
                                @Override
                                public void onResponse(final String clientId, final String accessToken) {
                                    Settings.getInstance().setClientId(clientId);
                                    Settings.getInstance().setAccessToken(accessToken);
                                    getSDK().setAccessToken(accessToken);
                                    getSystem(listener);
                                }

                                @Override
                                public void onError(final int errorCode, final String errorMessage) {
                                    // TODO エラー処理
                                }
                            });
                            break;
                        case AUTHORIZATION:
                            break;
                        default:
                            // TODO: エラー処理
                            break;
                    }
                }
            }
        });
    }

    protected List<DCDevicePlugin> getDevicePlugin(final DConnectResponseMessage response) {
        List<DCDevicePlugin> list = new ArrayList<>();
        List<Object> plugins = response.getList(SystemProfileConstants.PARAM_PLUGINS);
        if (plugins != null) {
            for (Object o : plugins) {
                DConnectMessage plugin = (DConnectMessage) o;
                String name = plugin.getString(SystemProfileConstants.PARAM_NAME);
                String id = plugin.getString(SystemProfileConstants.PARAM_ID);
                String pn = plugin.getString(SystemProfileConstants.PARAM_PACKAGE_NAME);

                DCDevicePlugin p = new DCDevicePlugin(name, id);
                p.setPackageName(pn);

                list.add(p);
            }
        }
        return list;
    }

    protected void getServiceInformation(final String serviceId, final OnReceivedServiceInformationListener listener) {
        getSDK().getServiceInformation(serviceId, new DConnectSDK.OnResponseListener() {
            @Override
            public void onResponse(final DConnectResponseMessage response) {
                if (response.getResult() == DConnectMessage.RESULT_OK) {
                    if (listener != null) {
                        listener.onReceived(generateProfiles(response));
                    }
                } else {
                    int errorCode = response.getErrorCode();
                    switch (DConnectMessage.ErrorCode.getInstance(errorCode)) {
                        case SCOPE:
                        case AUTHORIZATION:
                        case EXPIRED_ACCESS_TOKEN:
                        case EMPTY_ACCESS_TOKEN:
                        case NOT_FOUND_CLIENT_ID:
                            String[] profiles = new String[DConnectApplication.SCOPES.size()];
                            DConnectApplication.SCOPES.toArray(profiles);
                            String appName = getString(R.string.app_name);
                            getSDK().authorization(appName, profiles, new DConnectSDK.OnAuthorizationListener() {
                                @Override
                                public void onResponse(final String clientId, final String accessToken) {
                                    Settings.getInstance().setClientId(clientId);
                                    Settings.getInstance().setAccessToken(accessToken);
                                    getSDK().setAccessToken(accessToken);
                                    getServiceInformation(serviceId, listener);
                                }

                                @Override
                                public void onError(final int errorCode, final String errorMessage) {
                                    // TODO エラー処理
                                }
                            });
                            break;
                        default:
                            // TODO: エラー処理
                            break;
                    }
                }
            }
        });
    }

    protected List<DCProfile> generateProfiles(final DConnectResponseMessage response) {
        List<DCProfile> profileList = new ArrayList<>();

        DConnectMessage supportApis = response.getMessage(ServiceInformationProfileConstants.PARAM_SUPPORT_APIS);
        if (supportApis != null) {
            for (String profileName : supportApis.keySet()) {
                DCProfile p = new DCProfile(profileName);
                DConnectMessage profile = supportApis.getMessage(profileName);
                if (profile != null) {
                    DConnectMessage paths = profile.getMessage("paths");
                    if (paths != null) {
                        for (String path : paths.keySet()) {
                            DConnectMessage methods = paths.getMessage(path);
                            if (methods != null) {
                                for (String method : methods.keySet()) {
                                    DCApi api = new DCApi();
                                    api.setProfile(profileName);
                                    api.setMethod(DCApi.Method.get(method));
                                    api.setPath("/gotapi/" + profileName + path);
                                    DConnectMessage m = methods.getMessage(method);
                                    if (m != null) {
                                        String xType = m.getString("x-type");
                                        api.setXType(xType);

                                        List<Object> params = m.getList("parameters");
                                        if (params != null) {
                                            for (Object o : params) {
                                                DConnectMessage param = (DConnectMessage) o;
                                                DCParam pa = new DCParam();
                                                pa.setName(param.getString("name"));
                                                pa.setRequired(param.getBoolean("required"));
                                                pa.setType(param.getString("type"));
                                                pa.setFormat(param.getString("format"));
                                                pa.setEnum(param.getList("enum"));
                                                pa.setMax((Number) param.get("maximum"));
                                                pa.setMin((Number) param.get("minimum"));
                                                api.addParameter(pa);
                                            }
                                        }
                                    }
                                    p.addApi(api);
                                }
                            }
                        }
                    }
                }
                profileList.add(p);
            }
        }

        return profileList;
    }

    protected DCProfile getProfile(final List<DCProfile> profiles) {
        String pn = getIntent().getStringExtra("profileName");
        for (DCProfile profile : profiles) {
            if (pn.equalsIgnoreCase(profile.getName())) {
                return profile;
            }
        }
        return null;
    }

    protected DCApi getApi(final DCProfile profile) {
        String method = getIntent().getStringExtra("method");
        String path = getIntent().getStringExtra("path");
        List<DCApi> apiList = profile.getApiList();
        for (DCApi api : apiList) {
            if (!method.equalsIgnoreCase(api.getMethod().getValue())) {
                continue;
            }
            if (!path.equals(api.getPath())) {
                continue;
            }
            return api;
        }
        return null;
    }

    interface OnReceivedDevicePluginListener {
        void onReceived(List<DCDevicePlugin> pluginList);
    }

    interface OnReceivedServiceInformationListener {
        void onReceived(List<DCProfile> profiles);
    }
}
