/*
 IRKitCreateVirtualDeviceDialogFragment.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.irkit.settings.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.irkit.R;
import org.deviceconnect.android.deviceplugin.irkit.data.IRKitDBHelper;
import org.deviceconnect.android.deviceplugin.irkit.data.VirtualDeviceData;

import java.util.UUID;

/**
 * カテゴリー選択用のダイアログ.
 */
public class IRKitCreateVirtualDeviceDialogFragment extends DialogFragment {


    /**
     * サービスID.
     */
    private String mServiceId;
    /**
     * カテゴリ.
     */
    private String mCategory;

    /**
     *作成完了リスナー.
     */
    private static IRKitVirtualDeviceCreateEventListener mDelegate;

    /**
     * Virtual Device の作成完了リスナー.
     */
    public static interface IRKitVirtualDeviceCreateEventListener {
        void onCreated();
    }
    /**
     * ダイアログの作成.
     * @return ダイアログ
     */
    public static IRKitCreateVirtualDeviceDialogFragment newInstance() {
        return new IRKitCreateVirtualDeviceDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final Activity activity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_virtual_device
                                    , null);
        EditText deviceNameLbl = (EditText) dialogView.findViewById(R.id.device_name);
        deviceNameLbl.setText(mCategory);
        TextView serviceIdLbl = (TextView) dialogView.findViewById(R.id.service_id);
        serviceIdLbl.setText(mServiceId + "." + UUID.randomUUID().toString());
        TextView deviceCategoryLbl = (TextView) dialogView.findViewById(R.id.device_category);
        deviceCategoryLbl.setText(mCategory);

        Button cancelBtn = (Button) dialogView.findViewById(R.id.cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                IRKitCreateVirtualDeviceDialogFragment.this.dismiss();
            }
        });
        Button createBtn = (Button) dialogView.findViewById(R.id.create);
        createBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                saveVirtualDeviceData(dialogView);
                IRKitCreateVirtualDeviceDialogFragment.this.dismiss();
            }
        });
        builder.setView(dialogView);

        return builder.create();
    }

    /**
     * サービスID とカテゴリーの設定.
     * @param serviceId サービスID
     * @param category カテゴリー
     */
    public void setVirtualDeviceData(final String serviceId, final String category) {
        mServiceId = serviceId;
        mCategory = category;
    }

    /**
     * イベントリスナーの登録.
     * @param listener リスナー
     */
    public static void setEventListner(final IRKitVirtualDeviceCreateEventListener listener) {
        mDelegate = listener;
    }

    /**
     * Virtual Device と Virtual Profile をデータベースに登録する。
     */
    private void saveVirtualDeviceData(final View rootView) {
        IRKitDBHelper helper = new IRKitDBHelper(getActivity());
        VirtualDeviceData device = new VirtualDeviceData();
        TextView serviceIdView = (TextView) rootView.findViewById(R.id.service_id);
        device.setServiceId(serviceIdView.getText().toString());
        device.setCategoryName(mCategory);
        EditText deviceNameLbl = (EditText) rootView.findViewById(R.id.device_name);
        device.setDeviceName(deviceNameLbl.getText().toString());
        long i = helper.addVirtualDevice(device);
        showAlert(getActivity(), "作成", "デバイスを作成しました。");
        if (mDelegate != null) {
            mDelegate.onCreated();
        }
    }

    /**
     * アラートの表示.
     * @param activity Activity
     * @param title タイトル
     * @param message メッセージ
     */
    public static void showAlert(final Activity activity, final String title, final String message) {
        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}
