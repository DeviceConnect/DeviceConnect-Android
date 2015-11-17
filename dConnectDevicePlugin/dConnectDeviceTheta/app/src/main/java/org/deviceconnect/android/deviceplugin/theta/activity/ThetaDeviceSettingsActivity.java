/*
 ThetaDeviceSettingsActivity
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceModel;
import org.deviceconnect.android.deviceplugin.theta.fragment.ConfirmationFragment;
import org.deviceconnect.android.deviceplugin.theta.fragment.MovieModeFragment;
import org.deviceconnect.android.deviceplugin.theta.fragment.PhotoModeFragment;
import org.deviceconnect.android.deviceplugin.theta.fragment.SettingsFragment;
import org.deviceconnect.android.deviceplugin.theta.fragment.SummaryFragment;
import org.deviceconnect.android.deviceplugin.theta.fragment.WifiConnectionFragment;
import org.deviceconnect.android.ui.activity.DConnectSettingPageFragmentActivity;

import java.util.List;

/**
 * The settings window of THETA device plug-in.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaDeviceSettingsActivity extends DConnectSettingPageFragmentActivity {

    private static final String KEY_SELECTED_MODEL = "SelectedModel";

    private final SummaryFragment mSummary = new SummaryFragment();
    private final PhotoModeFragment mPhotoMode = new PhotoModeFragment();
    private final MovieModeFragment mMovieMode = new MovieModeFragment();

    private final Fragment[] mFragments = {
        mSummary,
        mPhotoMode,
        mMovieMode,
        new WifiConnectionFragment(),
        new ConfirmationFragment()
    };

    private ThetaDeviceModel mSelectedModel = ThetaDeviceModel.THETA_S;

    @Override
    public int getPageCount() {
        return mFragments.length;
    }

    @Override
    public Fragment createPage(int position) {
        return mFragments[position];
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_SELECTED_MODEL, mSelectedModel);
        Log.d("AAA", "onSaveInstanceState: model = " + mSelectedModel);
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mSelectedModel = (ThetaDeviceModel) savedInstanceState.get(KEY_SELECTED_MODEL);
        }
        Log.d("AAA", "SettingsActivity.onRestoreInstanceState: model = " + mSelectedModel);
    }

    public ThetaDeviceModel getSelectedModel() {
        return mSelectedModel;
    }

    public void setSelectedModel(final ThetaDeviceModel model) {
        mSelectedModel = model;
        onModelSelected(model);
    }

    private void onModelSelected(final ThetaDeviceModel model) {
        FragmentManager fragmentMgr = getSupportFragmentManager();
        List<Fragment> list = fragmentMgr.getFragments();
        if (list != null) {
            for (Fragment f : list) {
                if (f instanceof SettingsFragment) {
                    ((SettingsFragment) f).onModelSelected(model);
                }
            }
        }
    }

}
