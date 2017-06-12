package org.deviceconnect.android.deviceplugin.fabo.device;

public interface IMouseCar {
    /**
     * Mouse move.
     * <p>
     * 左右のスピードを変えて回転します。
     * </p>
     * @param leftSpeed 前進するスピード(0.0〜1.0)
     * @param rightSpeed 前進するスピード(0.0〜1.0)
     */
    void move(final float rightSpeed, final float leftSpeed);

    /**
     * 停止します.
     */
    void stop();
}
