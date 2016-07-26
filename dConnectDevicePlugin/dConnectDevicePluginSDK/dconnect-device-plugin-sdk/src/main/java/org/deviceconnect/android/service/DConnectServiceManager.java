package org.deviceconnect.android.service;


import android.content.Context;

import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.spec.DConnectApiSpec;
import org.deviceconnect.android.profile.spec.DConnectPluginSpec;
import org.deviceconnect.android.profile.spec.DConnectProfileSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DConnectServiceManager implements DConnectServiceProvider {

    private DConnectPluginSpec mPluginSpec;

    private Context mContext;

    public void setContext(final Context context) {
        mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    public void setPluginSpec(final DConnectPluginSpec pluginSpec) {
        mPluginSpec = pluginSpec;
    }

    private final Map<String, DConnectService> mDConnectServices
        = Collections.synchronizedMap(new HashMap<String, DConnectService>());

    @Override
    public void addService(final DConnectService service) {
        service.setContext(mContext);
        if (mPluginSpec != null) {
            for (DConnectProfile profile : service.getProfileList()) {
                DConnectProfileSpec profileSpec =
                    mPluginSpec.findProfileSpec(profile.getProfileName().toLowerCase());
                if (profileSpec == null) {
                    continue;
                }
                profile.setProfileSpec(profileSpec);
                for (DConnectApi api : profile.getApiList()) {
                    String path = createPath(api);
                    DConnectApiSpec spec = profileSpec.findApiSpec(path, api.getMethod());
                    if (spec != null) {
                        api.setApiSpec(spec);
                    }
                }
            }
        }
        for (DConnectProfile profile : service.getProfileList()) {
            profile.setContext(mContext);
        }
        mDConnectServices.put(service.getId(), service);
    }

    private String createPath(final DConnectApi api) {
        String interfaceName = api.getInterface();
        String attributeName = api.getAttribute();
        StringBuffer path = new StringBuffer();
        path.append("/");
        if (interfaceName != null) {
            path.append(interfaceName);
            path.append("/");
        }
        if (attributeName != null) {
            path.append(attributeName);
        }
        return path.toString();
    }

    @Override
    public void removeService(final DConnectService service) {
        mDConnectServices.remove(service.getId());
    }

    @Override
    public DConnectService getService(final String serviceId) {
        return mDConnectServices.get(serviceId);
    }

    @Override
    public List<DConnectService> getServiceList() {
        List<DConnectService> list = new ArrayList<DConnectService>();
        list.addAll(mDConnectServices.values());
        return list;
    }

    @Override
    public void removeAllServices() {
        mDConnectServices.clear();
    }

    @Override
    public boolean hasService(final String serviceId) {
        return getService(serviceId) != null;
    }
}
