/*
 Utils.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.slackmessagehook.setting.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import org.deviceconnect.android.deviceplugin.slackmessagehook.R;
import org.deviceconnect.android.deviceplugin.slackmessagehook.slack.SlackManager;

import java.io.File;

/**
 * ユーティリティクラス
 */
public class Utils {

    //---------------------------------------------------------------------------------------
    //region etc.

    /**
     * ネットワーク接続確認
     * @param context Context
     * @return 接続状態ならtrue
     */
    public static boolean onlineCheck(Context context){
        ConnectivityManager cm =  (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if( info != null ){
            return info.isConnected();
        } else {
            return false;
        }
    }

    /**
     * 画面遷移
     * @param fragment Fragment
     * @param manager FragmentManager
     */
    public static void transition(Fragment fragment, FragmentManager manager, boolean backStack) {
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.container, fragment, fragment.getClass().getName());
        if (backStack){
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    /**
     * キャッシュのフォルダを取得
     * @param context Context
     * @return フォルダ
     */
    public static File getCacheDir(Context context) {
        return new File(context.getApplicationContext().getCacheDir(), context.getString(R.string.app_name));
    }

    /**
     * フォルダを削除
     * @param dir フォルダ
     * @return 成功ならtrue
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // The directory is now empty so delete it
        return dir.delete();
    }


    //endregion
    //---------------------------------------------------------------------------------------
    //region Token

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


    //endregion
    //---------------------------------------------------------------------------------------
    //region Online

    /**
     * オンラインステータスを取得
     * @param context Context
     * @return オンラインステータス
     */
    public static boolean getOnlineStatus(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("onlineStatus", false);
    }

    /**
     * オンラインステータスを保存
     * @param context Context
     * @param status オンラインステータス
     */
    public static void saveOnlineStatus(Context context, boolean status) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("onlineStatus", status);
        editor.apply();
    }


    //endregion
    //---------------------------------------------------------------------------------------
    //region Dialog

    /**
     * 警告ダイアログ表示
     * @param context Context
     * @param msg メッセージ
     */
    public static void showAlertDialog(Context context, String msg) {
        new AlertDialog.Builder(context)
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .show();
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
     * 確認ダイアログ表示
     * @param context Context
     * @param title タイトル
     * @param msg メッセージ
     * @param listener OKボタンイベントリスナー
     */
    public static void showConfirmDialog(Context context, String title, String msg, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("OK", listener)
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * ネットワークエラーダイアログ表示
     * @param context Context
     * @param callback Callback
     */
    public static void showNetworkErrorDialog(final Context context, SlackManager.FinishCallback<Boolean> callback) {
        Utils.showErrorDialog(context, context.getString(R.string.error_network), null, 1, callback);
    }

    /**
     * Slackサーバーエラーダイアログ表示
     * @param context Context
     * @param callback Callback
     */
    public static void showSlackErrorDialog(final Context context, SlackManager.FinishCallback<Boolean> callback) {
        Utils.showErrorDialog(context, context.getString(R.string.error_slack), null, 2, callback);
    }

    /**
     * Slack認証エラーダイアログ表示
     * @param context Context
     * @param manager FragmentManager
     * @param callback Callback
     */
    public static void showSlackAuthErrorDialog(final Context context, FragmentManager manager, SlackManager.FinishCallback<Boolean> callback) {
        Utils.showErrorDialog(context, context.getString(R.string.error_auth), manager, 3, callback);
    }

    /**
     * 不明なエラーダイアログ表示
     * @param context Context
     * @param callback Callback
     */
    public static void showErrorDialog(final Context context, SlackManager.FinishCallback<Boolean> callback) {
        Utils.showErrorDialog(context, context.getString(R.string.error_unknown), null, 0, callback);
    }

    /**
     * エラーダイアログ表示（ベース）
     * @param context Context
     * @param msg メッセージ
     * @param manager FragmentManager
     * @param type Type
     * @param callback Callback
     */
    private static void showErrorDialog(final Context context, String msg,final FragmentManager manager, int type, final SlackManager.FinishCallback<Boolean> callback) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context)
                .setTitle("エラー")
                .setMessage(msg)
                .setNegativeButton("閉じる", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (callback != null) {
                            callback.onFinish(false, null);
                        }
                    }
                });

        boolean retry = false;
        boolean wifi = false;
        boolean token = false;
        switch (type) {
            case 1: // ネットワークエラー
                wifi = true;
                retry = true;
                break;
            case 2: // Slackサーバーエラー
                retry = true;
                break;
            case 3: // Slack認証エラー
                token = true;
                break;
            default:
                retry = true;
                break;

        }
        if (wifi) {
            dialog.setPositiveButton(context.getString(R.string.wifi_settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // WiFi設定を開く
                    context.startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
                    if (callback != null) {
                        callback.onFinish(false, null);
                    }
                }
            });
        }
        if (retry) {
            dialog.setNeutralButton(context.getString(R.string.retry), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (callback != null) {
                        callback.onFinish(true, null);
                    }
                }
            });
        }
        if (token) {
            dialog.setPositiveButton(context.getString(R.string.token_settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 画面遷移
                    Utils.transition(new SettingTokenFragment(), manager, true);
                }
            });
        }
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (callback != null) {
                    callback.onFinish(false, null);
                }
            }
        });
        dialog.show();
    }
    //endregion
    //---------------------------------------------------------------------------------------
}
