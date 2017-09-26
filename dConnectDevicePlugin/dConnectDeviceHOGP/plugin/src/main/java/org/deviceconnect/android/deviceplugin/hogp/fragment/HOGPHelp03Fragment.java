/*
 HOGPHelp03Fragment.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hogp.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.deviceconnect.android.deviceplugin.hogp.R;

public class HOGPHelp03Fragment extends Fragment {

    public static HOGPHelp03Fragment newInstance() {
        HOGPHelp03Fragment frag = new HOGPHelp03Fragment();
        return frag;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_help_03, null);
    }
}
