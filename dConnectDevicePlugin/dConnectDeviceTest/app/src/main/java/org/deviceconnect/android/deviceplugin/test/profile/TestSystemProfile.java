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

import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

/**
 * JUnit用テストデバイスプラグイン、Systemプロファイル.
 * @author NTT DOCOMO, INC.
 */
public class TestSystemProfile extends SystemProfile {

    public TestSystemProfile() {
        addApi(mPutWakeupApi);
        addApi(mDeleteEventsApi);
    }

    private final DConnectApi mPutWakeupApi = new PutApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            // /system/device/wakeupはテスト用デバイスプラグインでは疎通確認だけを行う.
            // 正常に設定画面が開かれることの確認は、実際のデバイスプラグインのテストで行う.
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }
    };

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
        return null; // テスト用プラグインでは実装しない
    }
}
