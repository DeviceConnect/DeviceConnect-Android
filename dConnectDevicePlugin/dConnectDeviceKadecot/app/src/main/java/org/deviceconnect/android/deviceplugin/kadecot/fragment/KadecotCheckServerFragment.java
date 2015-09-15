/*
 SummaryFragment
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.kadecot.fragment;

import org.deviceconnect.android.deviceplugin.kadecot.R;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

/**
 * The page which check Kadecot server window of Kadecot device plug-in.
 *
 * @author NTT DOCOMO, INC.
 */
public class KadecotCheckServerFragment extends Fragment {

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_check_kadecot_server, container, false);
        Button launchKadecot = (Button) root.findViewById(R.id.launch_kadecot);
        launchKadecot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                PackageManager pm = getActivity().getPackageManager();
                Intent intent = pm.getLaunchIntentForPackage(getString(R.string.kadecot_package_name));
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(getActivity(), getString(R.string.not_install_kadecot_body),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        return root;
    }

}
