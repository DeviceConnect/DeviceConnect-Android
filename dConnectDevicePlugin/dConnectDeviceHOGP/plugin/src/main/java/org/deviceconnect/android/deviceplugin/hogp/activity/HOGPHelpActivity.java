package org.deviceconnect.android.deviceplugin.hogp.activity;

import android.support.v4.app.Fragment;

import org.deviceconnect.android.deviceplugin.hogp.fragment.HOGPHelp01Fragment;
import org.deviceconnect.android.deviceplugin.hogp.fragment.HOGPHelp02Fragment;
import org.deviceconnect.android.deviceplugin.hogp.fragment.HOGPHelp03Fragment;
import org.deviceconnect.android.ui.activity.DConnectSettingPageFragmentActivity;

/**
 * ヘルプ画面用Activity.
 */
public class HOGPHelpActivity extends DConnectSettingPageFragmentActivity {
    @Override
    public int getPageCount() {
        return 3;
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
        }
        return null;
    }
}
