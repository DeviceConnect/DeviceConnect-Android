package org.deviceconnect.android.profile.api;


import android.content.Intent;

import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.spec.DConnectApiSpec;
import org.deviceconnect.android.service.DConnectService;

public abstract class DConnectApi {

    private DConnectApiSpec mApiSpec;

    public String getInterface() {
        return null;
    }

    public String getAttribute() {
        return null;
    }

    public abstract DConnectApiSpec.Method getMethod();

    public DConnectApiSpec getApiSpec() {
        return mApiSpec;
    }

    public void setApiSpec(final DConnectApiSpec apiSpec) {
        mApiSpec = apiSpec;
    }

    public boolean onRequest(final Intent request, final Intent response,
                             final DConnectService service) {
        MessageUtils.setNotSupportAttributeError(response);
        return false;
    }

}
