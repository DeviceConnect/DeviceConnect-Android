/*
 AlertDialogFragment.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.setting;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * AlertDialogを表示するためのDialogFragment.
 *
 * @author NTT DOCOMO, INC.
 */
public class AlertDialogFragment extends DialogFragment {
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
    public static AlertDialogFragment create(final String tag, final String title,
                                             final String message, final String positive) {
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
    public static AlertDialogFragment create(final String tag,
                                               final String title, final String message,
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
            builder.setPositiveButton(getArguments().getString(KEY_POSITIVE),
                    (dialog, which) -> {
                        try {
                            ((OnAlertDialogListener) getActivity()).onPositiveButton(getArguments().getString(KEY_TAG));
                        } catch (Exception e) {
                            return;
                        }
                    });
        }
        if (getArguments().getString(KEY_NEGATIVE) != null) {
            builder.setNegativeButton(getArguments().getString(KEY_NEGATIVE),
                    (dialog, which) -> {
                        try {
                            ((OnAlertDialogListener) getActivity()).onNegativeButton(getArguments().getString(KEY_TAG));
                        } catch (Exception e) {
                            return;
                        }
                    });
        }
        return builder.create();
    }

    @Override
    public void onCancel(final DialogInterface dialog) {
        try {
            ((OnAlertDialogListener) getActivity()).onNegativeButton(getArguments().getString(KEY_TAG));
        } catch (Exception e) {
            return;
        }
    }

    /**
     * ボタンが押下された時のイベントを通知するリスナー.
     */
    public interface OnAlertDialogListener {
        /**
         * Positiveボタンが押下された場合に呼び出します.
         * @param tag タグ
         */
        void onPositiveButton(String tag);

        /**
         * Negativeボタんが押下された場合に呼び出します.
         * @param tag タグ
         */
        void onNegativeButton(String tag);
    }
}
