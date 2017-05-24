/*
FaBoGPIOProfile
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
*/
package org.deviceconnect.android.deviceplugin.fabo.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.fabo.FaBoDeviceService;
import org.deviceconnect.android.deviceplugin.fabo.device.RobotCar;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import io.fabo.serialkit.FaBoUsbManager;

/**
 * GPIO Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class FaBoDriveControllerProfile extends DConnectProfile {

    private final static String TAG = "FABO_PLUGIN";

    public FaBoDriveControllerProfile() {
        // PUT /driveController/rotate
        addApi(new PutApi() {
            @Override
            public String getAttribute() {
                return "rotate";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                Float angle = parseFloat(request, "angle");

                RobotCar robotCar = getRobotCar();
                if (robotCar == null) {
                    MessageUtils.setIllegalDeviceStateError(response);
                    return true;
                }

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

                RobotCar robotCar = getRobotCar();
                if (robotCar == null) {
                    MessageUtils.setIllegalDeviceStateError(response);
                    return true;
                }

                if (speed != null) {
                    if (speed == 0) {
                        robotCar.stop();
                    } else if (speed > 0) {
                        robotCar.goForward(speed);
                    } else {
                        robotCar.goBack(Math.abs(speed));
                    }
                }

                if (angle != null) {
                    robotCar.turnHandle(calcAngle(angle));
                }

                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            }
        });

        addApi(new DeleteApi() {
            @Override
            public String getAttribute() {
                return "move";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                RobotCar robotCar = getRobotCar();
                if (robotCar == null) {
                    MessageUtils.setIllegalDeviceStateError(response);
                    return true;
                }
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

    /**
     * RobotCarのインスタンスを取得します.
     * @return RobotCarのインスタンス
     */
    private RobotCar getRobotCar() {
        FaBoUsbManager manager = ((FaBoDeviceService) getContext()).getFaBoUsbManager();
        if (manager == null) {
            return null;
        }

        return new RobotCar(manager);
    }
}
