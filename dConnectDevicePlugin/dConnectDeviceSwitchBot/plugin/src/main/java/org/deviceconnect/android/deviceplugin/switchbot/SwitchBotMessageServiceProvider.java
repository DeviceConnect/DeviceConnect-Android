/*
 SwitchBotMessageServiceProvider.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
*/
package org.deviceconnect.android.deviceplugin.switchbot;

import android.app.Service;

import org.deviceconnect.android.message.DConnectMessageServiceProvider;

public class SwitchBotMessageServiceProvider<T extends Service> extends DConnectMessageServiceProvider<Service> {
    @SuppressWarnings("unchecked")
    @Override
    protected Class<Service> getServiceClass() {
        Class<? extends Service> clazz = (Class<? extends Service>) SwitchBotMessageService.class;
        return (Class<Service>) clazz;
    }
}