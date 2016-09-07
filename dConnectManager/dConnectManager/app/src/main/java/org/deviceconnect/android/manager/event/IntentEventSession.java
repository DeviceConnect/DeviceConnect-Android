package org.deviceconnect.android.manager.event;


import android.content.ComponentName;
import android.content.Intent;

import java.io.IOException;

public class IntentEventSession extends EventSession {

    private ComponentName mBroadcastReceiver;

    public ComponentName getBroadcastReceiver() {
        return mBroadcastReceiver;
    }

    void setBroadcastReceiver(final ComponentName broadcastReceiver) {
        mBroadcastReceiver = broadcastReceiver;
    }

    @Override
    public void sendEvent(final Intent event) throws IOException {
        event.setComponent(mBroadcastReceiver);
        getContext().sendBroadcast(event);
    }
}
