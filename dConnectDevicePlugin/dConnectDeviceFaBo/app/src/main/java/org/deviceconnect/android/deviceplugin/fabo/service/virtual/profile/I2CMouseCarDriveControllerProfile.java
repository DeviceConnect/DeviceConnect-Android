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

                if (angle == null) {
                    angle = 0.0f;
                }

                if (speed == null) {
                    speed = 0.0f;
                }

                FaBoDeviceControl mgr = getFaBoDeviceControl();
                if (!getService().isOnline() || mgr == null) {
                    MessageUtils.setIllegalDeviceStateError(response);
                    return true;
                }

                float speed_right;
                float speed_left;
                if (angle >= 0) {
                    float gain = angle / 360.0f;
                    speed_right = speed;
                    speed_left = speed * (1 - gain);
                } else {
                    float gain = -angle / 360.0f;
                    speed_right = speed * (1 - gain);
                    speed_left = speed;
                }

                IMouseCar mouseCar = mgr.getMouseCar();
                mouseCar.move(speed_right, speed_left);

                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            }
        });

        // DELETE /driveController/move
        addApi(new DeleteApi() {
            @Override
            public String getAttribute() {
                return "move";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                FaBoDeviceControl mgr = getFaBoDeviceControl();
                if (!getService().isOnline() || mgr == null) {
                    MessageUtils.setIllegalDeviceStateError(response);
                    return true;
                }

                IMouseCar mouseCar = mgr.getMouseCar();
                mouseCar.stop();
                return true;
            }
        });
    }

    @Override
    public String getProfileName() {
        return "driveController";
    }
}
