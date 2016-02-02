/*
 LinkingImageFragment.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.setting.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.deviceconnect.android.deviceplugin.linking.R;

/**
 * Fragment for show Linking image.
 *
 * @author NTT DOCOMO, INC.
 */
public class LinkingImageFragment extends Fragment {

    private static final String PACKAGE_NAME = "com.nttdocomo.android.smartdeviceagent";
    private View mRoot;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.show_linking, container, false);
        mRoot = root;
        mRoot.findViewById(R.id.google_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + PACKAGE_NAME)));

            }
        });
        mRoot.findViewById(R.id.launch_linking).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(getContext().getPackageManager().getLaunchIntentForPackage(PACKAGE_NAME));
            }
        });
        refreshLaunchButton();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshLaunchButton();
    }

    private void refreshLaunchButton() {
        if (isApplicationInstalled(getActivity())) {
            mRoot.findViewById(R.id.google_play).setVisibility(View.GONE);
            mRoot.findViewById(R.id.launch_linking).setVisibility(View.VISIBLE);
        } else {
            mRoot.findViewById(R.id.google_play).setVisibility(View.VISIBLE);
            mRoot.findViewById(R.id.launch_linking).setVisibility(View.GONE);
        }
    }

    private boolean isApplicationInstalled(final Context context) {
        try {
            context.getPackageManager().getPackageInfo(PACKAGE_NAME, PackageManager.GET_META_DATA);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

}
