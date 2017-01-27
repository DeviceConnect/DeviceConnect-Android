/*
 Utils.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.app.simplebot.utils;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.WindowManager;
import android.widget.EditText;

import org.deviceconnect.android.app.simplebot.R;
import org.deviceconnect.android.app.simplebot.data.SettingData;
import org.deviceconnect.message.DConnectMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import au.com.bytecode.opencsv.CSVReader;

/**
 * ユーティリティクラス
 */
public class Utils {

    //---------------------------------------------------------------------------------------
    //region Etc.

    /**
     * 画面遷移
     * @param fragment Fragment
     * @param manager FragmentManager
     */
    public static void transition(Fragment fragment, FragmentManager manager, boolean backStack) {
        FragmentTransaction transaction = manager.beginTransaction();
        String name = fragment.getClass().getName();
        transaction.replace(R.id.container, fragment, name);
        if (backStack){
            transaction.addToBackStack(name);
        }
        transaction.commit();
    }

    /**
     * JsonからMapへ変換する
     * @param json Json
     * @return Map
     */
    public static Map<String, Object> jsonToMap(String json) {
        if (json == null) {
            return null;
        }
        try {
            JSONObject jsonObj = new JSONObject(json);
            Iterator<String> keys = jsonObj.keys();
            // 今回は文字列限定
            Map<String, Object> params = new HashMap<>();
            while (keys.hasNext()) {
                String key = keys.next();
                String val = jsonObj.getString(key);
                params.put(key, val);
            }
            return params;
        } catch (JSONException e) {
            return null;
        }
    }

    /**
     * Scopeのチェック
     * @param context Context
     * @param scopes Scope
     * @param callback コールバック
     */
    public static void checkScopes(Context context, List<String> scopes, final DConnectHelper.FinishCallback<Boolean> callback) {
        // scopeチェック
        final SettingData settings = SettingData.getInstance(context);
        if (scopes == null || settings.scopes.containsAll(scopes)) {
            if (callback != null) {
                callback.onFinish(true, null);
            }
        } else {
            // 認証されていないscopeがあった
            final Set<String> currentScopes = new HashSet<>(settings.scopes);
            settings.scopes.addAll(scopes);
            // scopeを追加して再認証
            settings.accessToken = null;
            Utils.connect(context, new DConnectHelper.FinishCallback<DConnectHelper.AuthInfo>() {
                @Override
                public void onFinish(DConnectHelper.AuthInfo authInfo, Exception error) {
                    if (error == null) {
                        if (callback != null) {
                            callback.onFinish(false, null);
                        }
                    } else {
                        // scopeを戻す
                        settings.scopes = currentScopes;
                        settings.save();
                        if (callback != null) {
                            callback.onFinish(false, error);
                        }
                    }
                }
            });
        }
    }

    /**
     * 文字コードがUTF8かを判別する
     * @param file File
     * @return UTF8でtrue
     * @throws IOException エラー
     */
    public static boolean checkUTF8(File file) throws IOException {
        InputStream input = new FileInputStream(file);
        BufferedInputStream bstream = new BufferedInputStream(input);
        byte[] buff = new byte[255];
        bstream.read(buff);
        try {
            // UTF8で文字列にしてからByte配列に変換したものと元データを比較
            byte[] tmp = new String(buff, "UTF8").getBytes("UTF8");
            return Arrays.equals(tmp, buff);
        }
        catch(UnsupportedEncodingException e) {
            return false;
        }
    }

    /**
     * CSVファイルを読み込む
     * @param filepath ファイルパス
     * @return Stringのリスト
     */
    public static List<String[]> readCSV(String filepath) {
        File csvfile = new File(filepath);
        if (csvfile.exists()){
            try {
                // 文字コード判別
                String encode = "UTF8";
                if (!Utils.checkUTF8(csvfile)) {
                    encode = "ms932";
                }
                // CSV読み込み
                InputStream stream = new FileInputStream(csvfile);
                InputStreamReader reader = new InputStreamReader(stream, encode);
                BufferedReader buffer = new BufferedReader(reader);
                CSVReader csvReader = new CSVReader(buffer, ',', '"', 0);
                // 各行読み込み
                return csvReader.readAll();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    //endregion
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
     * エラーダイアログ表示
     * @param context Context
     * @param e Exception
     */
    public static void showErrorDialog(Context context, Exception e) {
        String msg;
        if (e != null) {
            if (e instanceof DConnectHelper.DConnectHelperException) {
                if (e.getClass().equals(DConnectHelper.DConnectInvalidResultException.class)) {
                    msg = context.getString(R.string.err_server_res);
                } else {
                    DConnectMessage.ErrorCode code = DConnectMessage.ErrorCode.getInstance(((DConnectHelper.DConnectHelperException) e).errorCode);
                    msg = code.toString();
                }
            } else {
                msg = e.toString();
            }
        } else {
            msg = context.getString(R.string.err_occurred);
        }
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.err))
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .show();
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
     * 接続処理を行う.
     *
     * @param context context
     * @param callback 終了コールバック
     */
    public static void connect(final Context context, final DConnectHelper.FinishCallback<DConnectHelper.AuthInfo> callback) {
        String appName = context.getString(R.string.app_name);
        final SettingData setting = SettingData.getInstance(context);
        String scopes[] = setting.getScopes();

        // 接続先設定
        DConnectHelper.INSTANCE.setHostInfo(
                setting.ssl,
                setting.host,
                setting.port
        );

        if (setting.accessToken == null) {
            // 新規接続
            // 認証
            DConnectHelper.INSTANCE.auth(appName, scopes, new DConnectHelper.FinishCallback<DConnectHelper.AuthInfo>() {
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
     * サービス一覧を取得する.
     *
     * @param context context
     * @param callback 終了コールバック
     */
    public static void fetchServices(final Context context, final DConnectHelper.FinishCallback<List<DConnectHelper.ServiceInfo>> callback) {
        DConnectHelper.FinishCallback<DConnectHelper.AuthInfo> finishCallback = new DConnectHelper.FinishCallback<DConnectHelper.AuthInfo>() {
            @Override
            public void onFinish(DConnectHelper.AuthInfo authInfo, Exception error) {
                if (error == null) {
                    // サービス検索
                    DConnectHelper.INSTANCE.serviceDiscovery(new DConnectHelper.FinishCallback<List<DConnectHelper.ServiceInfo>>() {
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
     * サービス情報を取得する.
     *
     * @param context context
     * @param callback 終了コールバック
     */
    public static void fetchServiceInformation(final Context context, final String serviceId, final DConnectHelper.FinishCallback<Map<String, List<DConnectHelper.APIInfo>>> callback) {
        DConnectHelper.FinishCallback<DConnectHelper.AuthInfo> finishCallback = new DConnectHelper.FinishCallback<DConnectHelper.AuthInfo>() {
            @Override
            public void onFinish(DConnectHelper.AuthInfo authInfo, Exception error) {
                if (error == null) {
                    // サービス検索
                    DConnectHelper.INSTANCE.serviceInformation(serviceId, new DConnectHelper.FinishCallback<Map<String, List<DConnectHelper.APIInfo>>>() {
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
     * Device Connect Managerの生存確認を行う.
     * @param context コンテキスト
     * @param callback 生存確認結果を通知するコールバック
     */
    public static void availability(final Context context, final DConnectHelper.FinishCallback<Void> callback) {
        DConnectHelper.INSTANCE.availability(new DConnectHelper.FinishCallback<Void>() {
            @Override
            public void onFinish(final Void object, final Exception error) {
                callback.onFinish(null, error);
            }
        });
    }

    /**
     * イベントを登録する.
     * @param context context
     * @param callback 終了コールバック
     */
    public static void registerEvent(final Context context, final DConnectHelper.FinishCallback<Void> callback) {
        DConnectHelper.FinishCallback<DConnectHelper.AuthInfo> finishCallback = new DConnectHelper.FinishCallback<DConnectHelper.AuthInfo>() {
            @Override
            public void onFinish(final DConnectHelper.AuthInfo authInfo, Exception error) {
                if (error == null) {
                    DConnectHelper.INSTANCE.openWebSocket();
                    callback.onFinish(null, null);
                } else {
                    callback.onFinish(null, error);
                }
            }
        };
        Utils.connect(context, finishCallback);
    }

    /**
     * イベントを解除する.
     * @param context context
     */
    public static void unregisterEvent(Context context) {
        DConnectHelper.FinishCallback<DConnectHelper.AuthInfo> finishCallback = new DConnectHelper.FinishCallback<DConnectHelper.AuthInfo>() {
            @Override
            public void onFinish(final DConnectHelper.AuthInfo authInfo, Exception error) {
                if (error == null) {
                    DConnectHelper.INSTANCE.closeWebSocket();
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
                    DConnectHelper.INSTANCE.sendMessage(setting.serviceId, channel, text, resource, new DConnectHelper.FinishCallback<Void>() {
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
    public static void sendRequest(final Context context, final String method, final String path, final String serviceId, final Map<String, Object> params, final DConnectHelper.FinishCallback<Map<String, Object>> callback) {
        final DConnectHelper.FinishCallback<DConnectHelper.AuthInfo> finishCallback = new DConnectHelper.FinishCallback<DConnectHelper.AuthInfo>() {
            @Override
            public void onFinish(DConnectHelper.AuthInfo authInfo, Exception error) {
                if (error == null) {
                    // リクエスト送信
                    DConnectHelper.INSTANCE.sendRequest(method, path, serviceId, params, new DConnectHelper.FinishCallback<Map<String, Object>>() {
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
