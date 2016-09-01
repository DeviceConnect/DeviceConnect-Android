/*
 HVCPServiceListActivity
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvcp.setting;

import android.app.Activity;

import org.deviceconnect.android.deviceplugin.hvcp.HVCPDeviceService;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.ui.activity.DConnectServiceListActivity;

/**
 * HVC-Pサービス一覧画面.
 *
 * @author NTT DOCOMO, INC.
 */
public class HVCPServiceListActivity extends DConnectServiceListActivity {

    @Override
    protected Class<? extends DConnectMessageService> getMessageServiceClass() {
        return HVCPDeviceService.class;
    }

    @Override
    protected Class<? extends Activity> getSettingManualActivityClass() {
        return SettingActivity.class;
    }
}
