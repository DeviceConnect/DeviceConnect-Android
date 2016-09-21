/*
 SlackMessageHookDeviceProvider.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.slackmessagehook;

import android.app.Service;

import org.deviceconnect.android.message.DConnectMessageServiceProvider;

/**
 * Device Connect Manager本体からのインテントを受信するクラス.
 * @author NTT DOCOMO, INC.
 * @param <T> SlackBotデバイスプラグインのプロファイルを公開するサービスのクラス
 */
public class SlackMessageHookDeviceProvider<T extends Service> extends DConnectMessageServiceProvider<Service> {

    @SuppressWarnings("unchecked")
    @Override
    protected Class<Service> getServiceClass() {
        Class<? extends Service> clazz = SlackMessageHookDeviceService.class;
        return (Class<Service>) clazz;
    }
}