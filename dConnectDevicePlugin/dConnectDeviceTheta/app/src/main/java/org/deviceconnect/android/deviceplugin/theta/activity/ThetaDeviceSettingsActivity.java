/*
 ThetaDeviceSettingsActivity
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.activity;

import android.support.v4.app.Fragment;

import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceModel;
import org.deviceconnect.android.deviceplugin.theta.fragment.ConfirmationFragment;
import org.deviceconnect.android.deviceplugin.theta.fragment.WifiConnectionFragment;
import org.deviceconnect.android.deviceplugin.theta.fragment.MovieModeFragment;
import org.deviceconnect.android.deviceplugin.theta.fragment.PhotoModeFragment;
import org.deviceconnect.android.deviceplugin.theta.fragment.SummaryFragment;
import org.deviceconnect.android.ui.activity.DConnectSettingPageFragmentActivity;

/**
 * The settings window of THETA device plug-in.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaDeviceSettingsActivity extends DConnectSettingPageFragmentActivity {

    private SummaryFragment mSummary = new SummaryFragment();
    private PhotoModeFragment mPhotoMode = new PhotoModeFragment();
    private MovieModeFragment mMovieMode = new MovieModeFragment();

    private final Fragment[] mFragments = {
        mSummary,
        mPhotoMode,
        mMovieMode,
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

    public ThetaDeviceModel getSelectedModel() {
        return mSummary.getSelectedModel();
    }

    public void onModelChanged() {
        mPhotoMode.updateView();
        mMovieMode.updateView();
    }

}
