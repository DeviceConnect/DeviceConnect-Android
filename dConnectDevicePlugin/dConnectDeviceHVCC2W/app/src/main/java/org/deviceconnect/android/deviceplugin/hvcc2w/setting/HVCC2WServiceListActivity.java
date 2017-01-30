/*
 HVCC2WServiceListActivity
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvcc2w.setting;


import android.app.Activity;

import org.deviceconnect.android.deviceplugin.hvcc2w.HVCC2WDeviceService;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.ui.activity.DConnectServiceListActivity;

/**
 * HVC-C2Wサービス一覧画面.
 *
 * @author NTT DOCOMO, INC.
 */
public class HVCC2WServiceListActivity extends DConnectServiceListActivity {

    @Override
    protected Class<? extends DConnectMessageService> getMessageServiceClass() {
        return HVCC2WDeviceService.class;
    }

    @Override
    protected Class<? extends Activity> getSettingManualActivityClass() {
        return SettingActivity.class;
    }

    @Override
    public void onResume() {
        super.onResume();
        start();
    }

    @Override
    public void onPause() {
        super.onPause();
        stop();
    }

    /**
     * タイマーのスタート.
     */
    private synchronized void start() {
    }

    /**
     * タイマーのストップ.
     */
    private synchronized void stop() {
    }
}
