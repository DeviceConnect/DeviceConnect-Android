package org.deviceconnect.android.deviceplugin.fabo.device.robotcar;

public class MouseCar extends BaseRobotCar {

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
            tmp_speed_right = speed_right;
        } else if (speed_right < 0) {
            right_dir = DRV8830_FORWARD;
            tmp_speed_right = -speed_right;
        }

        if (speed_left > 0) {
            left_dir = DRV8830_BACK;
            tmp_speed_left = speed_left;
        } else if (speed_left < 0) {
            left_dir = DRV8830_FORWARD;
            tmp_speed_left = -speed_left;
        }

        moveMotor((right_dir | ((calcSpeed(tmp_speed_right) + 5) << 2)), (left_dir | ((calcSpeed(tmp_speed_left) + 5) << 2)));
    }

    /**
     * 停止します.
     */
    public void stop() {
        setI2CConfig();
        moveMotor(DRV8830_STOP, DRV8830_STOP);
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
        getFaBoUsbManager().writeBuffer(commandDataLeft);

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
        getFaBoUsbManager().writeBuffer(commandDataRight);
    }
}
