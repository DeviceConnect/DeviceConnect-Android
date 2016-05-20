/*
 BotProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.Intent;

import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.profile.BotProfileConstants;

/**
 * Bot Profile.
 * @author NTT DOCOMO, INC.
 */
public class BotProfile extends DConnectProfile implements BotProfileConstants {

    @Override
    public final String getProfileName() {
        return PROFILE_NAME;
    }

    @Override
    protected boolean onGetRequest(final Intent request, final Intent response) {
        String interfaceName = getInterface(request);
        String attributeName = getAttribute(request);
        if (interfaceName == null && ATTRIBUTE_CHANNEL.equals(attributeName)) {
            return onGetChannel(request, response, getServiceID(request));
        }
        MessageUtils.setUnknownAttributeError(response);
        return true;
    }

    @Override
    protected boolean onPostRequest(final Intent request, final Intent response) {
        String attribute = getAttribute(request);
        boolean result = true;

        if (ATTRIBUTE_MESSAGE.equals(attribute)) {
            String channel = request.getStringExtra(PARAM_CHANNEL);
            String text = request.getStringExtra(PARAM_TEXT);
            String resource = request.getStringExtra(PARAM_RESOURCE);
            String mime = request.getStringExtra(PARAM_MIME_TYPE);
            result = onPostMessage(request, response, getServiceID(request), channel, text, resource, mime);
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }

        return result;
    }

    @Override
    protected boolean onPutRequest(final Intent request, final Intent response) {

        String attribute = getAttribute(request);
        boolean result = true;

        if (attribute == null) {
            MessageUtils.setUnknownAttributeError(response);
        } else {

            String serviceId = getServiceID(request);
            String sessionKey = getSessionKey(request);

            if (ATTRIBUTE_MESSAGE.equals(attribute)) {
                result = onPutOnMessageReceived(request, response, serviceId, sessionKey);
            } else {
                MessageUtils.setUnknownAttributeError(response);
            }
        }

        return result;
    }

    @Override
    protected boolean onDeleteRequest(final Intent request, final Intent response) {

        String attribute = getAttribute(request);
        boolean result = true;
        if (attribute == null) {
            MessageUtils.setUnknownAttributeError(response);
        } else {
            String serviceId = getServiceID(request);
            String sessionKey = getSessionKey(request);

            if (ATTRIBUTE_MESSAGE.equals(attribute)) {
                result = onDeleteOnMessageReceived(request, response, serviceId, sessionKey);
            } else {
                MessageUtils.setUnknownAttributeError(response);
            }
        }

        return result;
    }

    /**
     * Botが投稿するチャンネル取得リクエストハンドラー.
     * <p>
     * レスポンスパラメータの送信準備が出来た場合は返り値にtrueを指定する事。<br>
     * 送信準備ができていない場合は、返り値にfalseを指定し、スレッドを立ち上げてそのスレッドで最終的にレスポンスパラメータの送信を行う事。
     * </p>
     *
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @param serviceId サービスID
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onGetChannel(final Intent request, final Intent response, final String serviceId) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * メッセージ送信リクエストハンドラー.
     * <p>
     * レスポンスパラメータの送信準備が出来た場合は返り値にtrueを指定する事。<br>
     * 送信準備ができていない場合は、返り値にfalseを指定し、スレッドを立ち上げてそのスレッドで最終的にレスポンスパラメータの送信を行う事。
     * </p>
     *
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @param serviceId サービスID
     * @param channel 送信先チャンネルID
     * @param text 送信文字列
     * @param resource リソースURI
     * @param mimeType リソースのタイプ
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onPostMessage(final Intent request, final Intent response, final String serviceId,
                                    final String channel, final String text, final String resource, final String mimeType) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * メッセージ受信コールバック登録リクエストハンドラー.
     * <p>
     * レスポンスパラメータの送信準備が出来た場合は返り値にtrueを指定する事。<br>
     * 送信準備ができていない場合は、返り値にfalseを指定し、スレッドを立ち上げてそのスレッドで最終的にレスポンスパラメータの送信を行う事。
     * </p>
     *
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @param serviceId サービスID
     * @param sessionKey セッションキー
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onPutOnMessageReceived(final Intent request, final Intent response, final String serviceId,
                                           final String sessionKey) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * メッセージ受信コールバック解除リクエストハンドラー.
     * <p>
     * レスポンスパラメータの送信準備が出来た場合は返り値にtrueを指定する事。<br>
     * 送信準備ができていない場合は、返り値にfalseを指定し、スレッドを立ち上げてそのスレッドで最終的にレスポンスパラメータの送信を行う事。
     * </p>
     *
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @param serviceId サービスID
     * @param sessionKey セッションキー
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onDeleteOnMessageReceived(final Intent request, final Intent response, final String serviceId,
                                              final String sessionKey) {
        setUnsupportedError(response);
        return true;
    }

}
