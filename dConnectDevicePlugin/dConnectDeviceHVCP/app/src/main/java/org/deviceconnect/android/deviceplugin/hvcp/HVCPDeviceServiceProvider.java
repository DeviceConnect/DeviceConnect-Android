/*
 HVCC2WDeviceServiceProvider
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvcp;

import android.app.Service;

import org.deviceconnect.android.message.DConnectMessageServiceProvider;

/**
 * HVC-P Device Service Provider.
 *
 * @param <T>
 * @author NTT DOCOMO, INC.
 */
public class HVCPDeviceServiceProvider<T extends Service> extends DConnectMessageServiceProvider<Service> {
    @SuppressWarnings("unchecked")
    @Override
    protected Class<Service> getServiceClass() {
        Class<? extends Service> clazz = HVCPDeviceService.class;
        return (Class<Service>) clazz;
    }
}
