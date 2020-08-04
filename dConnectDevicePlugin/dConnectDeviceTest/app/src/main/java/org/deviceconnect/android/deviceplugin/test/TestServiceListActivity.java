/*
 TestServiceListActivity.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.ui.activity.DConnectServiceListActivity;


/**
 * テスト用サービス一覧画面.
 *
 * @author NTT DOCOMO, INC.
 */
public class TestServiceListActivity extends DConnectServiceListActivity {

    public static final String EXTRA_CAN_ADD_SERVICE = "canAddService";

    @Override
    protected Class<? extends DConnectMessageService> getMessageServiceClass() {
        return UnitTestDeviceService.class;
    }

    @Override
    protected Class<? extends Activity> getSettingManualActivityClass() {
        Intent intent = ((FragmentActivity) this).getIntent();
        if (intent == null) {
            return PlaceHolderSettingManualActivity.class;
        }
        Bundle param = intent.getBundleExtra(SystemProfile.SETTING_PAGE_PARAMS);
        if (param != null && param.getBoolean(EXTRA_CAN_ADD_SERVICE, true)) {
            return PlaceHolderSettingManualActivity.class;
        }
        return null;
    }
}
