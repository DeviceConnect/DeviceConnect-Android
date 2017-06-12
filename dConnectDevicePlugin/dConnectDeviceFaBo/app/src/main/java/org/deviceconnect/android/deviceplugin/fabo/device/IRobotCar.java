package org.deviceconnect.android.deviceplugin.fabo.device;

public interface IRobotCar {
    /**
     * ハンドルを切ります.
     * @param direction 向き(-1.0〜1.0)
     */
    void turnHandle(final float direction);

    /**
     * 前進します.
     * @param speed 前進するスピード(-1.0〜1.0)
     */
    void move(final float speed);

    /**
     * 停止します.
     */
    void stop();
}
