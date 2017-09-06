/*
 DConnectSettings.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import org.deviceconnect.android.manager.util.DConnectUtil;

import java.io.File;

/**
 * DConnectの設定を保持するクラス.
 * @author NTT DOCOMO, INC.
 */
public final class DConnectSettings {
    /** デフォルトのホスト名を定義. */
    private static final String DEFAULT_HOST = "localhost";
    /** デフォルトのポート番号を定義. */
    private static final int DEFAULT_PORT = 4035;
    /** Webサーバのデフォルトポート番号を定義. */
    private static final int DEFAULT_WEB_PORT = 8080;
    /** デフォルトのインターバルを定義. */
    private static final int DEFAULT_INTERVAL = 1000 * 60 * 5;
    /** デフォルトのキーワード. */
    public static final String DEFAULT_KEYWORD = DConnectUtil.createKeyword();

    /** 情報を共有するプリファレンス. */
    private SharedPreferences mPreferences;

    /** コンテキスト. */
    private final Context mContext;

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     */
    DConnectSettings(final Context context) {
        mContext = context;
        load();
    }

    /**
     * SharedPreferencesのデータを読み込む.
     */
    public synchronized void load() {
        mPreferences = mContext.getSharedPreferences(mContext.getPackageName() + "_preferences",
                Context.MODE_PRIVATE);

        String name = getManagerName();
        if (name == null) {
            setManagerName(DConnectUtil.createName());
        }

        String uuid = getManagerUUID();
        if (uuid == null) {
            setManagerUUID(DConnectUtil.createUuid());
        }
    }

    /**
     * Managerの起動フラグを取得する.
     * @return 起動する場合はtrue、それ以外はfalse
     */
    public boolean isManagerStartFlag() {
        return mPreferences.getBoolean(mContext.getString(R.string.key_settings_dconn_server_on_off), false);
    }

    /**
     * Managerの起動フラグを設定する.
     * @param flag 起動フラグ
     */
    public void setManagerStartFlag(final boolean flag) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(mContext.getString(R.string.key_settings_dconn_server_on_off), flag);
        editor.apply();
    }

    /**
     * ポート番号を取得する.
     * @return ポート番号
     */
    public int getPort() {
        return Integer.parseInt(mPreferences.getString(
                mContext.getString(R.string.key_settings_dconn_port),
                String.valueOf(DEFAULT_PORT)));
    }

    /**
     * ポート番号を設定する.
     * @param port ポート番号
     */
    public void setPort(final int port) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(mContext.getString(R.string.key_settings_dconn_port), String.valueOf(port));
        editor.apply();
    }

    /**
     * ホスト名を取得する.
     * @return ホスト名
     */
    public String getHost() {
        return mPreferences.getString(mContext.getString(R.string.key_settings_dconn_host),
                DConnectSettings.DEFAULT_HOST);
    }

    /**
     * ホスト名を設定する.
     * @param host ホスト名
     */
    public void setHost(final String host) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(mContext.getString(R.string.key_settings_dconn_host), host);
        editor.apply();
    }

    /**
     * ドキュメントルートパスを取得する.
     * @return ドキュメントルートパス
     */
    public String getDocumentRootPath() {
        File file = new File(Environment.getExternalStorageDirectory(), mContext.getPackageName());
        if (!file.exists()) {
            if (!file.mkdirs()) {
                throw new RuntimeException("Cannot make a folder. path=" + file.getPath());
            }
        }
        return mPreferences.getString(mContext.getString(R.string.key_settings_web_server_document_root_path),
                file.getAbsolutePath());
    }

    /**
     * ドキュメントルートパスを設定する.
     * @param documentRootPath パス
     */
    public void setDocumentRootPath(final String documentRootPath) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(mContext.getString(R.string.key_settings_web_server_document_root_path),
                documentRootPath);
        editor.apply();
    }

    /**
     * SSL使用フラグを取得する.
     * @return SSL使用フラグ
     */
    public boolean isSSL() {
        return mPreferences.getBoolean(mContext.getString(R.string.key_settings_dconn_ssl), false);
    }

    /**
     * SSL使用フラグを設定する.
     * @param ssl SSL使用フラグ
     */
    public void setSSL(final boolean ssl) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(mContext.getString(R.string.key_settings_dconn_ssl), ssl);
        editor.apply();
    }

    /**
     * LocalOAuth使用フラグを取得する.
     * @return 使用する場合はtrue、それ以外はfalse
     */
    public boolean isUseALocalOAuth() {
        return mPreferences.getBoolean(mContext.getString(R.string.key_settings_dconn_local_oauth), true);
    }

    /**
     * Local OAuth使用フラグを設定する.
     * @param useALocalOAuth 使用する場合はtrue、それ以外はfalse
     */
    public void setUseALocalOAuth(final boolean useALocalOAuth) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(mContext.getString(R.string.key_settings_dconn_local_oauth), useALocalOAuth);
        editor.apply();
    }

    /**
     * 外部IP承認フラグを取得する.
     * <p>
     * デフォルトではfalseに設定されている。
     * </p>
     * @return trueの場合は許可、falseの場合は不許可
     */
    public boolean allowExternalIP() {
        return mPreferences.getBoolean(mContext.getString(R.string.key_settings_dconn_allow_external_ip), false);
    }

    /**
     * 外部IP承認フラグを設定する.
     * <p>
     * デフォルトではfalseに設定されている。
     * </p>
     * @param allow trueの場合は許可、falseの場合は不許可
     */
    public void setAllowExternalIP(final boolean allow) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(mContext.getString(R.string.key_settings_dconn_allow_external_ip), allow);
        editor.apply();
    }

    /**
     * 外部アプリからの自動起動および自動終了を許可するフラグを取得する.
     * <p>
     * デフォルトではfalseに設定されている。
     * </p>
     * @return trueの場合は許可、falseの場合は不許可
     */
    public boolean allowExternalStartAndStop() {
        return mPreferences.getBoolean(mContext.getString(R.string.key_settings_dconn_allow_external_start_and_stop), true);
    }

    /**
     * 外部アプリからの自動起動および自動終了を許可するフラグを設定する.
     * <p>
     * デフォルトではfalseに設定されている。
     * </p>
     * @param allow trueの場合は許可、falseの場合は不許可
     */
    public void setAllowExternalStartAndStop(final boolean allow) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(mContext.getString(R.string.key_settings_dconn_allow_external_start_and_stop), allow);
        editor.apply();
    }

    /**
     * オリジン要求フラグを取得する.
     * <p>
     * デフォルトではtrueに設定されている。
     * </p>
     * @return trueの場合は必要、falseの場合は不要
     */
    public boolean requireOrigin() {
        return mPreferences.getBoolean(mContext.getString(R.string.key_settings_dconn_require_origin), true);
    }

    /**
     * オリジン要求フラグを設定する.
     * <p>
     * デフォルトではtrueに設定されている。
     * </p>
     * @param allow trueの場合は必要、falseの場合は不要
     */
    public void setRequireOrigin(final boolean allow) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(mContext.getString(R.string.key_settings_dconn_require_origin), allow);
        editor.apply();
    }

    /**
     * Originブロック機能の使用フラグを取得する.
     * @return trueの場合はホワイトリストに無いOriginからのアクセスを許可しない、
     *      falseの場合は任意のOriginからのアクセスを許可する
     */
    public boolean isBlockingOrigin() {
        return mPreferences.getBoolean(mContext.getString(R.string.key_settings_dconn_whitelist_origin_blocking), false);
    }

    /**
     * Originブロック機能の使用フラグを設定する.
     * @param enabled trueの場合はホワイトリストに無いOriginからのアクセスを許可しない、
     *      falseの場合は任意のOriginからのアクセスを許可する
     */
    public void setBlockingOrigin(final boolean enabled) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(mContext.getString(R.string.key_settings_dconn_whitelist_origin_blocking), enabled);
        editor.apply();
    }

    /**
     * Managerの名前を取得する.
     * <p>
     * Managerの名前が未設定の場合には{@code null}を返却する.
     * </p>
     * @return Manager名
     */
    public String getManagerName() {
        return mPreferences.getString(mContext.getString(R.string.key_settings_dconn_name), null);
    }

    /**
     * Managerの名前を設定する.
     * @param name Manager名
     */
    public void setManagerName(final String name) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(mContext.getString(R.string.key_settings_dconn_name), name);
        editor.apply();
    }

    /**
     * Managerの識別子を取得する.
     * <p>
     * Managerの識別子が未設定の場合には{@code null}を返却する.
     * </p>
     * @return Managerの識別子
     */
    public String getManagerUUID() {
        return mPreferences.getString(mContext.getString(R.string.key_settings_dconn_uuid), null);
    }

    /**
     * Managerの識別子を設定する.
     * @param uuid Managerの識別子
     */
    public void setManagerUUID(final String uuid) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(mContext.getString(R.string.key_settings_dconn_uuid), uuid);
        editor.apply();
    }

    /**
     * WakeLockが有効・無効を取得する.
     * @return trueの場合はWakeLockが有効、それ以外はWakeLockが無効
     */
    public boolean enableWakLock() {
        return mPreferences.getBoolean(mContext.getString(R.string.key_settings_wake_lock), true);
    }

    /**
     * Webサーバのポート番号を取得する.
     * @return ポート番号
     */
    public int getWebPort() {
        return Integer.parseInt(mPreferences.getString(
                    mContext.getString(R.string.key_settings_web_server_port),
                    String.valueOf(DEFAULT_WEB_PORT)));
    }

    /**
     * Webサーバのポート番号を設定する.
     * @param port ポート番号
     */
    public void setWebPort(final int port) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(mContext.getString(R.string.key_settings_web_server_port),
                String.valueOf(port));
        editor.apply();
    }

    /**
     * Webサーバのホスト名を取得する.
     * @return ホスト名
     */
    public String getWebHost() {
        return DEFAULT_HOST;
    }

    /**
     * Webサーバの起動フラグを取得する.
     * @return Webサーバを起動している場合はtrue、それ以外はfalse
     */
    public boolean isWebServerStartFlag() {
        return mPreferences.getBoolean(mContext.getString(R.string.key_settings_web_server_on_off), false);
    }

    /**
     * 監視するインターバルを取得する.
     * @return インターバル
     */
    public int getObservationInterval() {
        return Integer.parseInt(mPreferences.getString(
                    mContext.getString(R.string.key_settings_dconn_observation_interval),
                    String.valueOf(DEFAULT_INTERVAL)));
    }

    /**
     * 監視するインターバルを設定する.
     * @param interval インターバル
     */
    public void setObservationInterval(final int interval) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(mContext.getString(R.string.key_settings_dconn_observation_interval),
                String.valueOf(interval));
        editor.apply();
    }

    /**
     * キーワードを取得する.
     * @return キーワード
     */
    public String getKeyword() {
        return mPreferences.getString(mContext.getString(R.string.key_settings_dconn_keyword), DEFAULT_KEYWORD);
    }

    /**
     * キーワードを設定する.
     * @param keyword キーワード
     */
    public void setKeyword(final String keyword) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(mContext.getString(R.string.key_settings_dconn_keyword), keyword);
        editor.apply();
    }

    @Override
    public String toString() {
        return "{\n" +
                "    Host: " + getHost() + "\n" +
                "    Port: " + getPort() + "\n" +
                "    SSL: " + isSSL() + "\n" +
                "    External IP: " + allowExternalIP() + "\n" +
                "    Local OAuth: " + isUseALocalOAuth() + "\n" +
                "    Require Origin: " + requireOrigin() + "\n" +
                "    Blocking Origin: " + isBlockingOrigin() + "\n" +
        "}";
    }
}
