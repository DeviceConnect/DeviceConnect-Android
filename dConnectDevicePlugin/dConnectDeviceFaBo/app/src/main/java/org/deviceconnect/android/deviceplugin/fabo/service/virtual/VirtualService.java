package org.deviceconnect.android.deviceplugin.fabo.service.virtual;

import org.deviceconnect.android.deviceplugin.fabo.service.virtual.db.ServiceData;
import org.deviceconnect.android.service.DConnectService;

/**
 * 仮装サービス.
 */
public class VirtualService extends DConnectService {

    /**
     * 仮装サービスのデータ.
     */
    private ServiceData mServiceData;

    /**
     * コンストラクタ.
     * @param serviceData 仮装サービスデータ
     */
    VirtualService(final ServiceData serviceData) {
        super(serviceData.getServiceId());
        mServiceData = serviceData;
    }

    /**
     * 仮装サービスデータを取得します.
     * @return 仮装サービスデータ
     */
    public ServiceData getServiceData() {
        return mServiceData;
    }

    public void setServiceData(ServiceData serviceData) {
        mServiceData = serviceData;
    }
}
