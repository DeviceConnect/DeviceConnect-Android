/*
 HvcSettingFragment.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.hvc.setting;

import static android.content.Context.WIFI_SERVICE;

import org.deviceconnect.android.deviceplugin.hvc.BuildConfig;
import org.deviceconnect.android.deviceplugin.hvc.R;
import org.deviceconnect.android.deviceplugin.hvc.HvcDeviceService;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author NTT DOCOMO, INC.
 */
public class HvcSettingFragment extends Fragment {
    /** HvcのIPを表示するためのTextView. */
    private TextView mDeviceHvcIpTextView;

    /** context. */
    private Activity mActivity;

    /** 検索中のダイアログ. */
    private ProgressDialog mDialog;

    @SuppressLint("DefaultLocale")
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {

        // Activityを取得
        mActivity = this.getActivity();

        // Positionを取得
        Bundle mBundle = getArguments();
        int mPagePosition = mBundle.getInt("position", 0);

        int mPageLayoutId = this.getResources().getIdentifier("hvc_setting_" + mPagePosition, "layout",
                getActivity().getPackageName());

        View mView = inflater.inflate(mPageLayoutId, container, false);

        if (mPagePosition == 0) {
            WifiManager wifiManager = (WifiManager) this.getActivity().getSystemService(WIFI_SERVICE);
            int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
            final String formatedIpAddress = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                    (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
            
            // HVC IP表示用
            mDeviceHvcIpTextView = (TextView) mView.findViewById(R.id.hvc_ipaddress);
            mDeviceHvcIpTextView.setText("Your IP:" + formatedIpAddress);
        }

        return mView;
    }

    /**
     * コンテキストの取得する.
     * 
     * @return コンテキスト
     */
    public Context getContext() {
        return mActivity;
    }

    /**
     * プログレスバーが表示されているか.
     * 
     * @return 表示されている場合はtrue,それ以外はfalse
     */
    public boolean isShowProgressDialog() {
        return mDialog != null;
    }

    /**
     * プログレスバーを表示する.
     */
    public void showProgressDialog() {
        if (mDialog != null) {
            return;
        }
        mDialog = new ProgressDialog(getActivity());
        mDialog.setTitle("処理中");
        mDialog.setMessage("Now Loading...");
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setCancelable(false);
        mDialog.show();
    }

    /**
     * プログレスバーを非表示にする.
     */
    public void dismissProgressDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }


}
