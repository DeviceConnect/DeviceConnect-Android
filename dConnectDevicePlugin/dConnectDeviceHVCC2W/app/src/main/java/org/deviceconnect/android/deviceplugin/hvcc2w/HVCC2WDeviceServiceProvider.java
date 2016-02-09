/*
 HVCC2WDeviceServiceProvider
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvcc2w;

import android.app.Service;

import org.deviceconnect.android.message.DConnectMessageServiceProvider;

/**
 * HVC-C2W Device Service Provider.
 *
 * @param <T>
 * @author NTT DOCOMO, INC.
 */
public class HVCC2WDeviceServiceProvider<T extends Service> extends DConnectMessageServiceProvider<Service> {
    @SuppressWarnings("unchecked")
    @Override
    protected Class<Service> getServiceClass() {
        Class<? extends Service> clazz = (Class<? extends Service>) HVCC2WDeviceService.class;
        return (Class<Service>) clazz;
    }
}
