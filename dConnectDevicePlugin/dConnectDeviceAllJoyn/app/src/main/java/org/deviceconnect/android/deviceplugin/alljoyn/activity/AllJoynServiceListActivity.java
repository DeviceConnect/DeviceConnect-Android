package org.deviceconnect.android.deviceplugin.alljoyn.activity;


import android.app.Activity;

import org.deviceconnect.android.deviceplugin.alljoyn.AllJoynDeviceService;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.ui.activity.DConnectServiceListActivity;

/**
 * AllJoynサービス一覧画面.
 *
 * @author NTT DOCOMO, INC.
 */
public class AllJoynServiceListActivity extends DConnectServiceListActivity {

    @Override
    protected Class<? extends DConnectMessageService> getMessageServiceClass() {
        return AllJoynDeviceService.class;
    }

    @Override
    protected Class<? extends Activity> getSettingManualActivityClass() {
        return AllJoynSettingActivity.class;
    }
}
