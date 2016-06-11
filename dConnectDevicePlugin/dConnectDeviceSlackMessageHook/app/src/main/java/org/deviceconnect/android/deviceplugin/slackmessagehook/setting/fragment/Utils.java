/*
 Utils.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.slackmessagehook.setting.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.deviceconnect.android.deviceplugin.slackmessagehook.R;

/**
 * ユーティリティクラス
 */
public class Utils {

    /**
     * 画面遷移
     * @param fragment Fragment
     * @param manager FragmentManager
     */
    public static void transition(Fragment fragment, FragmentManager manager, boolean backStack) {
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.container, fragment);
        if (backStack){
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    /**
     * プログレスダイアログを表示
     * @param context Context
     * @return ダイアログ
     */
    public static ProgressDialog showProgressDialog(Context context) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage("Please wait...");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        return dialog;
    }

    /**
     * アクセストークンを取得
     * @param context Context
     * @return アクセストークン
     */
    public static String getAccessToken(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("accessToken", null);
    }

    /**
     * アクセストークンを保存
     * @param context Context
     * @param accessToken アクセストークン
     */
    public static void saveAccessToken(Context context, String accessToken) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        if (accessToken == null) {
            editor.remove("accessToken");
        } else {
            editor.putString("accessToken", accessToken);
        }
        editor.apply();
    }

}
