/*
 UVCDeviceServiceProvider.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.uvc;


import android.app.Service;

import org.deviceconnect.android.message.DConnectMessageServiceProvider;

/**
 * UVC Device Service Provider.
 *
 * @param <T>
 * @author NTT DOCOMO, INC.
 */
public class UVCDeviceServiceProvider<T extends Service> extends DConnectMessageServiceProvider<Service> {
    @SuppressWarnings("unchecked")
    @Override
    protected Class<Service> getServiceClass() {
        Class<? extends Service> clazz = UVCDeviceService.class;
        return (Class<Service>) clazz;
    }
}
