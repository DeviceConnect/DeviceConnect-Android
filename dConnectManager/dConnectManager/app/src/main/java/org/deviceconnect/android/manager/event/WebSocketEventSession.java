package org.deviceconnect.android.manager.event;


import android.content.Intent;

import org.deviceconnect.android.manager.DConnectService;

import java.io.IOException;

public class WebSocketEventSession extends EventSession {
    @Override
    public void sendEvent(final Intent event) throws IOException {
        DConnectService service = (DConnectService) getContext();
        service.sendEvent(null, event);
    }
}
