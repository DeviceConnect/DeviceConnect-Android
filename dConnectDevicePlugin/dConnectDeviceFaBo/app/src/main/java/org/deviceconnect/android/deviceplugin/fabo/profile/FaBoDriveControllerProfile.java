/*
FaBoGPIOProfile
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
*/
package org.deviceconnect.android.deviceplugin.fabo.profile;

import android.content.Intent;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.fabo.FaBoDeviceService;
import org.deviceconnect.android.deviceplugin.fabo.device.RobotCar;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import io.fabo.serialkit.FaBoUsbManager;

import static android.R.attr.angle;

/**
 * GPIO Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class FaBoDriveControllerProfile extends DConnectProfile {

    private final static String TAG = "FABO_PLUGIN";
    private final static String PARAM_MSG = "msg";

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
                int type = ((FaBoDeviceService) getContext()).getRobotType();

                RobotCar robotCar = getRobotCar(type);
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
                int type = ((FaBoDeviceService) getContext()).getRobotType();
                RobotCar robotCar = getRobotCar(type);
                if (robotCar == null) {
                    MessageUtils.setIllegalDeviceStateError(response);
                    return true;
                }
                Log.i(TAG,"type=" + type);
                if(type == robotCar.TYPE_CAR) {
                    Log.i(TAG,"TYPE_CAR");
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
                } else if(type == robotCar.TYPE_MOUSE) {
                    float speed_right = 0;
                    float speed_left = 0;
                    if(angle >= 0) {
                        float gain = angle / 360;
                        //Log.i(TAG, "angele + gain:" + gain);
                        speed_right = speed;
                        speed_left = speed * (1-gain);

                    } else {
                        float gain = - angle / 360;
                        //Log.i(TAG, "angle - gain:" + gain);
                        speed_right = speed * (1-gain);
                        speed_left = speed;
                    }
                    Log.i(TAG, "" + speed_right);
                    Log.i(TAG, "" + speed_left);

                    robotCar.moveMouse(speed_right, speed_left);
                }

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
                int type = ((FaBoDeviceService) getContext()).getRobotType();
                RobotCar robotCar = getRobotCar(type);
                if (robotCar == null) {
                    MessageUtils.setIllegalDeviceStateError(response);
                    return true;
                }
                robotCar.stop();
                return true;
            }
        });

        // POST /driveController/setType
        addApi(new PostApi() {
            @Override
            public String getAttribute() {
                return "setType";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                Integer type = parseInteger(request, "type");

                if (type == null) {
                    MessageUtils.setIllegalDeviceStateError(response);
                    return true;
                }

                ((FaBoDeviceService) getContext()).setRobotType(type);

                String[] typeStr = {"マウス型","ラジコン型"};
                setMessage(response, "RobotTypeを" + typeStr[type] + "(" + type + ")に設定しました。");
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
    private RobotCar getRobotCar(int type) {
        FaBoUsbManager manager = ((FaBoDeviceService) getContext()).getFaBoUsbManager();
        if (manager == null) {
            return null;
        }

        return new RobotCar(manager, type);
    }

    private void setMessage(final Intent message, final String msg) {
        message.putExtra(PARAM_MSG, msg);
    }
}
