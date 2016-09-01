/*
 ChromeCastSettingFragmentPage2.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.chromecast.setting;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.deviceconnect.android.deviceplugin.chromecast.R;

/**
 * チュートリアル画面.
 * <p>
 * 画面を作成する
 * </p>
 * 電源の接続
 * 
 * @author NTT DOCOMO, INC.
 */
public class ChromeCastSettingFragmentPage2 extends Fragment {
    @Override
    public View onCreateView(final LayoutInflater inflater,
            final ViewGroup container, final Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.chromecast_settings_step_2, container, false);
        Toolbar toolbar = (Toolbar) root.findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.activity_setting_page_title));

        toolbar.setNavigationIcon(R.drawable.ic_close_light);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });

        return root;
    }
}
