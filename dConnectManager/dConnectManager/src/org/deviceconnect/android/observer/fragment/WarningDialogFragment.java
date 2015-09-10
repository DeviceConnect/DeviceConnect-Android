/*
 WarningDialogFragment.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.observer.fragment;

import java.util.List;

import org.deviceconnect.android.manager.R;
import org.deviceconnect.android.observer.DConnectObservationService;
import org.deviceconnect.android.observer.receiver.ObserverReceiver;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 警告ダイアログフラグメント.
 * 
 *
 * @author NTT DOCOMO, INC.
 */
public class WarningDialogFragment extends DialogFragment {

    /**
     * 再監視停止フラグ.
     */
    private boolean mDisableFlg;

    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        String packageName = getActivity().getIntent().getStringExtra(DConnectObservationService.PARAM_PACKAGE_NAME);
        int port = getActivity().getIntent().getIntExtra(DConnectObservationService.PARAM_PORT, -1);
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.activity_warning_dialog, null);
        final TextView appNameView = (TextView) view.findViewById(R.id.alert_message);
        final ImageView appIconView = (ImageView) view.findViewById(R.id.alert_icon);
        String appName = getAppName(packageName);
        Drawable icon = null;
        try {
            icon = getActivity().getPackageManager().getApplicationIcon(packageName);
        } catch (NameNotFoundException e) {
            icon = getActivity().getResources().getDrawable(R.drawable.ic_launcher);
        }
        appNameView.setText(appName);
        appIconView.setImageDrawable(icon);
        String tempMessage = String.format(getString(R.string.activity_warning_mess), port);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Dialog dialog = builder.setTitle(getString(R.string.activity_warning)).setMessage(tempMessage).setView(view)
                .setPositiveButton(R.string.activity_warning_ok, new OnClickListener() {

                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        CheckBox box = (CheckBox) view.findViewById(R.id.disable_observer);
                        mDisableFlg = box.isChecked();
                        dismiss();
                    }
                }).create();

        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    /**
     * アプリケーション名を取得する.
     * 
     * @param packageName アプリケーションのパッケージ名
     * @return アプリケーション名
     */
    private String getAppName(final String packageName) {
        PackageManager pm = getActivity().getPackageManager();
        final List<ApplicationInfo> appInfoList = pm.getInstalledApplications(Context.BIND_AUTO_CREATE);

        for (ApplicationInfo ai : appInfoList) {
            String appName = ai.loadLabel(pm).toString();
            if (appName != null) {
                if (ai.packageName.equals(packageName)) {
                    return appName;
                }
            }
        }
        return "NoName";
    }

    @Override
    public void onStop() {
        super.onStop();

        if (!mDisableFlg) {
            Intent i = new Intent();
            i.setAction(DConnectObservationService.ACTION_START);
            i.setClass(getActivity(), ObserverReceiver.class);
            i.putExtra(DConnectObservationService.PARAM_RESULT_RECEIVER, new ResultReceiver(new Handler()) {
                @Override
                protected void onReceiveResult(int resultCode, Bundle resultData) {
                }
            });
            getActivity().sendBroadcast(i);
        }
        getActivity().finish();
    }
}
