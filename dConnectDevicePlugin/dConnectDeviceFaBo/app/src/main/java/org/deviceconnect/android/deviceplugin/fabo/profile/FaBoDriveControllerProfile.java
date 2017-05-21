/*
FaBoGPIOProfile
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
*/
package org.deviceconnect.android.deviceplugin.fabo.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.fabo.FaBoDeviceService;
import org.deviceconnect.android.deviceplugin.fabo.param.FirmataV32;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PostApi;

import static org.deviceconnect.message.DConnectMessage.RESULT_OK;

/**
 * GPIO Profile.
 * @author NTT DOCOMO, INC.
 */
public class FaBoDriveControllerProfile extends DConnectProfile {

    private final static String TAG = "FABO_PLUGIN";

    private String PROFILE_NAME = "driveController";

    private String INTERFACE_MOVE = "move";

    private final byte DRV8830_FORWARD = 0x01;
    private final byte DRV8830_BACK = 0x02;
    private final byte DRV8830_STOP = 0x00;
    private final byte DRV8830_ADDRESS = 0x64;

    public float arduino_map(float x, float in_min, float in_max, float out_min, float out_max) {
        return (x - in_min)*(out_max - out_min) / (in_max - in_min) + out_min;
    }

    // Motor Driver
    private void addGetDriveControllerApi() {

        // POST /driveController/move/
        addApi(new PostApi() {
            @Override
            public String getInterface() {
                return INTERFACE_MOVE;
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                return true;
            }

        });
    }

    // Motor Driver
    private void addPostDriveControllerApi() {

        // POST /driveController/move/
         addApi(new PostApi() {
            @Override
            public String getInterface() {
                return INTERFACE_MOVE;
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                int angleValue;
                float moveValue;
                String angle = request.getStringExtra("angle");
                String move = request.getStringExtra("move");
                if(angle != null) {
                    try {
                        angleValue = Integer.parseInt(angle);
                        if (angleValue < -360 || angleValue > 360) {
                            MessageUtils.setInvalidRequestParameterError(response, "The value of angle must be defined from -360 to 360.");
                            return true;
                        }

                    } catch (Exception e) {
                        MessageUtils.setInvalidRequestParameterError(response, "The value of angle must be defined from -360 to 360.");
                        return true;
                    }
                } else {
                    MessageUtils.setInvalidRequestParameterError(response, "The value of angle is null.");
                    return true;
                }
                if(move != null) {
                    try {
                        moveValue = Float.parseFloat(move);
                        if (moveValue < -1 || moveValue > 1) {
                            MessageUtils.setInvalidRequestParameterError(response, "The value of move must be defined from -1 to 1.");
                            return true;
                        }

                        int direction = 0;
                        if(moveValue < 0) {
                            direction = DRV8830_BACK;
                            moveValue = arduino_map(moveValue, 0, 1.0f, 0, 56.0f);
                        } else if(moveValue > 0) {
                            direction = DRV8830_FORWARD;
                        } else if(moveValue == 0) {
                            direction = DRV8830_STOP;
                        }
                        byte[] configCommandData = {FirmataV32.START_SYSEX, FirmataV32.I2C_CONFIG, (byte)0x00, (byte)0x00, FirmataV32.END_SYSEX};
                        ((FaBoDeviceService) getContext()).SendMessage(configCommandData);

                        byte speedLsb = (byte)((((int)moveValue << 2) | direction) & 0x7f);
                        byte speedMsb = (byte)(((((int)moveValue << 2) | direction) >> 7 )& 0x7f);

                        byte[] commandData = {FirmataV32.START_SYSEX, FirmataV32.I2C_REQUEST, DRV8830_ADDRESS, 0x00, 0x00, 0x00, speedLsb, speedMsb, FirmataV32.END_SYSEX};
                        ((FaBoDeviceService) getContext()).SendMessage(commandData);

                    } catch (Exception e) {
                        MessageUtils.setInvalidRequestParameterError(response, "The value of move must be defined from -1 to 1.");
                        return true;
                    }
                } else {
                    MessageUtils.setInvalidRequestParameterError(response, "The value of move is null.");
                    return true;
                }

                setResult(response, RESULT_OK);
                return true;
            }
        });
    }

    public FaBoDriveControllerProfile() {
        addPostDriveControllerApi();
        addGetDriveControllerApi();
    }

    @Override
    public String getProfileName() {
        return PROFILE_NAME;
    }
}
