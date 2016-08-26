/*
 IRKitServiceListActivity.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.irkit.settings.activity;


import android.app.Activity;
import android.content.Intent;

import org.deviceconnect.android.deviceplugin.irkit.IRKitDeviceService;
import org.deviceconnect.android.deviceplugin.irkit.service.IRKitService;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.android.ui.activity.DConnectServiceListActivity;

/**
 * IRKitサービス一覧画面.
 * @author NTT DOCOMO, INC.
 */
public class IRKitServiceListActivity extends DConnectServiceListActivity {

    @Override
    protected Class<? extends DConnectMessageService> getMessageServiceClass() {
        return IRKitDeviceService.class;
    }

    @Override
    protected Class<? extends Activity> getSettingManualActivityClass() {
        return IRKitSettingActivity.class;
    }

    @Override
    protected ListItemFilter getListItemFilter() {
        return new ListItemFilter() {
            @Override
            public boolean doFilter(final DConnectService service) {
                return service instanceof IRKitService;
            }
        };
    }

    @Override
    protected boolean enablesItemClick() {
        return true;
    }

    @Override
    protected void onItemClick(final DConnectService service) {
        Intent intent = new Intent(getApplicationContext(), IRKitVirtualDeviceListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(IRKitVirtualDeviceListActivity.EXTRA_SERVICE_ID, service.getId());
        startActivity(intent);
    }
}
