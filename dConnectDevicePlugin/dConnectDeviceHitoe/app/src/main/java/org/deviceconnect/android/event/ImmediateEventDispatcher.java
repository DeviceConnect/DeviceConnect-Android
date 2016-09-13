package org.deviceconnect.android.event;

import android.content.Intent;

import org.deviceconnect.android.message.DConnectMessageService;

public class ImmediateEventDispatcher extends EventDispatcher {

    public ImmediateEventDispatcher(final DConnectMessageService service) {
        super(service);
    }

    @Override
    public void sendEvent(final Event event, final Intent message) {
        sendEventInternal(event, message);
    }

    @Override
    public void start() {
        // do nothing.
    }

    @Override
    public void stop() {
        // do nothing.
    }
}
