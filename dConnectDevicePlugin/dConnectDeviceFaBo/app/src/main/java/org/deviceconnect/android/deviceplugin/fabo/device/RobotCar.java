package org.deviceconnect.android.deviceplugin.fabo.device;

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
    private final byte DRV8830_LEFT_ADDRESS = 0x64;
    private final byte DRV8830_RIGHT_ADDRESS = 0x63;
    private final byte SET_PIN_MODE = (byte)0xF4;

    /**
     * Usbに接続されたデバイスを管理するクラス.
     */
    private FaBoUsbManager mFaBoUsbManager;

    /**
     * RobotType.
     */
    private int type;

    /**
     * Mouse型
     */
    public final static int TYPE_MOUSE = 0;

    /**
     * Robot型
     */
    public final static int TYPE_CAR = 1;

    /**
     * コンストラクタ.
     * @param manager Usbに接続されたデバイスを管理するクラス
     */
    public RobotCar(final FaBoUsbManager manager, final int type) {
        mFaBoUsbManager = manager;
        this.type = type;
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
     * Mouse move.
     * @param speed_left 後進するスピード(0.0〜1.0)
     * @param speed_right 後進するスピード(0.0〜1.0)
     */
    public void moveMouse(final float speed_right, final float speed_left) {
        setI2CConfig();

        int right_dir = 0;
        int left_dir = 0;
        float tmp_speed_left = 0;
        float tmp_speed_right = 0;

        if (speed_right > 0) {
            right_dir = DRV8830_BACK;
            //right_dir = DRV8830_FORWARD;
            tmp_speed_right = speed_right;
        } else if (speed_right < 0) {
            right_dir = DRV8830_FORWARD;
            //right_dir = DRV8830_BACK;
            tmp_speed_right = -speed_right;
        }

        if (speed_left > 0) {
            left_dir = DRV8830_BACK;
            //left_dir = DRV8830_FORWARD;
            tmp_speed_left = speed_left;
        } else if (speed_left < 0) {
            left_dir = DRV8830_FORWARD;
            //left_dir = DRV8830_BACK;
            tmp_speed_left = -speed_left;
        }

        moveMotor((right_dir | ((calcSpeed(tmp_speed_right) + 5) << 2)), (left_dir | ((calcSpeed(tmp_speed_left) + 5) << 2)));
    }

    /**
     * モータの回転を送信します.
     * @param value 送信する値
     */
    private void moveMotor(final int value) {
        setI2CConfig();
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
     * モータの回転を送信します.
     * @param value_left 左側のモーター
     * @param value_right 右側のモーター
     */
    private void moveMotor(final int value_left, final int value_right) {
        byte[] commandDataLeft = {
                START_SYSEX,
                I2C_REQUEST,
                DRV8830_LEFT_ADDRESS,
                0x00,
                0x00,
                0x00,
                (byte) (value_left & 0x7f),
                (byte) ((value_left >> 7) & 0x7f),
                END_SYSEX
        };
        mFaBoUsbManager.writeBuffer(commandDataLeft);

        byte[] commandDataRight = {
                START_SYSEX,
                I2C_REQUEST,
                DRV8830_RIGHT_ADDRESS,
                0x00,
                0x00,
                0x00,
                (byte) (value_right & 0x7f),
                (byte) ((value_right >> 7) & 0x7f),
                END_SYSEX
        };
        mFaBoUsbManager.writeBuffer(commandDataRight);
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
