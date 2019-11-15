/*
 ChromeCastProvider.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.chromecast;

import android.app.Service;

import org.deviceconnect.android.message.DConnectMessageServiceProvider;

/**
 * サービスプロバイダー (Chromecast).
 * <p>
 * リクエストメッセージを受信し、レスポンスメッセージを送信するサービス
 * </p>
 * 
 * @param <T> Service
 * @author NTT DOCOMO, INC.
 */
public class ChromeCastProvider<T extends Service> extends DConnectMessageServiceProvider<Service> {

    @SuppressWarnings("unchecked")
    @Override
    protected Class<Service> getServiceClass() {
        Class<? extends Service> clazz = ChromeCastService.class;
        return (Class<Service>) clazz;
    }

}
