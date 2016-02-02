/*
 MessageUtils.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.message;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.BuildConfig;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectMessage.ErrorCode;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import java.util.logging.Logger;

/**
 * Device Connect メッセージユーティリティクラス.
 * @author NTT DOCOMO, INC.
 */
public final class MessageUtils {

    /**
     * ロガー.
     */
    private static Logger sLogger = Logger.getLogger("org.deviceconnect.dplugin");

    /**
     * コンストラクタ.
     */
    private MessageUtils() {
    }

    /**
     * actionにEventを設定したIntentを生成する.
     * 
     * @return イベントメッセージ用のIntent
     */
    public static Intent createEventIntent() {
        Intent intent = new Intent(IntentDConnectMessage.ACTION_EVENT);
        return intent;
    }

    /**
     * レスポンスインテントを生成する.
     *
     * @param request リクエストパラメータ
     * @return レスポンスインテント
     */
    public static Intent createResponseIntent(final Intent request) {
        return createResponseIntent(request.getExtras(), null);
    }

    /**
     * レスポンスインテントを生成する.
     * 
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @return レスポンスインテント
     */
    public static Intent createResponseIntent(final Bundle request, final Bundle response) {
        Intent intent;
        if (request == null) {
            sLogger.warning("could not create response intent, request is null.");
            throw new NullPointerException("request is null.");
        }

        intent = new Intent(IntentDConnectMessage.ACTION_RESPONSE);
        intent.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_ERROR);
        if (response != null) {
            intent.putExtras(response);
        }

        ComponentName receiver = request.getParcelable(DConnectMessage.EXTRA_RECEIVER);
        if (receiver != null) {
            intent.setComponent(receiver);
        } else {
            sLogger.warning("request does not have receiver.");
        }

        int requestCode = request.getInt(DConnectMessage.EXTRA_REQUEST_CODE, Integer.MIN_VALUE);
        if (requestCode != Integer.MIN_VALUE) {
            intent.putExtra(DConnectMessage.EXTRA_REQUEST_CODE, requestCode);
        }

        if (BuildConfig.DEBUG) {
            intent.putExtra("debug", "DevicePlugin");
        }
        return intent;
    }

    /**
     * リクエストからサービスIDを取得する.
     * 
     * @param request リクエストパラメータ
     * @return サービスID
     */
    public static String getServiceID(final Intent request) {
        String serviceId = request.getExtras().getString(DConnectMessage.EXTRA_SERVICE_ID);
        return serviceId;
    }

    /**
     * リクエストからプロファイル名を取得する.
     * 
     * @param request リクエストパラメータ
     * @return プロファイル名
     */
    public static String getProfile(final Intent request) {
        String profile = request.getExtras().getString(DConnectMessage.EXTRA_PROFILE);
        return profile;
    }

    /**
     * リクエストからインターフェース名を取得する.
     * 
     * @param request リクエストパラメータ
     * @return インターフェース名
     */
    public static String getInterface(final Intent request) {
        String inter = request.getExtras().getString(DConnectMessage.EXTRA_INTERFACE);
        return inter;
    }

    /**
     * リクエストから属性名を取得する.
     * 
     * @param request リクエストパラメータ
     * @return 属性名
     */
    public static String getAttribute(final Intent request) {
        String attribute = request.getExtras().getString(DConnectMessage.EXTRA_ATTRIBUTE);
        return attribute;
    }

    /**
     * レスポンスにエラーを設定する.
     * 
     * @param response レスポンスパラメータ
     * @param error エラーコード
     * @param message エラーメッセージ　
     */
    private static void setError(final Intent response, final ErrorCode error, final String message) {
        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_ERROR);
        response.putExtra(DConnectMessage.EXTRA_ERROR_CODE, error.getCode());
        response.putExtra(DConnectMessage.EXTRA_ERROR_MESSAGE, (message == null ? error.toString() : message));
    }

    /**
     * レスポンスにエラーを設定する.
     * 
     * @param response レスポンスパラメータ
     * @param error エラーコード
     */
    private static void setError(final Intent response, final ErrorCode error) {
        setError(response, error, null);
    }

    /**
     * レスポンスにエラーを設定する.
     * 
     * @param response エラーを設定するレスポンスパラメータ
     * @param errorCode エラーコード
     * @param message エラーメッセージ
     */
    public static void setError(final Intent response, final int errorCode, final String message) {
        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_ERROR);
        response.putExtra(DConnectMessage.EXTRA_ERROR_CODE, errorCode);
        response.putExtra(DConnectMessage.EXTRA_ERROR_MESSAGE, message);
    }

    /**
     * レスポンスのエラーコードに 原因不明のエラー を設定する.
     * 
     * @param response レスポンスパラメータ
     */
    public static void setUnknownError(final Intent response) {
        setError(response, ErrorCode.UNKNOWN);
    }

    /**
     * レスポンスのエラーコードに 原因不明のエラー を設定し、指定されたエラーメッセージを設定する.
     * 
     * @param response レスポンスパラメータ
     * @param message エラーメッセージ
     */
    public static void setUnknownError(final Intent response, final String message) {
        setError(response, ErrorCode.UNKNOWN, message);
    }

    /**
     * レスポンスのエラーコードに 未サポートプロファイルエラー を設定する.
     * 
     * @param response レスポンスパラメータ
     */
    public static void setNotSupportProfileError(final Intent response) {
        setError(response, ErrorCode.NOT_SUPPORT_PROFILE);
    }

    /**
     * レスポンスのエラーコードに 未サポートプロファイルエラー を設定し、指定されたエラーメッセージを設定する.
     * 
     * @param response レスポンスパラメータ
     * @param message エラーメッセージ
     */
    public static void setNotSupportProfileError(final Intent response, final String message) {
        setError(response, ErrorCode.NOT_SUPPORT_PROFILE, message);
    }

    /**
     * レスポンスのエラーコードに 未サポート属性・インターフェースエラー を設定する.
     * 
     * @param response レスポンスパラメータ
     */
    public static void setNotSupportAttributeError(final Intent response) {
        setError(response, ErrorCode.NOT_SUPPORT_ATTRIBUTE);
    }

    /**
     * レスポンスのエラーコードに 未サポート属性・インターフェースエラー を設定し、指定されたエラーメッセージを設定する.
     * 
     * @param response レスポンスパラメータ
     * @param message エラーメッセージ
     */
    public static void setNotSupportAttributeError(final Intent response, final String message) {
        setError(response, ErrorCode.NOT_SUPPORT_ATTRIBUTE, message);
    }

    /**
     * レスポンスのエラーコードに 未サポートプアクションエラー を設定する.
     * 
     * @param response レスポンスパラメータ
     */
    public static void setNotSupportActionError(final Intent response) {
        setError(response, ErrorCode.NOT_SUPPORT_ACTION);
    }

    /**
     * レスポンスのエラーコードに 未サポートアクションエラー を設定し、指定されたエラーメッセージを設定する.
     * 
     * @param response レスポンスパラメータ
     * @param message エラーメッセージ
     */
    public static void setNotSupportActionError(final Intent response, final String message) {
        setError(response, ErrorCode.NOT_SUPPORT_ACTION, message);
    }

    /**
     * レスポンスのエラーコードに サービスIDが設定されていない を設定する.
     * 
     * @param response レスポンスパラメータ
     */
    public static void setEmptyServiceIdError(final Intent response) {
        setError(response, ErrorCode.EMPTY_SERVICE_ID);
    }

    /**
     * レスポンスのエラーコードに サービスIDが設定されていない を設定し、指定されたエラーメッセージを設定する.
     * 
     * @param response レスポンスパラメータ
     * @param message エラーメッセージ
     */
    public static void setEmptyServiceIdError(final Intent response, final String message) {
        setError(response, ErrorCode.EMPTY_SERVICE_ID, message);
    }

    /**
     * レスポンスのエラーコードに サービス発見失敗 を設定する.
     * 
     * @param response レスポンスパラメータ
     */
    public static void setNotFoundServiceError(final Intent response) {
        setError(response, ErrorCode.NOT_FOUND_SERVICE);
    }

    /**
     * レスポンスのエラーコードに サービス発見失敗 を設定し、指定されたエラーメッセージを設定する.
     * 
     * @param response レスポンスパラメータ
     * @param message エラーメッセージ
     */
    public static void setNotFoundServiceError(final Intent response, final String message) {
        setError(response, ErrorCode.NOT_FOUND_SERVICE, message);
    }

    /**
     * レスポンスのエラーコードに タイムアウトエラー を設定する.
     * 
     * @param response レスポンスパラメータ
     */
    public static void setTimeoutError(final Intent response) {
        setError(response, ErrorCode.TIMEOUT);
    }

    /**
     * レスポンスのエラーコードに タイムアウトエラー を設定し、指定されたエラーメッセージを設定する.
     * 
     * @param response レスポンスパラメータ
     * @param message エラーメッセージ
     */
    public static void setTimeoutError(final Intent response, final String message) {
        setError(response, ErrorCode.TIMEOUT, message);
    }

    /**
     * レスポンスのエラーコードに 未知のインターフェース、属性へのアクセスエラー を設定する.
     * 
     * @param response レスポンスパラメータ
     */
    public static void setUnknownAttributeError(final Intent response) {
        setError(response, ErrorCode.UNKNOWN_ATTRIBUTE);
    }

    /**
     * レスポンスのエラーコードに 未知のインターフェース、属性へのアクセスエラー を設定し、指定されたエラーメッセージを設定する.
     * 
     * @param response レスポンスパラメータ
     * @param message エラーメッセージ
     */
    public static void setUnknownAttributeError(final Intent response, final String message) {
        setError(response, ErrorCode.UNKNOWN_ATTRIBUTE, message);
    }

    /**
     * レスポンスのエラーコードに バッテリー低下エラー を設定する.
     * 
     * @param response レスポンスパラメータ
     */
    public static void setLowBatteryError(final Intent response) {
        setError(response, ErrorCode.LOW_BATTERY);
    }

    /**
     * レスポンスのエラーコードに バッテリー低下エラー を設定し、指定されたエラーメッセージを設定する.
     * 
     * @param response レスポンスパラメータ
     * @param message エラーメッセージ
     */
    public static void setLowBatteryError(final Intent response, final String message) {
        setError(response, ErrorCode.LOW_BATTERY, message);
    }

    /**
     * レスポンスのエラーコードに 不正なパラメータエラー を設定する.
     * 
     * @param response レスポンスパラメータ
     */
    public static void setInvalidRequestParameterError(final Intent response) {
        setError(response, ErrorCode.INVALID_REQUEST_PARAMETER);
    }

    /**
     * レスポンスのエラーコードに 不正なパラメータエラー を設定し、指定されたエラーメッセージを設定する.
     * 
     * @param response レスポンスパラメータ
     * @param message エラーメッセージ
     */
    public static void setInvalidRequestParameterError(final Intent response, final String message) {
        setError(response, ErrorCode.INVALID_REQUEST_PARAMETER, message);
    }

    /**
     * レスポンスのエラーコードに 認証エラー を設定する.
     * 
     * @param response レスポンスパラメータ
     */
    public static void setAuthorizationError(final Intent response) {
        setError(response, ErrorCode.AUTHORIZATION);
    }

    /**
     * レスポンスのエラーコードに 認証エラー を設定し、指定されたエラーメッセージを設定する.
     * 
     * @param response レスポンスパラメータ
     * @param message エラーメッセージ
     */
    public static void setAuthorizationError(final Intent response, final String message) {
        setError(response, ErrorCode.AUTHORIZATION, message);
    }

    /**
     * レスポンスのエラーコードに アクセストークン有効期限切れエラー を設定する.
     * 
     * @param response レスポンスパラメータ
     */
    public static void setExpiredAccessTokenError(final Intent response) {
        setError(response, ErrorCode.EXPIRED_ACCESS_TOKEN);
    }

    /**
     * レスポンスのエラーコードに アクセストークン有効期限切れエラー を設定し、指定されたエラーメッセージを設定する.
     * 
     * @param response レスポンスパラメータ
     * @param message エラーメッセージ
     */
    public static void setExpiredAccessTokenError(final Intent response, final String message) {
        setError(response, ErrorCode.EXPIRED_ACCESS_TOKEN, message);
    }

    /**
     * レスポンスのエラーコードに アクセストークン有効期限切れエラー を設定する.
     * 
     * @param response レスポンスパラメータ
     */
    public static void setEmptyAccessTokenError(final Intent response) {
        setError(response, ErrorCode.EMPTY_ACCESS_TOKEN);
    }

    /**
     * レスポンスのエラーコードに アクセストークン有効期限切れエラー を設定し、指定されたエラーメッセージを設定する.
     * 
     * @param response レスポンスパラメータ
     * @param message エラーメッセージ
     */
    public static void setEmptyAccessTokenError(final Intent response, final String message) {
        setError(response, ErrorCode.EMPTY_ACCESS_TOKEN, message);
    }

    /**
     * レスポンスのエラーコードに スコープ外エラー を設定する.
     * 
     * @param response レスポンスパラメータ
     */
    public static void setScopeError(final Intent response) {
        setError(response, ErrorCode.SCOPE);
    }

    /**
     * レスポンスのエラーコードに スコープ外エラー を設定し、指定されたエラーメッセージを設定する.
     * 
     * @param response レスポンスパラメータ
     * @param message エラーメッセージ
     */
    public static void setScopeError(final Intent response, final String message) {
        setError(response, ErrorCode.SCOPE, message);
    }

    /**
     * 認証時にクライアントIDが発見できなかった場合のエラーコードを設定する.
     * @param response レスポンスパラメータ
     */
    public static void setNotFoundClientId(final Intent response) {
        setError(response, ErrorCode.NOT_FOUND_CLIENT_ID);
    }

    /**
     * 認証時にクライアントIDが発見できなかった場合のエラーコードとエラーメッセージを設定する.
     * @param response レスポンスパラメータ
     * @param message メッセージ
     */
    public static void setNotFoundClientId(final Intent response, final String message) {
        setError(response, ErrorCode.NOT_FOUND_CLIENT_ID, message);
    }

    /**
     * レスポンスのエラーコードに デバイス状態異常エラー を設定する.
     * 
     * @param response レスポンスパラメータ
     */
    public static void setIllegalDeviceStateError(final Intent response) {
        setError(response, ErrorCode.ILLEGAL_DEVICE_STATE);
    }

    /**
     * レスポンスのエラーコードに デバイス状態異常エラー を設定し、指定されたエラーメッセージを設定する.
     * 
     * @param response レスポンスパラメータ
     * @param message エラーメッセージ　
     */
    public static void setIllegalDeviceStateError(final Intent response, final String message) {
        setError(response, ErrorCode.ILLEGAL_DEVICE_STATE, message);
    }

    /**
     * レスポンスのエラーコードに サーバー状態異常エラー を設定する.
     * 
     * @param response レスポンスパラメータ
     */
    public static void setIllegalServerStateError(final Intent response) {
        setError(response, ErrorCode.ILLEGAL_SERVER_STATE);
    }

    /**
     * レスポンスのエラーコードに サーバー状態異常エラー を設定し、指定されたエラーメッセージを設定する.
     * 
     * @param response レスポンスパラメータ
     * @param message エラーメッセージ　
     */
    public static void setIllegalServerStateError(final Intent response, final String message) {
        setError(response, ErrorCode.ILLEGAL_SERVER_STATE, message);
    }

    /**
     * レスポンスのエラーコードに 不正オリジンエラー を設定する.
     * 
     * @param response レスポンスパラメータ
     */
    public static void setInvalidOriginError(final Intent response) {
        setError(response, ErrorCode.INVALID_ORIGIN);
    }

    /**
     * レスポンスのエラーコードに 不正オリジンエラー を設定し、指定されたエラーメッセージを設定する.
     * 
     * @param response レスポンスパラメータ
     * @param message エラーメッセージ　
     */
    public static void setInvalidOriginError(final Intent response, final String message) {
        setError(response, ErrorCode.INVALID_ORIGIN, message);
    }

}
