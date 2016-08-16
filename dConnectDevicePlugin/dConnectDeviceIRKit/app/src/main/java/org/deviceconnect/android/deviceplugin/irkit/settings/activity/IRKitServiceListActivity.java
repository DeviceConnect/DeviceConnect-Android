package org.deviceconnect.android.deviceplugin.irkit.settings.activity;


import android.app.Activity;

import org.deviceconnect.android.deviceplugin.irkit.IRKitDeviceService;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.ui.activity.DConnectServiceListActivity;

public class IRKitServiceListActivity extends DConnectServiceListActivity {

    @Override
    protected Class<? extends DConnectMessageService> getMessageServiceClass() {
        return IRKitDeviceService.class;
    }

    @Override
    protected Class<? extends Activity> getSettingManualActivityClass() {
        return IRKitSettingActivity.class;
    }
}
