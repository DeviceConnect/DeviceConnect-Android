/*
 SpheroLightProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sphero.profile;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.sphero.SpheroManager;
import org.deviceconnect.android.deviceplugin.sphero.data.DeviceInfo;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.LightProfile;
import org.deviceconnect.message.DConnectMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Lightプロファイル.
 *
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

    private Map<String, FlashingExecutor> mFlashingMap = new HashMap<String, FlashingExecutor>();

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
                                  final String lightId, final Integer color, final Double brightness,
                                  final long[] flashing) {
        return changeLight(response, serviceId, lightId, color, brightness, flashing);
    }

    @Override
    protected boolean onPutLight(final Intent request, final Intent response, final String serviceId,
                                 final String lightId, final String name, final Integer color,
                                 final Double brightness, final long[] flashing) {
        return changeLight(response, serviceId, lightId, color, brightness, flashing);
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
            if (COLOR_LED_LIGHT_ID.equals(lightId) || lightId == null) {
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
     * Change the color of the light.
     *
     * @param response   response
     * @param serviceId  service id
     * @param lightId    light id
     * @param color      color
     * @param brightness brightness(0.0〜1.0)
     * @param flashing   flashing
     * @return true
     */
    private boolean changeLight(final Intent response, final String serviceId, final String lightId,
                                final Integer color, final Double brightness, final long[] flashing) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

        DeviceInfo info = SpheroManager.INSTANCE.getDevice(serviceId);
        if (info == null) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }

        if (COLOR_LED_LIGHT_ID.equals(lightId) || lightId == null) {
            changeFrontLight(serviceId, lightId, info, color, brightness, flashing);
            setResult(response, DConnectMessage.RESULT_OK);
        } else if (BACK_LED_LIGHT_ID.equals(lightId)) {
            changeBackLight(serviceId, lightId, info, brightness, flashing);
            setResult(response, DConnectMessage.RESULT_OK);
        } else {
            MessageUtils.setInvalidRequestParameterError(response, "lightId is invalid.");
        }

        return true;
    }

    private void changeFrontLight(String serviceId, String lightId, DeviceInfo info, Integer color, Double brightness, long[] flashing) {
        int[] colors = convertColor(color, brightness);
        if (flashing != null) {
            flashing(serviceId, lightId, info, colors, 0, flashing);
        } else {
            info.setColor(colors[0], colors[1], colors[2]);
        }
    }

    private void changeBackLight(String serviceId, String lightId, DeviceInfo info, Double brightness, long[] flashing) {
        int brightnessRaw = MAX_BRIGHTNESS;
        if (brightness != null) {
            brightnessRaw = (int) (MAX_BRIGHTNESS * brightness);
        }
        if (flashing != null) {
            flashing(serviceId, lightId, info, null, brightnessRaw, flashing);
        } else {
            float bf = brightnessRaw / (float) MAX_BRIGHTNESS;
            info.setBackBrightness(bf);
        }
    }

    private void flashing(String serviceId, final String lightId, final DeviceInfo info, final int[] colors, final int brightnessRaw, long[] flashing) {
        FlashingExecutor exe = mFlashingMap.get(serviceId + lightId);
        if (exe == null) {
            exe = new FlashingExecutor();
            mFlashingMap.put(serviceId + lightId, exe);
        }
        exe.setLightControllable(new FlashingExecutor.LightControllable() {
            @Override
            public void changeLight(boolean isOn, final FlashingExecutor.CompleteListener listener) {
                if (COLOR_LED_LIGHT_ID.equals(lightId) || lightId == null) {
                    if (isOn) {
                        info.setColor(colors[0], colors[1], colors[2]);
                    } else {
                        info.setColor(0, 0, 0);
                    }
                } else if (BACK_LED_LIGHT_ID.equals(lightId)) {
                    if (isOn) {
                        float bf = brightnessRaw / (float) MAX_BRIGHTNESS;
                        info.setBackBrightness(bf);
                    } else {
                        info.setBackBrightness(0.0f);
                    }
                }
                listener.onComplete();
            }
        });
        exe.start(flashing);
    }

    /**
     * Convert Integer to int[].
     *
     * @param color color
     * @return int[]
     * @paarm brightness brightness
     */
    private int[] convertColor(final Integer color, final Double brightness) {
        double b = 1.0;
        if (brightness != null) {
            b = brightness;
        }

        int[] colors = new int[3];
        if (color != null) {
            colors[0] = (int) (Color.red(color) * b);
            colors[1] = (int) (Color.green(color) * b);
            colors[2] = (int) (Color.blue(color) * b);
        } else {
            colors[0] = (int) (0xFF * b);
            colors[1] = (int) (0xFF * b);
            colors[2] = (int) (0xFF * b);
        }
        return colors;
    }
}
