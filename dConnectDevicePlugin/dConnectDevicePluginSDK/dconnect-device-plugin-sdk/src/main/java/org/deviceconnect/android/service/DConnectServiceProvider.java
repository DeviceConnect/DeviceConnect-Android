package org.deviceconnect.android.service;


import java.util.List;

public interface DConnectServiceProvider {

    DConnectService getService(String serviceId);

    List<DConnectService> getServiceList();

    void addService(DConnectService service);

    void removeService(String serviceId);
}
