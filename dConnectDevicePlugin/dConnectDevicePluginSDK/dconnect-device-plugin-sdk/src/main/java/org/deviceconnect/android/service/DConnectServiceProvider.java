package org.deviceconnect.android.service;


import java.util.List;

public interface DConnectServiceProvider {

    boolean hasService(String serviceId);

    DConnectService getService(String serviceId);

    List<DConnectService> getServiceList();

    void addService(DConnectService service);

    boolean removeService(DConnectService service);

    DConnectService removeService(String serviceId);

    void removeAllServices();
}
