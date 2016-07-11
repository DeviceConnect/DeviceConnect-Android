package org.deviceconnect.android.profile;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.util.HashMap;
import java.util.Map;

public class DConnectMessageService extends Service /* implements DConnectProfileProvider */ {

    private Map<String, DConnectServiceEndPoint> mServices = new HashMap<>();

    public void addService(final DConnectServiceEndPoint service) {
        mServices.put(service.getServiceId(), service);
    }

    public void removeService(final String serviceId) {
        mServices.remove(serviceId);
    }

    public DConnectServiceEndPoint getService(final String serviceId) {
        return mServices.get(serviceId);
    }

    protected void onRequest(final Intent request, final Intent response) {
        // TODO: LocalOAuth処理等を実行.
        String serviceId = request.getStringExtra("serviceId");
        DConnectServiceEndPoint service = getService(serviceId);
        boolean send = service.onRequest(request, response);
        if (send) {
            sendResponse(response);
        }
    }

    public final boolean sendResponse(final Intent response) {
        return true; // TODO: Managerへレスポンスを送信.
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
