/*
 TouchProfileActivity.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.activity.profile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.widget.Button;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.activity.HostDevicePluginBindActivity;
import org.deviceconnect.android.deviceplugin.host.profile.HostTouchProfile;
import org.deviceconnect.android.deviceplugin.host.sensor.HostEventManager;
import org.deviceconnect.android.deviceplugin.host.sensor.HostTouchEvent;

/**
 * Touch Profile Activity.
 * 
 * @author NTT DOCOMO, INC.
 */
public class TouchProfileActivity extends HostDevicePluginBindActivity {
    /**
     * Implementation of BroadcastReceiver.
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (HostTouchProfile.ACTION_FINISH_TOUCH_ACTIVITY.equals(intent.getAction())) {
                finish();
            }
        }
    };

    private GestureDetector mGestureDetector;
    private HostEventManager mEventManager;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touch_main);

        mGestureDetector = new GestureDetector(this, mSimpleOnGestureListener);

        Button button = findViewById(R.id.button_touch_close);
        button.setOnClickListener((v) -> finish());
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            unregisterReceiver(mReceiver);
        } catch (Exception e) {
            // ignore.
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter(HostTouchProfile.ACTION_FINISH_TOUCH_ACTIVITY);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onBindService() {
        super.onBindService();
        mEventManager = getHostDevicePlugin().getHostEventManager();
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if (mEventManager != null) {
            mEventManager.observeTouchEvent(new HostTouchEvent(event));
        }
        return mGestureDetector.onTouchEvent(event);
    }

    private final SimpleOnGestureListener mSimpleOnGestureListener = new SimpleOnGestureListener() {

        @Override
        public boolean onDoubleTap(final MotionEvent event) {
            if (mEventManager != null) {
                mEventManager.observeTouchEvent(new HostTouchEvent(HostTouchEvent.STATE_TOUCH_DOUBLE_TAP, event));
            }
            return super.onDoubleTap(event);
        }
    };
}
