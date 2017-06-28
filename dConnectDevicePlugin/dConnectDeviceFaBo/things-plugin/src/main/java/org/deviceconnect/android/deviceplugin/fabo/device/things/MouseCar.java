package org.deviceconnect.android.deviceplugin.fabo.device.things;

import com.google.android.things.pio.I2cDevice;

import org.deviceconnect.android.deviceplugin.fabo.device.IMouseCar;

import java.io.IOException;

class MouseCar implements IMouseCar {
    /**
     * 左側のモータへのアドレス.
     */
    private static final byte DRV8830_LEFT_ADDRESS = 0x64;

    /**
     * 右側のモータへのアドレス.
     */
    private static final byte DRV8830_RIGHT_ADDRESS = 0x65;

    /**
     * 前進.
     */
    private static final byte DRV8830_FORWARD = 0x01;

    /**
     * 行進.
     */
    private static final byte DRV8830_BACK = 0x02;

    /**
     * 停止.
     */
    private static final byte DRV8830_STOP = 0x00;

    /**
     * モータコントロール用のコマンド.
     */
    private static final int MOTOR_CONTROL = 0x00;

    /**
     * モータの回転数の上限を定義.
     */
    private static final int MAX_MOTOR = 55;

    /**
     * 左モータ用のI2Cデバイス.
     */
    private I2cDevice mLeftDevice;

    /**
     * 右モータ用のI2Cデバイス
     */
    private I2cDevice mRightDevice;

    MouseCar(final FaBoThingsDeviceControl control) {
        mRightDevice = control.getI2cDevice(DRV8830_RIGHT_ADDRESS);
        mLeftDevice = control.getI2cDevice(DRV8830_LEFT_ADDRESS);
    }

    @Override
    public void move(float rightSpeed, float leftSpeed) {
        int rightDir = 0;
        int leftDir = 0;
        float tmpLeftSpeed = 0;
        float tmpRightSpeed = 0;

        if (rightSpeed > 0) {
            rightDir = DRV8830_BACK;
            tmpRightSpeed = rightSpeed;
        } else if (rightSpeed < 0) {
            rightDir = DRV8830_FORWARD;
            tmpRightSpeed = -rightSpeed;
        }

        if (leftSpeed > 0) {
            leftDir = DRV8830_BACK;
            tmpLeftSpeed = leftSpeed;
        } else if (leftSpeed < 0) {
            leftDir = DRV8830_FORWARD;
            tmpLeftSpeed = -leftSpeed;
        }

        try {
            moveMotor((rightDir | ((calcSpeed(tmpRightSpeed) + 5) << 2)), (leftDir | ((calcSpeed(tmpLeftSpeed) + 5) << 2)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        try {
            moveMotor(DRV8830_STOP, DRV8830_STOP);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * モータの回転を送信します.
     * @param valueLeft 左側のモーター
     * @param valueRight 右側のモーター
     */
    private void moveMotor(final int valueLeft, final int valueRight) throws IOException {
        mLeftDevice.writeRegByte(MOTOR_CONTROL, (byte) valueLeft);
        mRightDevice.writeRegByte(MOTOR_CONTROL, (byte) valueRight);
    }

    /**
     * 0.0f〜1.0fの値を0〜55の値に変換します.
     * @param speed スピード(0.0f〜1.0f)
     * @return 0〜MAX_MOTORの値
     */
    private static int calcSpeed(final float speed) {
        float tmp = Math.abs(speed);
        if (tmp > 1.0f) {
            return MAX_MOTOR;
        }
        return (int) (MAX_MOTOR * tmp);
    }
}
