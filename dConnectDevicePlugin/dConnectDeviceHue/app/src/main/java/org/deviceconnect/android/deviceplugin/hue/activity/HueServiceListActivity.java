/*
HueServiceListActivity
Copyright (c) 2016 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
*/
package org.deviceconnect.android.deviceplugin.hue.activity;


import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.philips.lighting.model.PHLight;

import org.deviceconnect.android.deviceplugin.hue.HueConstants;
import org.deviceconnect.android.deviceplugin.hue.HueDeviceService;
import org.deviceconnect.android.deviceplugin.hue.db.HueManager;
import org.deviceconnect.android.deviceplugin.hue.service.HueLightService;
import org.deviceconnect.android.deviceplugin.hue.service.HueService;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.android.ui.activity.DConnectServiceListActivity;

import java.util.List;


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
        if (service instanceof HueService) {
            List<PHLight> lights = HueManager.INSTANCE.getLightsForIp(service.getId());
            if (lights == null) {
                return;
            }
            for (int i = 0; i < lights.size(); i++) {
                removeService(service.getId() + ":" + lights.get(i).getIdentifier());
            }
            HueManager.INSTANCE.removeHueService((HueService) service);
        } else if (service instanceof HueLightService) {
            HueManager.INSTANCE.removeHueLightService((HueLightService) service);
        }
    }
}
