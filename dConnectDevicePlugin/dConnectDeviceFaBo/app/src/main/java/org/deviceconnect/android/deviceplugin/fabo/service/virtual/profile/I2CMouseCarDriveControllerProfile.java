package org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.fabo.device.FaBoDeviceControl;
import org.deviceconnect.android.deviceplugin.fabo.device.IMouseCar;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

/**
 * RobotCar (Mouseタイプ)を操作するためのDriveControllerプロファイル.
 * <p>
 * ID: #1201 <br>
 * Name: RobotCar Kit <br>
 * </p>
 */
public class I2CMouseCarDriveControllerProfile extends BaseFaBoProfile {

    /**
     * RobotCarの向きを保持します.
     * <p>
     * この角度でRobotCarが回転します。
     * </p>
     */
    private float mAngle = 0;

    /**
     * RobotCarの速度を保持します.
     */
    private float mSpeed = 0;

    /**
     * コンストラクタ.
     */
    public I2CMouseCarDriveControllerProfile() {
        // PUT /driveController/rotate
        addApi(new PutApi() {
            @Override
            public String getAttribute() {
                return "rotate";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                Float angle = parseFloat(request, "angle");

                if (angle != null) {
                    mAngle = angle;
                }

                FaBoDeviceControl mgr = getFaBoDeviceControl();
                if (!getService().isOnline() || mgr == null) {
                    MessageUtils.setIllegalDeviceStateError(response);
                    return true;
                }

                // 速度が入っている場合には動作しているの移動する
                if (mSpeed != 0) {
                    moveRobotCar();
                }

                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            }
        });

        // POST /driveController/move
        addApi(new PostApi() {
            @Override
            public String getAttribute() {
                return "move";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                Float angle = parseFloat(request, "angle");
                Float speed = parseFloat(request, "speed");

                if (angle != null) {
                    mAngle = angle;
                }

                if (speed != null) {
                    mSpeed = speed;
                }

                FaBoDeviceControl mgr = getFaBoDeviceControl();
                if (!getService().isOnline() || mgr == null) {
                    MessageUtils.setIllegalDeviceStateError(response);
                    return true;
                }

                moveRobotCar();

                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            }
        });

        // DELETE /driveController/stop
        addApi(new DeleteApi() {
            @Override
            public String getAttribute() {
                return "stop";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {

                // 停止したので速度を0にしておく
                mSpeed = 0;

                FaBoDeviceControl mgr = getFaBoDeviceControl();
                if (!getService().isOnline() || mgr == null) {
                    MessageUtils.setIllegalDeviceStateError(response);
                    return true;
                }

                IMouseCar mouseCar = mgr.getMouseCar();
                mouseCar.stop();

                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            }
        });
    }

    @Override
    public String getProfileName() {
        return "driveController";
    }

    /**
     * RobotCar(Mouse)を移動させます.
     */
    private void moveRobotCar() {
        float speed_right;
        float speed_left;
        if (mAngle >= 0) {
            float gain = mAngle / 360.0f;
            speed_right = mSpeed;
            speed_left = mSpeed * (1 - gain);
        } else {
            float gain = -mAngle / 360.0f;
            speed_right = mSpeed * (1 - gain);
            speed_left = mSpeed;
        }

        FaBoDeviceControl mgr = getFaBoDeviceControl();
        IMouseCar mouseCar = mgr.getMouseCar();
        mouseCar.move(speed_right, speed_left);
    }
}
