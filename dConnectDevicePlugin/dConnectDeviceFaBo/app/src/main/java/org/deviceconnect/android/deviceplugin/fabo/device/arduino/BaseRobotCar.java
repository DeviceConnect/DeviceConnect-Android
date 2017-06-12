package org.deviceconnect.android.deviceplugin.fabo.device.arduino;

import org.deviceconnect.android.deviceplugin.fabo.device.FaBoDeviceControl;

class BaseRobotCar {
    /**
     * モータの回転数の上限を定義.
     */
    private static final int MAX_MOTOR = 55;

    static final byte START_SYSEX = (byte)0xF0;
    static final byte END_SYSEX = (byte)0xF7;
    static final byte I2C_REQUEST= 0x76;
    static final byte I2C_REPLY= 0x77;
    private static final byte I2C_CONFIG= 0x78;

    static final byte DRV8830_FORWARD = 0x01;
    static final byte DRV8830_BACK = 0x02;
    static final byte DRV8830_STOP = 0x00;

    /**
     * Usbに接続されたデバイスを管理するクラス.
     */
    private FaBoDeviceControl mFaBoDeviceControl;

    FaBoDeviceControl getFaBoDeviceControl() {
        return mFaBoDeviceControl;
    }

    void setFaBoDeviceControl(final FaBoDeviceControl manager) {
        mFaBoDeviceControl = manager;
    }

    /**
     * モータにI2Cコンフィグを送信します.
     */
    void setI2CConfig() {
        byte[] configCommandData = {
                START_SYSEX,
                I2C_CONFIG,
                (byte) 0x00,
                (byte) 0x00,
                END_SYSEX
        };
        getFaBoDeviceControl().writeI2C(configCommandData);
    }

    /**
     * 0.0f〜1.0fの値を0〜55の値に変換します.
     * @param speed スピード(0.0f〜1.0f)
     * @return 0〜MAX_MOTORの値
     */
    int calcSpeed(final float speed) {
        float tmp = Math.abs(speed);
        if (tmp > 1.0f) {
            return MAX_MOTOR;
        }
        return (int) (MAX_MOTOR * tmp);
    }

    /**
     * タイヤの回す角度を計算します。
     * @param x 回す値
     * @return 角度
     */
    int calcHandleDirection(final float x) {
        return (int) (100 + x * 40);
    }
}
