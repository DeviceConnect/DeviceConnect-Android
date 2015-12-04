/*
 HostSettingFragment.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.host.setting;

import android.annotation.SuppressLint;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.host.R;

import static android.content.Context.WIFI_SERVICE;

/**
 * @author NTT DOCOMO, INC.
 */
public class HostSettingFragment extends Fragment {
    @SuppressLint("DefaultLocale")
	@Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        // Positionを取得
        Bundle mBundle = getArguments();
        int mPagePosition = mBundle.getInt("position", 0);

        int mPageLayoutId = this.getResources().getIdentifier("host_setting_" + mPagePosition, "layout",
                getActivity().getPackageName());

        View mView = inflater.inflate(mPageLayoutId, container, false);

        if (mPagePosition == 0) {
            WifiManager wifiManager = (WifiManager) this.getActivity().getSystemService(WIFI_SERVICE);
            int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
            final String formatedIpAddress = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                    (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
            
            // Host IP表示用
            TextView ipTextView = (TextView) mView.findViewById(R.id.host_ipaddress);
            ipTextView.setText("Your IP:" + formatedIpAddress);
        }

        return mView;
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
