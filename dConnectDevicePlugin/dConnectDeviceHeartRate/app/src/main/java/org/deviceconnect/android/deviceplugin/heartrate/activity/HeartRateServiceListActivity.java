/*
 HeartRateServiceListActivity
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.heartrate.activity;

import android.app.Activity;

import org.deviceconnect.android.deviceplugin.heartrate.HeartRateDeviceService;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.ui.activity.DConnectServiceListActivity;

/**
 * Heart Rateサービス一覧画面.
 *
 * @author NTT DOCOMO, INC.
 */
public class HeartRateServiceListActivity extends DConnectServiceListActivity {

    @Override
    protected Class<? extends DConnectMessageService> getMessageServiceClass() {
        return HeartRateDeviceService.class;
    }

    @Override
    protected Class<? extends Activity> getSettingManualActivityClass() {
        return HeartRateDeviceSettingsActivity.class;
    }


}
