package org.deviceconnect.android.deviceplugin.hogp.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.deviceconnect.android.deviceplugin.hogp.R;

public class HOGPHelp01Fragment extends Fragment {

    public static HOGPHelp01Fragment newInstance() {
        HOGPHelp01Fragment frag = new HOGPHelp01Fragment();
        return frag;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_help_01, null);
    }
}
