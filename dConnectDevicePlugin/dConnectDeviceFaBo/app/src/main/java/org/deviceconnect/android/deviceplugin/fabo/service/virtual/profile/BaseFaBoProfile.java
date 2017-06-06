package org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile;

import org.deviceconnect.android.deviceplugin.fabo.FaBoDeviceService;
import org.deviceconnect.android.profile.DConnectProfile;

import io.fabo.serialkit.FaBoUsbManager;

abstract class BaseFaBoProfile extends DConnectProfile {
    /**
     * FaBoDeviceServiceを取得します.
     * <p>
     * 返り値がnullになる場合には、プログラムがおかしいので注意。
     * </p>
     * @return FaBoDeviceServiceのインスタンス
     */
    FaBoDeviceService getFaBoDeviceService() {
        return (FaBoDeviceService) getContext();
    }

    /**
     * 接続されているFaBoを操作するためのFaBoUsbManagerを取得します.
     * <p>
     * 接続されていない場合にはnullを返却します。
     * </p>
     * @return FaBoUsbManagerのインスタンス
     */
    FaBoUsbManager getFaBoUsbManager() {
        return getFaBoDeviceService().getFaBoUsbManager();
    }
}
