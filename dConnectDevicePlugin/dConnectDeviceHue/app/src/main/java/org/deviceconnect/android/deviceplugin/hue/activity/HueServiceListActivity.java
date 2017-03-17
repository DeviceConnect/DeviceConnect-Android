/*
HueServiceListActivity
Copyright (c) 2016 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
*/
package org.deviceconnect.android.deviceplugin.hue.activity;


import android.app.Activity;

import org.deviceconnect.android.deviceplugin.hue.HueDeviceService;
import org.deviceconnect.android.deviceplugin.hue.service.HueService;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.service.DConnectService;
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

    @Override
    public void onServiceRemoved(final DConnectService service) {
        super.onServiceRemoved(service);

        HueDeviceService s = (HueDeviceService) getMessageService();
        if (s != null) {
            s.removeHueService((HueService) service);
        }
    }
}
