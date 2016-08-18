/*
SonyCameraServiceListActivity
Copyright (c) 2016 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sonycamera.activity;


import android.app.Activity;

import org.deviceconnect.android.deviceplugin.sonycamera.SonyCameraDeviceService;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.ui.activity.DConnectServiceListActivity;


/**
 * SonyCameraサービス一覧画面.
 *
 * @author NTT DOCOMO, INC.
 */
public class SonyCameraServiceListActivity extends DConnectServiceListActivity {

    @Override
    protected Class<? extends DConnectMessageService> getMessageServiceClass() {
        return SonyCameraDeviceService.class;
    }

    @Override
    protected Class<? extends Activity> getSettingManualActivityClass() {
        return SonyCameraSettingActivity.class;
    }
}
