/*
 SummaryFragment
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.kadecot.fragment;

import org.deviceconnect.android.deviceplugin.kadecot.R;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * The page which install window of Kadecot device plug-in.
 *
 * @author NTT DOCOMO, INC.
 */
public class KadecotInstallFragment extends Fragment {

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_install_kadecot, container, false);
        ImageButton installKadecotButton = (ImageButton) root.findViewById(R.id.install_kadecot_button_install_kadecot);
        installKadecotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Uri uri = Uri.parse("market://details?id=" + getString(R.string.kadecot_package_name));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        String dispText;
        if (isInstalledKadecotPackage()) {
            dispText = getString(R.string.installed_kadecot_body);
        } else {
            dispText = getString(R.string.not_install_kadecot_body);
        }
        ((TextView) getView().findViewById(R.id.application_check)).setText(dispText);
    }

    /**
     * Check install Kadecot package.
     *
     * @return true : installed, false : Not install.
     */
    private boolean isInstalledKadecotPackage() {
        String appId = getString(R.string.kadecot_package_name);

        try {
            PackageManager pm = getActivity().getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(appId, PackageManager.GET_META_DATA);
            if (appInfo != null) {
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return false;
    }

}
