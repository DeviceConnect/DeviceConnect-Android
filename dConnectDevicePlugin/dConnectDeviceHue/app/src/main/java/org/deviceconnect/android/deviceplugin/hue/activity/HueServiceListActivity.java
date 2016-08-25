/*
HueServiceListActivity
Copyright (c) 2016 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
*/
package org.deviceconnect.android.deviceplugin.hue.activity;


import android.app.Activity;

import org.deviceconnect.android.deviceplugin.hue.HueDeviceService;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.ui.activity.DConnectServiceListActivity;


/**
 * Hueサービス一覧画面.
 *
 * @author NTT DOCOMO, INC.
 */
public class HueServiceListActivity extends DConnectServiceListActivity {

    @Override
    protected Class<? extends DConnectMessageService> getMessageServiceClass() {
        return HueDeviceService.class;
    }

    @Override
    protected Class<? extends Activity> getSettingManualActivityClass() {
        return HueMainActivity.class;
    }
}
