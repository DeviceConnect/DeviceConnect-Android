/*
 TestSystemProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.test.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.test.TestServiceListActivity;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.message.DConnectMessage;

/**
 * JUnit用テストデバイスプラグイン、Systemプロファイル.
 * @author NTT DOCOMO, INC.
 */
public class TestSystemProfile extends SystemProfile {

    public TestSystemProfile() {
        addApi(mDeleteEventsApi);
    }

    private final DConnectApi mDeleteEventsApi = new DeleteApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            boolean removed = EventManager.INSTANCE.removeEvents(getSessionKey(request));
            if (removed) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setUnknownError(response, "Failed to remove events.");
            }
            return true;
        }
    };

    @Override
    protected Class<? extends Activity> getSettingPageActivity(final Intent request, final Bundle param) {
        if (request.hasExtra("show")) {
            param.putBoolean("canAddService", !request.hasExtra("cannotAddService"));
            return TestServiceListActivity.class;
        }
        return null; // テスト用プラグインでは実装しない
    }
}
