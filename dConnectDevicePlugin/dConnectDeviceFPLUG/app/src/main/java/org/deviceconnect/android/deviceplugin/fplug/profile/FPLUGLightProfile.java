/*
 FPLUGLightProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.fplug.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.fplug.FPLUGApplication;
import org.deviceconnect.android.deviceplugin.fplug.FPLUGDeviceService;
import org.deviceconnect.android.deviceplugin.fplug.fplug.FPLUGController;
import org.deviceconnect.android.deviceplugin.fplug.fplug.FPLUGRequestCallback;
import org.deviceconnect.android.deviceplugin.fplug.fplug.FPLUGResponse;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.LightProfile;
import org.deviceconnect.message.DConnectMessage;

import java.util.List;

/**
 * Light Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class FPLUGLightProfile extends LightProfile {

    private static final int RGB_LENGTH = 6;

    @Override
    protected boolean onGetLight(Intent request, Intent response) {
        String serviceId = getServiceID(request);

        FPLUGApplication app = ((FPLUGApplication) getContext().getApplicationContext());
        List<FPLUGController> fplugs = app.getConnectedController();
        if (fplugs.size() == 0) {
            MessageUtils.setNotFoundServiceError(response, "Not found fplug: " + serviceId);
            return true;
        }

        Bundle lightParam = null;
        for (FPLUGController fplug : fplugs) {
            if (fplug.getAddress().equals(serviceId)) {
                lightParam = new Bundle();
                lightParam.putString(PARAM_LIGHT_ID, fplug.getAddress());
                lightParam.putString(PARAM_NAME, "F-PLUG");
                lightParam.putString(PARAM_CONFIG, "");
                lightParam.putBoolean(PARAM_ON, false);//f-plug's status can not be take. So always OFF.
                break;
            }
        }
        if (lightParam == null) {
            MessageUtils.setNotFoundServiceError(response, "Not found fplug: " + serviceId);
            return true;
        }

        Bundle[] lightParams = {lightParam};
        response.putExtra(PARAM_LIGHTS, lightParams);

        sendResultOK(response);
        return true;
    }

    @Override
    protected boolean onPostLight(Intent request, final Intent response) {
        String lightId = request.getStringExtra(PARAM_LIGHT_ID);
        if (lightId == null || lightId.length() == 0) {
            MessageUtils.setInvalidRequestParameterError(response, "lightId is not specified.");
            return true;
        }
        float brightness = getBrightnessParam(request, response);
        if (brightness == -1) {
            return true;
        }

        int[] colorParam = new int[3];
        if (!(getColorParam(request, response, colorParam))) {
            return true;
        }

        FPLUGApplication app = ((FPLUGApplication) getContext().getApplicationContext());
        FPLUGController controller = app.getFPLUGController(lightId);
        if (controller == null) {
            MessageUtils.setInvalidRequestParameterError(response, "Not found fplug: " + lightId);
            return true;
        }
        controller.requestLEDControl(true, new FPLUGRequestCallback() {
            @Override
            public void onSuccess(FPLUGResponse fResponse) {
                sendResultOK(response);

            }

            @Override
            public void onError(String message) {
                sendResultError(response);

            }

            @Override
            public void onTimeout() {
                sendResultTimeout(response);
            }
        });
        return false;
    }

    @Override
    protected boolean onDeleteLight(Intent request, final Intent response) {
        String lightId = request.getStringExtra(PARAM_LIGHT_ID);
        // 必須パラメータの存在チェック
        if (lightId == null || lightId.length() == 0) {
            MessageUtils.setInvalidRequestParameterError(response, "lightId is not specified.");
            return true;
        }
        FPLUGApplication app = ((FPLUGApplication) getContext().getApplicationContext());
        FPLUGController controller = app.getFPLUGController(lightId);
        if (controller == null) {
            MessageUtils.setInvalidRequestParameterError(response, "Not found fplug: " + lightId);
            return true;
        }
        controller.requestLEDControl(false, new FPLUGRequestCallback() {
            @Override
            public void onSuccess(FPLUGResponse fResponse) {
                sendResultOK(response);

            }

            @Override
            public void onError(String message) {
                sendResultError(response);

            }

            @Override
            public void onTimeout() {
                sendResultTimeout(response);
            }
        });
        return false;
    }

    private void sendResultOK(Intent response) {
        setResult(response, DConnectMessage.RESULT_OK);
        ((FPLUGDeviceService) getContext()).sendResponse(response);
    }

    private void sendResultError(Intent response) {
        MessageUtils.setUnknownError(response);
        ((FPLUGDeviceService) getContext()).sendResponse(response);
    }

    private void sendResultTimeout(Intent response) {
        MessageUtils.setTimeoutError(response);
        ((FPLUGDeviceService) getContext()).sendResponse(response);
    }

    /**
     * Get brightness parameter.
     *
     * @param request  request
     * @param response response
     * @return Brightness parameter, if -1, parameter error.
     */
    private static float getBrightnessParam(final Intent request, final Intent response) {
        float brightness;
        if (getBrightness(request) != null) {
            try {
                brightness = Float.valueOf(getBrightness(request));
                if (brightness > 1.0 || brightness < 0) {
                    MessageUtils.setInvalidRequestParameterError(response,
                            "brightness should be a value between 0 and 1.0");
                    return -1;
                }
            } catch (NumberFormatException e) {
                MessageUtils
                        .setInvalidRequestParameterError(response, "brightness should be a value between 0 and 1.0");
                return -1;
            }
        } else {
            brightness = 1;
        }
        return brightness;
    }

    private static String getBrightness(final Intent request) {
        return request.getStringExtra(PARAM_BRIGHTNESS);
    }

    private static String getColor(final Intent request) {
        return request.getStringExtra(PARAM_COLOR);
    }

    /**
     * Get color parameter.
     *
     * @param request  request
     * @param response response
     * @param color    Color parameter.
     * @return true : Success, false : failure.
     */
    private static boolean getColorParam(final Intent request, final Intent response, final int[] color) {
        if (getColor(request) != null) {
            try {
                String colorParam = getColor(request);
                String rr = colorParam.substring(0, 2);
                String gg = colorParam.substring(2, 4);
                String bb = colorParam.substring(4, 6);
                if (colorParam.length() == RGB_LENGTH) {
                    color[0] = Integer.parseInt(rr, 16);
                    color[1] = Integer.parseInt(gg, 16);
                    color[2] = Integer.parseInt(bb, 16);
                } else {
                    MessageUtils.setInvalidRequestParameterError(response, "color rgb length is invalid.");
                    return false;
                }
            } catch (NumberFormatException e) {
                MessageUtils.setInvalidRequestParameterError(response, "color rgb format is invalid.");
                return false;
            } catch (IllegalArgumentException e) {
                MessageUtils.setInvalidRequestParameterError(response, "color is invalid.");
                return false;
            }
        } else {
            color[0] = 0xFF;
            color[1] = 0xFF;
            color[2] = 0xFF;
        }
        return true;
    }

}
