/*
 WearVibrationProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.wear.profile;

import android.content.Intent;


import org.deviceconnect.android.deviceplugin.wear.WearDeviceService;
import org.deviceconnect.android.deviceplugin.wear.WearManager;
import org.deviceconnect.android.deviceplugin.wear.WearManager.OnMessageResultListener;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.VibrationProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

/**
 * Vibration Profile.
 * 
 * @author NTT DOCOMO, INC.
 */
public class WearVibrationProfile extends VibrationProfile {

    public WearVibrationProfile() {
        addApi(mPutVibrate);
        addApi(mDeleteVibrate);
    }

    private final DConnectApi mPutVibrate = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_VIBRATE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String pattern = getPattern(request);
            String nodeId = WearUtils.getNodeId(getServiceID(request));
            getManager().sendMessageToWear(nodeId, WearConst.DEVICE_TO_WEAR_VIBRATION_RUN,
                convertPatternToString(parsePattern(pattern)), new OnMessageResultListener() {
                    @Override
                    public void onResult() {
                        setResult(response, DConnectMessage.RESULT_OK);
                        sendResponse(response);
                    }
                    @Override
                    public void onError() {
                        MessageUtils.setIllegalDeviceStateError(response);
                        sendResponse(response);
                    }
                });
            return false;
        }
    };

    private final DConnectApi mDeleteVibrate = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_VIBRATE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String nodeId = WearUtils.getNodeId(getServiceID(request));
            getManager().sendMessageToWear(nodeId, WearConst.DEVICE_TO_WEAR_VIBRATION_DEL,
                "", new OnMessageResultListener() {
                    @Override
                    public void onResult() {
                        setResult(response, DConnectMessage.RESULT_OK);
                        sendResponse(response);
                    }
                    @Override
                    public void onError() {
                        MessageUtils.setIllegalDeviceStateError(response);
                        sendResponse(response);
                    }
                });
            return false;
        }
    };

    /**
     * バイブレーションのパターンを文字列に変換する.
     * @param pattern パターン
     * @return 文字列に変換されたパターン
     */
    private String convertPatternToString(final long[] pattern) {
        // Convert pattern in string.
        String patternStr = "";
        for (int i = 0; i < pattern.length; i++) {
            if (i == 0) {
                patternStr += pattern[i];
            } else {
                patternStr += "," + pattern[i];
            }
        }
        return patternStr;
    }

    /**
     * Android Wear管理クラスを取得する.
     * @return WearManager管理クラス
     */
    private WearManager getManager() {
        return ((WearDeviceService) getContext()).getManager();
    }
}
