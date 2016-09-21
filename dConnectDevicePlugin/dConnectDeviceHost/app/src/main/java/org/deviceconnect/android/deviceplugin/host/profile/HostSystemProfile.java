/*
 HostSystemProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.host.setting.HostSettingActivity;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

/**
 * ホストデバイスプラグイン, System プロファイル.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HostSystemProfile extends SystemProfile {

    private final DConnectApi mDeleteEventsApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_EVENTS;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String origin = request.getStringExtra(IntentDConnectMessage.EXTRA_ORIGIN);
            if (EventManager.INSTANCE.removeEvents(origin)) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setUnknownError(response);
            }
            return true;
        }
    };

    public HostSystemProfile() {
        addApi(mDeleteEventsApi);
    }

    /**
     * 設定画面を設定.
     * 
     * @param request リクエスト
     * @param bundle バンドル
     * 
     * @return 設定アクティビティ
     */
    protected Class<? extends Activity> getSettingPageActivity(final Intent request, final Bundle bundle) {
        return HostSettingActivity.class;
    }
}
