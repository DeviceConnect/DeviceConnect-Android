/*
 HOGPHelp02Fragment.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hogp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import org.deviceconnect.android.deviceplugin.hogp.R;

public class HOGPHelp02Fragment extends Fragment {

    public static HOGPHelp02Fragment newInstance() {
        HOGPHelp02Fragment frag = new HOGPHelp02Fragment();
        return frag;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_help_02, null);
    }
}
