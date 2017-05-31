package org.deviceconnect.android.deviceplugin.fabo.device.robotcar.car;

import org.deviceconnect.android.deviceplugin.fabo.device.robotcar.BaseRobotCar;


public class RobotCar extends BaseRobotCar {
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
        getFaBoUsbManager().writeBuffer(commandData);

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
        getFaBoUsbManager().writeBuffer(commandSend);
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
        getFaBoUsbManager().writeBuffer(commandData);
    }
}
