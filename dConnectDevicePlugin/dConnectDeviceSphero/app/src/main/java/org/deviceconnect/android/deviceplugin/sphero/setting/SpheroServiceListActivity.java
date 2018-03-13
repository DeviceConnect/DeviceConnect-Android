/*
 SpheroServiceListActivity
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sphero.setting;

import android.app.Activity;
import android.content.Intent;

import org.deviceconnect.android.deviceplugin.sphero.SpheroDeviceService;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.android.ui.activity.DConnectServiceListActivity;

/**
 * Spheroサービス一覧画面.
 *
 * @author NTT DOCOMO, INC.
 */
public class SpheroServiceListActivity extends DConnectServiceListActivity {

    @Override
    protected Class<? extends DConnectMessageService> getMessageServiceClass() {
        return SpheroDeviceService.class;
    }

    @Override
    protected Class<? extends Activity> getSettingManualActivityClass() {
        return SettingActivity.class;
    }

    @Override
    public void onServiceRemoved(final DConnectService service) {
        super.onServiceRemoved(service);
        Intent removedService = new Intent(SpheroDeviceService.ACTION_SPHERO_REMOVE);
        removedService.putExtra(SpheroDeviceService.PARAM_SERVICE_ID, service.getId());
        sendBroadcast(removedService);
    }
}