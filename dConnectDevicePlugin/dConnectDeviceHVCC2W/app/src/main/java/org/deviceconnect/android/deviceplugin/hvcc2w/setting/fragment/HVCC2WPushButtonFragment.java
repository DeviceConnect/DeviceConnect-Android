/*
 HVCC2WPushButtonFragment
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.hvcc2w.setting.fragment;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.deviceconnect.android.deviceplugin.hvcc2w.R;

/**
 * HVC-C2W Settings Fragment Page 3.
 *
 * @author NTT DOCOMO, INC.
 */
public class HVCC2WPushButtonFragment extends Fragment {
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {

        return inflater.inflate(R.layout.setting_push, null);
    }
}
