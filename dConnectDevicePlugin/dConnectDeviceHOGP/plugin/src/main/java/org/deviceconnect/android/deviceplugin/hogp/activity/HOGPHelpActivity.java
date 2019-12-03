/*
 HOGPHelpActivity.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hogp.activity;


import androidx.fragment.app.Fragment;

import org.deviceconnect.android.deviceplugin.hogp.fragment.HOGPHelp01Fragment;
import org.deviceconnect.android.deviceplugin.hogp.fragment.HOGPHelp02Fragment;
import org.deviceconnect.android.deviceplugin.hogp.fragment.HOGPHelp03Fragment;
import org.deviceconnect.android.deviceplugin.hogp.fragment.HOGPHelp04Fragment;
import org.deviceconnect.android.ui.activity.DConnectSettingPageFragmentActivity;

/**
 * ヘルプ画面用Activity.
 *
 * @author NTT DOCOMO, INC.
 */
public class HOGPHelpActivity extends DConnectSettingPageFragmentActivity {
    @Override
    public int getPageCount() {
        return 4;
    }

    @Override
    public Fragment createPage(final int position) {
        switch (position) {
            case 0:
                return HOGPHelp01Fragment.newInstance();
            case 1:
                return HOGPHelp02Fragment.newInstance();
            case 2:
                return HOGPHelp03Fragment.newInstance();
            case 3:
                return HOGPHelp04Fragment.newInstance();
        }
        return null;
    }
}
