/*
 .java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.event;


import android.content.Intent;

import org.deviceconnect.android.manager.DConnectService;

import java.io.IOException;

/**
 * WebSocket上のイベントセッション.
 *
 * @author NTT DOCOMO, INC.
 */
class WebSocketEventSession extends EventSession {
    @Override
    public void sendEvent(final Intent event) throws IOException {
        DConnectService service = (DConnectService) getContext();
        service.sendEvent(null, event);
    }
}
