/*
 HvcDeviceProvider.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc;

import org.deviceconnect.android.message.DConnectMessageServiceProvider;

import android.app.Service;

/**
 * HVC Device Provider.
 * 
 * @param <T> service class
 * @author NTT DOCOMO, INC.
 */
public class HvcDeviceProvider<T extends Service> extends DConnectMessageServiceProvider<Service> {

    @SuppressWarnings("unchecked")
    @Override
    protected Class<Service> getServiceClass() {
        Class<? extends Service> clazz = (Class<? extends Service>) HvcDeviceService.class;

        return (Class<Service>) clazz;
    }

}
