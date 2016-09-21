package org.deviceconnect.android.event;

import android.content.Intent;

import org.deviceconnect.android.message.DConnectMessageService;

public final class EventDispatcherFactory {

    private EventDispatcherFactory() {
    }

    public static EventDispatcher createEventDispatcher(final DConnectMessageService service, final Intent request) {
        if (request.getExtras().containsKey("interval")) {
            int interval = getInterval(request);
            if (interval > 0) {
                return createIntervalEventDispatcher(service, interval);
            }
        }
        return createImmediateEventDispatcher(service);
    }

    public static EventDispatcher createIntervalEventDispatcher(final DConnectMessageService service, final int periodTime) {
        return new IntervalEventDispatcher(service, periodTime, periodTime);
    }

    public static EventDispatcher createImmediateEventDispatcher(final DConnectMessageService service) {
        return new ImmediateEventDispatcher(service);
    }

    private static int getInterval(final Intent request) {
        try {
            String interval = request.getStringExtra("interval");
            return Integer.parseInt(interval);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
