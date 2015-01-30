/*
 TouchActivity.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.activity;

import java.util.ArrayList;
import java.util.List;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.profile.TouchProfile;
import org.deviceconnect.message.DConnectMessage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

/**
 * Touch Activity.
 * 
 * @author NTT DOCOMO, INC.
 */
public class TouchActivity extends Activity {

    /** Gesture detector. */
    GestureDetector gestureDetector;
    /** Service Id. */
    String mServiceId;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.touch_main);

        // Get serviceId.
        Intent intent = getIntent();
        mServiceId = intent.getStringExtra(DConnectMessage.EXTRA_SERVICE_ID);
        // Create GestureDetector instance.
        gestureDetector = new GestureDetector(this, simpleOnGestureListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        List<Event> events;

        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN: // 1st touch only.
        case MotionEvent.ACTION_POINTER_DOWN: // Others touch.
            // "ontouch" event processing.
            events = EventManager.INSTANCE.getEventList(mServiceId, TouchProfile.PROFILE_NAME, null,
                    TouchProfile.ATTRIBUTE_ON_TOUCH);
            sendEventData(event, events);

            // "ontouchstart" event processing.
            events = EventManager.INSTANCE.getEventList(mServiceId, TouchProfile.PROFILE_NAME, null,
                    TouchProfile.ATTRIBUTE_ON_TOUCH_START);
            break;
        case MotionEvent.ACTION_UP: // Last touch remove only.
        case MotionEvent.ACTION_POINTER_UP: // Others touch move.
            // "ontouchend" event processing.
            events = EventManager.INSTANCE.getEventList(mServiceId, TouchProfile.PROFILE_NAME, null,
                    TouchProfile.ATTRIBUTE_ON_TOUCH_END);
            break;
        case MotionEvent.ACTION_MOVE:
            // "ontouchmove" event processing.
            events = EventManager.INSTANCE.getEventList(mServiceId, TouchProfile.PROFILE_NAME, null,
                    TouchProfile.ATTRIBUTE_ON_TOUCH_MOVE);
            break;
        case MotionEvent.ACTION_CANCEL:
            // "ontouchcancel" event processing.
            events = EventManager.INSTANCE.getEventList(mServiceId, TouchProfile.PROFILE_NAME, null,
                    TouchProfile.ATTRIBUTE_ON_TOUCH_CANCEL);
            break;
        default:
            return gestureDetector.onTouchEvent(event);
        }

        sendEventData(event, events);
        return gestureDetector.onTouchEvent(event);
    }

    /**
     * Gesture Listener.
     */
    private final SimpleOnGestureListener simpleOnGestureListener = new SimpleOnGestureListener() {

        @Override
        public boolean onDoubleTap(final MotionEvent event) {
            List<Event> events = EventManager.INSTANCE.getEventList(mServiceId, TouchProfile.PROFILE_NAME, null,
                    TouchProfile.ATTRIBUTE_ON_DOUBLE_TAP);

            sendEventData(event, events);
            return super.onDoubleTap(event);
        }
    };

    /**
     * Send event data.
     * 
     * @param event MotionEvent.
     * @param events Event request list.
     */
    private void sendEventData(final MotionEvent event, final List<Event> events) {

        for (int i = 0; i < events.size(); i++) {
            Bundle touchdata = new Bundle();
            List<Bundle> touchlist = new ArrayList<Bundle>();
            Bundle touches = new Bundle();
            for (int n = 0; n < event.getPointerCount(); n++) {
                int pointerId = event.getPointerId(n);
                touchdata.putInt(TouchProfile.PARAM_ID, pointerId);
                touchdata.putFloat(TouchProfile.PARAM_X, event.getX(n));
                touchdata.putFloat(TouchProfile.PARAM_Y, event.getY(n));
                touchlist.add((Bundle) touchdata.clone());
            }
            touches.putParcelableArray(TouchProfile.PARAM_TOUCHES, touchlist.toArray(new Bundle[touchlist.size()]));
            Event eventdata = events.get(i);
            Intent intent = EventManager.createEventMessage(eventdata);
            intent.putExtra(TouchProfile.PARAM_TOUCH, touches);
            getBaseContext().sendBroadcast(intent);
        }
    }

}
