/*
 BaseHostSettingPageFragment.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.setting;

import android.app.Activity;
import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import org.deviceconnect.android.deviceplugin.host.R;

/**
 * 各設定項目のベースフラグメント.
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class BaseHostSettingPageFragment extends Fragment {

    protected abstract String getPageTitle();

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        showPageTitle(getPageTitle());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        showPageTitle(getString(R.string.host_settings_title));
    }

    private void showPageTitle(final String title) {
        Activity activity = getActivity();
        if (activity != null) {
            ((AppCompatActivity) activity).getSupportActionBar().setTitle(title);
        }
    }
}
