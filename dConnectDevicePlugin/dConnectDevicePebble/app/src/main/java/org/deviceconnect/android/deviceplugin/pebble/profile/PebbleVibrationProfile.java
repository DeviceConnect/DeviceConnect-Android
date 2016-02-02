/*
 PebbleVibrationProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.pebble.profile;

import android.content.Intent;

import com.getpebble.android.kit.util.PebbleDictionary;

import org.deviceconnect.android.deviceplugin.pebble.PebbleDeviceService;
import org.deviceconnect.android.deviceplugin.pebble.util.PebbleManager;
import org.deviceconnect.android.deviceplugin.pebble.util.PebbleManager.OnSendCommandListener;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.VibrationProfile;
import org.deviceconnect.message.DConnectMessage;

/**
 * Pebble用バイブレーションプロファイル.
 * @author NTT DOCOMO, INC.
 */
public class PebbleVibrationProfile extends VibrationProfile {
    @Override
    protected boolean onPutVibrate(final Intent request, final Intent response, final String serviceId,
            final long[] pattern) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        } else if (!PebbleUtil.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        } else {
            // リクエスト作成
            byte[] p = PebbleManager.convertVibrationPattern(pattern);
            PebbleDictionary dic = new PebbleDictionary();
            dic.addInt8(PebbleManager.KEY_PROFILE, (byte) PebbleManager.PROFILE_VIBRATION);
            dic.addInt8(PebbleManager.KEY_ATTRIBUTE, (byte) PebbleManager.VIBRATION_ATTRIBUTE_VIBRATE);
            dic.addInt8(PebbleManager.KEY_ACTION, (byte) PebbleManager.ACTION_PUT);
            if (p == null) {
                dic.addInt16(PebbleManager.KEY_PARAM_VIBRATION_LEN, (short) 0);
            } else {
                dic.addInt16(PebbleManager.KEY_PARAM_VIBRATION_LEN, (short) (p.length / 2));
                dic.addBytes(PebbleManager.KEY_PARAM_VIBRATION_PATTERN, p);
            }
            // Pebbleに送信
            PebbleManager mgr = ((PebbleDeviceService) getContext()).getPebbleManager();
            mgr.sendCommandToPebble(dic, new OnSendCommandListener() {
                @Override
                public void onReceivedData(final PebbleDictionary dic) {
                    if (dic == null) {
                        MessageUtils.setTimeoutError(response);
                    } else {
                        setResult(response, DConnectMessage.RESULT_OK);
                    }
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
        } else if (!PebbleUtil.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        } else {
            PebbleDictionary dic = new PebbleDictionary();
            dic.addInt8(PebbleManager.KEY_PROFILE, (byte) PebbleManager.PROFILE_VIBRATION);
            dic.addInt8(PebbleManager.KEY_ATTRIBUTE, (byte) PebbleManager.VIBRATION_ATTRIBUTE_VIBRATE);
            dic.addInt8(PebbleManager.KEY_ACTION, (byte) PebbleManager.ACTION_DELETE);
            // Pebbleに送信
            PebbleManager mgr = ((PebbleDeviceService) getContext()).getPebbleManager();
            mgr.sendCommandToPebble(dic, new OnSendCommandListener() {
                @Override
                public void onReceivedData(final PebbleDictionary dic) {
                    if (dic == null) {
                        MessageUtils.setTimeoutError(response);
                    } else {
                        setResult(response, DConnectMessage.RESULT_OK);
                    }
                    sendResponse(response);
                }
            });
            return false;
        }
    }
}
