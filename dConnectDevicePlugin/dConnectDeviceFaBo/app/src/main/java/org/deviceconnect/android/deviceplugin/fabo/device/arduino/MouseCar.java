package org.deviceconnect.android.deviceplugin.fabo.device.arduino;

import org.deviceconnect.android.deviceplugin.fabo.device.IMouseCar;

/**
 * マウス型RobotCarを操作しるためのクラス.
 */
class MouseCar extends BaseRobotCar implements IMouseCar {
    /**
     * 左側のモータへのアドレス.
     */
    private static final byte DRV8830_LEFT_ADDRESS = 0x64;

    /**
     * 右側のモータへのアドレス.
     */
    private static final byte DRV8830_RIGHT_ADDRESS = 0x63;

    @Override
    public void move(final float rightSpeed, final float leftSpeed) {
        setI2CConfig();

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

        moveMotor((rightDir | ((calcSpeed(tmpRightSpeed) + 5) << 2)), (leftDir | ((calcSpeed(tmpLeftSpeed) + 5) << 2)));
    }

    /**
     * 停止します.
     */
    @Override
    public void stop() {
        setI2CConfig();
        moveMotor(DRV8830_STOP, DRV8830_STOP);
    }

    /**
     * モータの回転を送信します.
     * @param valueLeft 左側のモーター
     * @param valueRight 右側のモーター
     */
    private void moveMotor(final int valueLeft, final int valueRight) {
        byte[] commandDataLeft = {
                START_SYSEX,
                I2C_REQUEST,
                DRV8830_LEFT_ADDRESS,
                0x00,
                0x00,
                0x00,
                (byte) (valueLeft & 0x7f),
                (byte) ((valueLeft >> 7) & 0x7f),
                END_SYSEX
        };
        getFaBoDeviceControl().writeI2C(commandDataLeft);

        byte[] commandDataRight = {
                START_SYSEX,
                I2C_REQUEST,
                DRV8830_RIGHT_ADDRESS,
                0x00,
                0x00,
                0x00,
                (byte) (valueRight & 0x7f),
                (byte) ((valueRight >> 7) & 0x7f),
                END_SYSEX
        };
        getFaBoDeviceControl().writeI2C(commandDataRight);
    }
}
