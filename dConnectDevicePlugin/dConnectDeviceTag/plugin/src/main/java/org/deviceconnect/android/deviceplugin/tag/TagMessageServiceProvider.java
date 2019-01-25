package org.deviceconnect.android.deviceplugin.tag;

import android.app.Service;

import org.deviceconnect.android.message.DConnectMessageServiceProvider;

public class TagMessageServiceProvider<T extends Service> extends DConnectMessageServiceProvider<Service> {
    @SuppressWarnings("unchecked")
    @Override
    protected Class<Service> getServiceClass() {
        Class<? extends Service> clazz = (Class<? extends Service>) TagMessageService.class;
        return (Class<Service>) clazz;
    }
}