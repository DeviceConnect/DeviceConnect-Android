/*
 RuleEngineMessageServiceProvider.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.ruleengine;

import android.app.Service;

import org.deviceconnect.android.message.DConnectMessageServiceProvider;

/**
 * RuleEngineMessageServiceProviderクラス.
 * @author NTT DOCOMO, INC.
 */
public class RuleEngineMessageServiceProvider<T extends Service> extends DConnectMessageServiceProvider<Service> {
    @SuppressWarnings("unchecked")
    @Override
    protected Class<Service> getServiceClass() {
        Class<? extends Service> clazz = (Class<? extends Service>) RuleEngineMessageService.class;
        return (Class<Service>) clazz;
    }
}