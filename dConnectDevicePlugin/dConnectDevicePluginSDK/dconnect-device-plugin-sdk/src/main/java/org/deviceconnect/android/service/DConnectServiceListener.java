package org.deviceconnect.android.service;


public interface DConnectServiceListener {

    void onServiceAdded(DConnectService service);

    void onServiceRemoved(DConnectService service);

    void onStatusChange(DConnectService service);
}
