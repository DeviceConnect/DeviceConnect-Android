/*
 HelpActivity.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.slackmessagehook.setting;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.deviceconnect.android.deviceplugin.slackmessagehook.R;
import org.deviceconnect.android.deviceplugin.slackmessagehook.setting.fragment.HelpFragment;
import org.deviceconnect.android.ui.activity.DConnectSettingPageFragmentActivity;

/**
 * Help画面表示用Activity
 */
public class HelpActivity extends DConnectSettingPageFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getPageCount() {
        return 6;
    }

    @Override
    public Fragment createPage(final int position) {
        HelpFragment page = new HelpFragment();
        page.setType(position);
        return page;
    }
}
