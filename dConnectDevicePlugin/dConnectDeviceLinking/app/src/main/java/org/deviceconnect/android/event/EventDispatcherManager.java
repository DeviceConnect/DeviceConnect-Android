package org.deviceconnect.android.event;

import android.content.Intent;

import java.util.HashMap;
import java.util.Map;

public class EventDispatcherManager {

    private Map<Event, EventDispatcher> mEventMap = new HashMap<>();

    public void addEventDispatcher(final Event event, final EventDispatcher dispatcher) {
        if (containsEventDispatcher(event)) {
            return;
        }
        mEventMap.put(event, dispatcher);
        dispatcher.start();
    }

    public void removeEventDispatcher(final Event event) {
        EventDispatcher dispatcher = mEventMap.remove(event);
        if (dispatcher != null) {
            dispatcher.stop();
        }
    }

    public void removeAllEventDispatcher() {
        for (Map.Entry<Event, EventDispatcher> e : mEventMap.entrySet()) {
            e.getValue().stop();
        }
        mEventMap.clear();
    }

    public boolean containsEventDispatcher(final Event event) {
        return mEventMap.containsKey(event);
    }

    public void sendEvent(final Event event, final Intent message) {
        EventDispatcher dispatcher = mEventMap.get(event);
        if (dispatcher != null) {
            dispatcher.sendEvent(event, message);
        }
    }
}
