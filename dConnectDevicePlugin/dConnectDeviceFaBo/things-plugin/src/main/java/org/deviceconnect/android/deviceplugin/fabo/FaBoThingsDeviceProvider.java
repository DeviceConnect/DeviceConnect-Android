package org.deviceconnect.android.deviceplugin.fabo;

import android.app.Service;

import org.deviceconnect.android.message.DConnectMessageServiceProvider;

public class FaBoThingsDeviceProvider<T extends Service> extends DConnectMessageServiceProvider<Service> {

    @SuppressWarnings("unchecked")
    @Override
    protected Class<Service> getServiceClass() {
        Class<? extends Service> clazz = (Class<? extends Service>) FaBoThingsDeviceService.class;
        return (Class<Service>) clazz;
    }
}
