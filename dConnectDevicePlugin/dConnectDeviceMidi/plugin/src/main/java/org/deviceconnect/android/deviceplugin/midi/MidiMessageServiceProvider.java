/*
 MidiMessageServiceProvider.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.midi;

import android.app.Service;

import org.deviceconnect.android.message.DConnectMessageServiceProvider;

/**
 * DConnectMessageServiceProvider の実装.
 *
 * @param <T> サービスの拡張クラス
 * @author NTT DOCOMO, INC.
 */
public class MidiMessageServiceProvider<T extends Service> extends DConnectMessageServiceProvider<Service> {
    @SuppressWarnings("unchecked")
    @Override
    protected Class<Service> getServiceClass() {
        Class<? extends Service> clazz = MidiMessageService.class;
        return (Class<Service>) clazz;
    }
}