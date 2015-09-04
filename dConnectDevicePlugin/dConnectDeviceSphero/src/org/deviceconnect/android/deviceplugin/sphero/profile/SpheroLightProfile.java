/*
 SpheroLightProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sphero.profile;

import org.deviceconnect.android.deviceplugin.sphero.SpheroManager;
import org.deviceconnect.android.deviceplugin.sphero.data.DeviceInfo;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.LightProfile;
import org.deviceconnect.message.DConnectMessage;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

/**
 * Lightプロファイル.
 * @author NTT DOCOMO, INC.
 */
public class SpheroLightProfile extends LightProfile {

    /**
     * 本体の色設定用ライトのID.
     */
    private static final String COLOR_LED_LIGHT_ID = "1";

    /**
     * バックライトのID.
     */
    private static final String BACK_LED_LIGHT_ID = "2";

    /**
     * 本体の色設定用ライトの名前.
     */
    private static final String COLOR_LED_LIGHT_NAME = "Sphero LED";

    /**
     * バックライトの名前.
     */
    private static final String BACK_LED_LIGHT_NAME = "Sphero CalibrationLED";

    /** 
     * brightnessの最大値.
     */
    public static final int MAX_BRIGHTNESS = 255;

    @Override
    protected boolean onGetLight(final Intent request, final Intent response, final String serviceId) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

        DeviceInfo info = SpheroManager.INSTANCE.getDevice(serviceId);
        if (info == null) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }

        Bundle[] lights = new Bundle[2];
        synchronized (info) {
            lights[0] = new Bundle();
            setLightId(lights[0], COLOR_LED_LIGHT_ID);
            setName(lights[0], COLOR_LED_LIGHT_NAME);
            setOn(lights[0], (Color.BLACK != info.getColor()));
            setConfig(lights[0], "");

            lights[1] = new Bundle();
            setLightId(lights[1], BACK_LED_LIGHT_ID);
            setName(lights[1], BACK_LED_LIGHT_NAME);
            setOn(lights[1], info.getBackBrightness() > 0);
            setConfig(lights[1], "");
        }
        setLights(response, lights);
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    @Override
    protected boolean onPostLight(final Intent request, final Intent response, final String serviceId,
            final String lightId, final Integer color, final Double brightness, final long[] flashing) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

        DeviceInfo info = SpheroManager.INSTANCE.getDevice(serviceId);
        if (info == null) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }

        synchronized (info) {
            int brightnessRaw = MAX_BRIGHTNESS;
            if (brightness != null) {
                brightnessRaw = (int) (MAX_BRIGHTNESS * brightness);
            }

            int[] colors = convertColor(color);

            if (COLOR_LED_LIGHT_ID.equals(lightId)) {
                if (flashing != null) {
                    SpheroManager.flashFrontLight(info, colors, flashing);
                } else {
                    info.setColor(colors[0], colors[1], colors[2]);
                }
                setResult(response, DConnectMessage.RESULT_OK);
            } else if (BACK_LED_LIGHT_ID.equals(lightId)) {
                if (flashing != null) {
                    SpheroManager.flashBackLight(info, brightnessRaw, flashing);
                } else {
                    float bf = brightnessRaw / (float) MAX_BRIGHTNESS;
                    info.setBackBrightness(bf);
                }
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response);
            }
        }
        return true;
    }

    @Override
    protected boolean onDeleteLight(final Intent request, final Intent response, final String serviceId,
            final String lightId) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

        DeviceInfo info = SpheroManager.INSTANCE.getDevice(serviceId);
        if (info == null) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }

        synchronized (info) {
            if (COLOR_LED_LIGHT_ID.equals(lightId)) {
                info.setColor(0, 0, 0);
                setResult(response, DConnectMessage.RESULT_OK);
            } else if (BACK_LED_LIGHT_ID.equals(lightId)) {
                info.setBackBrightness(0.0f);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response, "lightId is invalid.");
            }
        }
        return true;
    }

    /**
     * Convert Integer to int[].
     * @param color color
     * @return int[]
     */
    private int[] convertColor(final Integer color) {
        int[] colors = new int[3];
        if (color != null) {
            colors[0] = Color.red(color);
            colors[1] = Color.green(color);
            colors[2] = Color.blue(color);
        } else {
            colors[0] = 0xFF;
            colors[1] = 0xFF;
            colors[2] = 0xFF;
        }
        return colors;
    }
}
