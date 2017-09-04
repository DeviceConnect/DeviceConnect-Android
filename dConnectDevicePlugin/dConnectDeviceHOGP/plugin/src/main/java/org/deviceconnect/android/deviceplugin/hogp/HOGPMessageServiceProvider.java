/*
 HOGPMessageServiceProvider.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hogp;

import android.app.Service;

import org.deviceconnect.android.message.DConnectMessageServiceProvider;

/**
 * Broadcastで受け取ったIntentをServiceに渡すためのReceiver.
 *
 * @author NTT DOCOMO, INC.
 */
public class HOGPMessageServiceProvider<T extends Service> extends DConnectMessageServiceProvider<Service> {
    @SuppressWarnings("unchecked")
    @Override
    protected Class<Service> getServiceClass() {
        Class<? extends Service> clazz = (Class<? extends Service>) HOGPMessageService.class;
        return (Class<Service>) clazz;
    }
}