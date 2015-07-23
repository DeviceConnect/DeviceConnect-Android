package org.deviceconnect.android.deviceplugin.theta.activity;

import android.support.v4.app.Fragment;

import org.deviceconnect.android.deviceplugin.theta.fragment.ConfirmationFragment;
import org.deviceconnect.android.deviceplugin.theta.fragment.WifiConnectionFragment;
import org.deviceconnect.android.deviceplugin.theta.fragment.MovieModeFragment;
import org.deviceconnect.android.deviceplugin.theta.fragment.PhotoModeFragment;
import org.deviceconnect.android.deviceplugin.theta.fragment.SummaryFragment;
import org.deviceconnect.android.ui.activity.DConnectSettingPageFragmentActivity;


public class ThetaDeviceSettingsActivity extends DConnectSettingPageFragmentActivity {

    private final Fragment[] mFragments = {
        new SummaryFragment(),
        new PhotoModeFragment(),
        new MovieModeFragment(),
        new WifiConnectionFragment(),
        new ConfirmationFragment()
    };

    @Override
    public int getPageCount() {
        return mFragments.length;
    }

    @Override
    public Fragment createPage(int position) {
        return mFragments[position];
    }

}
