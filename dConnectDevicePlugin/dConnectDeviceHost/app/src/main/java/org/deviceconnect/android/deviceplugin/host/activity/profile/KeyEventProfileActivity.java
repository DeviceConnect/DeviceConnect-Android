/*
 KeyEventProfileActivity.java
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
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.activity.HostDevicePluginBindActivity;
import org.deviceconnect.android.deviceplugin.host.profile.HostKeyEventProfile;
import org.deviceconnect.android.deviceplugin.host.sensor.HostEventManager;
import org.deviceconnect.android.deviceplugin.host.sensor.HostKeyEvent;
import org.deviceconnect.profile.KeyEventProfileConstants;

/**
 * Key Event Profile Activity.
 * 
 * @author NTT DOCOMO, INC.
 */
public class KeyEventProfileActivity extends HostDevicePluginBindActivity implements OnTouchListener, OnCheckedChangeListener {
    /** Key Mode. */
    private KeyMode mKeyMode = KeyMode.STD_KEY;

    /** enum:Key Mode. */
    private enum KeyMode {
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
    private final String[] mConfigStdKey = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ".", "Enter"};

    /** Configure (Media Control). */
    private final String[] mConfigMediaCtrl = {"stop", "previous", "pause", "next", "", "", "", "", "", "", "", "play"};

    /** Configure (Directional Pad). */
    private final String[] mConfigDpad = {"", "", "down", "", "left", "center", "right", "", "up", "", "", ""};

    /** Configure (User defined). */
    private final String[] mConfigUser = {"", "", "", "", "", "", "", "", "", "", "USER_CANCEL", "USER_OK"};

    /**
     * Implementation of BroadcastReceiver.
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (HostKeyEventProfile.ACTION_FINISH_KEYEVENT_ACTIVITY.equals(intent.getAction())) {
                finish();
            }
        }
    };

    private HostEventManager mEventManager;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keyevent_main);

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
        radioGroup.check(R.id.radioButton1);
        radioGroup.setOnCheckedChangeListener(this);
    }

    @Override
    protected void onBindService() {
        super.onBindService();
        mEventManager = getHostDevicePlugin().getHostEventManager();
    }

    @Override
    public boolean onTouch(final View v, final MotionEvent event) {
        int action = event.getAction();

        // Emulate ten key down/up event.
        switch (action) {
        case MotionEvent.ACTION_DOWN:
        case MotionEvent.ACTION_UP:
            KeyEvent keyevent = null;
            int i = v.getId();
            if (i == R.id.button_0) {
                keyevent = new KeyEvent(action, KeyEvent.KEYCODE_NUMPAD_0);
            } else if (i == R.id.button_1) {
                keyevent = new KeyEvent(action, KeyEvent.KEYCODE_NUMPAD_1);
            } else if (i == R.id.button_2) {
                keyevent = new KeyEvent(action, KeyEvent.KEYCODE_NUMPAD_2);
            } else if (i == R.id.button_3) {
                keyevent = new KeyEvent(action, KeyEvent.KEYCODE_NUMPAD_3);
            } else if (i == R.id.button_4) {
                keyevent = new KeyEvent(action, KeyEvent.KEYCODE_NUMPAD_4);
            } else if (i == R.id.button_5) {
                keyevent = new KeyEvent(action, KeyEvent.KEYCODE_NUMPAD_5);
            } else if (i == R.id.button_6) {
                keyevent = new KeyEvent(action, KeyEvent.KEYCODE_NUMPAD_6);
            } else if (i == R.id.button_7) {
                keyevent = new KeyEvent(action, KeyEvent.KEYCODE_NUMPAD_7);
            } else if (i == R.id.button_8) {
                keyevent = new KeyEvent(action, KeyEvent.KEYCODE_NUMPAD_8);
            } else if (i == R.id.button_9) {
                keyevent = new KeyEvent(action, KeyEvent.KEYCODE_NUMPAD_9);
            } else if (i == R.id.button_dot) {
                keyevent = new KeyEvent(action, KeyEvent.KEYCODE_NUMPAD_DOT);
            } else if (i == R.id.button_enter) {
                keyevent = new KeyEvent(action, KeyEvent.KEYCODE_NUMPAD_ENTER);
            } else if (i == R.id.button_keyevent_close) {
                finish();
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
        RadioButton radioButton = findViewById(checkedId);

        // Change key mode.
        int i = radioButton.getId();
        if (i == R.id.radioButton1) {
            mKeyMode = KeyMode.STD_KEY;
        } else if (i == R.id.radioButton2) {
            mKeyMode = KeyMode.MEDIA_CTRL;
        } else if (i == R.id.radioButton3) {
            mKeyMode = KeyMode.DPAD_BUTTON;
        } else if (i == R.id.radioButton4) {
            mKeyMode = KeyMode.USER;
        }
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

        IntentFilter filter = new IntentFilter(HostKeyEventProfile.ACTION_FINISH_KEYEVENT_ACTIVITY);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        HostKeyEvent keyEvent = new HostKeyEvent(HostKeyEvent.STATE_KEY_DOWN,
                getKeyId(keyCode), getConfig(mKeyMode, keyCode));
        mEventManager.observeKeyEvent(keyEvent);
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(final int keyCode, final KeyEvent event) {
        HostKeyEvent keyEvent = new HostKeyEvent(HostKeyEvent.STATE_KEY_UP,
                getKeyId(keyCode), getConfig(mKeyMode, keyCode));
        mEventManager.observeKeyEvent(keyEvent);
        return super.onKeyUp(keyCode, event);
    }

    private int getKeyId(int keyCode) {
        int keyId = keyCode;
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
        return keyId;
    }

    /**
     * Get Configure string.
     *
     * @param keymode Key Mode.
     * @param keyId Key ID.
     * @return config Configure string.
     */
    private String getConfig(final KeyMode keymode, final int keyId) {
        String config;
        int nIndex;
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
