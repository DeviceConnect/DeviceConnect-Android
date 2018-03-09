package org.deviceconnect.android.deviceplugin.ruleengine;

import android.app.Service;

import org.deviceconnect.android.message.DConnectMessageServiceProvider;

public class RuleEngineMessageServiceProvider<T extends Service> extends DConnectMessageServiceProvider<Service> {
    @SuppressWarnings("unchecked")
    @Override
    protected Class<Service> getServiceClass() {
        Class<? extends Service> clazz = (Class<? extends Service>) RuleEngineMessageService.class;
        return (Class<Service>) clazz;
    }
}