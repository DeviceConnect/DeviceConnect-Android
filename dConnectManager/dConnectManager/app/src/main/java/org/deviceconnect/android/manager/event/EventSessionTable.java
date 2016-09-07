package org.deviceconnect.android.manager.event;


import java.util.ArrayList;
import java.util.List;

public class EventSessionTable {

    private final List<EventSession> mEventSessions = new ArrayList<>();

    public List<EventSession> getAll() {
        synchronized (mEventSessions) {
            return new ArrayList<>(mEventSessions);
        }
    }

    public void add(final EventSession session) {
        synchronized(mEventSessions) {
            mEventSessions.add(session);
        }
    }

    public void remove(final EventSession session) {
        synchronized (mEventSessions) {
            mEventSessions.remove(session);
        }
    }
}
