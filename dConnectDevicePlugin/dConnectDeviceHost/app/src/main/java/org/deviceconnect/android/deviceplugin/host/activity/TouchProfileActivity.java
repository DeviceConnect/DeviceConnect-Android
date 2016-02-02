/*
 TouchProfileActivity.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.activity;

import java.util.ArrayList;
import java.util.List;

import org.deviceconnect.android.deviceplugin.host.HostDeviceApplication;
import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.profile.HostTouchProfile;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.profile.TouchProfile;
import org.deviceconnect.message.DConnectMessage;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

/**
 * Touch Profile Activity.
 * 
 * @author NTT DOCOMO, INC.
 */
public class TouchProfileActivity extends Activity {

    /** Application class instance. */
    private HostDeviceApplication mApp;

    /** Gesture detector. */
    GestureDetector mGestureDetector;
    /** Service Id. */
    String mServiceId;

    /**
     * Implementation of BroadcastReceiver.
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            if (HostTouchProfile.ACTION_FINISH_TOUCH_ACTIVITY.equals(action)) {
                finish();
            }
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.touch_main);
        
        // Get Application class instance.
        mApp = (HostDeviceApplication) this.getApplication();

        // Get serviceId.
        Intent intent = getIntent();
        mServiceId = intent.getStringExtra(DConnectMessage.EXTRA_SERVICE_ID);
        // Create GestureDetector instance.
        mGestureDetector = new GestureDetector(this, mSimpleOnGestureListener);
        // onclicklistener register.
        Button button = (Button) findViewById(R.id.button_touch_close);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(HostTouchProfile.ACTION_FINISH_TOUCH_ACTIVITY);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        List<Event> events;

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN: // 1st touch only.
        case MotionEvent.ACTION_POINTER_DOWN: // Others touch.
            // "ontouch" event processing.
            events = EventManager.INSTANCE.getEventList(mServiceId, TouchProfile.PROFILE_NAME, null,
                    TouchProfile.ATTRIBUTE_ON_TOUCH);
            if (events != null) {
                sendEventData(event, events);
            }

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
            return mGestureDetector.onTouchEvent(event);
        }

        if (events != null) {
            sendEventData(event, events);
        }
        return mGestureDetector.onTouchEvent(event);
    }

    /**
     * Gesture Listener.
     */
    private final SimpleOnGestureListener mSimpleOnGestureListener = new SimpleOnGestureListener() {

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
            String attr = eventdata.getAttribute();
            Intent intent = EventManager.createEventMessage(eventdata);
            intent.putExtra(TouchProfile.PARAM_TOUCH, touches);
            getBaseContext().sendBroadcast(intent);
            mApp.setTouchCache(attr, touches);
        }
    }

}
