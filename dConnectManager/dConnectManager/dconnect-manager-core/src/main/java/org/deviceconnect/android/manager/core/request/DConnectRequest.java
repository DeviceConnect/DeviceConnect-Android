/*
 DConnectRequest.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.core.request;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import org.deviceconnect.android.manager.core.DConnectInterface;
import org.deviceconnect.android.manager.core.plugin.DevicePlugin;
import org.deviceconnect.android.manager.core.plugin.DevicePluginManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

/**
 * DConnectリクエスト.
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class DConnectRequest {
    /**
     * タイムアウト時間を定義する.
     */
    private static final int DEFAULT_TIMEOUT = 60 * 1000;

    /**
     * オリジナルのリクエスト.
     */
    protected Intent mRequest;

    /**
     * デバイスプラグインからのレスポンス.
     */
    protected Intent mResponse;

    /**
     * このクラスが属するコンテキスト.
     */
    protected Context mContext;

    /**
     * プラグイン管理クラス.
     */
    protected DevicePluginManager mPluginMgr;

    /**
     * タイムアウト時間.
     */
    protected int mTimeout = DEFAULT_TIMEOUT;

    /**
     * レスポンスを返却するコールバック.
     */
    private OnResponseCallback mOnResponseCallback;

    /**
     * リクエスト管理クラス.
     */
    DConnectRequestManager mRequestManager;

    /**
     * アプリからの設定を取得するためのインターフェース.
     */
    protected DConnectInterface mInterface;

    /**
     * コンストラクタ.
     */
    public DConnectRequest() {
        mResponse = null;
    }

    /**
     * レスポンスを返却するコールバックを設定します.
     *
     * @param callback コールバック
     */
    public void setOnResponseCallback(final OnResponseCallback callback) {
        mOnResponseCallback = callback;
    }

    /**
     * {@link DConnectRequestManager} を設定します.
     *
     * @param requestManager リクエスト管理クラス
     */
    public void setRequestManager(final DConnectRequestManager requestManager) {
        mRequestManager = requestManager;
    }

    /**
     * {@link DConnectInterface} を設定します.
     *
     * @param i インターフェース
     */
    public void setDConnectInterface(final DConnectInterface i) {
        mInterface = i;
    }

    /**
     * コンテキストを設定する.
     *
     * @param context このクラスが属するコンテキスト
     */
    public void setContext(final Context context) {
        mContext = context;
    }

    /**
     * コンテキストを取得する.
     *
     * @return コンテキスト
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * 送信先のデバイスプラグインを設定する.
     *
     * @param mgr デバイスプラグイン
     */
    public void setDevicePluginManager(final DevicePluginManager mgr) {
        mPluginMgr = mgr;
    }

    /**
     * 設定されているタイムアウト時間を取得する.
     *
     * @return タイムアウト時間
     */
    public int getTimeout() {
        return mTimeout;
    }

    /**
     * タイムアウト時間を設定する.
     *
     * @param timeout タイムアウト時間(ミリ秒)
     */
    public void setTimeout(final int timeout) {
        mTimeout = timeout;
    }

    /**
     * リクエストを設定する.
     *
     * @param request リクエスト
     */
    public void setRequest(final Intent request) {
        mRequest = request;
    }

    /**
     * リクエストを取得する.
     *
     * @return リクエスト
     */
    public Intent getRequest() {
        return mRequest;
    }

    /**
     * レスポンスを受け取る.
     *
     * @param response レスポンス
     */
    public void setResponse(final Intent response) {
        mResponse = response;
    }

    /**
     * 各デバイスプラグインへ配送するリクエストを作成する.
     *
     * @param request 配送元のリクエスト用Intent
     * @param plugin  配送先のデバイスプラグイン
     * @return 配送用Intent
     */
    protected Intent createRequestMessage(final Intent request, final DevicePlugin plugin) {
        Intent targetIntent = new Intent(request);
        String serviceId = request.getStringExtra(DConnectMessage.EXTRA_SERVICE_ID);
        if (plugin != null) {
            if (serviceId != null) {
                mPluginMgr.splitPluginIdToServiceId(targetIntent);
            }
        }

        targetIntent.putExtra(IntentDConnectMessage.EXTRA_RECEIVER,
                new ComponentName(mContext, mInterface.getDConnectBroadcastReceiverClass()));
        targetIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        return targetIntent;
    }

    /**
     * 取得したレスポンスを返却する.
     * <p>
     * レスポンスには、リクエストとして設定された Intent の
     * リクエストコードを付加してから送信します。
     * </p>
     * @param response 返却するレスポンス
     */
    public void sendResponse(final Intent response) {
        // リクエストと同じリクエストコードをレスポンスに追加
        response.putExtra(IntentDConnectMessage.EXTRA_REQUEST_CODE,
                mRequest.getIntExtra(IntentDConnectMessage.EXTRA_REQUEST_CODE, Integer.MIN_VALUE));

        if (mOnResponseCallback != null) {
            mOnResponseCallback.onResponse(response);
        }
    }

    /**
     * プラグイン無効エラーレスポンスを返却する.
     */
    protected void sendPluginDisabledError() {
        sendResponse(createPluginDisabledError());
    }

    /**
     * プラグイン無効エラーレスポンスを作成する.
     */
    protected Intent createPluginDisabledError() {
        Intent response = new Intent(IntentDConnectMessage.ACTION_RESPONSE);
        MessageUtils.setPluginDisabledError(response);
        return response;
    }

    /**
     * プラグイン連携中止エラーレスポンスを返却する.
     */
    protected void sendPluginSuspendedError() {
        sendResponse(createPluginSuspendedError());
    }

    /**
     * プラグイン連携中止エラーレスポンスを作成する.
     */
    protected Intent createPluginSuspendedError() {
        Intent response = new Intent(IntentDConnectMessage.ACTION_RESPONSE);
        MessageUtils.setPluginSuspendedError(response);
        return response;
    }

    /**
     * タイムアウトエラーのレスポンスを返却する.
     */
    protected void sendTimeoutError() {
        sendResponse(createTimeoutError());
    }

    /**
     * タイムアウトエラーのレスポンスを作成する.
     */
    protected Intent createTimeoutError() {
        Intent response = new Intent(IntentDConnectMessage.ACTION_RESPONSE);
        MessageUtils.setTimeoutError(response);
        return response;
    }

    /**
     * 不明なエラーレスポンスを返却する.
     */
    protected void sendIllegalServerStateError(final String message) {
        sendResponse(createIllegalServerStateError(message));
    }

    /**
     * 不明なエラーレスポンスを作成する.
     */
    protected Intent createIllegalServerStateError(final String message) {
        Intent response = new Intent(IntentDConnectMessage.ACTION_RESPONSE);
        MessageUtils.setIllegalServerStateError(response, message);
        return response;
    }

    /**
     * 実行時エラーが発生したことを通知する.
     *
     * @param message エラーメッセージ
     */
    protected void sendRuntimeException(final String message) {
        sendResponse(createRuntimeException(message));
    }

    /**
     * 実行時エラーが発生したことを通知するためのレスポンスを作成する.
     *
     * @param message エラーメッセージ
     */
    protected Intent createRuntimeException(final String message) {
        Intent response = new Intent(IntentDConnectMessage.ACTION_RESPONSE);
        MessageUtils.setUnknownError(response, message);
        return response;
    }

    /**
     * 指定されたリクエストコードを持っているかチェックする.
     *
     * @param requestCode リクエストコード
     * @return リクエストコードを持っている場合はtrue、それ以外はfalse
     */
    public abstract boolean hasRequestCode(final int requestCode);

    /**
     * 各デバイスプラグインへのリクエスト送信とレスポンスを待つ処理を行う.
     */
    public abstract void run();

    /**
     * レスポンスを返却するコールバック.
     */
    public interface OnResponseCallback {
        void onResponse(Intent response);
    }
}
