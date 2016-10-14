/*
 CommandDetailsHelpFragment.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.app.simplebot.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.deviceconnect.android.app.simplebot.R;

/**
 *　コマンド詳細画面
 */
public class CommandDetailsHelpFragment extends Fragment {

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getString(R.string.app_name) + " [ヘルプ]");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_command_help, container, false);
    }
}
