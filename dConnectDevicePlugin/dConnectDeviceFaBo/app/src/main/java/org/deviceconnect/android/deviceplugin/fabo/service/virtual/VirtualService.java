package org.deviceconnect.android.deviceplugin.fabo.service.virtual;

import org.deviceconnect.android.deviceplugin.fabo.service.virtual.db.ServiceData;
import org.deviceconnect.android.service.DConnectService;

public class VirtualService extends DConnectService {

    private ServiceData mServiceData;

    public VirtualService(final ServiceData serviceData) {
        super(serviceData.getServiceId());
        mServiceData = serviceData;
    }

    public ServiceData getServiceData() {
        return mServiceData;
    }
}
