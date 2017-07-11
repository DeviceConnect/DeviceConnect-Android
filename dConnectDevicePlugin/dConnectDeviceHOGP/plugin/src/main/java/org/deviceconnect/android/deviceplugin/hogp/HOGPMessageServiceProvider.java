package org.deviceconnect.android.deviceplugin.hogp;

import android.app.Service;

import org.deviceconnect.android.message.DConnectMessageServiceProvider;

public class HOGPMessageServiceProvider<T extends Service> extends DConnectMessageServiceProvider<Service> {
    @SuppressWarnings("unchecked")
    @Override
    protected Class<Service> getServiceClass() {
        Class<? extends Service> clazz = (Class<? extends Service>) HOGPMessageService.class;
        return (Class<Service>) clazz;
    }
}