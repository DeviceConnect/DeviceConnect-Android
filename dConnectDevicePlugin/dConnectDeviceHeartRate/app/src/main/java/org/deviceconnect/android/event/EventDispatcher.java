package org.deviceconnect.android.event;

import android.content.Intent;

import org.deviceconnect.android.message.DConnectMessageService;

public abstract class EventDispatcher {

    private DConnectMessageService mMessageService;

    public EventDispatcher(final DConnectMessageService service) {
        if (service == null) {
            throw new NullPointerException("service is null.");
        }
        mMessageService = service;
    }

    public abstract void sendEvent(final Event event, final Intent message);
    public abstract void start();
    public abstract void stop();

    protected void sendEventInternal(final Event event, final Intent message) {
        mMessageService.sendEvent(message, event.getAccessToken());
    }
}
