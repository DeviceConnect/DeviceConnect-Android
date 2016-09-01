package org.deviceconnect.android.deviceplugin.chromecast.setting;

import android.app.Activity;

import org.deviceconnect.android.deviceplugin.chromecast.ChromeCastService;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.ui.activity.DConnectServiceListActivity;

/**
 * Chromecastサービス一覧画面.
 * @author NTT DOCOMO, INC.
 */

public class ChromeCastServiceListActivity extends DConnectServiceListActivity {
    @Override
    protected Class<? extends DConnectMessageService> getMessageServiceClass() {
        return ChromeCastService.class;
    }

    @Override
    protected Class<? extends Activity> getSettingManualActivityClass() {
        return ChromeCastSettingFragmentActivity.class;
    }

}
