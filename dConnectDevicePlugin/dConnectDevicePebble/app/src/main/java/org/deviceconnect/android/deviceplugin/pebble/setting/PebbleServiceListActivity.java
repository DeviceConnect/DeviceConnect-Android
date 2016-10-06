/*
 PebbleServiceListActivity.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.pebble.setting;


import android.app.Activity;

import org.deviceconnect.android.deviceplugin.pebble.PebbleDeviceService;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.ui.activity.DConnectServiceListActivity;

/**
 * Pebbleサービス一覧画面.
 * @author NTT DOCOMO, INC.
 */
public class PebbleServiceListActivity extends DConnectServiceListActivity {

    @Override
    protected Class<? extends DConnectMessageService> getMessageServiceClass() {
        return PebbleDeviceService.class;
    }

    @Override
    protected Class<? extends Activity> getSettingManualActivityClass() {
        return PebbleSettingActivity.class;
    }

}
