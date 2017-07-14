package org.deviceconnect.android.deviceplugin.fabo.device.arduino;

class RobotCarUtil {
    /**
     * モータの回転数の上限を定義.
     */
    private static final int MAX_MOTOR = 55;

    static final byte DRV8830_FORWARD = 0x01;
    static final byte DRV8830_BACK = 0x02;
    static final byte DRV8830_STOP = 0x00;

    /**
     * 0.0f〜1.0fの値を0〜55の値に変換します.
     * @param speed スピード(0.0f〜1.0f)
     * @return 0〜MAX_MOTORの値
     */
    static int calcSpeed(final float speed) {
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
    static int calcHandleDirection(final float x) {
        return (int) (90 + x * 40);
    }
}
