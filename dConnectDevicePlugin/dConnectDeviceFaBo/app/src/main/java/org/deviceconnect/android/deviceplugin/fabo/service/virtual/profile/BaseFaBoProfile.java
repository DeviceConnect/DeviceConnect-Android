package org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile;

import org.deviceconnect.android.deviceplugin.fabo.FaBoDeviceService;
import org.deviceconnect.android.deviceplugin.fabo.device.FaBoDeviceControl;
import org.deviceconnect.android.profile.DConnectProfile;

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

//    /**
//     * 接続されているFaBoを操作するためのFaBoUsbManagerを取得します.
//     * <p>
//     * 接続されていない場合にはnullを返却します。
//     * </p>
//     * @return FaBoUsbManagerのインスタンス
//     */
//    FaBoUsbManager getFaBoUsbManager() {
//        return getFaBoDeviceService().getFaBoUsbManager();
//    }

    FaBoDeviceControl getFaBoDeviceControl() {
        return getFaBoDeviceService().getFaBoDeviceControl();
    }

    /**
     * Arduinoから渡されてくる値を変換します.
     * @param x Arduinoから取得した値
     * @param inMin Arduinoから取得できる最小値
     * @param inMax Arduinoから取得できる最大値
     * @param outMin 変換後の最小値
     * @param outMax 変換後の最大値
     * @return 変換された値
     */
    int calcArduinoMap(final int x, final int inMin, final int inMax, final int outMin, final int outMax) {
        return (x - inMin) * (outMax - outMin) / (inMax - inMin) + outMin;
    }
}
