/*
 HitoeDeviceServiceProvider
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe;

import android.app.Service;

import org.deviceconnect.android.message.DConnectMessageServiceProvider;

/**
 * Hitoe Device Plug-in.
 * @param <T>
 * @author NTT DOCOMO, INC.
 */
public class HitoeDeviceServiceProvider<T extends Service> extends DConnectMessageServiceProvider<Service> {
    @SuppressWarnings("unchecked")
    @Override
    protected Class<Service> getServiceClass() {
        Class<? extends Service> clazz = HitoeDeviceService.class;
        return (Class<Service>) clazz;
    }
}
