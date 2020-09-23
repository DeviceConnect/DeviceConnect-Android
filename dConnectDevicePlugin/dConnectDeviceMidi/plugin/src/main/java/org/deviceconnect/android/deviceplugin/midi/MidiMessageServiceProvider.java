package org.deviceconnect.android.deviceplugin.midi;

import android.app.Service;

import org.deviceconnect.android.message.DConnectMessageServiceProvider;

public class MidiMessageServiceProvider<T extends Service> extends DConnectMessageServiceProvider<Service> {
    @SuppressWarnings("unchecked")
    @Override
    protected Class<Service> getServiceClass() {
        Class<? extends Service> clazz = MidiMessageService.class;
        return (Class<Service>) clazz;
    }
}