package org.deviceconnect.android.service;


import android.content.Context;
import android.util.Log;

import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.spec.DConnectApiSpec;
import org.deviceconnect.android.profile.spec.DConnectApiSpecList;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DConnectServiceManager implements DConnectServiceProvider {

    private DConnectApiSpecList mApiSpecs;

    private Context mContext;

    public void setContext(final Context context) {
        mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    public void setApiSpecDictionary(final DConnectApiSpecList dictionary) {
        mApiSpecs = dictionary;
    }

    private final Map<String, DConnectService> mDConnectServices
        = Collections.synchronizedMap(new HashMap<String, DConnectService>());


    @Override
    public void addService(final DConnectService service) {
        Log.d("AAA", "addService: id = " + service.getId());

        if (mApiSpecs != null) {
            for (DConnectProfile profile : service.getProfileList()) {
                for (DConnectApi api : profile.getApiList()) {
                    String path = createPath(profile.getProfileName(), api);
                    DConnectApiSpec spec = mApiSpecs.findApiSpec(api.getMethod().getName(), path);
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

        Log.d("AAA", "mDConnectServices.size = " + mDConnectServices.size());
    }

    private String createPath(final String profileName, final DConnectApi api) {
        String interfaceName = api.getInterface();
        String attributeName = api.getAttribute();
        StringBuffer path = new StringBuffer();
        path.append("/");
        path.append(DConnectMessage.DEFAULT_API);
        path.append("/");
        path.append(profileName);
        if (interfaceName != null) {
            path.append("/");
            path.append(interfaceName);
        }
        if (attributeName != null) {
            path.append("/");
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
        Log.d("AAA", "getServiceList: " + mDConnectServices.size());

        List<DConnectService> list = new ArrayList<DConnectService>();
        list.addAll(mDConnectServices.values());
        return list;
    }


    public boolean hasService(final String serviceId) {
        return getService(serviceId) != null;
    }
}
