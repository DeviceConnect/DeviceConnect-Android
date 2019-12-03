/*
 IRKitServiceListActivity.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.irkit.settings.activity;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.irkit.IRKitDeviceService;
import org.deviceconnect.android.deviceplugin.irkit.R;
import org.deviceconnect.android.deviceplugin.irkit.data.IRKitDBHelper;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.android.service.DConnectServiceListener;
import org.deviceconnect.android.ui.activity.DConnectServiceListActivity;

/**
 * IRKitサービス一覧画面.
 * @author NTT DOCOMO, INC.
 */
public class IRKitServiceListActivity extends DConnectServiceListActivity implements DConnectServiceListener{
    /**
     *  ダイアログタイプ:{@value}.
     */
    private static final String DIALOG_TYPE_VIRTUAL_DEIVCE = "TYPE_VIRTUAL_DEIVCE";

    @Override
    protected Class<? extends DConnectMessageService> getMessageServiceClass() {
        return IRKitDeviceService.class;
    }

    @Override
    protected Class<? extends Activity> getSettingManualActivityClass() {
        return IRKitSettingActivity.class;
    }

    @Override
    protected boolean enablesItemClick() {
        return true;
    }

    public void onServiceRemoved(final DConnectService service) {
        super.onServiceRemoved(service);
        if (service.getId().contains(".")) {
            IRKitDBHelper helper = new IRKitDBHelper(this);
            helper.removeVirtualDevice(service.getId());
        }
    }

    @Override
    protected void onItemClick(final DConnectService service) {
        // ServiceIdに.が含まれている場合は仮想デバイスとみなす。
        if (service.getId().contains(".")) {
            AlertDialogFragment vDialog = AlertDialogFragment.create(DIALOG_TYPE_VIRTUAL_DEIVCE,
                    getString(R.string.not_open_virtual_device_title),
                    getString(R.string.not_open_virtual_device_message),
                    getString(R.string.dialog_ok));
            vDialog.show(getFragmentManager(), DIALOG_TYPE_VIRTUAL_DEIVCE);
            return;
        }
        Intent intent = new Intent(getApplicationContext(), IRKitVirtualDeviceListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(IRKitVirtualDeviceListActivity.EXTRA_SERVICE_ID, service.getId());
        startActivity(intent);
    }

    /**
     * エラーダイアログ.
     */
    public static class AlertDialogFragment extends DialogFragment {
        /**
         * タグのキーを定義します.
         */
        private static final String KEY_TAG = "tag";

        /**
         * タイトルのキーを定義します.
         */
        private static final String KEY_TITLE = "title";

        /**
         * メッセージのキーを定義します.
         */
        private static final String KEY_MESSAGE = "message";

        /**
         * Positiveボタンのキーを定義します.
         */
        private static final String KEY_POSITIVE = "yes";

        /**
         * Negativeボタンのキーを定義します.
         */
        private static final String KEY_NEGATIVE = "no";

        /**
         * ボタン無しでAlertDialogを作成します.
         * @param tag タグ
         * @param title タイトル
         * @param message メッセージ
         * @return AlertDialogFragmentのインスタンス
         */
        public static AlertDialogFragment create(final String tag, final String title, final String message) {
            return create(tag, title, message, null, null);
        }

        /**
         * PositiveボタンのみでAlertDialogを作成します.
         * @param tag タグ
         * @param title タイトル
         * @param message メッセージ
         * @param positive positiveボタン名
         * @return AlertDialogFragmentのインスタンス
         */
        public static AlertDialogFragment create(final String tag, final String title, final String message, final String positive) {
            return create(tag, title, message, positive, null);
        }

        /**
         * ボタン有りでAlertDialogを作成します.
         * @param tag タグ
         * @param title タイトル
         * @param message メッセージ
         * @param positive positiveボタン名
         * @param negative negativeボタン名
         * @return AlertDialogFragmentのインスタンス
         */
        public static AlertDialogFragment create(final String tag, final String title, final String message,
                                                 final String positive, final String negative) {
            Bundle args = new Bundle();
            args.putString(KEY_TAG, tag);
            args.putString(KEY_TITLE, title);
            args.putString(KEY_MESSAGE, message);
            if (positive != null) {
                args.putString(KEY_POSITIVE, positive);
            }
            if (negative != null) {
                args.putString(KEY_NEGATIVE, negative);
            }

            AlertDialogFragment dialog = new AlertDialogFragment();
            dialog.setArguments(args);
            return dialog;
        }

        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getArguments().getString(KEY_TITLE));
            builder.setMessage(getArguments().getString(KEY_MESSAGE));
            if (getArguments().getString(KEY_POSITIVE) != null) {
                builder.setPositiveButton(getArguments().getString(KEY_POSITIVE), null);
            }
            if (getArguments().getString(KEY_NEGATIVE) != null) {
                builder.setNegativeButton(getArguments().getString(KEY_NEGATIVE), null);
            }
            return builder.create();
        }

        @Override
        public void onCancel(final DialogInterface dialog) {

        }
    }
}
