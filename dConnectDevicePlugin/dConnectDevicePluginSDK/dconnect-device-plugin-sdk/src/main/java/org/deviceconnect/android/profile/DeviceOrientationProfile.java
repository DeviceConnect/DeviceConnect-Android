/*
 DeviceOrientationProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.profile.DeviceOrientationProfileConstants;

/**
 * Device Orientation プロファイル.
 * 
 * <p>
 * スマートデバイスのセンサー操作機能を提供するAPI.<br>
 * センサー操作機能を提供するデバイスプラグインは当クラスを継承し、対応APIを実装すること。 <br>
 * </p>
 *
 * @deprecated
 *  swagger定義ファイルで定数を管理することになったので、このクラスは使用しないこととする。
 *  プロファイルを実装する際は本クラスではなく、{@link DConnectProfile} クラスを継承すること。
 *
 * @author NTT DOCOMO, INC.
 */
public class DeviceOrientationProfile extends DConnectProfile implements DeviceOrientationProfileConstants {

    @Override
    public final String getProfileName() {
        return PROFILE_NAME;
    }

    // ------------------------------------
    // メッセージセッターメソッド群
    // ------------------------------------

    /**
     * センサー情報にインターバルを設定する.
     * 
     * @param orientation センサー情報
     * @param interval インターバル(ミリ秒)
     */
    public static void setInterval(final Bundle orientation, final long interval) {
        orientation.putLong(PARAM_INTERVAL, interval);
    }

    /**
     * メッセージにセンサー情報を設定する.
     * 
     * @param message メッセージパラメータ
     * @param orientation センサー情報
     */
    public static void setOrientation(final Intent message, final Bundle orientation) {
        message.putExtra(PARAM_ORIENTATION, orientation);
    }

    /**
     * センサー情報に加速度センサー情報を設定します.
     * 
     * @param orientation センサー情報
     * @param acceleration 加速度センサー情報
     */
    public static void setAcceleration(final Bundle orientation, final Bundle acceleration) {
        orientation.putBundle(PARAM_ACCELERATION, acceleration);
    }

    /**
     * センサー情報に重力加速度センサー情報を設定します.
     * 
     * @param orientation センサー情報
     * @param accelerationIncludingGravity 重力加速度センサー情報
     */
    public static void setAccelerationIncludingGravity(final Bundle orientation,
            final Bundle accelerationIncludingGravity) {
        orientation.putBundle(PARAM_ACCELERATION_INCLUDING_GRAVITY, accelerationIncludingGravity);
    }

    /**
     * センサー情報に回転加速度センサー情報を設定します.
     * 
     * @param orientation センサー情報
     * @param rotationRate 回転加速度センサー情報
     */
    public static void setRotationRate(final Bundle orientation, final Bundle rotationRate) {
        orientation.putBundle(PARAM_ROTATION_RATE, rotationRate);
    }

    /**
     * センサー情報にX方向への加速度を設定する.
     * 
     * @param sensor センサー情報
     * @param x X方向への加速度
     */
    public static void setX(final Bundle sensor, final double x) {
        sensor.putDouble(PARAM_X, x);
    }

    /**
     * センサー情報にY方向への加速度を設定する.
     * 
     * @param sensor センサー情報
     * @param y Y方向への加速度
     */
    public static void setY(final Bundle sensor, final double y) {
        sensor.putDouble(PARAM_Y, y);
    }

    /**
     * センサー情報にZ方向への加速度を設定する.
     * 
     * @param sensor センサー情報
     * @param z Z方向への加速度
     */
    public static void setZ(final Bundle sensor, final double z) {
        sensor.putDouble(PARAM_Z, z);
    }

    /**
     * 回転加速度センサー情報にz軸回転の角度を設定する.
     * 
     * @param rotationRate 回転加速度センサー情報
     * @param alpha z軸回転の角度
     */
    public static void setAlpha(final Bundle rotationRate, final double alpha) {
        rotationRate.putDouble(PARAM_ALPHA, alpha);
    }

    /**
     * 回転加速度センサー情報にx軸回転の角度を設定する.
     * 
     * @param rotationRate 回転加速度センサー情報
     * @param beta x軸回転の角度
     */
    public static void setBeta(final Bundle rotationRate, final double beta) {
        rotationRate.putDouble(PARAM_BETA, beta);
    }

    /**
     * 回転加速度センサー情報にy軸回転の角度を設定する.
     * 
     * @param rotationRate 回転加速度センサー情報
     * @param gamma y軸回転の角度
     */
    public static void setGamma(final Bundle rotationRate, final double gamma) {
        rotationRate.putDouble(PARAM_GAMMA, gamma);
    }
}
