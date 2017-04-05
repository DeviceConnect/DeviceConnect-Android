/*
 BatteryProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.profile.BatteryProfileConstants;

/**
 * Battery プロファイル.
 * 
 * <p>
 * スマートデバイスのバッテリー情報を提供するAPI.<br>
 * バッテリー情報を提供するデバイスプラグインは当クラスを継承し、対応APIを実装すること。
 * </p>
 *
 * @deprecated
 *  swagger定義ファイルで定数を管理することになったので、このクラスは使用しないこととする。
 *  プロファイルを実装する際は本クラスではなく、{@link DConnectProfile} クラスを継承すること。
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class BatteryProfile extends DConnectProfile implements BatteryProfileConstants {

    @Override
    public final String getProfileName() {
        return PROFILE_NAME;
    }

    // ------------------------------------
    // セッターメソッド群
    // ------------------------------------

    /**
     * レスポンスに充電状態のフラグを設定する.
     * 
     * @param response レスポンスパラメータ
     * @param charging 充電状態フラグ
     */
    public static void setCharging(final Intent response, final boolean charging) {
        response.putExtra(PARAM_CHARGING, charging);
    }

    /**
     * バッテリーパラメータに充電状態のフラグを設定する.
     * 
     * @param battery バッテリーパラメータ
     * @param charging 充電状態フラグ
     */
    public static void setCharging(final Bundle battery, final boolean charging) {
        battery.putBoolean(PARAM_CHARGING, charging);
    }

    /**
     * レスポンスに完全充電までの時間(秒)を設定する.
     * 
     * @param response レスポンスパラメータ
     * @param chargingTime 完全充電までの時間(秒)
     */
    public static void setChargingTime(final Intent response, final double chargingTime) {

        if (chargingTime < 0) {
            throw new IllegalArgumentException("Charging time must be greater than and equals to 0.");
        }
        response.putExtra(PARAM_CHARGING_TIME, chargingTime);
    }

    /**
     * バッテリーパラメータに完全充電までの時間(秒)を設定する.
     * 
     * @param battery バッテリーパラメータ
     * @param chargingTime 完全充電までの時間(秒)
     */
    public static void setChargingTime(final Bundle battery, final double chargingTime) {

        if (chargingTime < 0) {
            throw new IllegalArgumentException("Charging time must be greater than and equals to 0.");
        }

        battery.putDouble(PARAM_CHARGING_TIME, chargingTime);
    }

    /**
     * レスポンスに完全放電までの時間(秒)を設定する.
     * 
     * @param response レスポンスパラメータ
     * @param dischargingTime 完全放電までの時間(秒)
     */
    public static void setDischargingTime(final Intent response, final double dischargingTime) {
        if (dischargingTime < 0) {
            throw new IllegalArgumentException("Discharging time must be greater than and equals to 0.");
        }
        response.putExtra(PARAM_DISCHARGING_TIME, dischargingTime);
    }

    /**
     * レスポンスに完全放電までの時間(秒)を設定する.
     * 
     * @param battery バッテリーパラメータ
     * @param dischargingTime 完全放電までの時間(秒)
     */
    public static void setDischargingTime(final Bundle battery, final double dischargingTime) {
        if (dischargingTime < 0) {
            throw new IllegalArgumentException("Discharging time must be greater than and equals to 0.");
        }
        battery.putDouble(PARAM_DISCHARGING_TIME, dischargingTime);
    }

    /**
     * レスポンスにバッテリー残量を設定する.
     * 
     * @param response レスポンスパラメータ
     * @param level バッテリー残量。0 ~ 1.0。
     */
    public static void setLevel(final Intent response, final double level) {
        if (level < 0 || level > 1) {
            throw new IllegalArgumentException("Level must be between 0 and 1.");
        }
        response.putExtra(PARAM_LEVEL, level);
    }

    /**
     * レスポンスにバッテリー残量を設定する.
     * 
     * @param battery バッテリーパラメータ
     * @param level バッテリー残量。0 ~ 1.0。
     */
    public static void setLevel(final Bundle battery, final double level) {
        if (level < 0 || level > 1) {
            throw new IllegalArgumentException("Level must be between 0 and 1.");
        }
        battery.putDouble(PARAM_LEVEL, level);
    }

    /**
     * メッセージにバッテリー情報を設定する.
     * 
     * @param message イベントメッセージ
     * @param battery バッテリー情報
     */
    public static void setBattery(final Intent message, final Bundle battery) {
        message.putExtra(PARAM_BATTERY, battery);
    }
}
