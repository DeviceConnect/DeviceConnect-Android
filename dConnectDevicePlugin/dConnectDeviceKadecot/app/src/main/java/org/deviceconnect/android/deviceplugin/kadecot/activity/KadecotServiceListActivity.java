/*
 KadecotServiceListActivity
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.kadecot.activity;

import android.app.Activity;

import org.deviceconnect.android.deviceplugin.kadecot.KadecotDeviceService;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.ui.activity.DConnectServiceListActivity;

/**
 * Kadecotサービス一覧画面.
 *
 * @author NTT DOCOMO, INC.
 */
public class KadecotServiceListActivity extends DConnectServiceListActivity {

    @Override
    protected Class<? extends DConnectMessageService> getMessageServiceClass() {
        return KadecotDeviceService.class;
    }

    @Override
    protected Class<? extends Activity> getSettingManualActivityClass() {
        return KadecotDeviceSettingsActivity.class;
    }
}
