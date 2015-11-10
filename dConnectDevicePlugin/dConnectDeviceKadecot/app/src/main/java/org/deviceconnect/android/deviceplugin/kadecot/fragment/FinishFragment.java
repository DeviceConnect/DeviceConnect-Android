/*
 SummaryFragment
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.kadecot.fragment;

import org.deviceconnect.android.deviceplugin.kadecot.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * The page which finish window of Kadecot device plug-in.
 *
 * @author NTT DOCOMO, INC.
 */
public class FinishFragment extends Fragment {

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_finish, null);
    }

}
