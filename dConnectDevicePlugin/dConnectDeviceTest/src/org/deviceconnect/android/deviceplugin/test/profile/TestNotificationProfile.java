/*
 TestNotificationProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.test.profile;

import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.NotificationProfile;
import org.deviceconnect.message.DConnectMessage;

import android.content.Intent;

/**
 * JUnit用テストデバイスプラグイン、Notificationプロファイル.
 * 
 * @author NTT DOCOMO, INC.
 */
public class TestNotificationProfile extends NotificationProfile {
    /**
     * テスト用のnotification id.
     */
    public static final String[] NOTIFICATION_ID = {"1", "2", "3", "4", "5", };

    /**
     * サービスIDをチェックする.
     * 
     * @param serviceId サービスID
     * @return <code>serviceId</code>がテスト用サービスIDに等しい場合はtrue、そうでない場合はfalse
     */
    private boolean checkserviceId(final String serviceId) {
        return TestServiceDiscoveryProfile.SERVICE_ID.equals(serviceId);
    }

    /**
     * サービスIDが空の場合のエラーを作成する.
     * 
     * @param response レスポンスを格納するIntent
     */
    private void createEmptyserviceId(final Intent response) {
        MessageUtils.setEmptyServiceIdError(response);
    }

    /**
     * セッションキーが空の場合のエラーを作成する.
     * 
     * @param response レスポンスを格納するIntent
     */
    private void createEmptySessionKey(final Intent response) {
        MessageUtils.setInvalidRequestParameterError(response);
    }

    /**
     * デバイスが発見できなかった場合のエラーを作成する.
     * 
     * @param response レスポンスを格納するIntent
     */
    private void createNotFoundService(final Intent response) {
        MessageUtils.setNotFoundServiceError(response);
    }

    @Override
    protected boolean onPostNotify(final Intent request, final Intent response, final String serviceId,
            final NotificationType type, final Direction dir, final String lang, final String body, final String tag,
            final byte[] iconData) {
        if (serviceId == null) {
            createNotFoundService(response);
        } else if (!checkserviceId(serviceId)) {
            createEmptyserviceId(response);
        } else if (type == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
            switch (type) {
            case UNKNOWN:
                setNotificationId(response, NOTIFICATION_ID[4]);
                break;
            case PHONE:
                setNotificationId(response, NOTIFICATION_ID[0]);
                break;
            case MAIL:
                setNotificationId(response, NOTIFICATION_ID[1]);
                break;
            case SMS:
                setNotificationId(response, NOTIFICATION_ID[2]);
                break;
            case EVENT:
                setNotificationId(response, NOTIFICATION_ID[3]);
                break;
            default:
                MessageUtils.setInvalidRequestParameterError(response);
                break;
            }
        }
        return true;
    }

    @Override
    protected boolean onDeleteNotify(final Intent request, final Intent response, final String serviceId,
            final String notificationId) {
        if (serviceId == null) {
            createNotFoundService(response);
        } else if (!checkserviceId(serviceId)) {
            createEmptyserviceId(response);
        } else if (notificationId == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onPutOnClick(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyserviceId(response);
        } else if (!checkserviceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);

            Intent message = MessageUtils.createEventIntent();
            setSessionKey(message, sessionKey);
            setServiceID(message, serviceId);
            setProfile(message, getProfileName());
            setAttribute(message, ATTRIBUTE_ON_CLICK);
            setNotificationId(message, NOTIFICATION_ID[0]);
            Util.sendBroadcast(getContext(), message);
        }
        return true;
    }

    @Override
    protected boolean onPutOnClose(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyserviceId(response);
        } else if (!checkserviceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);

            Intent message = MessageUtils.createEventIntent();
            setSessionKey(message, sessionKey);
            setServiceID(message, serviceId);
            setProfile(message, getProfileName());
            setAttribute(message, ATTRIBUTE_ON_CLOSE);
            setNotificationId(message, NOTIFICATION_ID[0]);
            Util.sendBroadcast(getContext(), message);
        }
        return true;
    }

    @Override
    protected boolean onPutOnError(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyserviceId(response);
        } else if (!checkserviceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);

            Intent message = MessageUtils.createEventIntent();
            setSessionKey(message, sessionKey);
            setServiceID(message, serviceId);
            setProfile(message, getProfileName());
            setAttribute(message, ATTRIBUTE_ON_ERROR);
            setNotificationId(message, NOTIFICATION_ID[0]);
            Util.sendBroadcast(getContext(), message);
        }
        return true;
    }

    @Override
    protected boolean onPutOnShow(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyserviceId(response);
        } else if (!checkserviceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);

            Intent message = MessageUtils.createEventIntent();
            setSessionKey(message, sessionKey);
            setServiceID(message, serviceId);
            setProfile(message, getProfileName());
            setAttribute(message, ATTRIBUTE_ON_SHOW);
            setNotificationId(message, NOTIFICATION_ID[0]);
            Util.sendBroadcast(getContext(), message);
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnClick(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyserviceId(response);
        } else if (!checkserviceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnClose(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyserviceId(response);
        } else if (!checkserviceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnError(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyserviceId(response);
        } else if (!checkserviceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnShow(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyserviceId(response);
        } else if (!checkserviceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

}
