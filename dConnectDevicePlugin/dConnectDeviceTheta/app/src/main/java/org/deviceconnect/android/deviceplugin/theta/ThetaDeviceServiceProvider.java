package org.deviceconnect.android.deviceplugin.theta;

import org.deviceconnect.android.message.DConnectMessageServiceProvider;

import android.app.Service;

/**
 * Theta Device Service Provider.
 *
 * @param <T>
 * @author NTT DOCOMO, INC.
 */
public class ThetaDeviceServiceProvider<T extends Service> extends DConnectMessageServiceProvider<Service> {
    @SuppressWarnings("unchecked")
    @Override
    protected Class<Service> getServiceClass() {
        Class<? extends Service> clazz = (Class<? extends Service>) ThetaDeviceService.class;
        return (Class<Service>) clazz;
    }
}
