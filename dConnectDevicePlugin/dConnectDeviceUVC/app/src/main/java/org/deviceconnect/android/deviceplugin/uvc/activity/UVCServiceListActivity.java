/*
 UVCServiceListActivity.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.uvc.activity;


import android.app.Activity;

import org.deviceconnect.android.deviceplugin.uvc.UVCDeviceService;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.ui.activity.DConnectServiceListActivity;


/**
 * UVCサービス一覧画面.
 *
 * @author NTT DOCOMO, INC.
 */
public class UVCServiceListActivity extends DConnectServiceListActivity {

    @Override
    protected Class<? extends DConnectMessageService> getMessageServiceClass() {
        return UVCDeviceService.class;
    }

    @Override
    protected Class<? extends Activity> getSettingManualActivityClass() {
        return UVCDeviceSettingsActivity.class;
    }
}
