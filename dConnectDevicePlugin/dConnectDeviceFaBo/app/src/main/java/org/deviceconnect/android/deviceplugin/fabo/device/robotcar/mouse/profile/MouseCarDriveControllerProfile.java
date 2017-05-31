package org.deviceconnect.android.deviceplugin.fabo.device.robotcar.mouse.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.fabo.FaBoDeviceService;
import org.deviceconnect.android.deviceplugin.fabo.device.robotcar.mouse.MouseCar;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import io.fabo.serialkit.FaBoUsbManager;

public class MouseCarDriveControllerProfile extends DConnectProfile {

    private MouseCar mMouseCar = new MouseCar();

    public MouseCarDriveControllerProfile() {
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

                FaBoUsbManager mgr = getFaBoUsbManager();
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
                mMouseCar.setFaBoUsbManager(mgr);
                mMouseCar.moveMouse(speed_right, speed_left);

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
                FaBoUsbManager mgr = getFaBoUsbManager();
                if (!getService().isOnline() || mgr == null) {
                    MessageUtils.setIllegalDeviceStateError(response);
                    return true;
                }

                mMouseCar.setFaBoUsbManager(mgr);
                mMouseCar.stop();
                return true;
            }
        });
    }

    @Override
    public String getProfileName() {
        return "driveController";
    }

    /**
     * 角度(-360〜360)を-1.0〜1.0の範囲に変換します.
     * @param angle 角度
     * @return 返還後の値
     */
    private float calcAngle(final float angle) {
        if (angle > 360) {
            return 1.0f;
        }
        if (angle < -360) {
            return -1.0f;
        }
        return angle / 360.0f;
    }

    private FaBoUsbManager getFaBoUsbManager() {
        FaBoDeviceService service = (FaBoDeviceService) getContext();
        return service.getFaBoUsbManager();
    }
}
