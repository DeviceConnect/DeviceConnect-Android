package org.deviceconnect.android.deviceplugin.alljoyn;

import android.app.Service;

import org.deviceconnect.android.message.DConnectMessageServiceProvider;

/**
 * Device Connect service provider for AllJoyn.
 *
 * @author NTT DOCOMO, INC.
 */
public class AllJoynDeviceProvider<T extends Service> extends DConnectMessageServiceProvider<Service> {
    @SuppressWarnings("unchecked")
    @Override
    protected Class<Service> getServiceClass() {
        Class<? extends Service> clazz = (Class<? extends Service>) AllJoynDeviceService.class;
        return (Class<Service>) clazz;
    }
}
