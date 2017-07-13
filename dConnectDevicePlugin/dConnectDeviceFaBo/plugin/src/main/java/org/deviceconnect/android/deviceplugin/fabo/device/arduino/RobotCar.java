package org.deviceconnect.android.deviceplugin.fabo.device.arduino;

import org.deviceconnect.android.deviceplugin.fabo.device.IRobotCar;

import static org.deviceconnect.android.deviceplugin.fabo.device.arduino.FirmataV32.END_SYSEX;
import static org.deviceconnect.android.deviceplugin.fabo.device.arduino.FirmataV32.I2C_REQUEST;
import static org.deviceconnect.android.deviceplugin.fabo.device.arduino.FirmataV32.I2C_WRITE;
import static org.deviceconnect.android.deviceplugin.fabo.device.arduino.FirmataV32.START_SYSEX;
import static org.deviceconnect.android.deviceplugin.fabo.device.arduino.RobotCarUtil.DRV8830_BACK;
import static org.deviceconnect.android.deviceplugin.fabo.device.arduino.RobotCarUtil.DRV8830_FORWARD;
import static org.deviceconnect.android.deviceplugin.fabo.device.arduino.RobotCarUtil.DRV8830_STOP;
import static org.deviceconnect.android.deviceplugin.fabo.device.arduino.RobotCarUtil.calcHandleDirection;
import static org.deviceconnect.android.deviceplugin.fabo.device.arduino.RobotCarUtil.calcSpeed;

/**
 * RobotCarを操作するためのクラス.
 */
class RobotCar extends BaseI2C implements IRobotCar {
    /**
     * モータへのアドレス.
     */
    private static final byte DRV8830_ADDRESS = 0x64;

    /**
     * サーボへのアドレス.
     */
    private static final byte EXTENDED_ANALOG = 0x6F;

    /**
     * ピン設定モード.
     */
    private static final byte SET_PIN_MODE = (byte)0xF4;

    @Override
    public void turnHandle(final float direction) {
        byte[] commandData = {
                SET_PIN_MODE,
                (byte) 0x09,
                (byte) 0x04
        };
        getFaBoDeviceControl().writeI2C(commandData);

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
        getFaBoDeviceControl().writeI2C(commandSend);
    }

    @Override
    public void move(final float speed) {
        setI2CConfig();
        if (speed > 0) {
            moveMotor((DRV8830_FORWARD | ((calcSpeed(speed) + 5) << 2)));
        } else {
            moveMotor((DRV8830_BACK | ((calcSpeed(speed) + 5) << 2)));
        }
    }

    @Override
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
                I2C_WRITE,
                0x00,
                0x00,
                (byte) (value & 0x7f),
                (byte) ((value >> 7) & 0x7f),
                END_SYSEX
        };
        getFaBoDeviceControl().writeI2C(commandData);
    }
}
