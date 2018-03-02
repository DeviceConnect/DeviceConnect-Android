/*
HueThingsDeviceService
Copyright (c) 2018 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
*/
package org.deviceconnect.android.deviceplugin.hue;

/**
 * 本デバイスプラグインのプロファイルをDeviceConnectに登録するサービス(Android Things版).
 *
 * @author NTT DOCOMO, INC.
 */
public class HueThingsDeviceService extends HueDeviceService {


    @Override
    public void onCreate() {
        super.onCreate();
        setUseLocalOAuth(false);
    }
}
