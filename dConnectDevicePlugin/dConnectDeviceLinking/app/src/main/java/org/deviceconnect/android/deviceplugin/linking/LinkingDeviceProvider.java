/*
 LinkingDeviceProvider.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking;

import android.app.Service;

import org.deviceconnect.android.message.DConnectMessageServiceProvider;

/**
 * Service for Linking.
 *
 * @param <T> Service
 * @author NTT DOCOMO, INC.
 */
public class LinkingDeviceProvider<T extends Service> extends DConnectMessageServiceProvider<Service> {

    @SuppressWarnings("unchecked")
    @Override
    protected Class<Service> getServiceClass() {
        Class<? extends Service> clazz = LinkingDeviceService.class;
        return (Class<Service>) clazz;
    }

}
