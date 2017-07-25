package org.deviceconnect.android.deviceplugin.fabo.device;

/**
 * マウス型RobotCarを操作するためのインターフェース.
 */
public interface IMouseCar {
    /**
     * Mouse move.
     * <p>
     * 左右のスピードを変えて回転します。
     * </p>
     * @param leftSpeed 移動するスピード(-1.0〜1.0)
     * @param rightSpeed 移動するスピード(-1.0〜1.0)
     */
    void move(final float rightSpeed, final float leftSpeed);

    /**
     * 停止します.
     */
    void stop();
}
