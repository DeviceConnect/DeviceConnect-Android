/*
 LinkingEventManager.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking;

import android.content.Intent;

import org.deviceconnect.android.event.EventManager;

import java.util.ArrayList;
import java.util.List;

public class LinkingEventManager {

    private static LinkingEventManager sInstance;
    private List<LinkingEvent> mEventList = new ArrayList<>();

    public static LinkingEventManager getInstance() {
        if (sInstance == null) {
            sInstance = new LinkingEventManager();
        }
        return sInstance;
    }

    public synchronized void add(LinkingEvent event) {
        LinkingEvent target;
        if (!mEventList.contains(event)) {
            mEventList.add(event);
            target = event;
        } else {
            target = mEventList.get(mEventList.indexOf(event));
            target.invalidate();
        }
        EventManager.INSTANCE.addEvent(target.getEventInfo());
        target.listen();
    }

    public synchronized void remove(Intent eventInfo) {
        boolean needRemove = false;
        List<LinkingEvent> removeList = new ArrayList<>();
        for (LinkingEvent event : mEventList) {
            if (event.isSameEventInfo(eventInfo)) {
                event.invalidate();
                removeList.add(event);
                needRemove = true;
            }
        }
        if (needRemove) {
            EventManager.INSTANCE.removeEvent(eventInfo);
            for (LinkingEvent event : removeList) {
                mEventList.remove(event);
            }
        }
    }

    public synchronized void removeAll() {
        EventManager.INSTANCE.removeAll();
        for (LinkingEvent event : mEventList) {
            event.invalidate();
        }
        mEventList.clear();
    }

}
