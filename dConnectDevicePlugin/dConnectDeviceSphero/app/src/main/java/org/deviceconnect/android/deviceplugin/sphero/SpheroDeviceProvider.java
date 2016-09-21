/*
 SpheroDeviceProvider.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sphero;

import android.app.Service;

import org.deviceconnect.android.message.DConnectMessageServiceProvider;

import java.util.logging.Logger;

/**
 * Sphero用のService.
 * 
 * @param <T> Service
 * @author NTT DOCOMO, INC.
 */
public class SpheroDeviceProvider<T extends Service> extends DConnectMessageServiceProvider<Service> {
    
    /**
     * DebugLog.
     */
    private static final String TAG = "PluginShepro";
    
    /**
     * ロガー.
     */
    private Logger mLogger = Logger.getLogger("dconnect.dplugin.sphero");

    @SuppressWarnings("unchecked")
    @Override
    protected Class<Service> getServiceClass() {
        mLogger.entering(this.getClass().getName(), "getServiceClass");
        Class<? extends Service> clazz = (Class<? extends Service>) SpheroDeviceService.class;

        mLogger.exiting(this.getClass().getName(), "getServiceClass", clazz);
        return (Class<Service>) clazz;
    }

}
