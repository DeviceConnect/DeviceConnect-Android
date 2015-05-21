/*
 KeyEventProfileActivity.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.activity;

import java.util.List;

import org.deviceconnect.android.deviceplugin.host.HostDeviceApplication;
import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.profile.HostKeyEventProfile;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.profile.KeyEventProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.profile.KeyEventProfileConstants;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

/**
 * Key Event Profile Activity.
 * 
 * @author NTT DOCOMO, INC.
 */
public class KeyEventProfileActivity extends Activity implements OnTouchListener, OnCheckedChangeListener {

    /** Application class instance. */
    private HostDeviceApplication mApp;
    /** Service Id. */
    String mServiceId;
    /** Key Mode. */
    KeyMode mKeyMode;

    /** enum:Key Mode. */
    public enum KeyMode {
        /** Standard Keyboard. */
        STD_KEY,
        /** Media Control. */
        MEDIA_CTRL,
        /** Directional Pad / Button. */
        DPAD_BUTTON,
        /** User defined. */
        USER;
    }

    /** Configure (Standard Keyboard). */
    String[] mConfigStdKey = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ".", "Enter"};
    /** Configure (Media Control). */
    String[] mConfigMediaCtrl = {"stop", "previous", "pause", "next", "", "", "", "", "", "", "", "play"};
    /** Configure (Directional Pad). */
    String[] mConfigDpad = {"", "", "down", "", "left", "center", "right", "", "up", "", "", ""};
    /** Configure (User defined). */
    String[] mConfigUser = {"", "", "", "", "", "", "", "", "", "", "USER_CANCEL", "USER_OK"};

    /**
     * Implementation of BroadcastReceiver.
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            if (HostKeyEventProfile.ACTION_FINISH_KEYEVENT_ACTIVITY.equals(action)) {
                finish();
            }
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.keyevent_main);

        // Get Application class instance.
        mApp = (HostDeviceApplication) this.getApplication();

        // Set button touchlistener. (Ten Key Emulated)
        findViewById(R.id.button_0).setOnTouchListener(this);
        findViewById(R.id.button_1).setOnTouchListener(this);
        findViewById(R.id.button_2).setOnTouchListener(this);
        findViewById(R.id.button_3).setOnTouchListener(this);
        findViewById(R.id.button_4).setOnTouchListener(this);
        findViewById(R.id.button_5).setOnTouchListener(this);
        findViewById(R.id.button_6).setOnTouchListener(this);
        findViewById(R.id.button_7).setOnTouchListener(this);
        findViewById(R.id.button_8).setOnTouchListener(this);
        findViewById(R.id.button_9).setOnTouchListener(this);
        findViewById(R.id.button_dot).setOnTouchListener(this);
        findViewById(R.id.button_enter).setOnTouchListener(this);
        findViewById(R.id.button_keyevent_close).setOnTouchListener(this);

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.RadioGroup);
        // Set default select radio button.
        radioGroup.check(R.id.radioButton1);
        mKeyMode = KeyMode.STD_KEY;
        // set radiogroup changelistener
        radioGroup.setOnCheckedChangeListener(this);

        // Get serviceId.
        Intent intent = getIntent();
        mServiceId = intent.getStringExtra(DConnectMessage.EXTRA_SERVICE_ID);
    }

    @Override
    public boolean onTouch(final View v, final MotionEvent event) {
        int action = event.getAction();

        // Emulate ten key down/up event.
        switch (action) {
        case MotionEvent.ACTION_DOWN:
        case MotionEvent.ACTION_UP:
            KeyEvent keyevent = null;
            switch (v.getId()) {
            case R.id.button_0:
                keyevent = new KeyEvent(action, KeyEvent.KEYCODE_NUMPAD_0);
                break;
            case R.id.button_1:
                keyevent = new KeyEvent(action, KeyEvent.KEYCODE_NUMPAD_1);
                break;
            case R.id.button_2:
                keyevent = new KeyEvent(action, KeyEvent.KEYCODE_NUMPAD_2);
                break;
            case R.id.button_3:
                keyevent = new KeyEvent(action, KeyEvent.KEYCODE_NUMPAD_3);
                break;
            case R.id.button_4:
                keyevent = new KeyEvent(action, KeyEvent.KEYCODE_NUMPAD_4);
                break;
            case R.id.button_5:
                keyevent = new KeyEvent(action, KeyEvent.KEYCODE_NUMPAD_5);
                break;
            case R.id.button_6:
                keyevent = new KeyEvent(action, KeyEvent.KEYCODE_NUMPAD_6);
                break;
            case R.id.button_7:
                keyevent = new KeyEvent(action, KeyEvent.KEYCODE_NUMPAD_7);
                break;
            case R.id.button_8:
                keyevent = new KeyEvent(action, KeyEvent.KEYCODE_NUMPAD_8);
                break;
            case R.id.button_9:
                keyevent = new KeyEvent(action, KeyEvent.KEYCODE_NUMPAD_9);
                break;
            case R.id.button_dot:
                keyevent = new KeyEvent(action, KeyEvent.KEYCODE_NUMPAD_DOT);
                break;
            case R.id.button_enter:
                keyevent = new KeyEvent(action, KeyEvent.KEYCODE_NUMPAD_ENTER);
                break;
            case R.id.button_keyevent_close:
                finish();
                break;
            default:
                break;
            }
            if (keyevent != null) {
                dispatchKeyEvent(keyevent);
            }
            break;
        default:
            break;
        }

        return false;
    }

    @Override
    public void onCheckedChanged(final RadioGroup group, final int checkedId) {
        RadioButton radioButton = (RadioButton) findViewById(checkedId);

        // Change key mode.
        switch (radioButton.getId()) {
        case R.id.radioButton1:
            mKeyMode = KeyMode.STD_KEY;
            break;
        case R.id.radioButton2:
            mKeyMode = KeyMode.MEDIA_CTRL;
            break;
        case R.id.radioButton3:
            mKeyMode = KeyMode.DPAD_BUTTON;
            break;
        case R.id.radioButton4:
            mKeyMode = KeyMode.USER;
            break;
        default:
            break;
        }
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
        filter.addAction(HostKeyEventProfile.ACTION_FINISH_KEYEVENT_ACTIVITY);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
    }

    @Override
    public boolean dispatchKeyEvent(final KeyEvent event) {
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        // "ondown" event processing.
        List<Event> events = EventManager.INSTANCE.getEventList(mServiceId, KeyEventProfile.PROFILE_NAME, null,
                KeyEventProfile.ATTRIBUTE_ON_DOWN);
        sendEventData(keyCode, events);
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(final int keyCode, final KeyEvent event) {
        // "onup" event processing.
        List<Event> events = EventManager.INSTANCE.getEventList(mServiceId, KeyEventProfile.PROFILE_NAME, null,
                KeyEventProfile.ATTRIBUTE_ON_UP);
        sendEventData(keyCode, events);
        return super.onKeyUp(keyCode, event);
    }

    /**
     * Send event data.
     * 
     * @param keycode key Code.
     * @param events Event request list.
     */
    private void sendEventData(final int keycode, final List<Event> events) {

        for (int i = 0; i < events.size(); i++) {
            Bundle keyevent = new Bundle();
            int keyId = keycode;
            String keyConfig = "";

            // Get configure string.
            keyConfig = getConfig(mKeyMode, keyId);

            // Set key type.
            switch (mKeyMode) {
            case MEDIA_CTRL:
                keyId += KeyEventProfileConstants.KEYTYPE_MEDIA_CTRL;
                break;
            case DPAD_BUTTON:
                keyId += KeyEventProfileConstants.KEYTYPE_DPAD_BUTTON;
                break;
            case USER:
                keyId += KeyEventProfileConstants.KEYTYPE_USER;
                break;
            case STD_KEY:
            default:
                keyId += KeyEventProfileConstants.KEYTYPE_STD_KEY;
                break;
            }

            keyevent.putInt(KeyEventProfile.PARAM_ID, keyId);
            keyevent.putString(KeyEventProfile.PARAM_CONFIG, keyConfig);

            Event eventdata = events.get(i);
            String attr = eventdata.getAttribute();
            Intent intent = EventManager.createEventMessage(eventdata);
            intent.putExtra(KeyEventProfile.PARAM_KEYEVENT, keyevent);
            getBaseContext().sendBroadcast(intent);
            mApp.setKeyEventCache(attr, keyevent);
        }
    }

    /**
     * Get Configure string.
     * 
     * @param keymode Key Mode.
     * @param keyId Key ID.
     * @return config Configure string.
     */
    private String getConfig(final KeyMode keymode, final int keyId) {
        String config = "";
        int nIndex = -1;
        switch (keyId) {
        case KeyEvent.KEYCODE_NUMPAD_0:
            nIndex = 0;
            break;
        case KeyEvent.KEYCODE_NUMPAD_1:
            nIndex = 1;
            break;
        case KeyEvent.KEYCODE_NUMPAD_2:
            nIndex = 2;
            break;
        case KeyEvent.KEYCODE_NUMPAD_3:
            nIndex = 3;
            break;
        case KeyEvent.KEYCODE_NUMPAD_4:
            nIndex = 4;
            break;
        case KeyEvent.KEYCODE_NUMPAD_5:
            nIndex = 5;
            break;
        case KeyEvent.KEYCODE_NUMPAD_6:
            nIndex = 6;
            break;
        case KeyEvent.KEYCODE_NUMPAD_7:
            nIndex = 7;
            break;
        case KeyEvent.KEYCODE_NUMPAD_8:
            nIndex = 8;
            break;
        case KeyEvent.KEYCODE_NUMPAD_9:
            nIndex = 9;
            break;
        case KeyEvent.KEYCODE_NUMPAD_DOT:
            nIndex = 10;
            break;
        case KeyEvent.KEYCODE_NUMPAD_ENTER:
            nIndex = 11;
            break;
        default:
            nIndex = -1;
            break;
        }
        if (nIndex != -1) {
            switch (mKeyMode) {
            case MEDIA_CTRL:
                config = mConfigMediaCtrl[nIndex];
                break;
            case DPAD_BUTTON:
                config = mConfigDpad[nIndex];
                break;
            case USER:
                config = mConfigUser[nIndex];
                break;
            case STD_KEY:
            default:
                config = mConfigStdKey[nIndex];
                break;
            }
        } else {
            config = "";
        }

        return config;
    }
}
