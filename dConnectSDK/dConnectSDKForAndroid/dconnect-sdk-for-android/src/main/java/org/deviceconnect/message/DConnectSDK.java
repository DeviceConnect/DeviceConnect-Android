/*
 DConnectSDK.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.message;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.deviceconnect.message.entity.Entity;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.AvailabilityProfileConstants;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;
import org.deviceconnect.profile.ServiceInformationProfileConstants;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>
 * Device Connect Managerへのアクセスを行うクラス.
 * </p>
 * <span style="margin:0;padding:2px;background:#029EBC;color:#EBF7FA;line-height:140%;font-weight:bold;">サンプルコード</span>
 * <pre>
 * // sdkは、使い回すこと。
 * final DConnectSDK sdk = DConnectSDKFactory.create(context, DConnectSDKFactory.Type.HTTP);
 *
 * String[] scopes = {
 *      "serviceDiscovery",
 *      "serviceInformation",
 *      "battery"
 * };
 *
 * DConnectResponseMessage response = sdk.authorization("SampleApp", scopes, new OnAuthorizationListener() {
 *     <code>@</code>Override
 *     public void onResponse(String clientId, String accessToken) {
 *          // Local OAuthの認証に成功
 *          // 必要に応じて、アクセストークンはファイルなどに保存して使いまわすこと。
 *          // 取得したアクセストークンをSDKに設定
 *         sdk.setAccessToken(accessToken);
 *     }
 *     <code>@</code>Override
 *     public void onError(int errorCode, String errorMessage) {
 *          // Local OAuthの認証に失敗
 *     }
 * });
 *
 * // 省略・・・
 *
 * DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
 * builder.setProfile("battery");
 * builder.setServiceId(serviceId);
 * sdk.get(builder.build(), new OnResponseListener() {
 *     <code>@</code>Override
 *     public void onResponse(DConnectResponseMessage response) {
 *         if (response.getResult() == DConnectMessage.RESULT_OK) {
 *             float level = response.getFloat("level");
 *         } else {
 *             // エラー
 *         }
 *     }
 * });
 * </pre>
 * @author NTT DOCOMO, INC.
 */
public abstract class DConnectSDK {
    /**
     * Device Connect Manager起動確認用ActivityへのComponentName.
     */
    private static final ComponentName MANAGER_LAUNCH_ACTIVITY = new ComponentName("org.deviceconnect.android.manager",
            "org.deviceconnect.android.manager.DConnectLaunchActivity");

    /**
     * メソッド.
     */
    enum Method {
        /**
         * GETメソッド.
         */
        GET("GET"),

        /**
         * PUTメソッド.
         */
        PUT("PUT"),

        /**
         * POSTメソッド.
         */
        POST("POST"),

        /**
         * DELETEメソッド.
         */
        DELETE("DELETE");

        String mValue;
        Method(final String value) {
            mValue = value;
        }
        String getValue() {
            return mValue;
        }
    }

    /**
     * Device Connect Managerへのホスト名.
     */
    private String mHost = "localhost";

    /**
     * Device Connect Managerのポート番号.
     */
    private int mPort = 4035;

    /**
     * アプリケーションのオリジン.
     */
    private String mOrigin;

    /**
     * SSL使用フラグ.
     * <p>
     *     trueの場合はSSLを使用する。
     *     falseの場合にはSSLを使用しない。
     * </p>
     */
    private boolean mSSL;

    /**
     * アクセストークン.
     */
    private String mAccessToken;

    /**
     * 通信を行うスレッド.
     */
    private ExecutorService mExecutorService = Executors.newFixedThreadPool(4);

    DConnectSDK() {}

    /**
     * Device Connect Managerのホスト名を取得する.
     * <p>
     * デフォルトではlocalhostが設定してあります。
     * </p>
     * @return Device Connect Managerのホスト名
     */
    public String getHost() {
        return mHost;
    }

    /**
     * Device Connect Managerのホスト名を設定する.
     * <p>
     * デフォルトではlocalhostが設定してあります。<br>
     * 別のホストにあるDevice Connect Managerにアクセスする場合には、設定をしなおしてください。<br>
     * </p>
     * @param host Device Connect Managerのホスト名
     */
    public void setHost(final String host) {
        if (host == null) {
            throw new NullPointerException("host is null.");
        }
        if (host.isEmpty()) {
            throw new IllegalArgumentException("host is empty.");
        }
        mHost = host;
    }

    /**
     * Device Connect Managerのポート番号を取得する.
     * @return Device Connect Managerのポート番号
     */
    public int getPort() {
        return mPort;
    }

    /**
     * Device Connect Managerのポート番号を設定する.
     * <p>
     * デフォルトでは、4035が設定してあります。
     * </p>
     * @param port Device Connect Managerのポート番号
     */
    public void setPort(final int port) {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("port is invalid. port=" + port);
        }
        mPort = port;
    }

    /**
     * アプリケーションのOriginを取得する.
     * @return アプリケーションのOrigin
     */
    public String getOrigin() {
        return mOrigin;
    }

    /**
     * アプリケーションのOriginを設定する.
     * <p>
     * アプリケーションのOriginを変更したい場合には、ここで変更します。<br>
     * デフォルトでは、アプリケーションのパッケージ名が設定してあります。
     * </p>
     * @param origin アプリケーションのOrigin
     */
    public void setOrigin(final String origin) {
        if (origin == null) {
            throw new NullPointerException("origin is null.");
        }
        if (origin.isEmpty()) {
            throw new IllegalArgumentException("origin is empty.");
        }
        mOrigin = origin;
    }

    /**
     * SSL使用フラグを取得する.
     *
     * @return trueの場合はSSLを使用する、それ以外の場合は使用しない。
     */
    public boolean isSSL() {
        return mSSL;
    }

    /**
     * SSL使用フラグを設定する.
     *
     * @param SSL trueの場合はSSLを使用する、それ以外の場合は使用しない。
     */
    public void setSSL(final boolean SSL) {
        mSSL = SSL;
    }

    /**
     * アクセストークンを取得する.
     * <p>
     *     設定されていない場合には{@code null}を返却します。<br>
     *     デフォルトでは、何も設定されていないので{@code null}を返却します。
     * </p>
     * @return アクセストークン
     */
    public String getAccessToken() {
        return mAccessToken;
    }

    /**
     * アクセストークンを設定する.
     * <p>
     * アクセストークンを取得する方法は、{@link #authorization(String, String[])}を参照してください。
     * </p>
     * @param accessToken アクセストークン
     */
    public void setAccessToken(final String accessToken) {
        if (accessToken == null) {
            throw new NullPointerException("accessToken is null.");
        }
        if (accessToken.isEmpty()) {
            throw new IllegalArgumentException("accessToken is empty.");
        }
        mAccessToken = accessToken;
    }

    /**
     * イベント受信用のWebSocketを Device Connect Managerへ接続する.
     * <p>
     * すでに接続されている場合には、無視します。<br>
     * この関数でWebSocketを開いたあとは、必ず{@link #disconnectWebSocket()}を呼び出して、WebSocketを切断してください。
     * </p>
     * <span style="margin:0;padding:2px;background:#029EBC;color:#EBF7FA;line-height:140%;font-weight:bold;">サンプルコード</span>
     * <pre>
     * DConnectSDK sdk = DConnectSDKFactory.create(context, DConnectSDKFactory.Type.HTTP);
     * sdk.connectWebSocket(new OnWebSocketListener() {
     *     <code>@</code>Override
     *     public void onOpen() {
     *     }
     *
     *     <code>@</code>Override
     *     void onClose() {
     *     }
     *
     *     <code>@</code>Override
     *     void onError(Exception e) {
     *     }
     * });
     * </pre>
     * @param listener WebSocket状態通知リスナー
     */
    public abstract void connectWebSocket(final OnWebSocketListener listener);

    /**
     * イベント受信用のWebSocketを切断する.
     * <p>
     * WebSocketが接続されていない場合には、処理を無視します。
     * </p>
     */
    public abstract void disconnectWebSocket();

    /**
     * イベントを登録する.
     * <p>
     * イベントの登録成功・失敗やイベントメッセージは、第２引数のlistenerに通知されます。
     * </p>
     * <span style="margin:0;padding:2px;background:#029EBC;color:#EBF7FA;line-height:140%;font-weight:bold;">サンプルコード</span>
     * <pre>
     * DConnectSDK sdk = DConnectSDKFactory.create(context, DConnectSDKFactory.Type.HTTP);
     * DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
     * builder.setProfile("battery");
     * builder.setServiceId("serviceId");
     * sdk.addEventListener(builder.build(), new OnEventListener() {
     *      <code>@</code>Override
     *      public void onResponse(DConnectResponseMessage response) {
     *          if (response.getResult() == DConnectMessage.RESULT_OK) {
     *              // イベント登録成功
     *          } else {
     *              // イベント登録失敗
     *          }
     *      }
     *      <code>@</code>Override
     *      public void onMessage(DConnectEventMessage message) {
     *          // イベント
     *      }
     * });
     * </pre>
     * @param uri 登録するイベントへのURI
     * @param listener イベント通知リスナー
     */
    public void addEventListener(final String uri, final OnEventListener listener) {
        addEventListener(Uri.parse(uri), listener);
    }

    /**
     * イベントを登録する.
     * @param uri 登録するイベントへのURI
     * @param listener イベント通知リスナー
     */
    public abstract void addEventListener(final Uri uri, final OnEventListener listener);

    /**
     * イベントを解除する.
     * @param uri 削除するイベントへのURI
     */
    public void removeEventListener(final String uri) {
        removeEventListener(Uri.parse(uri));
    }

    /**
     * イベントを削除する.
     * @param uri 削除するイベントへのURI
     */
    public abstract void removeEventListener(final Uri uri);

    /**
     * Device Connect Managerを起動する.
     * <p>
     * Device Connect Managerを起動するために一瞬透明なActivityが起動するので、
     * Activityが一時停止されることに注意する必要があります。
     * </p>
     * <span style="margin:0;padding:2px;background:#029EBC;color:#EBF7FA;line-height:140%;font-weight:bold;">サンプルコード</span>
     * <pre>
     * DConnectSDK sdk = DConnectSDKFactory.create(context, DConnectSDKFactory.Type.HTTP);
     * DConnectResponseMessage response = sdk.availability();
     * if (response.getResult() == DConnectMessage.RESULT_ERROR) {
     *     sdk.startManager(context);
     * }
     * </pre>
     * @param context コンテキスト
     */
    public void startManager(final Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setComponent(MANAGER_LAUNCH_ACTIVITY);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse("gotapi://start/server"));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    /**
     * Device Connect Manager起動確認用のActivityを起動する.
     * @param context コンテキスト
     */
    public void startManagerWithActivity(final Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setComponent(MANAGER_LAUNCH_ACTIVITY);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse("gotapi://start/activity"));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    /**
     * Device Connect Managerを停止する.
     * <p>
     * Device Connect Managerを停止するために一瞬透明なActivityが起動するので、
     * Activityが一時停止されることに注意すること。
     * </p>
     * @param context コンテキスト
     */
    public void stopManager(final Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setComponent(MANAGER_LAUNCH_ACTIVITY);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse("gotapi://stop/server"));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    /**
     * Device Connect Manager停止確認用のActivityを起動する.
     * @param context コンテキスト
     */
    public void stopManagerWithActivity(final Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setComponent(MANAGER_LAUNCH_ACTIVITY);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse("gotapi://stop/activity"));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    /**
     * Device Connect Managerとの通信を行う.
     * <p>
     * 同期的に処理を行い、Device Connect Managerとの通信結果を返却する。<br>
     * レスポンスにはnullを返さないようにすること。
     * </p>
     * @param method メソッド
     * @param uri アクセス先のURI
     * @param headers リクエストに追加するヘッダー
     * @param body リクエストに追加するボディデータ
     * @return レスポンス
     */
    protected abstract DConnectResponseMessage sendRequest(final Method method, final Uri uri, final Map<String, String> headers, final Entity body);

    /**
     * URIBuilderを生成する.
     * <br>
     * <span style="margin:0;padding:2px;background:#029EBC;color:#EBF7FA;line-height:140%;font-weight:bold;">サンプルコード</span>
     * <pre>
     * DConnectSDK sdk = DConnectSDKFactory.create(context, DConnectSDKFactory.Type.HTTP);
     * DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
     * builder.setProfile("battery");
     * builder.setServiceId("serviceId");
     * </pre>
     * @return URIBuilderのインスタンス
     */
    public URIBuilder createURIBuilder() {
        return new URIBuilder();
    }

    /**
     * GETメソッドで指定したURIにアクセスし、レスポンスを取得する.
     * <p>
     * Device Connect Managerに同期的にアクセスを行う為にUIスレッドなどから呼び出すとエラーになります。<br>
     * 非同期的に呼び出したい場合には、{@link #get(String, OnResponseListener)}を使用してください。
     * </p>
     * <span style="margin:0;padding:2px;background:#029EBC;color:#EBF7FA;line-height:140%;font-weight:bold;">サンプルコード</span>
     * <pre>
     * DConnectSDK sdk = DConnectSDKFactory.create(context, DConnectSDKFactory.Type.HTTP);
     * DConnectResponseMessage response = sdk.get("http://localhost:4035/gotapi/availability");
     * if (response.getResult() == DConnectMessage.RESULT_OK) {
     *     // Device Connect Manager起動中
     * }
     * </pre>
     * @param uri アクセス先のURI
     * @return レスポンス
     */
    public DConnectResponseMessage get(final String uri) {
        if (uri == null) {
            throw new NullPointerException("uri is null.");
        }
        return get(Uri.parse(uri));
    }

    /**
     * GETメソッドで指定したURIにアクセスし、レスポンスを取得する.
     * <p>
     * Device Connect Managerに同期的にアクセスを行う為にUIスレッドなどから呼び出すとエラーになります。<br>
     * 非同期的に呼び出したい場合には、{@link #get(String, OnResponseListener)}を使用してください。
     * </p>
     * <span style="margin:0;padding:2px;background:#029EBC;color:#EBF7FA;line-height:140%;font-weight:bold;">サンプルコード</span>
     * <pre>
     * DConnectSDK sdk = DConnectSDKFactory.create(context, DConnectSDKFactory.Type.HTTP);
     *
     * DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
     * builder.setProfile("availability");
     *
     * DConnectResponseMessage response = sdk.get(builder.build());
     * if (response.getResult() == DConnectMessage.RESULT_OK) {
     *     // Device Connect Manager起動中
     * }
     * </pre>
     * @param uri アクセス先のURI
     * @return レスポンス
     */
    public DConnectResponseMessage get(final Uri uri) {
        if (uri == null) {
            throw new NullPointerException("uri is null.");
        }
        return sendRequest(Method.GET, uri, null, null);
    }

    /**
     * 非同期にGETメソッドで指定したURIにアクセスし、レスポンスをリスナーに通知する.
     * @param uri アクセス先のURI
     * @param listener レスポンスを通知するリスナー
     */
    public void get(final String uri, final OnResponseListener listener) {
        if (uri == null) {
            throw new NullPointerException("uri is null.");
        }
        get(Uri.parse(uri), listener);
    }

    /**
     * 非同期にGETメソッドで指定したURIにアクセスし、レスポンスをリスナーに通知する.
     * @param uri アクセス先のURI
     * @param listener レスポンスを通知するリスナー
     */
    public void get(final Uri uri, final OnResponseListener listener) {
        if (uri == null) {
            throw new NullPointerException("uri is null.");
        }
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                DConnectResponseMessage response = get(uri);
                if (listener != null) {
                    listener.onResponse(response);
                }
            }
        });
    }

    /**
     * PUTメソッドで指定したURIにアクセスし、レスポンスを取得する.
     * @param uri アクセス先のURI
     * @param data 送信するボディデータ
     * @return レスポンス
     */
    public DConnectResponseMessage put(final String uri, final Entity data) {
        if (uri == null) {
            throw new NullPointerException("uri is null.");
        }
        return put(Uri.parse(uri), data);
    }

    /**
     * PUTメソッドで指定したURIにアクセスし、レスポンスを取得する.
     * @param uri アクセス先のURI
     * @param data 送信するボディデータ
     * @return レスポンス
     */
    public DConnectResponseMessage put(final Uri uri, final Entity data) {
        if (uri == null) {
            throw new NullPointerException("uri is null.");
        }
        return sendRequest(Method.PUT, uri, null, data);
    }

    /**
     * 非同期にPUTメソッドで指定したURIにアクセスし、レスポンスをリスナーに通知する.
     * @param uri アクセス先のURI
     * @param data 送信するボディデータ
     * @param listener レスポンスを通知するリスナー
     */
    public void put(final String uri, final Entity data, final OnResponseListener listener) {
        if (uri == null) {
            throw new NullPointerException("uri is null.");
        }
        put(Uri.parse(uri), data, listener);
    }

    /**
     * 非同期にPUTメソッドで指定したURIにアクセスし、レスポンスをリスナーに通知する.
     * @param uri アクセス先のURI
     * @param data 送信するボディデータ
     * @param listener レスポンスを通知するリスナー
     */
    public void put(final Uri uri, final Entity data, final OnResponseListener listener) {
        if (uri == null) {
            throw new NullPointerException("uri is null.");
        }

        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                DConnectResponseMessage result = put(uri, data);
                if (listener != null) {
                    listener.onResponse(result);
                }
            }
        });
    }

    /**
     * POSTメソッドで指定したURIにアクセスし、レスポンスを取得する.
     * <p>
     * 引数のdataに{@link org.deviceconnect.message.entity.MultipartEntity MultipartEntity}を渡した場合には、ボディにマルチパートを格納してDevice Connect Managerに送信する。<br>
     * {@link org.deviceconnect.message.entity.MultipartEntity MultipartEntity}には、{@link org.deviceconnect.message.entity.BinaryEntity BinaryEntity}、
     * {@link org.deviceconnect.message.entity.FileEntity FileEntity}、{@link org.deviceconnect.message.entity.StringEntity StringEntity}をマルチパートに格納することができます。
     * </p>
     * <p>
     * 引数のdataに{@link org.deviceconnect.message.entity.StringEntity StringEntity}を渡した場合には、ボディに文字列を入れてDevice Connect Managerに送信する。
     * </p>
     * <p>
     * 引数のdataに{@link org.deviceconnect.message.entity.BinaryEntity BinaryEntity}を渡した場合には、ボディにバイナリを入れてDevice Connect Managerに送信する。
     * </p>
     * <p>
     * dataに{@code null}が指定された場合には、データは何もつけずにDevice Connect Managerにアクセスする。
     * </p>
     * @param uri アクセス先のURI
     * @param data 送信するボディデータ
     * @return レスポンス
     */
    public DConnectResponseMessage post(final String uri, final Entity data) {
        if (uri == null) {
            throw new NullPointerException("uri is null.");
        }
        return post(Uri.parse(uri), data);
    }

    /**
     * POSTメソッドで指定したURIにアクセスし、レスポンスを取得する.
     * <p>
     * 引数のdataに{@link org.deviceconnect.message.entity.MultipartEntity MultipartEntity}を渡した場合には、ボディにマルチパートを格納してDevice Connect Managerに送信する。<br>
     * {@link org.deviceconnect.message.entity.MultipartEntity MultipartEntity}には、{@link org.deviceconnect.message.entity.BinaryEntity BinaryEntity}、
     * {@link org.deviceconnect.message.entity.FileEntity FileEntity}、{@link org.deviceconnect.message.entity.StringEntity StringEntity}をマルチパートに格納することができます。
     * </p>
     * <p>
     * 引数のdataに{@link org.deviceconnect.message.entity.StringEntity StringEntity}を渡した場合には、ボディに文字列を入れてDevice Connect Managerに送信する。
     * </p>
     * <p>
     * 引数のdataに{@link org.deviceconnect.message.entity.BinaryEntity BinaryEntity}を渡した場合には、ボディにバイナリを入れてDevice Connect Managerに送信する。
     * </p>
     * <p>
     * dataに{@code null}が指定された場合には、データは何もつけずにDevice Connect Managerにアクセスする。
     * </p>
     * <span style="margin:0;padding:2px;background:#029EBC;color:#EBF7FA;line-height:140%;font-weight:bold;">サンプルコード1</span>
     * <pre>
     * MultipartEntity dataMap = new MultipartEntity();
     * dataMap.add("mode", "scales");
     * dataMap.add("data", new FileEntity(new File("/data/data/0/org.mycompany.sample/files/sample.png")));
     *
     * DConnectSDK sdk = DConnectSDKFactory.create(context, DConnectSDKFactory.Type.HTTP);
     * DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
     * builder.setProfile("canvas");
     * builder.setAttribute("drawImage");
     * builder.setServiceId(hostServiceId);
     *
     * DConnectResponseMessage response = sdk.post(builder.build(), dataMap);
     * if (response.getResult() == DConnectMessage.RESULT_OK) {
     *     // 成功時の処理
     * }
     * </pre>
     * <span style="margin:0;padding:2px;background:#029EBC;color:#EBF7FA;line-height:140%;font-weight:bold;">サンプルコード2</span>
     * <pre>
     * DConnectSDK sdk = DConnectSDKFactory.create(context, DConnectSDKFactory.Type.HTTP);
     * DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
     * builder.setProfile("canvas");
     * builder.setAttribute("drawImage");
     * builder.setServiceId(hostServiceId);
     *
     * DConnectResponseMessage response = sdk.post(builder.build(), new StringEntity("テストデータ"));
     * if (response.getResult() == DConnectMessage.RESULT_OK) {
     *     // 成功時の処理
     * }
     * </pre>
     * @param uri アクセス先のURI
     * @param data 送信するボディデータ
     * @return レスポンス
     */
    public DConnectResponseMessage post(final Uri uri, final Entity data) {
        if (uri == null) {
            throw new NullPointerException("uri is null.");
        }
        return sendRequest(Method.POST, uri, null, data);
    }

    /**
     * 非同期にPOSTメソッドで指定したURIにアクセスし、レスポンスをリスナーに通知する.
     *
     * @param uri アクセス先のURI
     * @param data 送信するボディデータ
     * @param listener レスポンスを通知するリスナー
     */
    public void post(final String uri, final Entity data, final OnResponseListener listener) {
        if (uri == null) {
            throw new NullPointerException("uri is null.");
        }
        post(Uri.parse(uri), data, listener);
    }

    /**
     * 非同期にPOSTメソッドで指定したURIにアクセスし、レスポンスをリスナーに通知する.
     *
     * @param uri アクセス先のURI
     * @param data 送信するボディデータ
     * @param listener レスポンスを通知するリスナー
     */
    public void post(final Uri uri, final Entity data, final OnResponseListener listener) {
        if (uri == null) {
            throw new NullPointerException("uri is null.");
        }
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                DConnectResponseMessage result = post(uri, data);
                if (listener != null) {
                    listener.onResponse(result);
                }
            }
        });
    }

    /**
     * DELETEメソッドで指定したURIにアクセスし、レスポンスを取得する.
     * @param uri アクセス先のURI
     * @return レスポンス
     */
    public DConnectResponseMessage delete(final String uri) {
        if (uri == null) {
            throw new NullPointerException("uri is null.");
        }
        return delete(Uri.parse(uri));
    }

    /**
     * DELETEメソッドで指定したURIにアクセスし、レスポンスを取得する.
     * @param uri アクセス先のURI
     * @return レスポンス
     */
    public DConnectResponseMessage delete(final Uri uri) {
        if (uri == null) {
            throw new NullPointerException("uri is null.");
        }
        return sendRequest(Method.DELETE, uri, null, null);
    }

    /**
     * 非同期にDELETEメソッドで指定したURIにアクセスし、レスポンスをリスナーに通知する.
     * @param uri アクセス先のURI
     * @param listener レスポンスを通知するリスナー
     */
    public void delete(final String uri, final OnResponseListener listener) {
        if (uri == null) {
            throw new NullPointerException("uri is null.");
        }
        delete(Uri.parse(uri), listener);
    }

    /**
     * 非同期にDELETEメソッドで指定したURIにアクセスし、レスポンスをリスナーに通知する.
     * @param uri アクセス先のURI
     * @param listener レスポンスを通知するリスナー
     */
    public void delete(final Uri uri, final OnResponseListener listener) {
        if (uri == null) {
            throw new NullPointerException("uri is null.");
        }
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                DConnectResponseMessage result = delete(uri);
                if (listener != null) {
                    listener.onResponse(result);
                }
            }
        });
    }

    /**
     * Availabilityプロファイルにアクセスし、レスポンスを取得する.
     * <p>
     * この関数のレスポンスからDevice Connect Managerの有効・無効を確認します。<br>
     * この関数の中で、Device Connect Managerへの通信処理が発生しますので、UIスレッドから呼び出すことはできません。
     * </p>
     * <span style="margin:0;padding:2px;background:#029EBC;color:#EBF7FA;line-height:140%;font-weight:bold;">サンプルコード</span>
     * <pre>
     * DConnectSDK sdk = DConnectSDKFactory.create(context, DConnectSDKFactory.Type.HTTP);
     *
     * DConnectResponseMessage response = sdk.availability();
     * if (response.getResult() == DConnectMessage.RESULT_OK) {
     *     // Device Connect Managerが有効
     * } else {
     *     // Device Connect Managerが無効
     * }
     * </pre>
     * @return レスポンス
     */
    public DConnectResponseMessage availability() {
        URIBuilder builder = new URIBuilder();
        builder.setProfile(AvailabilityProfileConstants.PROFILE_NAME);
        return get(builder.build());
    }

    /**
     * 非同期にAvailabilityプロファイルにアクセスし、レスポンスをリスナーに通知する.
     * <p>
     * この関数のレスポンスからDevice Connect Managerの有効・無効を確認します。<br>
     * </p>
     * <span style="margin:0;padding:2px;background:#029EBC;color:#EBF7FA;line-height:140%;font-weight:bold;">サンプルコード</span>
     * <pre>
     * DConnectSDK sdk = DConnectSDKFactory.create(context, DConnectSDKFactory.Type.HTTP);
     *
     * DConnectResponseMessage response = sdk.availability(new OnResponseListener() {
     *     <code>@</code>Override
     *     public void onResponse(DConnectResponseMessage response) {
     *          if (response.getResult() == DConnectMessage.RESULT_OK) {
     *              // Device Connect Managerが有効
     *          } else {
     *              // Device Connect Managerが無効
     *          }
     *     }
     * });
     * </pre>
     * @param listener 結果を通知するリスナー
     */
    public void availability(final OnResponseListener listener) {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                DConnectResponseMessage result = availability();
                if (listener != null) {
                    listener.onResponse(result);
                }
            }
        });
    }

    /**
     * 同期的にLocal OAuth処理を行う。
     * <p>
     * ユーザに使用許可ダイアログを表示して確認を行います。<br>
     * この関数の中で、Device Connect Managerへの通信処理が発生しますので、UIスレッドから呼び出すことはできません。
     * </p>
     * <span style="margin:0;padding:2px;background:#029EBC;color:#EBF7FA;line-height:140%;font-weight:bold;">サンプルコード</span>
     * <pre>
     * DConnectSDK sdk = DConnectSDKFactory.create(context, DConnectSDKFactory.Type.HTTP);
     *
     * String[] scopes = {
     *      "serviceDiscovery",
     *      "serviceInformation",
     *      "battery"
     * };
     *
     * DConnectResponseMessage response = sdk.authorization("SampleApp", scopes);
     * if (response.getResult() == DConnectMessage.RESULT_OK) {
     *     // Local OAuthの認証に成功
     *     String accessToken = response.getString("accessToken");
     *     sdk.setAccessToken(accessToken);
     * } else {
     *     // Local OAuthの認証に失敗
     * }
     * </pre>
     * @param appName アプリケーション名
     * @param scopes アクセスするプロファイル一覧
     * @return レスポンス
     */
    public DConnectResponseMessage authorization(final String appName, final String[] scopes) {
        DConnectResponseMessage response = createCreateClient();
        if (response.getResult() == DConnectMessage.RESULT_ERROR) {
            return response;
        }
        String clientId = response.getString(DConnectMessage.EXTRA_CLIENT_ID);
        return createAccessToken(clientId, appName, scopes);
    }

    /**
     * 非同期にLocal OAuth処理を行い、リスナーに結果を通知する.
     * <p>
     * ユーザに使用許可ダイアログを表示して確認を行います。<br>
     * </p>
     * <span style="margin:0;padding:2px;background:#029EBC;color:#EBF7FA;line-height:140%;font-weight:bold;">サンプルコード</span>
     * <pre>
     * final DConnectSDK sdk = DConnectSDKFactory.create(context, DConnectSDKFactory.Type.HTTP);
     *
     * String[] scopes = {
     *      "serviceDiscovery",
     *      "serviceInformation",
     *      "battery"
     * };
     *
     * DConnectResponseMessage response = sdk.authorization("SampleApp", scopes, new OnAuthorizationListener() {
     *     <code>@</code>Override
     *     public void onResponse(String clientId, String accessToken) {
     *          // Local OAuthの認証に成功
     *         sdk.setAccessToken(accessToken);
     *     }
     *     <code>@</code>Override
     *     public void onError(int errorCode, String errorMessage) {
     *          // Local OAuthの認証に失敗
     *     }
     * });
     * </pre>
     * @param appName アプリケーション名
     * @param scopes アクセスするプロファイル一覧
     * @param listener Local OAuthのレスポンスを通知するリスナー
     */
    public void authorization(final String appName, final String[] scopes, final OnAuthorizationListener listener) {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                DConnectResponseMessage response = createCreateClient();
                if (response.getResult() == DConnectMessage.RESULT_ERROR) {
                    if (listener != null) {
                        listener.onError(response.getErrorCode(), response.getErrorMessage());
                    }
                    return;
                }
                String clientId = response.getString(DConnectMessage.EXTRA_CLIENT_ID);
                response = createAccessToken(clientId, appName, scopes);
                if (response.getResult() == DConnectMessage.RESULT_ERROR) {
                    if (listener != null) {
                        listener.onError(response.getErrorCode(), response.getErrorMessage());
                    }
                } else {
                    String accessToken = response.getString(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN);
                    listener.onResponse(clientId, accessToken);
                }
            }
        });
    }

    /**
     * ServiceDiscoveryプロファイルにアクセスし、Device Connect Managerに接続されているサービス一覧を取得する.
     * <span style="margin:0;padding:2px;background:#029EBC;color:#EBF7FA;line-height:140%;font-weight:bold;">サンプルコード</span>
     * <pre>
     * DConnectSDK sdk = DConnectSDKFactory.create(context, DConnectSDKFactory.Type.HTTP);
     * sdk.setAccessToken("xxxxxxx");
     *
     * DConnectResponseMessage response = sdk.serviceDiscovery();
     * if (response.getResult() == DConnectMessage.RESULT_OK) {
     *     // サービス一覧の取得に成功
     *     List&lt;Object&gt; services = response.getList(ServiceDiscoveryProfileConstants.PARAM_SERVICES);
     *     for (Object obj : services) {
     *         DConnectMessage service = (DConnectMessage) obj;
     *         String serviceId = service.getString(ServiceDiscoveryProfileConstants.PARAM_ID);
     *         String name = service.getString(ServiceDiscoveryProfileConstants.PARAM_NAME);
     *         boolean online = service.getBoolean(ServiceDiscoveryProfileConstants.PARAM_ONLINE);
     *     }
     * } else {
     *     // サービス一覧の取得に失敗
     * }
     * </pre>
     * @return レスポンス
     */
    public DConnectResponseMessage serviceDiscovery() {
        URIBuilder builder = new URIBuilder();
        builder.setProfile(ServiceDiscoveryProfileConstants.PROFILE_NAME);
        return get(builder.build());
    }

    /**
     * 非同期にServiceDiscoveryプロファイルにアクセスし、Device Connect Managerに接続されているサービス一覧をリスナーに通知する.
     * <span style="margin:0;padding:2px;background:#029EBC;color:#EBF7FA;line-height:140%;font-weight:bold;">サンプルコード</span>
     * <pre>
     * DConnectSDK sdk = DConnectSDKFactory.create(context, DConnectSDKFactory.Type.HTTP);
     * sdk.setAccessToken("xxxxxxx");
     *
     * DConnectResponseMessage response = sdk.serviceDiscovery(new OnResponseListener() {
     *     <code>@</code>Override
     *     public void onResponse(DConnectResponseMessage response) {
     *          if (response.getResult() == DConnectMessage.RESULT_OK) {
     *              // サービス一覧の取得に成功
     *          } else {
     *              // サービス一覧の取得に失敗
     *          }
     *     }
     * });
     * </pre>
     * @param listener レスポンスを通知するリスナー
     */
    public void serviceDiscovery(final OnResponseListener listener) {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                DConnectResponseMessage result = serviceDiscovery();
                if (listener != null) {
                    listener.onResponse(result);
                }
            }
        });
    }

    /**
     * ServiceInformationプロファイルにアクセスし、サービスの情報を取得する.
     * @param serviceId サービスID
     * @return レスポンス
     */
    public DConnectResponseMessage getServiceInformation(final String serviceId) {
        if (serviceId == null) {
            throw new NullPointerException("serviceId is null.");
        }
        URIBuilder builder = new URIBuilder();
        builder.setProfile(ServiceInformationProfileConstants.PROFILE_NAME);
        builder.setServiceId(serviceId);
        return get(builder.build());
    }

    /**
     * 非同期にServiceInformationプロファイルにアクセスし、サービスの情報を取得する.
     * @param serviceId サービスID
     * @param listener レスポンスを通知するリスナー
     */
    public void getServiceInformation(final String serviceId, final OnResponseListener listener) {
        if (serviceId == null) {
            throw new NullPointerException("serviceId is null.");
        }
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                DConnectResponseMessage result = getServiceInformation(serviceId);
                if (listener != null) {
                    listener.onResponse(result);
                }
            }
        });
    }

    private DConnectResponseMessage createCreateClient() {
        URIBuilder builder = new URIBuilder();
        builder.setProfile(AuthorizationProfileConstants.PROFILE_NAME);
        builder.setAttribute(AuthorizationProfileConstants.ATTRIBUTE_GRANT);
        return get(builder.build());
    }

    private DConnectResponseMessage createAccessToken(final String clientId, final String appName, final String[] scopes) {
        URIBuilder builder = new URIBuilder();
        builder.setProfile(AuthorizationProfileConstants.PROFILE_NAME);
        builder.setAttribute(AuthorizationProfileConstants.ATTRIBUTE_ACCESS_TOKEN);
        builder.addParameter(AuthorizationProfileConstants.PARAM_CLIENT_ID, clientId);
        builder.addParameter(AuthorizationProfileConstants.PARAM_APPLICATION_NAME, appName);
        builder.addParameter(AuthorizationProfileConstants.PARAM_SCOPE, combineStr(scopes));
        return get(builder.build());
    }

    private String combineStr(final String[] scopes) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < scopes.length; i++) {
            if (i > 0) {
                builder.append(",");
            }
            builder.append(scopes[i].trim());
        }
        return builder.toString();
    }

    DConnectResponseMessage createErrorMessage(final int errorCode, final String errorMessage) {
        DConnectResponseMessage message = new DConnectResponseMessage(DConnectMessage.RESULT_ERROR);
        message.setErrorCode(errorCode);
        message.setErrorMessage(errorMessage);
        return message;
    }

    DConnectResponseMessage createTimeout() {
        return createErrorMessage(DConnectMessage.ErrorCode.TIMEOUT.getCode(), DConnectMessage.ErrorCode.TIMEOUT.toString());
    }

    /**
     * 指定された情報からAPIへのURLを提供するクラス.
     *
     * <p>
     * Host、Port、AccessTokenは、DConnectSDKに設定された値がデフォルトで入っています。
     * </p>
     *
     * <span style="margin:0;padding:2px;background:#029EBC;color:#EBF7FA;line-height:140%;font-weight:bold;">サンプルコード</span>
     * <pre>
     * URIBuilder builder = sdk.createURIBuilder();
     * builder.setProfile(BatteryProfileConstants.PROFILE_NAME)
     *      .setAttribute(BatteryProfileConstants.ATTRIBUTE_ON_BATTERY_CHANGE)
     *      .addParameter(DConnectMessage.EXTRA_SERVICE_ID, "xxxxxxxx")
     *
     * URI uri = builder.build();
     * String uriStr = builder.toString(true);
     * </pre>
     *
     * @author NTT DOCOMO, INC.
     */
    public class URIBuilder {

        /**
         * スキーム.
         */
        private String mScheme = isSSL() ? "https" : "http";

        /**
         * ホスト.
         */
        private String mHost = DConnectSDK.this.mHost;

        /**
         * ポート番号.
         */
        private int mPort = DConnectSDK.this.mPort;

        /**
         * パス.
         */
        private String mPath;

        /**
         * パラメータ.
         */
        private Map<String, String> mParameters = new HashMap<>();

        /**
         * API.
         */
        private String mApi = DConnectMessage.DEFAULT_API;

        /**
         * プロファイル.
         */
        private String mProfile;

        /**
         * インターフェース.
         */
        private String mInterface;

        /**
         * アトリビュート.
         */
        private String mAttribute;

        /**
         * コンストラクタ.
         */
        URIBuilder() {
            if (mAccessToken != null) {
                addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, mAccessToken);
            }
        }

        /**
         * URIから {@link URIBuilder} クラスを生成する.
         *
         * @param uri URI
         * @throws URISyntaxException URIフォーマットが不正な場合
         */
        URIBuilder(final String uri) throws URISyntaxException {
            this(new URI(uri));
        }

        /**
         * URIから {@link URIBuilder} クラスを生成する.
         *
         * @param uri URI
         */
        URIBuilder(final URI uri) {
            mScheme = uri.getScheme();
            mHost = uri.getHost();
            mPort = uri.getPort();
            mPath = uri.getPath();

            String query = uri.getQuery();
            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    String[] splitted = param.split("=");
                    if (splitted.length == 2) {
                        addParameter(splitted[0], splitted[1]);
                    } else {
                        addParameter(splitted[0], "");
                    }
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public synchronized String toString() {
            return toString(false);
        }

        /**
         * ASCIIのオブジェクト文字列を取得する.
         *
         * @return オブジェクト文字列
         */
        public String toASCIIString() {
            return toString(true);
        }

        /**
         * スキームを取得する.
         *
         * @return スキーム
         */
        public synchronized String getScheme() {
            return mScheme;
        }

        /**
         * スキームを設定する.
         *
         * @param scheme スキーム
         * @return {@link URIBuilder} インスタンス
         */
        public synchronized URIBuilder setScheme(final String scheme) {
            if (scheme == null) {
                throw new NullPointerException("scheme is null.");
            }
            if (scheme.isEmpty()) {
                throw new IllegalArgumentException("scheme is empty.");
            }
            mScheme = scheme;
            return this;
        }

        /**
         * ホスト名を取得する.
         *
         * @return ホスト名
         */
        public synchronized String getHost() {
            return mHost;
        }

        /**
         * ホスト名を設定する.
         *
         * @param host ホスト名
         * @return {@link URIBuilder} インスタンス
         */
        public synchronized URIBuilder setHost(final String host) {
            if (host == null) {
                throw new NullPointerException("host is null.");
            }
            if (host.isEmpty()) {
                throw new IllegalArgumentException("host is empty.");
            }
            mHost = host;
            return this;
        }

        /**
         * ポート番号を取得する. ポート番号が指定されていない場合は-1を返す
         *
         * @return ポート番号
         */
        public synchronized int getPort() {
            return mPort;
        }

        /**
         * ポート番号を設定する.
         *
         * @param port ポート番号
         * @return {@link URIBuilder} インスタンス
         */
        public synchronized URIBuilder setPort(final int port) {
            if (port < 0 || port > 65535) {
                throw new IllegalArgumentException("port is invalid. port=" + port);
            }
            mPort = port;
            return this;
        }

        /**
         * パスを取得する.
         *
         * @return パス
         */
        public synchronized String getPath() {
            return mPath;
        }

        /**
         * APIのパスを文字列で設定する.
         * このパラメータが設定されている場合はビルド時に api、profile、interface、attribute は無視される。
         *
         * @param path パス
         * @return {@link URIBuilder} インスタンス
         */
        public synchronized URIBuilder setPath(final String path) {
            mPath = path;
            return this;
        }

        /**
         * APIを取得する.
         *
         * @return API
         */
        public synchronized String getApi() {
            return mApi;
        }

        /**
         * APIを取得する.
         * パスが設定されている場合には、このパラメータは無視される。
         *
         * @param api API
         * @return {@link URIBuilder} インスタンス
         */
        public synchronized URIBuilder setApi(final String api) {
            mApi = api;
            return this;
        }

        /**
         * プロファイルを取得する.
         *
         * @return プロファイル
         */
        public synchronized String getProfile() {
            return mProfile;
        }

        /**
         * プロファイルを設定する.
         * <p>
         * パスが設定されている場合には、このパラメータは無視される。
         *
         * @param profile プロファイル
         * @return {@link URIBuilder} インスタンス
         */
        public synchronized URIBuilder setProfile(final String profile) {
            mProfile = profile;
            return this;
        }

        /**
         * インターフェースを取得する.
         *
         * @return インターフェース
         */
        public synchronized String getInterface() {
            return mInterface;
        }

        /**
         * インターフェースを設定する.
         * <p>
         * パスが設定されている場合には、このパラメータは無視される。
         *
         * @param inter インターフェース
         * @return {@link URIBuilder} インスタンス
         */
        public synchronized URIBuilder setInterface(final String inter) {
            mInterface = inter;
            return this;
        }

        /**
         * アトリビュートを取得する.
         *
         * @return アトリビュート
         */
        public synchronized String getAttribute() {
            return mAttribute;
        }

        /**
         * アトリビュートを設定する.
         * <p>
         * {@link #setPath}でパスが設定されている場合には、このパラメータは無視される。
         *
         * @param attribute アトリビュート
         * @return {@link URIBuilder} インスタンス
         */
        public synchronized URIBuilder setAttribute(final String attribute) {
            mAttribute = attribute;
            return this;
        }

        /**
         * アクセストークンを設定する.
         * <p>
         * {@link HttpDConnectSDK#setAccessToken}で設定された値がデフォルトで指定されている。<br>
         * ここで、新たなアクセストークンが指定された場合には上書きする。
         * </p>
         * @param accessToken アクセストークン
         * @return {@link URIBuilder} インスタンス
         */
        public synchronized URIBuilder setAccessToken(final String accessToken) {
            addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, accessToken);
            return this;
        }

        /**
         * アクセストークンを取得する.
         * @return アクセストークン
         */
        public synchronized String getAccessToken() {
            return getParameter(DConnectMessage.EXTRA_ACCESS_TOKEN);
        }

        /**
         * サービスIDを設定する.
         * @param serviceId サービスID
         * @return {@link URIBuilder} インスタンス
         */
        public synchronized URIBuilder setServiceId(final String serviceId) {
            addParameter(DConnectMessage.EXTRA_SERVICE_ID, serviceId);
            return this;
        }

        /**
         * サービスIDを取得する.
         * <p>
         * 設定されていない場合にはnullを返却する。
         * </p>
         * @return サービスID
         */
        public synchronized String getServiceId() {
            return getParameter(DConnectMessage.EXTRA_SERVICE_ID);
        }

        /**
         * 指定したクエリパラメータを取得する.
         * <p>
         * 指定されたクエリパラメータが存在しない場合にはnullを返却する。
         * </p>
         * @param name クエリパラメータ名
         * @return クエリパラメータ
         */
        public synchronized String getParameter(final String name) {
            return mParameters.get(name);
        }

        /**
         * キーバリューでクエリパラメータを追加する.
         *
         * @param key  キー
         * @param value バリュー
         * @return {@link URIBuilder} インスタンス
         */
        public synchronized URIBuilder addParameter(final String key, final String value) {
            if (key == null) {
                throw new NullPointerException("key is null.");
            }
            if (value == null) {
                throw new NullPointerException("value is null.");
            }
            mParameters.put(key, value);
            return this;
        }

        /**
         * 指定されたクエリパラメータを削除する.
         * @param key クエリパラメータ名
         * @return {@link URIBuilder} インスタンス
         */
        public synchronized URIBuilder removeParameter(final String key) {
            if (key == null) {
                throw new NullPointerException("key is null.");
            }
            mParameters.remove(key);
            return this;
        }

        /**
         * {@link Uri} オブジェクトを取得する.
         *
         * @return {@link Uri} オブジェクト
         */
        public Uri build() {
            return Uri.parse(toString(true));
        }

        /**
         * URIを文字列にして取得する.
         *
         * @param ascii ASCII変換の有無
         * @return URIを表す文字列
         */
        private synchronized String toString(final boolean ascii) {
            StringBuilder builder = new StringBuilder();

            if (mScheme != null) {
                builder.append(mScheme);
                builder.append("://");
            }
            if (mHost != null) {
                builder.append(mHost);
            }
            if (mPort > 0) {
                builder.append(":");
                builder.append(mPort);
            }
            if (mPath != null) {
                builder.append(mPath);
            } else {
                if (mApi != null) {
                    builder.append("/");
                    builder.append(mApi);
                }
                if (mProfile != null) {
                    builder.append("/");
                    builder.append(mProfile);
                }
                if (mInterface != null) {
                    builder.append("/");
                    builder.append(mInterface);
                }
                if (mAttribute != null) {
                    builder.append("/");
                    builder.append(mAttribute);
                }
            }

            if (mParameters != null && mParameters.size() > 0) {
                if (ascii) {
                    builder.append("?");
                    builder.append(concatenateStringWithEncode(mParameters, "UTF-8"));
                } else {
                    builder.append("?");
                    builder.append(concatenateString(mParameters));
                }
            }

            return builder.toString();
        }

        private String concatenateString(final Map<String, String> map) {
            String string = "";
            for (Map.Entry<String, String> e : map.entrySet()) {
                if (string.length() > 0) {
                    string += "&";
                }
                string += e.getKey() + "=" + e.getValue();
            }
            return string;
        }

        private String concatenateStringWithEncode(final Map<String, String> map, final String charset) {
            try {
                String string = "";
                for (Map.Entry<String, String> e : map.entrySet()) {
                    if (string.length() > 0) {
                        string += "&";
                    }
                    string += e.getKey() + "=" + URLEncoder.encode(e.getValue(), charset);
                }
                return string;
            } catch (UnsupportedEncodingException e) {
                return "";
            }
        }
    }

    /**
     * 非同期でDevice Connect Managerからのレスポンスを受け取るためのリスナー.
     * @author NTT DOCOMO, INC.
     */
    public interface OnResponseListener {
        /**
         * レスポンスを受け取った時に通知される.
         * @param response レスポンス
         */
        void onResponse(DConnectResponseMessage response);
    }

    /**
     * Device Connect Managerからイベントを受け取るためのリスナー.
     * @author NTT DOCOMO, INC.
     */
    public interface OnEventListener extends OnResponseListener {
        /**
         * イベントを通知する.
         * @param message イベントメッセージ
         */
        void onMessage(DConnectEventMessage message);
    }

    /**
     * 非同期でDevice Connect Managerの認証結果を受け取るリスナー.
     * @author NTT DOCOMO, INC.
     */
    public interface OnAuthorizationListener {
        /**
         * 認証に成功した結果を通知する.
         * <p>
         * このリスナーで受け取ったアクセストークンを使うことで、Device Connect Manager
         * の各プロファイルにアクセスすることができるようになります。
         * </p>
         * @param clientId クライアントID
         * @param accessToken アクセストークン
         */
        void onResponse(String clientId, String accessToken);

        /**
         * 認証に失敗した結果を通知する.
         * @param errorCode エラーコード
         * @param errorMessage エラーメッセージ
         */
        void onError(int errorCode, String errorMessage);
    }

    /**
     * WebSocketとの接続状態を通知するリスナー.
     * @author NTT DOCOMO, INC.
     */
    public interface OnWebSocketListener {
        /**
         * 接続確立を通知する.
         */
        void onOpen();

        /**
         * 接続切断を通知する.
         */
        void onClose();

        /**
         * 接続のエラーが発生したことを通知する.
         * @param e 発生した時の例外
         */
        void onError(Exception e);
    }
}
