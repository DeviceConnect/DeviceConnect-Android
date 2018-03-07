/*
 SpheroService.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sphero.service;

import org.deviceconnect.android.deviceplugin.sphero.data.DeviceInfo;
import org.deviceconnect.android.deviceplugin.sphero.profile.SpheroDeviceOrientationProfile;
import org.deviceconnect.android.deviceplugin.sphero.profile.SpheroDriveControllerProfile;
import org.deviceconnect.android.deviceplugin.sphero.profile.SpheroLightProfile;
import org.deviceconnect.android.deviceplugin.sphero.profile.SpheroProfile;
import org.deviceconnect.android.service.DConnectService;

/**
 * Spheroデバイスを管理する.
 * @author NTT DOCOMO, INC.
 */
public class SpheroLightService extends DConnectService {
    /**
     * 本体の色設定用ライトのID.
     */
    public static final String COLOR_LED_LIGHT_ID = "1";

    /**
     * バックライトのID.
     */
    public static final String BACK_LED_LIGHT_ID = "2";

    /**
     * 本体の色設定用ライトの名前.
     */
    public static final String COLOR_LED_LIGHT_NAME = " LED";

    /**
     * バックライトの名前.
     */
    public static final String BACK_LED_LIGHT_NAME = " CalibrationLED";

    /**
     * コンストラクタ.
     * @param info Spheroの情報
     * @param lightId SpheroのライトID
     * @param lightName Spheroのライト名
     */
    public SpheroLightService(final DeviceInfo info, final String lightId, final String lightName) {
        super(info.getDevice().getRobot().getIdentifier() + "_" + lightId);
        setName(info.getDevice().getRobot().getName() + lightName);
        setNetworkType(NetworkType.BLUETOOTH);
        setOnline(info.getDevice().isConnected());

        addProfile(new SpheroLightProfile());
    }
}
