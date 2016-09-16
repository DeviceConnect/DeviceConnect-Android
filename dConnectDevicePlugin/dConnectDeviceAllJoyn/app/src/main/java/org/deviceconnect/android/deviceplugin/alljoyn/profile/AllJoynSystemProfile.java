package org.deviceconnect.android.deviceplugin.alljoyn.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.alljoyn.activity.AllJoynServiceListActivity;
import org.deviceconnect.android.profile.SystemProfile;

/**
 * System profile for AllJoyn.
 *
 * @author NTT DOCOMO, INC.
 */
public class AllJoynSystemProfile extends SystemProfile {

    /**
     * 設定画面を設定。
     *
     * @param intent リクエストインテント
     * @param bundle バンドル
     *
     * @return 設定アクティビティ
     */
    @Override
    protected Class<? extends Activity> getSettingPageActivity(Intent intent, Bundle bundle) {
        return AllJoynServiceListActivity.class;
    }
}
