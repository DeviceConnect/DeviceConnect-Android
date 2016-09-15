/*
WearKeyEventProfileActivity.java
Copyright (c) 2015 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.wear.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.wearable.view.WatchViewStub;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import org.deviceconnect.android.deviceplugin.wear.R;
import org.deviceconnect.android.deviceplugin.wear.WearConst;

/**
 * WearKeyEventProfileActivity.
 *
 * @author NTT DOCOMO, INC.
 */
public class WearKeyEventProfileActivity extends Activity {
    /** Broadcast receiver. */
    MyBroadcastReceiver mReceiver;

    /** Intent filter. */
    IntentFilter mIntentFilter;

    /** Event flag. */
    private int mRegisterEvent = 0;
    /** Event flag define (down). */
    private static final int REGIST_FLAG_KEYEVENT_DOWN = 0x01;
    /** Event flag define (up). */
    private static final int REGIST_FLAG_KEYEVENT_UP = 0x02;

    /** Button define. */
    private Button mBtnKeyMode, mBtnCancel, mBtnOk;
    /** Key mode. */
    private int mKeyMode = 0;

    /** Key mode count. */
    private static final int KM_MAX_CNT = 4;
    /** Key mode (Standard Keyboard). */
    private static final int KM_STD_KEY = 0;
    /** Key mode (Media Control). */
    private static final int KM_MEDIA_CTRL = 1;
    /** Key mode (Direction PAd / Button ). */
    private static final int KM_DPAD_BUTTON = 2;
    /** Key mode (User Define). */
    private static final int KM_USER = 3;

    /** Key Code define(cancel). */
    private static final int KEYCODE_CANCEL = 0;
    /** Key Code define(ok). */
    private static final int KEYCODE_OK = 1;

    /** Configure (Standard Keyboard). */
    private static final String[] CONFIG_STD_KEY =
            {"Cancel", "OK"};
    /** Configure (Media Control). */
    private static final String[] CONFIG_MEDIA_CTRL =
            {"stop", "play"};
    /** Configure (Directional Pad). */
    private static final String[] CONFIG_DPAD =
            {"up", "down"};
    /** Configure (User defined). */
    private static final String[] CONFIG_USER =
            {"USER_CANCEL", "USER_OK"};

    /**
     * Wakelock.
     */
    private PowerManager.WakeLock mWakeLock;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                | PowerManager.FULL_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TouchWakelockTag");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }
        // Get intent data.
        Intent intent = getIntent();
        setRegisterEvent(intent.getStringExtra(WearConst.PARAM_KEYEVENT_REGIST));

        setContentView(R.layout.activity_wear_keyevent_profile);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(final WatchViewStub stub) {

                mBtnKeyMode = (Button) stub.findViewById(R.id.button_key_mode);
                mBtnKeyMode.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        // Update Key Mode.
                        mKeyMode++;
                        if (mKeyMode >= KM_MAX_CNT) {
                            mKeyMode = KM_STD_KEY;
                        }

                        String keyMode;
                        switch (mKeyMode) {
                            case KM_MEDIA_CTRL:
                                keyMode = getString(R.string.key_mode_media_ctrl);
                                break;
                            case KM_DPAD_BUTTON:
                                keyMode = getString(R.string.key_mode_dpad_button);
                                break;
                            case KM_USER:
                                keyMode = getString(R.string.key_mode_user);
                                break;
                            case KM_STD_KEY:
                            default:
                                keyMode = getString(R.string.key_mode_std_key);
                                break;
                        }
                        mBtnKeyMode.setText(keyMode);
                    }
                });

                mBtnCancel = (Button) stub.findViewById(R.id.button_cancel);
                mBtnCancel.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(final View view, final MotionEvent event) {
                        int action = event.getAction();
                        switch (action) {
                            case MotionEvent.ACTION_DOWN:
                            case MotionEvent.ACTION_UP:
                                sendMessageData(action, KEYCODE_CANCEL);
                                break;
                            default:
                                break;
                        }
                        return false;
                    }
                });

                mBtnOk = (Button) stub.findViewById(R.id.button_ok);
                mBtnOk.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(final View view, final MotionEvent event) {
                        int action = event.getAction();
                        switch (action) {
                            case MotionEvent.ACTION_DOWN:
                            case MotionEvent.ACTION_UP:
                                sendMessageData(action, KEYCODE_OK);
                                break;
                            default:
                                break;
                        }
                        return false;
                    }
                });

            }
        });

        mReceiver = new MyBroadcastReceiver();
        mIntentFilter = new IntentFilter(WearConst.PARAM_DC_WEAR_KEYEVENT_SVC_TO_ACT);

        // For service destruction suppression.
        Intent i = new Intent(WearConst.ACTION_WEAR_PING_SERVICE);
        startService(i);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWakeLock.release();
    }

    /**
     * Get Configure string.
     *
     * @param keyMode Key Mode.
     * @param keyId Key ID.
     * @return config Configure string.
     */
    private String getConfig(final int keyMode, final int keyId) {
        String config;
        int nIndex;
        switch (keyId) {
            case KEYCODE_CANCEL: nIndex = 0;    break;
            case KEYCODE_OK:     nIndex = 1;    break;
            default:             nIndex = -1;   break;
        }
        if (nIndex != -1) {
            switch (keyMode) {
                case KM_MEDIA_CTRL:     config = CONFIG_MEDIA_CTRL[nIndex];   break;
                case KM_DPAD_BUTTON:    config = CONFIG_DPAD[nIndex];        break;
                case KM_USER:           config = CONFIG_USER[nIndex];        break;
                case KM_STD_KEY:
                default:                config = CONFIG_STD_KEY[nIndex];      break;
            }
        } else {
            config = "";
        }

        return config;
    }

    /**
     * Send message data.
     *
     * @param action MotionEvent action.
     * @param keyId Key ID.
     */
    private void sendMessageData(final int action, final int keyId) {
        int keycode = keyId;
        String keyConfig = getConfig(mKeyMode, keycode);
        String keyAction;

        if (action == MotionEvent.ACTION_DOWN) {
            if ((mRegisterEvent & REGIST_FLAG_KEYEVENT_DOWN) == 0) {
                return;
            }
            keyAction = WearConst.PARAM_KEYEVENT_DOWN;
        } else {
            if ((mRegisterEvent & REGIST_FLAG_KEYEVENT_UP) == 0) {
                return;
            }
            keyAction = WearConst.PARAM_KEYEVENT_UP;
        }

        switch (mKeyMode) {
            case KM_MEDIA_CTRL:
                keycode += WearConst.KEYTYPE_MEDIA_CTRL;
                break;
            case KM_DPAD_BUTTON:
                keycode += WearConst.KEYTYPE_DPAD_BUTTON;
                break;
            case KM_USER:
                keycode += WearConst.KEYTYPE_USER;
                break;
            case KM_STD_KEY:
            default:
                keycode += WearConst.KEYTYPE_STD_KEY;
                break;
        }

        String data = keyAction + "," + String.valueOf(keycode) + "," + keyConfig;

        // Send key event data to service.
        Intent i = new Intent(WearConst.PARAM_DC_WEAR_KEYEVENT_ACT_TO_SVC);
        i.putExtra(WearConst.PARAM_KEYEVENT_DATA, data);
        sendBroadcast(i);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * Set register event.
     *
     * @param regist Request event.
     */
    private void setRegisterEvent(final String regist) {
        if (WearConst.DEVICE_TO_WEAR_KEYEVENT_ONDOWN_REGISTER.equals(regist)) {
            mRegisterEvent |= REGIST_FLAG_KEYEVENT_DOWN;
        } else if (WearConst.DEVICE_TO_WEAR_KEYEVENT_ONUP_REGISTER.equals(regist)) {
            mRegisterEvent |= REGIST_FLAG_KEYEVENT_UP;
        } else if (WearConst.DEVICE_TO_WEAR_KEYEVENT_ONDOWN_UNREGISTER.equals(regist)) {
            mRegisterEvent &= ~(REGIST_FLAG_KEYEVENT_DOWN);
            if (mRegisterEvent == 0) {
                finish();
            }
        } else if (WearConst.DEVICE_TO_WEAR_KEYEVENT_ONUP_UNREGISTER.equals(regist)) {
            mRegisterEvent &= ~(REGIST_FLAG_KEYEVENT_UP);
            if (mRegisterEvent == 0) {
                finish();
            }
        }
    }

    /**
     * Broadcast receiver.
     */
    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent i) {
            setRegisterEvent(i.getStringExtra(WearConst.PARAM_KEYEVENT_REGIST));
        }
    }
}
