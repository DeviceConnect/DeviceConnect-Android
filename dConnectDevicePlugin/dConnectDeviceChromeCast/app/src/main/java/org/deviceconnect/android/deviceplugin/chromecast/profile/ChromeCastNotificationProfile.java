/*
 ChromeCastNotificationProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.chromecast.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.chromecast.ChromeCastService;
import org.deviceconnect.android.deviceplugin.chromecast.core.ChromeCastMessage;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.NotificationProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.message.DConnectMessage;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Notification プロファイル (Chromecast).
 * <p>
 * Chromecastのノーティフィケーションの操作機能を提供する
 * </p>
 * @author NTT DOCOMO, INC.
 */
public class ChromeCastNotificationProfile extends NotificationProfile implements ChromeCastConstants {
    /** Chromecastが無効になっているときのエラーメッセージ. */
    private static final String ERROR_MESSAGE_DEVICE_NOT_ENABLED = "Chromecast is not enabled.";

    /** 通知ID. 全リクエストに対して共通. */
    private static final String COMMON_ID = "dConnectDeviceChromeCast";

    public ChromeCastNotificationProfile() {
        addApi(mPostNotificationApi);
        addApi(mDeleteNotificationApi);
    }


    /**
     * デバイスが有効か否かを返す<br/>.
     * デバイスが無効の場合、レスポンスにエラーを設定する
     * 
     * @param   response    レスポンス
     * @param   app         ChromeCastMediaPlayer
     * @return  デバイスが有効か否か（有効: true, 無効: false）
     */
    private boolean isDeviceEnable(final Intent response, final ChromeCastMessage app) {
        if (!app.isDeviceEnable()) {
            MessageUtils.setIllegalDeviceStateError(response, ERROR_MESSAGE_DEVICE_NOT_ENABLED);
            setResult(response, DConnectMessage.RESULT_ERROR);
            return false;
        }
        return true;
    }

    private final DConnectApi mPostNotificationApi = new PostApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_NOTIFY;
        }
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String serviceId = getServiceID(request);
            final String body = NotificationProfile.getBody(request);
            final NotificationType type = NotificationProfile.getType(request);
            ((ChromeCastService) getContext()).connectChromeCast(serviceId, (connected) -> {
                if (!connected) {
                    MessageUtils.setIllegalDeviceStateError(response, "The chromecast is not in local network.");
                    sendResponse(response);
                    return;
                }
                ChromeCastMessage app = ((ChromeCastService) getContext()).getChromeCastMessage();
                if (body == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "body is null");
                    response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_ERROR);
                    sendResponse(response);
                    return;
                }
                switch (type) {
                    case PHONE:
                        break;
                    case MAIL:
                        break;
                    case SMS:
                        break;
                    case EVENT:
                        break;
                    default:
                        MessageUtils.setInvalidRequestParameterError(response, "type is null or invalid");
                        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_ERROR);
                        sendResponse(response);
                        return;
                }

                if (!isDeviceEnable(response, app)) {
                    sendResponse(response);
                    return;
                }
                try {
                    JSONObject json = new JSONObject();
                    json.put(KEY_FUNCTION, FUNCTION_POST_NOTIFICATION);
                    json.put(KEY_TYPE, type.getValue());
                    json.put(KEY_MESSAGE, body);
                    setNotificationId(response, COMMON_ID);
                    app.sendMessage(response, json.toString());
                } catch (JSONException e) {
                    MessageUtils.setUnknownError(response);
                    sendResponse(response);
                }
            });
            return false;
        }
    };

    private final DConnectApi mDeleteNotificationApi = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_NOTIFY;
        }
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String serviceId = getServiceID(request);
            final String notificationId = NotificationProfile.getNotificationId(request);
            ((ChromeCastService) getContext()).connectChromeCast(serviceId, (connected) -> {
                if (!connected) {
                    MessageUtils.setIllegalDeviceStateError(response, "The chromecast is not in local network.");
                    sendResponse(response);
                    return;
                }
                if (notificationId == null || !COMMON_ID.equals(notificationId)) {
                    MessageUtils.setInvalidRequestParameterError(response, "notificationId is invalid.");
                    sendResponse(response);
                    return;
                }
                ChromeCastMessage app = ((ChromeCastService) getContext()).getChromeCastMessage();
                if (!isDeviceEnable(response, app)) {
                    sendResponse(response);
                    return;
                }
                try {
                    JSONObject json = new JSONObject();
                    json.put(KEY_FUNCTION, FUNCTION_DELETE_NOTIFICATION);
                    app.sendMessage(response, json.toString());
                } catch (JSONException e) {
                    MessageUtils.setUnknownError(response);
                    sendResponse(response);
                }
            });
            return false;
        }
    };

}
