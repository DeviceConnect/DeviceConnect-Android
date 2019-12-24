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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.deviceconnect.android.deviceplugin.irkit.IRKitDeviceService;
import org.deviceconnect.android.deviceplugin.irkit.R;
import org.deviceconnect.android.deviceplugin.irkit.data.IRKitDBHelper;
import org.deviceconnect.android.deviceplugin.irkit.data.VirtualDeviceData;

import java.util.UUID;

/**
 * カテゴリー選択用のダイアログ.
 */
public class IRKitCreateVirtualDeviceDialogFragment extends DialogFragment {

    /**
     * カテゴリーのキー名.
     */
    private static final String KEY_CATEGORY = "category";

    /**
     * サービスIDのキー名.
     */
    private static final String KEY_SERVICE_ID = "serviceId";

    /**
     * 仮装デバイスのサービスIDのキー名.
     */
    private static final String KEY_VIRTUAL_ID = "virtualServiceId";

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
    public static IRKitCreateVirtualDeviceDialogFragment newInstance(final String serviceId, final String category) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_CATEGORY, category);
        bundle.putString(KEY_SERVICE_ID, serviceId);
        bundle.putString(KEY_VIRTUAL_ID, serviceId + "." + UUID.randomUUID().toString());

        IRKitCreateVirtualDeviceDialogFragment d = new IRKitCreateVirtualDeviceDialogFragment();
        d.setArguments(bundle);
        return d;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View dialogView = inflater.inflate(R.layout.dialog_virtual_device, null);

        final EditText deviceNameLbl = (EditText) dialogView.findViewById(R.id.device_name);
        deviceNameLbl.setText(getArguments().getString(KEY_CATEGORY));
        deviceNameLbl.addTextChangedListener(mWatchHandler);
        deviceNameLbl.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(final View v, final boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getActivity()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });

        TextView serviceIdLbl = (EditText) dialogView.findViewById(R.id.service_id);
        serviceIdLbl.setText(getArguments().getString(KEY_VIRTUAL_ID));

        TextView deviceCategoryLbl = (TextView) dialogView.findViewById(R.id.device_category);
        deviceCategoryLbl.setText(getArguments().getString(KEY_CATEGORY));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.virtual_device_title);
        builder.setView(dialogView);
        builder.setPositiveButton(R.string.virtual_device_create, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                saveVirtualDeviceData(deviceNameLbl.getText().toString());
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.virtual_device_delete_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                dialog.dismiss();
            }
        });

        return builder.create();
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
    private void saveVirtualDeviceData(final String name) {
        IRKitDBHelper helper = new IRKitDBHelper(getActivity());
        VirtualDeviceData device = new VirtualDeviceData();
        device.setServiceId(getArguments().getString(KEY_VIRTUAL_ID));
        device.setCategoryName(getArguments().getString(KEY_CATEGORY));
        device.setDeviceName(name);
        long i = helper.addVirtualDevice(device);
        if (i < 0) {
            // TODO 登録失敗
        } else {
            sendEventOnAdded(device);

            showAlert(getActivity(), getString(R.string.virtual_device_create),
                    getString(R.string.created_virtual_device));
            if (mDelegate != null) {
                mDelegate.onCreated();
            }
        }
    }

    /**
     * 仮想デバイスを追加する.
     * @param device 追加する仮想デバイス
     */
    private void sendEventOnAdded(final VirtualDeviceData device) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        Intent intent = new Intent(IRKitDeviceService.ACTION_VIRTUAL_DEVICE_ADDED);
        intent.putExtra(IRKitDeviceService.EXTRA_VIRTUAL_DEVICE_ID, device.getServiceId());
        LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
    }

    /**
     * アラートの表示.
     * @param activity Activity
     * @param title タイトル
     * @param message メッセージ
     */
    public static void showAlert(final Activity activity, final String title, final String message) {
        if (activity == null) {
            return;
        }

        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.dialog_ok, null)
                .show();
    }

    /**
     * 文字数チェック.
     */
    private TextWatcher mWatchHandler = new TextWatcher() {
        @Override
        public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
        }
        @Override
        public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
            AlertDialog dialog = (AlertDialog) getDialog();
            Button btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btn.setEnabled(s.length() > 0);
        }
        @Override
        public void afterTextChanged(final Editable s) {
        }
    };
}
