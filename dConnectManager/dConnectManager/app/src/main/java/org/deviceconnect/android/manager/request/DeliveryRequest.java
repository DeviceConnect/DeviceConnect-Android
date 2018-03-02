/*
 DeliveryRequest.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.request;

import android.content.Intent;

import org.deviceconnect.android.manager.BuildConfig;
import org.deviceconnect.android.manager.event.EventBroker;
import org.deviceconnect.android.manager.plugin.DevicePlugin;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import java.util.logging.Logger;

/**
 * 指定されたリクエストメッセージを各デバイスプラグインに配送するDConnectRequset実装クラス.
 * @author NTT DOCOMO, INC.
 */
public class DeliveryRequest extends LocalOAuthRequest {
    /** ロガー. */
    private final Logger mLogger = Logger.getLogger("dconnect.manager");

    /** イベント管理クラス. */
    private final EventBroker mEventBroker;

    /**
     * コンストラクタ.
     *
     * @param eventBroker イベント管理クラス
     */
    public DeliveryRequest(final EventBroker eventBroker) {
        mEventBroker = eventBroker;
    }

    @Override
    protected void onAccessTokenUpdated(final DevicePlugin plugin, final String newAccessToken) {
        mEventBroker.updateAccessTokenForPlugin(plugin.getPluginId(), newAccessToken);
    }

    @Override
    protected void executeRequest(final String accessToken) {
        // 命令を実行する前にレスポンスを初期化しておく
        mResponse = null;

        if (BuildConfig.DEBUG) {
            mLogger.info(String.format("Delivery Request: %s, intent: %s",
                    mDevicePlugin.getPackageName(), mRequest.getExtras()));
        }

        // プラグインのサポートしない命令にエラーを返す
        String profileName = DConnectProfile.getProfile(mRequest);
        if (profileName != null && !mDevicePlugin.supportsProfile(profileName)) {
            Intent response = new Intent(IntentDConnectMessage.ACTION_RESPONSE);
            MessageUtils.setNotSupportProfileError(response);
            sendResponse(response);
            return;
        }

        // 命令をデバイスプラグインに送信
        Intent request = createRequestMessage(mRequest, mDevicePlugin);
        request.setComponent(mDevicePlugin.getComponentName());
        request.putExtra(IntentDConnectMessage.EXTRA_REQUEST_CODE, mRequestCode);
        if (accessToken != null) {
            request.putExtra(DConnectMessage.EXTRA_ACCESS_TOKEN, accessToken);
        }

        sendRequest(request);
    }

    @Override
    protected void onResponseReceived(final Intent request, final Intent response) {
        int result = getResult(mResponse);
        if (result == DConnectMessage.RESULT_OK) {
            sendResponse(mResponse);
            return;
        }

        mRetryCount++;
        int errorCode = getErrorCode(mResponse);
        if (mRetryCount < MAX_RETRY_COUNT
                && errorCode == DConnectMessage.ErrorCode.NOT_FOUND_CLIENT_ID.getCode()) {
            // クライアントIDが発見できなかった場合は、dConnectManagerとデバイスプラグインで
            // 一致していないので、dConnectManagerのローカルに保存しているclientIdを削除
            // してから、再度デバイスプラグインにクライアントIDの作成を要求を行う.
            String serviceId = mRequest.getStringExtra(DConnectMessage.EXTRA_SERVICE_ID);
            String origin = mRequest.getStringExtra(IntentDConnectMessage.EXTRA_ORIGIN);
            if (serviceId != null) {
                mLocalOAuth.deleteOAuthData(origin, serviceId);
            }
            executeRequest();
        } else if (mRetryCount < MAX_RETRY_COUNT
                && errorCode == DConnectMessage.ErrorCode.EXPIRED_ACCESS_TOKEN.getCode()) {
            // アクセストークンの有効期限切れ
            String accessToken = request.getStringExtra(DConnectMessage.EXTRA_ACCESS_TOKEN);
            mLocalOAuth.deleteAccessToken(accessToken);
            executeRequest();
        } else {
            sendResponse(mResponse);
        }
    }
}
