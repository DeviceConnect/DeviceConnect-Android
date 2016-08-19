/*
 ThetaDeviceSettingsActivity
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.kadecot.activity;

import android.support.v4.app.Fragment;

import org.deviceconnect.android.deviceplugin.kadecot.fragment.FinishFragment;
import org.deviceconnect.android.deviceplugin.kadecot.fragment.KadecotCheckServerFragment;
import org.deviceconnect.android.deviceplugin.kadecot.fragment.KadecotInstallFragment;
import org.deviceconnect.android.deviceplugin.kadecot.fragment.SummaryFragment;
import org.deviceconnect.android.ui.activity.DConnectSettingPageFragmentActivity;

/**
 * The settings window of Kadecot device plug-in.
 *
 * @author NTT DOCOMO, INC.
 */
public class KadecotDeviceSettingsActivity extends DConnectSettingPageFragmentActivity {

    /** Fargment list. */
    private final Fragment[] mFragments = {
        new SummaryFragment(),
        new KadecotInstallFragment(),
        new KadecotCheckServerFragment(),
        new FinishFragment()
    };

    @Override
    public int getPageCount() {
        return mFragments.length;
    }

    @Override
    public Fragment createPage(final int position) {
        return mFragments[position];
    }

}
