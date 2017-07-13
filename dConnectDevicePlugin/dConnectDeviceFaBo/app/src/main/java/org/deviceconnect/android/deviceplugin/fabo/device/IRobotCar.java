package org.deviceconnect.android.deviceplugin.fabo.device;

/**
 * RobotCarを操作するためのインターフェース.
 */
public interface IRobotCar {
    /**
     * ハンドルを切ります.
     * @param direction 向き(-1.0〜1.0)
     */
    void turnHandle(final float direction);

    /**
     * 移動します.
     * @param speed 移動するスピード(-1.0〜1.0)
     */
    void move(final float speed);

    /**
     * 停止します.
     */
    void stop();
}
