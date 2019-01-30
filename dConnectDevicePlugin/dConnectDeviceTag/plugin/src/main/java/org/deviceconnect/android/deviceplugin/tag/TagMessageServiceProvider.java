/*
 TagMessageServiceProvider.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.tag;

import android.app.Service;

import org.deviceconnect.android.message.DConnectMessageServiceProvider;

/**
 * 旧 Device Connect Manager 用のプロバイダークラス.
 *
 * <p>
 * 旧 Device Connect Manager では、メッセージを BroadcastReceiver で受け取ってい他ので互換性のために残します。
 * </p>
 *
 * @author NTT DOCOMO, INC.
 */
public class TagMessageServiceProvider<T extends Service> extends DConnectMessageServiceProvider<Service> {
    @SuppressWarnings("unchecked")
    @Override
    protected Class<Service> getServiceClass() {
        Class<? extends Service> clazz = (Class<? extends Service>) TagMessageService.class;
        return (Class<Service>) clazz;
    }
}