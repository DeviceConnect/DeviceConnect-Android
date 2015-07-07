package org.deviceconnect.android.deviceplugin.alljoyn.activity;

import android.support.v4.app.Fragment;

import org.deviceconnect.android.ui.activity.DConnectSettingPageFragmentActivity;

/**
 * 設定アクティビティー。
 *
 * @author NTT DOCOMO, INC.
 */
public class AllJoynSettingActivity extends DConnectSettingPageFragmentActivity {
    @Override
    public int getPageCount() {
        return 1;
    }

    @Override
    public Fragment createPage(int i) {
        return new Fragment();
    }
}
