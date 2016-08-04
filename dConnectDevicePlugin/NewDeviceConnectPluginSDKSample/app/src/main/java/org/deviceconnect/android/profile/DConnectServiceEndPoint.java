package org.deviceconnect.android.profile;


import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

public class DConnectServiceEndPoint {

    private final String mServiceId;
    private final List<DConnectProfile.Api> mApiList = new ArrayList<>();

    public DConnectServiceEndPoint(final String serviceId) {
        mServiceId = serviceId;
    }

    public String getServiceId() {
        return mServiceId;
    }

    public DConnectProfile.Api[] getApiList() {
        return mApiList.toArray(new DConnectProfile.Api[mApiList.size()]);
    }

    public void addApi(final DConnectProfile.Api api) {
        mApiList.add(api);
    }

    private DConnectProfile.Api findSupportedApi(final String method, final String path) {
        return null; // TODO このサービスがサポートするAPIの実装を取り出す.
    }

    private String parsePath(final Intent request) {
        return null; // TODO リクエストされたAPIのパスを作成する.
    }

    public boolean onRequest(final Intent request, final Intent response) {
        String method = request.getStringExtra("method");
        String path = parsePath(request);
        DConnectProfile.Api api = findSupportedApi(method, path);
        if (api != null) {
            return api.onRequest(this, request, response);
        }
        return true; // Not supported error
    }
}
