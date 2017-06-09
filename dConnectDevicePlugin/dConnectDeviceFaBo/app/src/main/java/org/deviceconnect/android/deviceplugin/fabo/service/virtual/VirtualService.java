package org.deviceconnect.android.deviceplugin.fabo.service.virtual;

import org.deviceconnect.android.deviceplugin.fabo.service.virtual.db.ServiceData;
import org.deviceconnect.android.service.DConnectService;

/**
 * 仮想サービス.
 */
public class VirtualService extends DConnectService {

    /**
     * 仮想サービスのデータ.
     */
    private ServiceData mServiceData;

    /**
     * コンストラクタ.
     * @param serviceData 仮想サービスデータ
     */
    VirtualService(final ServiceData serviceData) {
        super(serviceData.getServiceId());
        mServiceData = serviceData;
    }

    /**
     * 仮想サービスデータを取得します.
     * @return 仮想サービスデータ
     */
    public ServiceData getServiceData() {
        return mServiceData;
    }

    /**
     * サービスデータを設定します.
     * @param serviceData 設定するサービスデータ
     */
    public void setServiceData(final ServiceData serviceData) {
        mServiceData = serviceData;
    }
}
