/*
 HueLightProfile
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hue.profile;

import android.content.Intent;
import android.os.Bundle;

import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResource;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLight.PHLightColorMode;
import com.philips.lighting.model.PHLightState;

import org.deviceconnect.android.deviceplugin.hue.HueDeviceService;
import org.deviceconnect.android.deviceplugin.hue.db.HueManager;
import org.deviceconnect.android.deviceplugin.hue.R;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.LightProfile;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 親クラスで振り分けられたメソッドに対して、Hueのlight attribute処理を呼び出す.
 *
 * @author NTT DOCOMO, INC.
 */
public class HueLightProfile extends LightProfile {

    /**
     * hue minimum brightness value.
     */
    private static final int HUE_BRIGHTNESS_MIN_VALUE = 1;

    /**
     * hue maximum brightness value.
     */
    private static final int HUE_BRIGHTNESS_MAX_VALUE = 255;

    /**
     * hue SDK maximum brightness value.
     */
    private static final int HUE_BRIGHTNESS_TUNED_MAX_VALUE = 254;

    /**
     * ライトフラッシング管理マップ.
     */
    private final Map<String, FlashingExecutor> mFlashingMap = new HashMap<String, FlashingExecutor>();

    public HueLightProfile() {
        addApi(new GetApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String serviceId = getServiceID(request);
                String lightId = null;
                if (serviceId.contains(":")) {
                    String[] ids = serviceId.split(":");
                    serviceId = ids[0];
                    lightId = ids[1];
                }
                PHBridge bridge = HueManager.INSTANCE.findBridge(serviceId);
                if (bridge == null) {
                    MessageUtils.setNotFoundServiceError(response, "Not found bridge: " + serviceId);
                    return true;
                }
                List<Bundle> lightList = new ArrayList<Bundle>();
                if (lightId == null) {
                    for (PHLight phLight : bridge.getResourceCache().getAllLights()) {
                        PHLightState phState = phLight.getLastKnownLightState();
                        Bundle light = new Bundle();
                        setLightId(light, phLight.getIdentifier());
                        setName(light, phLight.getName());
                        setOn(light, phState != null ? phState.isOn() : false);
                        setConfig(light, "");
                        lightList.add(light);
                    }
                } else {
                    // Lightである場合は自分自身の情報のみ返す
                    PHLight phLight = HueManager.INSTANCE.getCacheLight(getServiceID(request));
                    PHLightState phState = phLight.getLastKnownLightState();
                    Bundle light = new Bundle();
                    setLightId(light, phLight.getIdentifier());
                    setName(light, phLight.getName());
                    setOn(light, phState != null ? phState.isOn() : false);
                    setConfig(light, "");
                    lightList.add(light);
                }
                setLights(response, lightList);
                sendResultOK(response);
                return true;
            }
        });

        addApi(new PostApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String serviceId = getServiceID(request);
                String lightId = getLightId(request);
                Integer color = getColor(request);
                Double brightness = getBrightness(request);
                long[] flashing = getFlashing(request);
                if (serviceId.contains(":")) {
                    String[] ids = serviceId.split(":");
                    serviceId = ids[0];
                    lightId = ids[1];
                }
                final PHBridge bridge = HueManager.INSTANCE.findBridge(serviceId);
                if (bridge == null) {
                    MessageUtils.setNotFoundServiceError(response, "Not found bridge: " + serviceId);
                    return true;
                }

                final PHLight light = HueManager.INSTANCE.findLight(bridge, lightId);
                if (light == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "Not found light: " + lightId + "@" + serviceId);
                    return true;
                }
                final PHLightState lightState = makeLightState(color, brightness, flashing);
                if (flashing != null) {
                    flashing(lightId, lightState, bridge, light, flashing);
                    sendResultOK(response);//do not check result of flashing
                    return true;
                } else {
                    bridge.updateLightState(light, lightState, new PHLightAdapter() {
                        @Override
                        public void onStateUpdate(final Map<String, String> successAttribute, final List<PHHueError> errorAttribute) {
                            sendResultOK(response);
                        }

                        @Override
                        public void onError(final int code, final String message) {
                            if (code == PHHueError.AUTHENTICATION_FAILED) {
                                disconnectHueBridge(bridge);
                            }

                            MessageUtils.setUnknownError(response, code + ": " + message);
                            sendResultERR(response);
                        }
                    });
                    return false;
                }
            }
        });

        addApi(new DeleteApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String serviceId = getServiceID(request);
                String lightId = getLightId(request);
                if (serviceId.contains(":")) {
                    String[] ids = serviceId.split(":");
                    serviceId = ids[0];
                    lightId = ids[1];
                }

                final PHBridge bridge = HueManager.INSTANCE.findBridge(serviceId);
                if (bridge == null) {
                    MessageUtils.setNotFoundServiceError(response, "Not found bridge: " + serviceId);
                    return true;
                }

                PHLight light = HueManager.INSTANCE.findLight(bridge, lightId);
                if (light == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "Not found light: " + lightId + "@" + serviceId);
                    return true;
                }

                PHLightState lightState = new PHLightState();
                lightState.setOn(false);

                bridge.updateLightState(light, lightState, new PHLightAdapter() {
                    @Override
                    public void onStateUpdate(final Map<String, String> successAttribute,
                                              final List<PHHueError> errorAttribute) {
                        sendResultOK(response);
                    }

                    @Override
                    public void onError(final int code, final String message) {
                        if (code == PHHueError.AUTHENTICATION_FAILED) {
                            disconnectHueBridge(bridge);
                        }

                        String errMsg = getContext().getString(
                            R.string.error_message_failed_to_update_light,
                            code, message);
                        MessageUtils.setUnknownError(response, errMsg);
                        sendResultERR(response);
                    }
                });
                return false;
            }
        });

        addApi(new PutApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                final String[] serviceId = new String[1];
                serviceId[0] = getServiceID(request);
                final String[] lightId = new String[1];
                lightId[0] = getLightId(request);
                Integer color = getColor(request);
                Double brightness = getBrightness(request);
                long[] flashing = getFlashing(request);
                final String name = getName(request);
                if (serviceId[0].contains(":")) {
                    String[] ids = serviceId[0].split(":");
                    serviceId[0] = ids[0];
                    lightId[0] = ids[1];
                }

                final PHBridge bridge = HueManager.INSTANCE.findBridge(serviceId[0]);
                if (bridge == null) {
                    MessageUtils.setNotFoundServiceError(response, "Not found bridge: " + serviceId[0]);
                    return true;
                }

                final PHLight light = HueManager.INSTANCE.findLight(bridge, lightId[0]);
                if (light == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "Not found light: " + lightId[0] + "@" + serviceId[0]);
                    return true;
                }

                if (name == null || name.length() == 0) {
                    MessageUtils.setInvalidRequestParameterError(response, "name is invalid.");
                    return true;
                }

                //wait for change name and status
                final CountDownLatch countDownLatch = new CountDownLatch(2);
                sendResponseAfterAwait(response, countDownLatch);

                PHLight newLight = new PHLight(light);
                newLight.setName(name);

                bridge.updateLight(newLight, new PHLightAdapter() {
                    private boolean mErrorFlag = false;

                    @Override
                    public void onSuccess() {
                        super.onSuccess();
                        DConnectService service = ((HueDeviceService) getContext()).getServiceProvider().getService(serviceId[0] + ":" + lightId[0]);
                        if (service != null) {
                            service.setName(name);
                        }
                        countDown();
                    }

                    @Override
                    public void onError(final int code, final String message) {
                        super.onError(code, message);
                        String errMsg = getContext().getString(
                            R.string.error_message_failed_to_update_light,
                            code, message);
                        MessageUtils.setUnknownError(response, errMsg);
                        mErrorFlag = true;
                        countDown();
                    }

                    private void countDown() {
                        if (!mErrorFlag) {
                            setResult(response, DConnectMessage.RESULT_OK);
                        }
                        countDownLatch.countDown();
                    }
                });

                final PHLightState lightState = makeLightState(color, brightness, flashing);
                if (flashing != null) {
                    flashing(lightId[0], lightState, bridge, light, flashing);
                    countDownLatch.countDown();//do not check result of flashing
                } else {
                    bridge.updateLightState(light, lightState, new PHLightAdapter() {
                        private boolean mErrorFlag = false;

                        @Override
                        public void onStateUpdate(final Map<String, String> successAttribute, final List<PHHueError> errorAttribute) {
                            countDown();
                        }

                        @Override
                        public void onError(final int code, final String message) {
                            if (code == PHHueError.AUTHENTICATION_FAILED) {
                                disconnectHueBridge(bridge);
                            }

                            String errMsg = getContext().getString(
                                R.string.error_message_failed_to_update_light,
                                code, message);
                            MessageUtils.setUnknownError(response, errMsg);
                            mErrorFlag = true;
                            countDown();
                        }

                        private void countDown() {
                            if (!mErrorFlag) {
                                setResult(response, DConnectMessage.RESULT_OK);
                            }
                            countDownLatch.countDown();
                        }
                    });
                }
                return false;
            }
        });
    }

    private PHLightState makeLightState(Integer color, Double brightness, long[] flashing) {
        int[] colors = convertColor(color);

        // Brightness magnification conversion
        calcColorParam(colors, brightness);

        // Calculation of brightness.
        int calcBrightness = calcBrightnessParam(colors);

        PHLightState lightState = new PHLightState();
        lightState.setOn(true);
        lightState.setColorMode(PHLightColorMode.COLORMODE_XY);

        Color hueColor = new Color(color);
        lightState.setX(hueColor.mX);
        lightState.setY(hueColor.mY);
        lightState.setBrightness(calcBrightness);
        if (flashing != null) {
            lightState.setTransitionTime(1);
        }
        return lightState;
    }

    private void flashing(String lightId, final PHLightState lightState, final PHBridge bridge, final PHLight light, long[] flashing) {
        FlashingExecutor exe = mFlashingMap.get(lightId);
        if (exe == null) {
            exe = new FlashingExecutor();
            mFlashingMap.put(lightId, exe);
        }
        exe.setLightControllable((isOn, listener) -> {
            lightState.setOn(isOn);
            bridge.updateLightState(light, lightState, new PHLightAdapter() {
                @Override
                public void onStateUpdate(final Map<String, String> successAttribute, final List<PHHueError> errorAttribute) {
                    listener.onComplete();
                }

                @Override
                public void onError(final int code, final String message) {
                    listener.onComplete();
                }
            });
        });
        exe.start(flashing);
    }

    private void sendResponseAfterAwait(final Intent response, final CountDownLatch latch) {
        new Thread(() -> {
            try {
                if (!latch.await(30, TimeUnit.SECONDS)) {
                    MessageUtils.setTimeoutError(response);
                }
            } catch (InterruptedException e) {
                MessageUtils.setTimeoutError(response);
            }
            sendResponse(response);
        }).start();
    }

    /**
     * Convert Integer to int[].
     *
     * @param color color
     * @return int[]
     */
    private int[] convertColor(final Integer color) {
        int[] colors = new int[3];
        if (color != null) {
            colors[0] = android.graphics.Color.red(color);
            colors[1] = android.graphics.Color.green(color);
            colors[2] = android.graphics.Color.blue(color);
        } else {
            colors[0] = 0xFF;
            colors[1] = 0xFF;
            colors[2] = 0xFF;
        }
        return colors;
    }





    /**
     * 認証が失敗した場合にブリッジを切断します.
     * @param bridge 切断するブリッジ
     */
    private void disconnectHueBridge(final PHBridge bridge) {
        if (bridge == null) {
            return;
        }

        HueManager.INSTANCE.disconnectHueBridge(bridge);
        DConnectService service = getService();
        service.setOnline(false);
    }

    /**
     * 成功レスポンス送信.
     *
     * @param response response
     */
    private void sendResultOK(final Intent response) {
        setResult(response, DConnectMessage.RESULT_OK);
        sendResponse(response);
    }

    /**
     * エラーレスポンスを送信する.
     *
     * @param response エラーレスポンス
     */
    private void sendResultERR(final Intent response) {
        setResult(response, DConnectMessage.RESULT_ERROR);
        sendResponse(response);
    }

    /**
     * Calculate color parameter.
     *
     * @param color      Color parameters.
     * @param brightness Brightness parameter.
     */
    private void calcColorParam(final int[] color, final Double brightness) {
        if (brightness != null) {
            color[0] = (int) Math.round(color[0] * brightness);
            color[1] = (int) Math.round(color[1] * brightness);
            color[2] = (int) Math.round(color[2] * brightness);
        }
    }

    /**
     * Calculate brightness parameter.
     *
     * @param color Color parameters.
     * @return brightness Brightness parameter.
     */
    private int calcBrightnessParam(final int[] color) {
        int brightness = Math.max(color[0], color[1]);
        brightness = Math.max(brightness, color[2]);
        if (brightness < HUE_BRIGHTNESS_MIN_VALUE) {
            brightness = HUE_BRIGHTNESS_MIN_VALUE;
        } else if (brightness >= HUE_BRIGHTNESS_MAX_VALUE) {
            brightness = HUE_BRIGHTNESS_TUNED_MAX_VALUE;
        }
        return brightness;
    }

    /**
     * Hueの色指定.
     *
     * @author NTT DOCOMO, INC.
     */
    private static class Color {
        /**
         * モデル.
         */
        private static final String MODEL = "LCT001";

        /**
         * R.
         */
        final int mR;

        /**
         * G.
         */
        final int mG;

        /**
         * B.
         */
        final int mB;

        /**
         * 色相のX座標.
         */
        final float mX;

        /**
         * 色相のY座標.
         */
        final float mY;

        /**
         * コンストラクタ.
         *
         * @param rgb RGB
         */
        Color(final int rgb) {
            mR = android.graphics.Color.red(rgb);
            mG = android.graphics.Color.green(rgb);
            mB = android.graphics.Color.blue(rgb);
            float[] xy = PHUtilities.calculateXYFromRGB(mR, mG, mB, MODEL);
            mX = xy[0];
            mY = xy[1];
        }

        /**
         * コンストラクタ.
         *
         * @param rgb RGB
         */
        Color(final Integer rgb) {
            this(rgb != null ? rgb : 0xFFFFFF);
        }
    }

    /**
     * ライトのアダプター.
     *
     * @author NTT DOCOMO, INC.
     */
    private static class PHLightAdapter implements PHLightListener {

        @Override
        public void onError(final int code, final String message) {
        }

        @Override
        public void onStateUpdate(final Map<String, String> successAttribute, final List<PHHueError> errorAttribute) {
        }

        @Override
        public void onSuccess() {
        }

        @Override
        public void onReceivingLightDetails(final PHLight light) {
        }

        @Override
        public void onReceivingLights(final List<PHBridgeResource> lights) {
        }

        @Override
        public void onSearchComplete() {
        }
    }
}
