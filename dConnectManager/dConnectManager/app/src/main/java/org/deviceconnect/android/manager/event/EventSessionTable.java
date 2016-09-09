package org.deviceconnect.android.manager.event;


import org.deviceconnect.android.manager.DevicePlugin;

import java.util.ArrayList;
import java.util.List;

class EventSessionTable {

    private final List<EventSession> mEventSessions = new ArrayList<>();

    public List<EventSession> getAll() {
        synchronized (mEventSessions) {
            return new ArrayList<>(mEventSessions);
        }
    }

    List<EventSession> findEventSessionsForPlugin(final DevicePlugin plugin) {
        List<EventSession> result = new ArrayList<>();
        synchronized (mEventSessions) {
            for (EventSession session : mEventSessions) {
                if (plugin.getServiceId().equals(session.getPluginId())) {
                    result.add(session);
                }
            }
        }
        return result;
    }

    void add(final EventSession session) {
        synchronized(mEventSessions) {
            mEventSessions.add(session);
        }
    }

    void remove(final EventSession session) {
        synchronized (mEventSessions) {
            mEventSessions.remove(session);
        }
    }
}
