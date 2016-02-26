/*
 LinkingEvent.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.message.DConnectMessage;

import java.util.List;

abstract public class LinkingEvent {

    private Intent mEventInfo;
    private Context mContext;
    private LinkingEventListener mListener;
    private LinkingDevice mDevice;

    abstract public void listen();

    abstract public void invalidate();

    public LinkingEvent(Context context, LinkingDevice device) {
        mContext = context;
        mDevice = device;
    }

    public void setEventInfo(Intent intent) {
        checkEventInfo(intent);
        mEventInfo = intent;
    }

    public Intent getEventInfo() {
        return mEventInfo;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        LinkingEvent event = (LinkingEvent) obj;
        Intent eventInfo = event.getEventInfo();
        return isSameEventInfo(eventInfo);
    }

    public boolean isSameEventInfo(Intent eventInfo) {
        if (mEventInfo == eventInfo) {
            return true;
        }
        if (mEventInfo == null || eventInfo == null) {
            return false;
        }
        if (!hasSameExtra(mEventInfo, eventInfo, DConnectMessage.EXTRA_SERVICE_ID)) {
            return false;
        }
        if (!hasSameExtra(mEventInfo, eventInfo, DConnectMessage.EXTRA_PROFILE)) {
            return false;
        }
        if (!hasSameExtra(mEventInfo, eventInfo, DConnectMessage.EXTRA_INTERFACE)) {
            return false;
        }
        if (!hasSameExtra(mEventInfo, eventInfo, DConnectMessage.EXTRA_ATTRIBUTE)) {
            return false;
        }
        if (!hasSameExtra(mEventInfo, eventInfo, DConnectMessage.EXTRA_SESSION_KEY)) {
            return false;
        }
        if (!hasSameExtra(mEventInfo, eventInfo, DConnectMessage.EXTRA_ACCESS_TOKEN)) {
            return false;
        }
        return true;
    }

    public void setLinkingEventListener(LinkingEventListener listener) {
        mListener = listener;
    }

    protected void sendEvent(LinkingDevice device, Bundle parameters) {
        if (!mDevice.getBdAddress().equals(device.getBdAddress())) {
            return;
        }
        String serviceId = mEventInfo.getStringExtra(DConnectMessage.EXTRA_SERVICE_ID);
        String profileName = mEventInfo.getStringExtra(DConnectMessage.EXTRA_PROFILE);
        String attributeName = mEventInfo.getStringExtra(DConnectMessage.EXTRA_ATTRIBUTE);
        String interfaceName = mEventInfo.getStringExtra(DConnectMessage.EXTRA_INTERFACE);
        List<Event> eventList = EventManager.INSTANCE.getEventList(serviceId, profileName, interfaceName, attributeName);
        if (eventList.size() == 0) {
            if (mListener != null) {
                mListener.onReceiveEvent(null, parameters);
            }
            return;
        }
        for (Event event : eventList) {
            if (mListener != null) {
                mListener.onReceiveEvent(event, parameters);
            }
        }
    }

    protected Context getContext() {
        return mContext;
    }

    protected LinkingDevice getDevice() {
        return mDevice;
    }

    private boolean hasSameExtra(Intent lhs, Intent rhs, String name) {
        String lhsExtra = lhs.getStringExtra(name);
        String rhsExtra = rhs.getStringExtra(name);
        if (lhsExtra == null) {
            return rhsExtra == null;
        }
        if (rhsExtra == null) {
            return false;
        }
        return lhsExtra.equals(rhsExtra);
    }

    private void checkEventInfo(Intent eventInfo) {
        if (eventInfo == null) {
            throw new IllegalArgumentException("eventInfo must be specified");
        }
        String serviceId = eventInfo.getStringExtra(DConnectMessage.EXTRA_SERVICE_ID);
        if (serviceId == null || serviceId.equals("")) {
            throw new IllegalArgumentException("serviceId must be specified");
        }
        String profileName = eventInfo.getStringExtra(DConnectMessage.EXTRA_PROFILE);
        if (profileName == null || profileName.equals("")) {
            throw new IllegalArgumentException("profile must be specified");
        }
        String attributeName = eventInfo.getStringExtra(DConnectMessage.EXTRA_ATTRIBUTE);
        if (attributeName == null || attributeName.equals("")) {
            throw new IllegalArgumentException("attribute must be specified");
        }
        String accessToken = eventInfo.getStringExtra(DConnectMessage.EXTRA_ACCESS_TOKEN);
        if (accessToken == null || accessToken.equals("")) {
            throw new IllegalArgumentException("accessToken must be specified");
        }
    }

}
