package org.deviceconnect.android.deviceplugin.fabo.device.things;

import com.google.android.things.pio.I2cDevice;

import org.deviceconnect.android.deviceplugin.fabo.device.IRobotCar;

import java.io.IOException;

class RobotCar implements IRobotCar {

    private static final String TAG = "FaBo";

    /**
     * モータ用のI2Cデバイスのアドレス.
     */
    private static final int MOTOR_ADDRESS = 0x64;

    /**
     * サーボモータ用のI2Cデバイスのアドレス.
     */
    private static final int PCA9685_ADDRESS = 0x40;

    /**
     * モータコントロール用のコマンド.
     */
    private static final int MOTOR_CONTROL = 0x00;

    /**
     * モータコントロール用の前進コマンド.
     */
    private static final int MOTOR_FORWARD = 0x01;

    /**
     * モータコントロール用の後進コマンド.
     */
    private static final int MOTOR_BACK = 0x02;

    /**
     * モータコントロール用の停止コマンド.
     */
    private static final int MOTOR_STOP = 0x00;

    private static final int PWM0_ON_L = 0x06;
    private static final int PWM0_ON_H = 0x07;
    private static final int PWM0_OFF_L = 0x08;
    private static final int PWM0_OFF_H = 0x09;
    private static final int PRE_SCALE = 0xFE;
    private static final int SLEEP_BIT = 0x10;
    private static final int CONTROL_REG = 0x00;
    private static final int OSC_CLOCK = 25000000;

    /**
     * サーボモータの下限を定義.
     * <p>
     * 前のタイヤを左端まで回す値.
     * </p>
     */
    private static final float PWM_MIN = 6f;

    /**
     * サーボモータの上限を定義.
     * <p>
     * 前のタイヤを右端まで回す値.
     * </p>
     */
    private static final float PWM_MAX = 9f;

    /**
     * 初期化を行う周波数.
     */
    private static final int FREQUENCY = 50;

    /**
     * モータ用のI2Cデバイス.
     */
    private I2cDevice mMotorDevice;

    /**
     * サーボ用のI2Cデバイス
     */
    private I2cDevice mPCA9685Device;

    RobotCar(final FaBoThingsDeviceControl control) {
        mPCA9685Device = control.getI2cDevice(PCA9685_ADDRESS);
        mMotorDevice = control.getI2cDevice(MOTOR_ADDRESS);
    }

    @Override
    public void turnHandle(float direction) {
        if (direction < -1) {
            direction = -1;
        }
        if (direction > 1) {
            direction = 1;
        }

        try {
            setFreq(mPCA9685Device, FREQUENCY);
            setPWM(calcHandleDirection(direction));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void move(final float speed) {
        if (speed > 0) {
            move(MOTOR_FORWARD, speed);
        } else {
            move(MOTOR_BACK, Math.abs(speed));
        }
    }

    @Override
    public void stop() {
        try {
            mMotorDevice.writeRegByte(MOTOR_CONTROL, (byte) MOTOR_STOP);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 移動します.
     * @param dir 向き
     * @param speed 速度
     */
    private void move(final int dir, float speed) {
        if (speed < 0) {
            speed = 0;
        }

        if (speed > 1) {
            speed = 1;
        }

        byte data = (byte) (dir | ((calcSpeed(speed) + 5) << 2));
        try {
            mMotorDevice.writeRegByte(MOTOR_CONTROL, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * サーボモータの回転を行います.
     * @param direction 回転する向き
     * @throws IOException サーボモータの回転に失敗した場合に発生
     */
    private void setPWM(final float direction) throws IOException {
        int value = (int) (direction * 4096 / 100);
        mPCA9685Device.writeRegByte(PWM0_ON_L, (byte) 0x00);
        mPCA9685Device.writeRegByte(PWM0_ON_H, (byte) 0x00);
        mPCA9685Device.writeRegByte(PWM0_OFF_L, (byte) (value & 0xFF));
        mPCA9685Device.writeRegByte(PWM0_OFF_H, (byte) ((value >> 8) & 0xFF));
    }

    /**
     * タイヤの回す角度を計算します。
     * @param x 回す値
     * @return 角度
     */
    private float calcHandleDirection(final float x) {
        float b = PWM_MAX - ((PWM_MAX - PWM_MIN) / 2.0f);
        return ((PWM_MAX - PWM_MIN) / 2.0f) * x + b;
    }

    /**
     * 0.0f〜1.0fの値を0〜55の値に変換します.
     * @param speed スピード(0.0f〜1.0f)
     * @return 0〜55の値
     */
    private int calcSpeed(final float speed) {
        return (int) (55 * speed);
    }

    /**
     * サーボモータの初期化を行います.
     * @param device サーボモータのI2Cデバイス
     * @param hz 初期化する周波数
     * @throws IOException 初期化設定に失敗した時に発生
     */
    private void setFreq(final I2cDevice device, final int hz) throws IOException {
        int value = Math.round(OSC_CLOCK / (4096 * hz)) - 1;

        byte ctrl_dat = device.readRegByte(CONTROL_REG);
        device.writeRegByte(CONTROL_REG, (byte) (ctrl_dat | SLEEP_BIT));

        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        device.writeRegByte(PRE_SCALE, (byte) value);

        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        device.writeRegByte(CONTROL_REG, (byte) (ctrl_dat & (~SLEEP_BIT)));
    }
}
