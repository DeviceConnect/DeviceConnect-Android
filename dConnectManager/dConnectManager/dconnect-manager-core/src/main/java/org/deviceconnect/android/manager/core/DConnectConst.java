package org.deviceconnect.android.manager.core;

public interface DConnectConst {
    /**
     * ローカルのドメイン名.
     */
    String LOCALHOST_DCONNECT = "localhost.deviceconnect.org";

    /**
     * 起動用URIスキーム名.
     */
    String SCHEME_LAUNCH = "dconnect";

    /**
     * エクストラ名: インストールまたはアンインストールされたパッケージの名前.
     */
    String EXTRA_PACKAGE_NAME = "packageName";

    /**
     * リクエストコードのエラー値を定義.
     */
    int ERROR_CODE = Integer.MIN_VALUE;

    /**
     * 匿名オリジン.
     */
    String ANONYMOUS_ORIGIN = "<anonymous>";

    /**
     * セッションキーとreceiverを分けるセパレータ.
     */
    String SEPARATOR_SESSION = "@";

    /**
     * サービスIDやセッションキーを分割するセパレータ.
     */
    String SEPARATOR = ".";

    /**
     * KeepAliveで使用するエクストラキー.
     */
    String EXTRA_EVENT_RECEIVER_ID = "receiverId";

    /**
     * 内部用: 通信タイプを定義する.
     */
    String EXTRA_INNER_TYPE = "_type";

    /**
     * 通信タイプがHTTPであることを示す定数.
     */
    String INNER_TYPE_HTTP = "http";

    /**
     * 内部用: アプリケーションタイプを定義する.
     */
    String EXTRA_INNER_APP_TYPE = "_app_type";

    /**
     * 通信相手がWebアプリケーションであることを示す定数.
     */
    String INNER_APP_TYPE_WEB = "web";

    /**
     * JSONレスポンス用のContent-Type.
     */
    String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";

    /**
     * キーストアファイル名.
     */
    String KEYSTORE_FILE_NAME = "manager.p12";

    int WS_ERROR_CODE_NOT_FOUND_ACCESS_TOKEN = 1;
    int WS_ERROR_CODE_NOT_FOUND_ORIGIN = 2;
    int WS_ERROR_CODE_ACCESS_TOKEN_INVALID = 3;
    int WS_ERROR_CODE_ALREADY_ESTABLISHED = 4;
}
