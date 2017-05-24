package org.deviceconnect.android.deviceplugin.fabo.device;

import android.util.Log;

import io.fabo.serialkit.FaBoUsbManager;

/**
 * RobotCarを管理するクラス.
 */
public class RobotCar {

    /**
     * サーボモータの下限を定義.
     * <p>
     * 前のタイヤを左端まで回す値.
     * </p>
     */
    private static final float PWM_MIN = 7.9f;

    /**
     * サーボモータの上限を定義.
     * <p>
     * 前のタイヤを右端まで回す値.
     * </p>
     */
    private static final float PWM_MAX = 10.9f;

    /**
     * モータの回転数の上限を定義.
     */
    private static final int MAX_MOTOR = 55;

    private final byte START_SYSEX = (byte)0xF0;
    private final byte END_SYSEX = (byte)0xF7;
    private final byte I2C_REQUEST= 0x76;
    private final byte I2C_REPLY= 0x77;
    private final byte I2C_CONFIG= 0x78;
    private final byte EXTENDED_ANALOG = 0x6F;
    private final byte DRV8830_FORWARD = 0x01;
    private final byte DRV8830_BACK = 0x02;
    private final byte DRV8830_STOP = 0x00;
    private final byte DRV8830_ADDRESS = 0x64;
    private final byte SET_PIN_MODE = (byte)0xF4;

    /**
     * Usbに接続されたデバイスを管理するクラス.
     */
    private FaBoUsbManager mFaBoUsbManager;

    /**
     * コンストラクタ.
     * @param manager Usbに接続されたデバイスを管理するクラス
     */
    public RobotCar(final FaBoUsbManager manager) {
        mFaBoUsbManager = manager;
    }

    /**
     * ハンドルを切ります.
     * @param direction 向き(-1.0〜1.0)
     */
    public void turnHandle(final float direction) {
        byte[] commandData = {
                SET_PIN_MODE,
                (byte) 0x09,
                (byte) 0x04
        };
        mFaBoUsbManager.writeBuffer(commandData);

        int pwm = calcHandleDirection(direction);
        byte pwmLsb = (byte)(pwm & 0x7f);
        byte pwmMsb = (byte)((pwm >> 7) & 0x7f);
        Log.e("ABC", "turnHandle : " + direction + " " + pwm);
        byte[] commandSend = {
                START_SYSEX,
                EXTENDED_ANALOG,
                0x09,
                pwmLsb,
                pwmMsb,
                END_SYSEX
        };
        mFaBoUsbManager.writeBuffer(commandSend);
    }

    /**
     * 前進します.
     * @param speed 前進するスピード(0.0〜1.0)
     */
    public void goForward(final float speed) {
        setI2CConfig();
        moveMotor((DRV8830_FORWARD | ((calcSpeed(speed) + 5) << 2)));
    }

    /**
     * 後進します.
     * @param speed 後進するスピード(0.0〜1.0)
     */
    public void goBack(final float speed) {
        setI2CConfig();
        moveMotor((DRV8830_BACK | ((calcSpeed(speed) + 5) << 2)));
    }

    /**
     * 停止します.
     */
    public void stop() {
        setI2CConfig();
        moveMotor(DRV8830_STOP);
    }

    /**
     * モータにI2Cコンフィグを送信します.
     */
    private void setI2CConfig() {
        byte[] configCommandData = {
                START_SYSEX,
                I2C_CONFIG,
                (byte) 0x00,
                (byte) 0x00,
                END_SYSEX
        };
        mFaBoUsbManager.writeBuffer(configCommandData);
    }

    /**
     * モータの回転を送信します.
     * @param value 送信する値
     */
    private void moveMotor(final int value) {
        byte[] commandData = {
                START_SYSEX,
                I2C_REQUEST,
                DRV8830_ADDRESS,
                0x00,
                0x00,
                0x00,
                (byte) (value & 0x7f),
                (byte) ((value >> 7) & 0x7f),
                END_SYSEX
        };
        mFaBoUsbManager.writeBuffer(commandData);
    }

    /**
     * 0.0f〜1.0fの値を0〜55の値に変換します.
     * @param speed スピード(0.0f〜1.0f)
     * @return 0〜MAX_MOTORの値
     */
    private int calcSpeed(final float speed) {
        if (speed < 0) {
            return 0;
        }
        return (int) (MAX_MOTOR * speed);
    }

    /**
     * タイヤの回す角度を計算します。
     * @param x 回す値
     * @return 角度
     */
    private int calcHandleDirection(final float x) {
        return (int) (100 + x * 40);
    }
}
