package org.deviceconnect.android.deviceplugin.host;

import android.app.Service;

import org.deviceconnect.android.message.DConnectMessageServiceProvider;

public class HostDevicePluginProvider<T extends Service> extends DConnectMessageServiceProvider<Service> {
    @SuppressWarnings("unchecked")
    @Override
    protected Class<Service> getServiceClass() {
        Class<? extends Service> clazz = (Class<? extends Service>) HostDevicePlugin.class;
        return (Class<Service>) clazz;
    }
}