/*
 ThetaDeviceServiceProvider
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
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
        Class<? extends Service> clazz = ThetaDeviceService.class;
        return (Class<Service>) clazz;
    }
}
