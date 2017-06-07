package org.deviceconnect.android.deviceplugin.fabo.service.virtual;

import android.util.Log;

import org.deviceconnect.android.deviceplugin.fabo.BuildConfig;
import org.deviceconnect.android.deviceplugin.fabo.param.ArduinoUno;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.db.ProfileData;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.db.ServiceData;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile.GPIOHumidityProfile;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile.GPIOIlluminanceProfile;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile.GPIOKeyEventProfile;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile.GPIOLightProfile;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile.GPIOProximityProfile;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile.GPIOTemperatureProfile;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile.GPIOVibrationProfile;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile.I2CMouseCarDriveControllerProfile;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile.I2CRobotCarDriveControllerProfile;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile.I2CTemperatureProfile;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * 仮装サービスを作成するためのファクトリークラス.
 */
public final class VirtualServiceFactory {
    /**
     * デバッグフラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * デバッグ用タグ.
     */
    private static final String TAG = "FaBo";

    /**
     * 仮装サービスデータからDConnectServiceのインスタンスを作成します.
     * @param serviceData 仮装サービスデータ
     * @return DConnectServiceのインスタンス
     */
    public static DConnectService createService(final ServiceData serviceData) {
        if (DEBUG) {
            Log.d(TAG, "==========================");
            Log.d(TAG, "Create virtual service.");
            Log.d(TAG, "ID: " + serviceData.getServiceId());
            Log.d(TAG, "Name: " + serviceData.getName());
        }

        DConnectService service = new VirtualService(serviceData);
        service.setName(serviceData.getName());
        service.setNetworkType(ServiceDiscoveryProfileConstants.NetworkType.UNKNOWN);
        for (ProfileData p : serviceData.getProfileDataList()) {
            DConnectProfile profile = createProfile(p);
            if (profile != null) {
                service.addProfile(profile);
            }
        }
        if (DEBUG) {
            Log.d(TAG, "==========================");
        }
        return service;
    }

    /**
     * プロファイルデータからプロファイルを作成します.
     * @param profileData プロファイルデータ
     * @return DConnectProfileのインスタンス
     */
    private static DConnectProfile createProfile(final ProfileData profileData) {
        if (DEBUG) {
            Log.d(TAG, "  Add the profile. type=" + profileData.getType());
        }

        switch (profileData.getType()) {
            case GPIO_LIGHT:
                return new GPIOLightProfile(conv(profileData.getPinList()));

            case GPIO_TEMPERATURE:
                return new GPIOTemperatureProfile(conv(profileData.getPinList()));

            case GPIO_VIBRATION:
                return new GPIOVibrationProfile(conv(profileData.getPinList()));

            case GPIO_ILLUMINANCE:
                return new GPIOIlluminanceProfile(conv(profileData.getPinList()));

            case GPIO_HUMIDITY:
                return new GPIOHumidityProfile(conv(profileData.getPinList()));

            case GPIO_KEY_EVENT:
                return new GPIOKeyEventProfile(conv(profileData.getPinList()));

            case GPIO_PROXIMITY:
                return new GPIOProximityProfile(conv(profileData.getPinList()));

            case I2C_MOUSE_DRIVE_CONTROLLER:
                return new I2CMouseCarDriveControllerProfile();

            case I2C_ROBOT_DRIVE_CONTROLLER:
                return new I2CRobotCarDriveControllerProfile();

            case I2C_TEMPERATURE:
                return new I2CTemperatureProfile();

            default:
                if (DEBUG) {
                    Log.w(TAG, "Not found the profile.");
                    Log.w(TAG, "ID: " + profileData.getServiceId());
                    Log.w(TAG, "Type: " + profileData.getType());
                }
                break;
        }
        return null;
    }

    /**
     * ピン情報をArudinoUno.Pinのリストに変換します.
     * @param pins 変換前のピン情報
     * @return ArudinoUno.Pinのリスト
     */
    private static List<ArduinoUno.Pin> conv(List<Integer> pins) {
        List<ArduinoUno.Pin> pinList = new ArrayList<>();
        for (Integer i : pins) {
            pinList.add(ArduinoUno.Pin.getPin(i));
        }
        return pinList;
    }
}
