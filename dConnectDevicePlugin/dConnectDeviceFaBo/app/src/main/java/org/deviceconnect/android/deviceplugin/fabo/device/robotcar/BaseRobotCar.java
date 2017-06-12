package org.deviceconnect.android.deviceplugin.fabo.device.robotcar;

import org.deviceconnect.android.deviceplugin.fabo.device.FaBoDeviceControl;


public class BaseRobotCar {
    /**
     * モータの回転数の上限を定義.
     */
    private static final int MAX_MOTOR = 55;

    protected static final byte START_SYSEX = (byte)0xF0;
    protected static final byte END_SYSEX = (byte)0xF7;
    protected static final byte I2C_REQUEST= 0x76;
    protected static final byte I2C_REPLY= 0x77;
    private static final byte I2C_CONFIG= 0x78;
    protected static final byte EXTENDED_ANALOG = 0x6F;
    protected static final byte DRV8830_FORWARD = 0x01;
    protected static final byte DRV8830_BACK = 0x02;
    protected static final byte DRV8830_STOP = 0x00;
    protected static final byte DRV8830_ADDRESS = 0x64;
    protected static final byte DRV8830_LEFT_ADDRESS = 0x64;
    protected static final byte DRV8830_RIGHT_ADDRESS = 0x63;
    protected static final byte SET_PIN_MODE = (byte)0xF4;

    /**
     * Usbに接続されたデバイスを管理するクラス.
     */
    private FaBoDeviceControl mFaBoDeviceControl;

    protected FaBoDeviceControl getFaBoDeviceControl() {
        return mFaBoDeviceControl;
    }

    public void setFaBoDeviceControl(final FaBoDeviceControl manager) {
        mFaBoDeviceControl = manager;
    }

    /**
     * モータにI2Cコンフィグを送信します.
     */
    protected void setI2CConfig() {
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
    protected int calcSpeed(final float speed) {
        if (speed < 0) {
            return 0;
        }
        if (speed > 1.0f) {
            return MAX_MOTOR;
        }
        return (int) (MAX_MOTOR * speed);
    }

    /**
     * タイヤの回す角度を計算します。
     * @param x 回す値
     * @return 角度
     */
    protected int calcHandleDirection(final float x) {
        return (int) (100 + x * 40);
    }
}
