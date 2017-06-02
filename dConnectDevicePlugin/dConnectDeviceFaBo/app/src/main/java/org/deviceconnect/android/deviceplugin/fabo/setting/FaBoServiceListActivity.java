package org.deviceconnect.android.deviceplugin.fabo.setting;

import android.app.Activity;

import org.deviceconnect.android.deviceplugin.fabo.FaBoDeviceService;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.ui.activity.DConnectServiceListActivity;

public class FaBoServiceListActivity extends DConnectServiceListActivity {
    @Override
    protected Class<? extends DConnectMessageService> getMessageServiceClass() {
        return FaBoDeviceService.class;
    }

    @Override
    protected Class<? extends Activity> getSettingManualActivityClass() {
        return FaBoSettingActivity.class;
    }
}
