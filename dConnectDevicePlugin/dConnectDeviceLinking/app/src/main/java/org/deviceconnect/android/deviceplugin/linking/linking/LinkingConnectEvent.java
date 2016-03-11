/*
 LinkingConnectEvent.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking;

import android.content.Context;

import com.nttdocomo.android.sdaiflib.NotifyConnect;

import java.util.concurrent.atomic.AtomicBoolean;

public class LinkingConnectEvent extends LinkingEvent {

    AtomicBoolean mInvalidated = new AtomicBoolean(false);
    NotifyConnect mNotifyConnect;

    NotifyConnect.ConnectInterface mInterface = new NotifyConnect.ConnectInterface() {
        @Override
        public void onConnect() {
            if (mInvalidated.get()) {
                return;
            }
            //Do something
        }

        @Override
        public void onDisconnect() {
            if (mInvalidated.get()) {
                return;
            }
            //Do something


        }
    };

    public LinkingConnectEvent(Context context, LinkingDevice device) {
        super(context, device);
    }

    @Override
    public void listen() {
        mInvalidated.set(false);
        if (mNotifyConnect == null) {
            mNotifyConnect = new NotifyConnect(getContext(), mInterface);
        }
    }

    @Override
    public void invalidate() {
        mInvalidated.set(true);
    }

}
