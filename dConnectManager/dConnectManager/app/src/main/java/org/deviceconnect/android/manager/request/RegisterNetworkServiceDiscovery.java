/*
 RegisterNetworkServiceDiscovery.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.request;

import android.content.Intent;

import org.deviceconnect.android.manager.event.EventProtocol;
import org.deviceconnect.android.manager.plugin.DevicePlugin;
import org.deviceconnect.android.manager.plugin.MessagingException;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * Network Service Discovery Status Change Event 登録用リクエスト.
 * @author NTT DOCOMO, INC.
 */
public class RegisterNetworkServiceDiscovery extends DConnectRequest {
    /** ロガー. */
    private final Logger mLogger = Logger.getLogger("dconnect.manager");

    /** 送信先のデバイスプラグイン. */
    private DevicePlugin mDevicePlugin;

    /** ロックオブジェクト. */
    private final Object mLockObj = new Object();

    /** リクエストコード. */
    private int mRequestCode;

    /**
     * 送信先のデバイスプラグインを設定する.
     * @param plugin デバイスプラグイン
     */
    public void setDestination(final DevicePlugin plugin) {
        mDevicePlugin = plugin;
    }

    @Override
    public void setResponse(final Intent response) {
        super.setResponse(response);
        synchronized (mLockObj) {
            mLockObj.notifyAll();
        }
    }

    @Override
    public boolean hasRequestCode(final int requestCode) {
        return mRequestCode == requestCode;
    }

    @Override
    public void run() {
        // リクエストコードを作成する
        mRequestCode = UUID.randomUUID().hashCode();

        // リクエストを作成
        Intent request = EventProtocol.createRegistrationRequestForServiceChange(mContext, mDevicePlugin);
        request.putExtra(IntentDConnectMessage.EXTRA_REQUEST_CODE, mRequestCode);
        mRequest = request;

        // リクエスト送信
        if (!forwardRequest(request)) {
            return;
        }

        if (mResponse == null) {
            // 各デバイスのレスポンスを待つ
            waitForResponse();
        }

        // レスポンスを解析して、処理を行う
        if (mResponse != null) {
            // リカバリ不可能なのでログだけ出して終了
            // ここで、登録できなかった場合には、デバイス発見イベントは使用することができない。
            // ただし、Service Discoveryは使用できるので問題はないと考える。
            int result = getResult(mResponse);
            if (result == DConnectMessage.RESULT_ERROR) {
                int errorCode = getErrorCode(mResponse);
                String errorMsg = getErrorMessage(mResponse);
                mLogger.severe("Failed to register onservicechange event." 
                        + "errorCode=" + errorCode + " errorMessage=" + errorMsg);
            }
        } else {
            sendTimeoutError();
        }
    }

    private boolean forwardRequest(final Intent request) {
        if (mDevicePlugin == null) {
            throw new IllegalStateException("Destination is null.");
        }
        try {
            mDevicePlugin.send(request);
            return true;
        } catch (MessagingException e) {
            switch (e.getReason()) {
                case NOT_ENABLED:
                    sendPluginDisabledError();
                    break;
                case CONNECTION_SUSPENDED:
                    sendPluginSuspendedError();
                    break;
                default: // NOT_CONNECTED
                    sendIllegalServerStateError("Failed to send a message to the plugin: " + mDevicePlugin.getPackageName());
                    break;
            }
            return false;
        }
    }

    /**
     * resultの値をレスポンスのIntentから取得する.
     * @param response レスポンスのIntent
     * @return resultの値
     */
    private int getResult(final Intent response) {
        int result = response.getIntExtra(DConnectMessage.EXTRA_RESULT,
                DConnectMessage.RESULT_ERROR);
        return result;
    }

    /**
     * エラーコードを取得する.
     * @param response レスポンス
     * @return エラーコード
     */
    private int getErrorCode(final Intent response) {
        int code = response.getIntExtra(DConnectMessage.EXTRA_ERROR_CODE,
                DConnectMessage.ErrorCode.UNKNOWN.getCode());
        return code;
    }

    /**
     * エラーメッセージを取得する.
     * @param response レスポンス
     * @return エラーメッセージ
     */
    private String getErrorMessage(final Intent response) {
        String msg = response.getStringExtra(DConnectMessage.EXTRA_ERROR_MESSAGE);
        return msg;
    }
    /**
     * 各デバイスからのレスポンスを待つ.
     * 
     * この関数から返答があるのは以下の条件になる。
     * <ul>
     * <li>デバイスプラグインからレスポンスがあった場合
     * <li>指定された時間無いにレスポンスが返ってこない場合
     * </ul>
     */
    private void waitForResponse() {
        synchronized (mLockObj) {
            try {
                mLockObj.wait(mTimeout);
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}
