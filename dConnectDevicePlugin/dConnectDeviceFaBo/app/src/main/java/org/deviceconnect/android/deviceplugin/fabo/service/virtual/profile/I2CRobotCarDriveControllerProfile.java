package org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.fabo.device.FaBoDeviceControl;
import org.deviceconnect.android.deviceplugin.fabo.device.IRobotCar;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

/**
 * RobotCar (Carタイプ)を操作するためのDriveControllerプロファイル.
 */
public class I2CRobotCarDriveControllerProfile extends BaseFaBoProfile {

    /**
     * コンストラクタ.
     */
    public I2CRobotCarDriveControllerProfile() {
        // PUT /driveController/rotate
        addApi(new PutApi() {
            @Override
            public String getAttribute() {
                return "rotate";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                Float angle = parseFloat(request, "angle");

                FaBoDeviceControl mgr = getFaBoDeviceControl();
                if (!getService().isOnline() || mgr == null) {
                    MessageUtils.setIllegalDeviceStateError(response);
                    return true;
                }

                IRobotCar robotCar = mgr.getRobotCar();
                if (angle != null) {
                    robotCar.turnHandle(calcAngle(angle));
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

                FaBoDeviceControl mgr = getFaBoDeviceControl();
                if (!getService().isOnline() || mgr == null) {
                    MessageUtils.setIllegalDeviceStateError(response);
                    return true;
                }

                IRobotCar robotCar = mgr.getRobotCar();
                if (speed != null) {
                    if (speed == 0) {
                        robotCar.stop();
                    } else {
                        robotCar.move(speed);
                    }
                }

                if (angle != null) {
                    robotCar.turnHandle(calcAngle(angle));
                }

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
                FaBoDeviceControl mgr = getFaBoDeviceControl();
                if (!getService().isOnline() || mgr == null) {
                    MessageUtils.setIllegalDeviceStateError(response);
                    return true;
                }

                IRobotCar robotCar = mgr.getRobotCar();
                robotCar.stop();
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
}
