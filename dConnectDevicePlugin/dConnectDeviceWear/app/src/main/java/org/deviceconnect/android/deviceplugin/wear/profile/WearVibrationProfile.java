/*
 WearVibrationProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.wear.profile;

import android.content.Intent;

import com.google.android.gms.wearable.MessageApi.SendMessageResult;

import org.deviceconnect.android.deviceplugin.wear.WearDeviceService;
import org.deviceconnect.android.deviceplugin.wear.WearManager;
import org.deviceconnect.android.deviceplugin.wear.WearManager.OnMessageResultListener;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.VibrationProfile;
import org.deviceconnect.message.DConnectMessage;

/**
 * Vibration Profile.
 * 
 * @author NTT DOCOMO, INC.
 */
public class WearVibrationProfile extends VibrationProfile {

    @Override
    protected boolean onPutVibrate(final Intent request, final Intent response,
            final String serviceId, final long[] pattern) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        } else if (!WearUtils.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        } else {
            String nodeId = WearUtils.getNodeId(serviceId);
            getManager().sendMessageToWear(nodeId, WearConst.DEVICE_TO_WEAR_VIBRATION_RUN,
                    convertPatternToString(pattern), new OnMessageResultListener() {
                @Override
                public void onResult(final SendMessageResult result) {
                    if (result.getStatus().isSuccess()) {
                        setResult(response, DConnectMessage.RESULT_OK);
                    } else {
                        MessageUtils.setIllegalDeviceStateError(response);
                    }
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
    }

    @Override
    protected boolean onDeleteVibrate(final Intent request, final Intent response, final String serviceId) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        } else if (!WearUtils.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        } else {
            String nodeId = WearUtils.getNodeId(serviceId);
            getManager().sendMessageToWear(nodeId, WearConst.DEVICE_TO_WEAR_VIBRATION_DEL,
                    "", new OnMessageResultListener() {
                @Override
                public void onResult(final SendMessageResult result) {
                    if (result.getStatus().isSuccess()) {
                        setResult(response, DConnectMessage.RESULT_OK);
                    } else {
                        MessageUtils.setIllegalDeviceStateError(response);
                    }
                    sendResponse(response);
                }
                @Override
                public void onError() {
                    MessageUtils.setIllegalDeviceStateError(response);
                    sendResponse(response);
                }
            });
        }
        return false;
    }

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
