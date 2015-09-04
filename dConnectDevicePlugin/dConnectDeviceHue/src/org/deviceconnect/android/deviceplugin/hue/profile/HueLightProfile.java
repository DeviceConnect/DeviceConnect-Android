/*
 HueLightProfile
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hue.profile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.deviceconnect.android.deviceplugin.hue.HueDeviceService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.LightProfile;
import org.deviceconnect.message.DConnectMessage;

import android.content.Intent;
import android.os.Bundle;

import com.philips.lighting.hue.listener.PHGroupListener;
import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResource;
import com.philips.lighting.model.PHBridgeResourcesCache;
import com.philips.lighting.model.PHGroup;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLight.PHLightColorMode;
import com.philips.lighting.model.PHLightState;

/**
 * 親クラスで振り分けられたメソッドに対して、Hueのlight attribute処理を呼び出す.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HueLightProfile extends LightProfile {

    /** hue minimum brightness value. */
    private static final int HUE_BRIGHTNESS_MIN_VALUE = 1;
    /** hue maximum brightness value. */
    private static final int HUE_BRIGHTNESS_MAX_VALUE = 255;
    /** hue SDK maximum brightness value. */
    private static final int HUE_BRIGHTNESS_TUNED_MAX_VALUE = 254;

    /** エラーコード301. */
    private static final int HUE_SDK_ERROR_301 = 301;

    @Override
    protected boolean onGetLight(final Intent request, final Intent response, final String serviceId) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

        PHBridge bridge = findBridge(serviceId);
        if (bridge == null) {
            MessageUtils.setNotFoundServiceError(response, "Not found bridge: " + serviceId);
            return true;
        }

        List<Bundle> lightList = new ArrayList<Bundle>();
        for (PHLight phLight : bridge.getResourceCache().getAllLights()) {
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

    @Override
    protected boolean onPostLight(final Intent request, final Intent response, final String serviceId,
            final String lightId, final Integer color, final Double brightness, final long[] flashing) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

        if (lightId == null || lightId.length() == 0) {
            MessageUtils.setInvalidRequestParameterError(response, "lightId is not specified.");
            return true;
        }

        PHBridge bridge = findBridge(serviceId);
        if (bridge == null) {
            MessageUtils.setNotFoundServiceError(response, "Not found bridge: " + serviceId);
            return true;
        }

        PHLight light = bridge.getResourceCache().getLights().get(lightId);
        if (light == null) {
            MessageUtils.setInvalidRequestParameterError(response, "Not found light: " + lightId + "@" + serviceId);
            return true;
        }

        int[] colors = convertColor(color);

        // Brightness magnification conversion
        calcColorParam(colors, brightness);

        // Calculation of brightness.
        int mCalcBrightness = calcBrightnessParam(colors);

        PHLightState lightState = new PHLightState();
        lightState.setOn(true);
        lightState.setColorMode(PHLightColorMode.COLORMODE_XY);

        Color hueColor = new Color(color);
        lightState.setX(hueColor.mX);
        lightState.setY(hueColor.mY);
        lightState.setBrightness(mCalcBrightness);

        bridge.updateLightState(light, lightState, new PHLightAdapter() {
            @Override
            public void onStateUpdate(final Map<String, String> successAttribute,
                    final List<PHHueError> errorAttribute) {
                sendResultOK(response);
            }
            @Override
            public void onError(final int code, final String message) {
                MessageUtils.setUnknownError(response, code + ": " + message);
                sendResultERR(response);
            }
        });
        return false;
    }

    @Override
    protected boolean onDeleteLight(final Intent request, final Intent response, final String serviceId,
            final String lightId) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

        if (lightId == null || lightId.length() == 0) {
            MessageUtils.setInvalidRequestParameterError(response, "lightId is not specified.");
            return true;
        }

        PHBridge bridge = findBridge(serviceId);
        if (bridge == null) {
            MessageUtils.setNotFoundServiceError(response, "Not found bridge: " + serviceId);
            return true;
        }

        PHLight light = bridge.getResourceCache().getLights().get(lightId);
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
                String errMsg = "ライトの状態更新に失敗しました hue:code = " + code + "  message = " + message;
                MessageUtils.setUnknownError(response, errMsg);
                sendResultERR(response);
            }
        });
        return false;
    }

    @Override
    protected boolean onPutLight(final Intent request, final Intent response, final String serviceId,
            final String lightId, final String name, final Integer color, final Double brightness, final long[] flashing) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

        if (lightId == null || lightId.length() == 0) {
            MessageUtils.setInvalidRequestParameterError(response, "lightId is not specified.");
            return true;
        }

        if (name == null || name.length() == 0) {
            MessageUtils.setInvalidRequestParameterError(response, "name is not specified.");
            return true;
        }

        PHBridge bridge = findBridge(serviceId);
        if (bridge == null) {
            MessageUtils.setNotFoundServiceError(response, "Not found bridge: " + serviceId);
            return true;
        }

        PHLight light = getLight(bridge, lightId);
        if (light == null) {
            MessageUtils.setInvalidRequestParameterError(response, "Not found light: " + lightId + "@" + serviceId);
            return true;
        }

        final CountDownLatch countDownLatch = new CountDownLatch(2);
        final PHLightAdapter adaptor = new PHLightAdapter() {
            private boolean mErrorFlag = false;
            private void countDown() {
                if (!mErrorFlag) {
                    setResult(response, DConnectMessage.RESULT_OK);
                }
                countDownLatch.countDown();
            }

            @Override
            public void onSuccess() {
                countDown();
            }

            @Override
            public void onStateUpdate(final Map<String, String> successAttribute,
                    final List<PHHueError> errorAttribute) {
                countDown();
            }

            @Override
            public void onError(final int code, final String message) {
                String errMsg = "ライトの状態更新に失敗しました hue:code = " + 
                        code + "  message = " + message;
                MessageUtils.setUnknownError(response, errMsg);
                mErrorFlag = true;
                countDown();
            }
        };

        new Thread(new Runnable() {
            public void run() {
                try {
                    if (!countDownLatch.await(30, TimeUnit.SECONDS)) {
                        MessageUtils.setTimeoutError(response);
                    }
                } catch (InterruptedException e) {
                    MessageUtils.setTimeoutError(response);
                }
                HueDeviceService service = (HueDeviceService) getContext();
                service.sendResponse(response);
            }
        }).start();

        // change the name
        PHLight newLight = new PHLight(light);
        newLight.setName(name);
        bridge.updateLight(newLight, adaptor);

        int[] colors = convertColor(color);

        // Brightness magnification conversion
        calcColorParam(colors, brightness);

        // Calculation of brightness.
        int mCalcBrightness = calcBrightnessParam(colors);

        PHLightState lightState = new PHLightState();
        lightState.setOn(true);
        lightState.setColorMode(PHLightColorMode.COLORMODE_XY);

        Color hueColor = new Color(color);
        lightState.setX(hueColor.mX);
        lightState.setY(hueColor.mY);
        lightState.setBrightness(mCalcBrightness);

        bridge.updateLightState(light, lightState, adaptor);
        return false;
    }

    @Override
    protected boolean onGetLightGroup(final Intent request, final Intent response, final String serviceId) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

        PHBridge bridge = findBridge(serviceId);
        if (bridge == null) {
            MessageUtils.setNotFoundServiceError(response, "Not found bridge: " + serviceId);
            return true;
        }

        List<Bundle> groupList = new ArrayList<Bundle>();
        Map<String, PHLight> phAllLights = bridge.getResourceCache().getLights();
        for (PHGroup phGroup : bridge.getResourceCache().getAllGroups()) {
            List<Bundle> lightList = new ArrayList<Bundle>();
            for (String lightId : phGroup.getLightIdentifiers()) {
                PHLight phLight = phAllLights.get(lightId);
                if (phLight != null) {
                    PHLightState state = phLight.getLastKnownLightState();
                    Bundle light = new Bundle();
                    setLightId(light, lightId);
                    setName(light, lightId);
                    setOn(light, state != null ? state.isOn() : false);
                    setConfig(light, "");
                    lightList.add(light);
                }
            }
            Bundle group = new Bundle();
            setGroupId(group, phGroup.getIdentifier());
            setGroupName(group, phGroup.getName());
            setLights(group, lightList);
            setGroupConfig(group, "");
            groupList.add(group);
        }
        setLightGroups(response, groupList);
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    @Override
    protected boolean onPostLightGroup(final Intent request, final Intent response, final String serviceId,
            final String groupId, final Integer color, final Double brightness, final long[] flashing) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

        if (groupId == null || groupId.length() == 0) {
            MessageUtils.setInvalidRequestParameterError(response, "groupId is not specified.");
            return true;
        }

        PHBridge bridge = findBridge(serviceId);
        if (bridge == null) {
            MessageUtils.setNotFoundServiceError(response, "Not found bridge: " + serviceId);
            return true;
        }

        int[] colors = convertColor(color);

        // Brightness magnification conversion
        calcColorParam(colors, brightness);

        // Calculation of brightness.
        int mCalcBrightness = calcBrightnessParam(colors);

        PHLightState lightState = new PHLightState();
        lightState.setOn(true);
        lightState.setColorMode(PHLightColorMode.COLORMODE_XY);

        Color hueColor = new Color(color);
        lightState.setX(hueColor.mX);
        lightState.setY(hueColor.mY);
        lightState.setBrightness(mCalcBrightness);

        if ("0".equals(groupId)) {
            bridge.setLightStateForDefaultGroup(lightState);
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }

        PHGroup group = getGroup(bridge, groupId);
        if (group == null) {
            MessageUtils.setUnknownError(response, "Not found group: " + groupId);
            return true;
        }

        bridge.setLightStateForGroup(group.getIdentifier(), lightState, new PHGroupAdapter() {
            @Override
            public void onError(final int code, final String message) {
                String msg = "ライトの状態更新に失敗しました hue:code = " + code + "  message = " + message;
                MessageUtils.setUnknownError(response, msg);
                sendResultERR(response);
            }

            @Override
            public void onStateUpdate(final Map<String, String> successAttributes,
                    final List<PHHueError> errorAttributes) {
                sendResultOK(response);
            }
        });
        return false;
    }

    @Override
    protected boolean onDeleteLightGroup(final Intent request, final Intent response, final String serviceId,
            final String groupId) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

        if (groupId == null || groupId.length() == 0) {
            MessageUtils.setInvalidRequestParameterError(response, "groupId is not specified.");
            return true;
        }

        PHBridge bridge = findBridge(serviceId);
        if (bridge == null) {
            MessageUtils.setNotFoundServiceError(response, "Not found bridge: " + serviceId);
            return true;
        }

        PHLightState lightState = new PHLightState();
        lightState.setOn(false);

        if ("0".equals(groupId)) {
            bridge.setLightStateForDefaultGroup(lightState);
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }

        PHGroup group = getGroup(bridge, groupId);
        if (group == null) {
            MessageUtils.setUnknownError(response, "Not found group: " + groupId);
            return true;
        }
        bridge.setLightStateForGroup(group.getIdentifier(), lightState, new PHGroupAdapter() {
            @Override
            public void onError(final int code, final String message) {
                String msg = "ライトの状態更新に失敗しました hue:code = " + code + "  message = " + message;
                MessageUtils.setUnknownError(response, msg);
                sendResultERR(response);
            }

            @Override
            public void onStateUpdate(final Map<String, String> successAttributes,
                    final List<PHHueError> errorAttributes) {
                sendResultOK(response);
            }
        });
        return false;
    }

    @Override
    protected boolean onPutLightGroup(final Intent request, final Intent response, final String serviceId,
            final String groupId, final String name, final Integer color, final Double brightness, final long[] flashing) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

        if (groupId == null || groupId.length() == 0) {
            MessageUtils.setInvalidRequestParameterError(response, "groupId is not specified.");
            return true;
        }

        PHBridge bridge = findBridge(serviceId);
        if (bridge == null) {
            MessageUtils.setNotFoundServiceError(response, "Not found bridge: " + serviceId);
            return true;
        }

        if (name == null || name.length() == 0) {
            MessageUtils.setInvalidRequestParameterError(response, "name is not specified.");
            return true;
        }

        PHGroup group = getGroup(bridge, groupId);
        if (group == null) {
            MessageUtils.setUnknownError(response, "Not found group: " + groupId);
            return true;
        }

        PHGroup newGroup = new PHGroup(name, group.getIdentifier());
        bridge.updateGroup(newGroup, new PHGroupAdapter() {
            @Override
            public void onSuccess() {
                sendResultOK(response);
            }

            @Override
            public void onError(final int code, final String message) {
                String errMsg = "グループの名称変更に失敗しました hue:code = " + code + "  message = " + message;
                MessageUtils.setUnknownError(response, errMsg);
                sendResultERR(response);
            }
        });
        return false;
    }

    @Override
    protected boolean onPostLightGroupCreate(final Intent request, final Intent response,
            final String serviceId, final String[] lightIds, final String groupName) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

        if (groupName == null || groupName.length() == 0) {
            MessageUtils.setInvalidRequestParameterError(response, "groupName is not specified.");
            return true;
        }

        if (lightIds == null || lightIds.length == 0) {
            MessageUtils.setInvalidRequestParameterError(response, "lightIds is not specified.");
            return true;
        }

        PHBridge bridge = findBridge(serviceId);
        if (bridge == null) {
            MessageUtils.setNotFoundServiceError(response, "Not found bridge: " + serviceId);
            return true;
        }

        bridge.createGroup(groupName, Arrays.asList(lightIds), new PHGroupAdapter() {
            @Override
            public void onCreated(final PHGroup group) {
                response.putExtra(PARAM_GROUP_ID, group.getIdentifier());
                sendResultOK(response);
            }

            @Override
            public void onError(final int code, final String msg) {
                String errMsg = "グループ作成に失敗しました hue:code = " + code + "  message = " + msg;
                if (code == HUE_SDK_ERROR_301) {
                    MessageUtils.setUnknownError(response, "グループが作成できる上限に達しています");
                } else {
                    MessageUtils.setUnknownError(response, errMsg);
                }
                sendResultERR(response);
            }
        });
        return false;
    }

    @Override
    protected boolean onDeleteLightGroupClear(final Intent request, final Intent response, final String serviceId, final String groupId) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

        if (groupId == null || groupId.length() == 0) {
            MessageUtils.setInvalidRequestParameterError(response, "groupId is not specified.");
            return true;
        }

        PHBridge bridge = findBridge(serviceId);
        if (bridge == null) {
            MessageUtils.setNotFoundServiceError(response, "Not found bridge: " + serviceId);
            return true;
        }

        bridge.deleteGroup(groupId, new PHGroupAdapter() {
            @Override
            public void onSuccess() {
                sendResultOK(response);
            }

            @Override
            public void onError(final int code, final String msg) {
                String errMsg = "グループ削除に失敗しました hue:code = " + code + "  message = " + msg;
                MessageUtils.setUnknownError(response, errMsg);
                sendResultERR(response);
            }
        });
        return false;
    }

    /**
     * Convert Integer to int[].
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
     * Hueのブリッジを検索する.
     * 
     * @param serviceId Service ID
     * @return Hueのブリッジを管理するオブジェクト
     */
    private PHBridge findBridge(final String serviceId) {
        PHBridge bridge = PHHueSDK.getInstance().getSelectedBridge();
        if (bridge != null) {
            PHBridgeResourcesCache cache = bridge.getResourceCache();
            String ipAddress = cache.getBridgeConfiguration().getIpAddress();
            if (serviceId.equals(ipAddress)) {
                return bridge;
            }
        }
        return null;
    }

    /**
     * Hue Lightを管理するオブジェクトを取得する.
     * 
     * @param bridge Hueのブリッジ
     * @param lightId Light ID
     * @return Lightを管理するオブジェクト
     */
    private PHLight getLight(final PHBridge bridge, final String lightId) {
        for (PHLight light : bridge.getResourceCache().getAllLights()) {
            if (light.getIdentifier().equals(lightId)) {
                return light;
            }
        }
        return null;
    }

    /**
     * Hue Lightのグループ情報を管理するオブジェクトを取得する.
     * 
     * @param bridge Hueのブリッジ
     * @param groupID Group ID
     * @return Lightのグループ情報を持つオブジェクト
     */
    private PHGroup getGroup(final PHBridge bridge, final String groupID) {
        for (PHGroup group : bridge.getResourceCache().getAllGroups()) {
            if (groupID.equals(group.getIdentifier())) {
                return group;
            }
        }
        return null;
    }

    /**
     * 成功レスポンス送信.
     * 
     * @param response response
     */
    private void sendResultOK(final Intent response) {
        setResult(response, DConnectMessage.RESULT_OK);
        HueDeviceService service = (HueDeviceService) getContext();
        service.sendResponse(response);
    }

    /**
     * エラーレスポンスを送信する.
     * 
     * @param response エラーレスポンス
     */
    private void sendResultERR(final Intent response) {
        setResult(response, DConnectMessage.RESULT_ERROR);
        HueDeviceService service = (HueDeviceService) getContext();
        service.sendResponse(response);
    }

    /**
     * Calculate color parameter.
     * 
     * @param color Color parameters.
     * @param brightness Brightness parameter.
     * @return Color parameter.
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
        /** モデル. */
        private static final String MODEL = "LCT001";
        /** R. */
        final int mR;
        /** G. */
        final int mG;
        /** B. */
        final int mB;
        /** 色相のX座標. */
        final float mX;
        /** 色相のY座標. */
        final float mY;

        /**
         * コンストラクタ.
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

    /**
     * ライトグループのアダプター.
     * 
     * @author NTT DOCOMO, INC.
     */
    private static class PHGroupAdapter implements PHGroupListener {
        @Override
        public void onError(final int code, final String msg) {
        }

        @Override
        public void onStateUpdate(final Map<String, String> arg0, final List<PHHueError> arg1) {
        }

        @Override
        public void onSuccess() {
        }

        @Override
        public void onCreated(final PHGroup group) {
        }

        @Override
        public void onReceivingAllGroups(final List<PHBridgeResource> arg0) {
        }

        @Override
        public void onReceivingGroupDetails(final PHGroup group) {
        }
    }
}
