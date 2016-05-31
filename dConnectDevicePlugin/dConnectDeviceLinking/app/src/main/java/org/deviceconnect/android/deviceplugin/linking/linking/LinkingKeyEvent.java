/*
 LinkingKeyEvent.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking;

import android.content.Context;
import android.os.Bundle;

public class LinkingKeyEvent extends LinkingEvent {

    public final static String EXTRA_KEY_CODE = "keyEvent";
    private LinkingManager mManager;

    private LinkingManager.KeyEventListener mListener = new LinkingManager.KeyEventListener() {
        @Override
        public void onKeyEvent(LinkingDevice device, int keyCode) {
            Bundle parameters = new Bundle();
            parameters.putInt(EXTRA_KEY_CODE, keyCode);
            sendEvent(device, parameters);
        }
    };

    public LinkingKeyEvent(Context context, LinkingDevice device) {
        super(context, device);
        mManager = LinkingManagerFactory.createManager(context);
    }

    @Override
    public void listen() {
        mManager.setKeyEventListener(mListener);
    }

    @Override
    public void invalidate() {
        mManager.setKeyEventListener(null);
    }
}
