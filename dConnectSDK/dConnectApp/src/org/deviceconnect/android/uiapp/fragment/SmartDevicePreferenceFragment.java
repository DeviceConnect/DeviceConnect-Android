/*
 SmartDevicePreferenceFragment.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.uiapp.fragment;

import java.io.IOException;
import java.util.logging.Logger;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.deviceconnect.android.uiapp.R;
import org.deviceconnect.android.uiapp.device.SmartDevice;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.client.DConnectClient;
import org.deviceconnect.message.http.impl.client.HttpDConnectClient;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.preference.PreferenceFragment;

/**
 * スマートデバイスプロパティインターフェース.
 */
public abstract class SmartDevicePreferenceFragment extends PreferenceFragment {

    /**
     * スマートデバイス情報.
     */
    private SmartDevice mDevice;

    /**
     * Device Connectクライアント.
     */
    private DConnectClient mDConnectClient;

    /**
     * ロガー.
     */
    private Logger mLogger = Logger.getLogger("deviceconnect.uiapp");

    /**
     * デフォルトターゲット.
     */
    private HttpHost mDefaultTarget = null;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        mLogger.entering(getClass().getName(), "onCreate", savedInstanceState);
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        mDConnectClient = new HttpDConnectClient();
        loadTargetHostSettings();

        mDevice = getArguments().getParcelable("device");
        mLogger.exiting(getClass().getName(), "onCreate");
    }

    @Override
    public void onResume() {
        mLogger.entering(getClass().getName(), "onResume");

        super.onResume();
        loadTargetHostSettings();

        mLogger.exiting(getClass().getName(), "onResume");
    }

    /**
     * スマートデバイスを取得する.
     * @return スマートデバイス
     */
    public SmartDevice getSmartDevice() {
        return mDevice;
    }

    /**
     * Device Connectクライアントを取得する.
     * @return Device Connectクライアント
     */
    public DConnectClient getDConnectClient() {
        return mDConnectClient;
    }

    /**
     * HTTPリクエストを同期的に送信する.
     * <p>
     * 送信する前に送信元のAndroidアプリのオリジンをHTTPリクエストヘッダに追加する.
     * </p>
     * @param request HTTPリクエスト
     * @return 受信したHTTPレスポンス
     * @throws ClientProtocolException プロトコルでエラーが発生した場合
     * @throws IOException HTTP通信に失敗した場合
     */
    public HttpResponse sendHttpRequest(final HttpRequest request) throws ClientProtocolException, IOException {
        request.addHeader(DConnectMessage.HEADER_GOTAPI_ORIGIN, getActivity().getPackageName());
        return getDConnectClient().execute(getDefaultHost(), request);
    }

    /**
     * アクセストークンを取得する.
     * アクセストークンがない場合にはnullを返却する。
     * @return アクセストークン
     */
    public String getAccessToken() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getActivity().getApplicationContext());
        String accessToken = prefs.getString(
                getString(R.string.key_settings_dconn_access_token), null);
        return accessToken;
    }

    /**
     * デフォルトホストを取得する.
     * @return ホスト
     */
    public HttpHost getDefaultHost() {
        return mDefaultTarget;
    }

    /**
     * ターゲットホスト設定を読み込む.
     */
    private void loadTargetHostSettings() {
        mLogger.entering(getClass().getName(), "loadTargetHostSettings");

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getActivity());

        String scheme;
        if (prefs.getBoolean(getString(R.string.key_settings_dconn_ssl), false)) {
            scheme = "https";
        } else {
            scheme = "http";
        }

        String hostname = prefs.getString(
                getString(R.string.key_settings_dconn_host),
                getString(R.string.default_host));

        int port = Integer.parseInt(prefs.getString(
                getString(R.string.key_settings_dconn_port),
                getString(R.string.default_port)));

        mDefaultTarget = new HttpHost(hostname, port , scheme);

        mLogger.exiting(getClass().getName(), "loadTargetHostSettings");
    }

}
