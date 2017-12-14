/*
 PebbleSettingProfile.java
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
import org.deviceconnect.android.profile.SettingProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.message.DConnectMessage;

/**
 * Pebble端末内時間情報取得プロファイル.
 * @author NTT DOCOMO, INC.
 */
public class PebbleSettingProfile extends SettingProfile {

    private final DConnectApi mGetDateApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_DATE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            // Pebbleに送信
            PebbleManager mgr = ((PebbleDeviceService) getContext()).getPebbleManager();
            PebbleDictionary dic = new PebbleDictionary();
            dic.addInt8(PebbleManager.KEY_PROFILE, (byte) PebbleManager.PROFILE_SETTING);
            dic.addInt8(PebbleManager.KEY_ATTRIBUTE, (byte) PebbleManager.SETTING_ATTRIBUTE_DATE);
            dic.addInt8(PebbleManager.KEY_ACTION, (byte) PebbleManager.ACTION_GET);
            mgr.sendCommandToPebble(dic, new OnSendCommandListener() {
                @Override
                public void onReceivedData(final PebbleDictionary dic) {
                    if (dic == null) {
                        MessageUtils.setTimeoutError(response);
                    } else {
                        String date = dic.getString(PebbleManager.KEY_PARAM_SETTING_DATE);
                        if (date == null) {
                            MessageUtils.setTimeoutError(response);
                        } else {
                            if (date.charAt(date.length() - 5) == '+'
                                    || date.charAt(date.length() - 5) == '-') {
                                //ISO8601形式で日付データがくるので、「:」を入れRFC3339形式にする
                                date = date.substring(0, date.length() - 2) + ":" + date.substring(date.length() - 2, date.length());
                            }
                            setResult(response, DConnectMessage.RESULT_OK);
                            setDate(response, date);
                        }
                    }
                    sendResponse(response);
                }
            });
            return false;
        }
    };

    public PebbleSettingProfile() {
        addApi(mGetDateApi);
    }
}
