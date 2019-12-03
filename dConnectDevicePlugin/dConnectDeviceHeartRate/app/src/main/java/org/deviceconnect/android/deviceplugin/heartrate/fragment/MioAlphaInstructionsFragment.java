/*
 MioAlphaInstructionsFragment
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.heartrate.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.deviceconnect.android.deviceplugin.heartrate.R;

/**
 * This fragment explain settings of Mio Alpha.
 * @author NTT DOCOMO, INC.
 */
public class MioAlphaInstructionsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_mio_alpha_instructions, null);
        return rootView;
    }
}
