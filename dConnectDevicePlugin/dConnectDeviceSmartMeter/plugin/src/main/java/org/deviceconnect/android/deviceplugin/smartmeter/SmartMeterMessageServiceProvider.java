/*
 SmartMeterMessageServiceProvider.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.smartmeter;

import android.app.Service;

import org.deviceconnect.android.message.DConnectMessageServiceProvider;

/**
 * SmartMeter Message Service Provider.
 * @param <T> Service.
 */
public class SmartMeterMessageServiceProvider<T extends Service> extends DConnectMessageServiceProvider<Service> {
    @SuppressWarnings("unchecked")
    @Override
    protected Class<Service> getServiceClass() {
        Class<? extends Service> clazz = (Class<? extends Service>) SmartMeterMessageService.class;
        return (Class<Service>) clazz;
    }
}
