/*
 Utils.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvcc2w.util;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.WindowManager;
import android.widget.EditText;

import org.deviceconnect.android.deviceplugin.hvcc2w.R;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;

import java.util.HashSet;
import java.util.List;
import java.util.Map;


/**
 * ユーティリティクラス
 */
public class Utils {


    //---------------------------------------------------------------------------------------
    //region Dialog

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
     * 入力ダイアログ表示
     * @param context Context
     * @param title タイトル
     * @param text 初期表示テキスト
     * @param inputType InputType
     * @param callback Callback
     */
    public static void showInputDialog(Context context, String title, String text, int inputType, final DConnectHelper.FinishCallback<String> callback) {
        final EditText editView = new EditText(context);
        editView.setInputType(inputType);
        editView.setText(text);
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(editView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (callback != null) {
                            callback.onFinish(editView.getText().toString(), null);
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }

    /**
     * 選択ダイアログ表示
     * @param context Context
     * @param title タイトル
     * @param items アイテム
     * @param callback Callback
     */
    public static void showSelectDialog(Context context, String title, String[] items, final DConnectHelper.FinishCallback<Integer> callback) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (callback != null) {
                            callback.onFinish(which, null);
                        }
                    }
                })
                .show();
    }


    //endregion
    //---------------------------------------------------------------------------------------
    //region Connection

    /**
     * 接続処理
     * @param context context
     * @param callback 終了コールバック
     */
    public static void connect(final Context context, final DConnectHelper.FinishCallback<DConnectHelper.AuthInfo> callback) {
        final String origin = context.getPackageName();
        String appName = context.getString(R.string.app_name);
        final SettingData setting = SettingData.getInstance(context);
        if (setting.scopes == null) {
            // 初期スコープ
            setting.scopes = new HashSet<>();
            setting.scopes.add(ServiceDiscoveryProfileConstants.PROFILE_NAME);
            setting.save();
        }
        String scopes[] = setting.scopes.toArray(new String[setting.scopes.size()]);

        // 接続先設定
        DConnectHelper.INSTANCE.setHostInfo(
                setting.ssl,
                setting.host,
                setting.port,
                origin
        );

        if (setting.accessToken == null) {
            // 新規接続
            // 認証
            DConnectHelper.INSTANCE.auth(appName, setting.clientId, scopes, new DConnectHelper.FinishCallback<DConnectHelper.AuthInfo>() {
                @Override
                public void onFinish(DConnectHelper.AuthInfo authInfo, Exception error) {
                    Exception outError = error;
                    DConnectHelper.AuthInfo outAuthInfo = authInfo;
                    if (authInfo != null) {
                        // 設定に保存
                        setting.accessToken = authInfo.accessToken;
                        setting.clientId = authInfo.clientId;
                    } else {
                        if (error != null) {
                            // ErrorCode=2の場合はLocalOAuth非対応サーバーなので、正常終了とする。
                            DConnectHelper.DConnectHelperException e = (DConnectHelper.DConnectHelperException)error;
                            if (e.errorCode == 2) {
                                outError = null;
                                outAuthInfo = new DConnectHelper.AuthInfo("dummy_id", null);
                            }
                        }
                        // 失敗したらクリア
                        setting.accessToken = null;
                        setting.clientId = null;
                    }
                    setting.save();
                    callback.onFinish(outAuthInfo, outError);
                }
            });
        } else {
            // 接続済み情報あり
            callback.onFinish(new DConnectHelper.AuthInfo(setting.clientId, setting.accessToken), null);
        }
    }

    /**
     * サービス一覧を取得
     * @param context context
     * @param callback 終了コールバック
     */
    public static void fetchServices(final Context context, final DConnectHelper.FinishCallback<List<DConnectHelper.ServiceInfo>> callback) {
        DConnectHelper.FinishCallback<DConnectHelper.AuthInfo> finishCallback = new DConnectHelper.FinishCallback<DConnectHelper.AuthInfo>() {
            @Override
            public void onFinish(DConnectHelper.AuthInfo authInfo, Exception error) {
                if (error == null) {
                    // サービス検索
                    DConnectHelper.INSTANCE.serviceDiscovery(authInfo.accessToken, new DConnectHelper.FinishCallback<List<DConnectHelper.ServiceInfo>>() {
                        @Override
                        public void onFinish(List<DConnectHelper.ServiceInfo> serviceInfos, Exception error) {
                            if (retryCheck(context, error)) {
                                fetchServices(context, callback);
                            } else {
                                callback.onFinish(serviceInfos, error);
                            }
                        }
                    });
                } else {
                    callback.onFinish(null, error);
                }
            }
        };
        Utils.connect(context, finishCallback);
    }

    /**
     * サービス情報を取得
     * @param context context
     * @param callback 終了コールバック
     */
    public static void fetchServiceInformation(final Context context, final String serviceId, final DConnectHelper.FinishCallback<Map<String, List<DConnectHelper.APIInfo>>> callback) {
        DConnectHelper.FinishCallback<DConnectHelper.AuthInfo> finishCallback = new DConnectHelper.FinishCallback<DConnectHelper.AuthInfo>() {
            @Override
            public void onFinish(DConnectHelper.AuthInfo authInfo, Exception error) {
                if (error == null) {
                    // サービス検索
                    DConnectHelper.INSTANCE.serviceInformation(authInfo.accessToken, serviceId, new DConnectHelper.FinishCallback<Map<String, List<DConnectHelper.APIInfo>>>() {
                        @Override
                        public void onFinish(Map<String, List<DConnectHelper.APIInfo>> apiInfos, Exception error) {
                            if (retryCheck(context, error)) {
                                fetchServiceInformation(context, serviceId, callback);
                            } else {
                                callback.onFinish(apiInfos, error);
                            }
                        }
                    });
                } else {
                    callback.onFinish(null, error);
                }
            }
        };
        Utils.connect(context, finishCallback);
    }

    /**
     * イベントを登録
     * @param context context
     * @param callback 終了コールバック
     */
    public static void registEvent(final Context context, final boolean unregist, final DConnectHelper.FinishCallback<Void> callback) {
        DConnectHelper.FinishCallback<DConnectHelper.AuthInfo> finishCallback = new DConnectHelper.FinishCallback<DConnectHelper.AuthInfo>() {
            @Override
            public void onFinish(final DConnectHelper.AuthInfo authInfo, Exception error) {
                if (error == null) {
                    // 登録
                    SettingData setting = SettingData.getInstance(context);
                    DConnectHelper.INSTANCE.registerEvent("messageHook", "message", setting.serviceId, setting.accessToken, authInfo.clientId, unregist, new DConnectHelper.FinishCallback<Void>() {
                        @Override
                        public void onFinish(Void aVoid, Exception error) {
                            if (error == null) {
                                // WebSocket接続
                                DConnectHelper.INSTANCE.openWebsocket(authInfo.clientId);
                                callback.onFinish(null, null);
                            } else {
                                if (retryCheck(context, error)) {
                                    registEvent(context, unregist, callback);
                                } else {
                                    callback.onFinish(null, error);
                                }
                            }
                        }
                    });
                } else {
                    callback.onFinish(null, error);
                }
            }
        };
        Utils.connect(context, finishCallback);
    }

    /**
     * メッセージ送信
     * @param context context
     * @param callback 終了コールバック
     */
    public static void sendMessage(final Context context, final String channel, final String text, final String resource, final DConnectHelper.FinishCallback<Void> callback) {
        DConnectHelper.FinishCallback<DConnectHelper.AuthInfo> finishCallback = new DConnectHelper.FinishCallback<DConnectHelper.AuthInfo>() {
            @Override
            public void onFinish(DConnectHelper.AuthInfo authInfo, Exception error) {
                if (error == null) {
                    // メッセージ送信
                    SettingData setting = SettingData.getInstance(context);
                    DConnectHelper.INSTANCE.sendMessage(setting.serviceId, setting.accessToken, channel, text, resource, new DConnectHelper.FinishCallback<Void>() {
                        @Override
                        public void onFinish(Void aVoid, Exception error) {
                            callback.onFinish(null, error);
                        }
                    });
                } else {
                    callback.onFinish(null, error);
                }
            }
        };
        Utils.connect(context, finishCallback);
    }

    /**
     * リクエスト送信
     * @param context context
     * @param method メソッド
     * @param path パス
     * @param params パラメータ
     * @param callback 終了コールバック
     */
    public static void sendRequest(final Context context, final String method, final String path, final String serviceId, final Map<String, String> params, final DConnectHelper.FinishCallback<Map<String, Object>> callback) {
        final DConnectHelper.FinishCallback<DConnectHelper.AuthInfo> finishCallback = new DConnectHelper.FinishCallback<DConnectHelper.AuthInfo>() {
            @Override
            public void onFinish(DConnectHelper.AuthInfo authInfo, Exception error) {
                if (error == null) {
                    // リクエスト送信
                    DConnectHelper.INSTANCE.sendRequest(method, path, serviceId, authInfo.accessToken, params, new DConnectHelper.FinishCallback<Map<String, Object>>() {
                        @Override
                        public void onFinish(Map<String, Object> stringObjectMap, Exception error) {
                            callback.onFinish(stringObjectMap, error);
                        }
                    });
                } else {
                    callback.onFinish(null, error);
                }
            }
        };
        Utils.connect(context, finishCallback);
    }

    /** 再接続カウンタ */
    private static int retryCount = 0;

    /**
     * 再接続チェック
     * @param context Context
     * @param error Error
     * @return 再接続必要ならtrue
     */
    public static boolean retryCheck(Context context, Exception error) {
        if (error instanceof DConnectHelper.DConnectInvalidResultException) {
            // "clientId was not found"の場合はclientIdを消して再接続
            if (((DConnectHelper.DConnectInvalidResultException) error).errorCode == 15) {
                if (retryCount++ > 3) {
                    retryCount = 0;
                    return false;
                }
                SettingData setting = SettingData.getInstance(context);
                setting.accessToken = null;
                setting.clientId = null;
                setting.save();
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    //endregion
    //---------------------------------------------------------------------------------------
}
