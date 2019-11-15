/*
WearTouchProfileActivity.java
Copyright (c) 2015 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.wear.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.WindowManager;

import org.deviceconnect.android.deviceplugin.wear.R;
import org.deviceconnect.android.deviceplugin.wear.WearApplication;
import org.deviceconnect.android.deviceplugin.wear.WearConst;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * WearTouchProfileActivity.
 *
 * @author NTT DOCOMO, INC.
 */
public class WearTouchProfileActivity extends Activity {

    /** Gesture detector. */
    private GestureDetector mGestureDetector;

    /** Device NodeID . */
    private final List<String> mIds = Collections.synchronizedList(new ArrayList<String>());

    /**
     * Wakelock.
     */
    private PowerManager.WakeLock mWakeLock;

    /** Event flag. */
    private int mRegisterEvent = 0;
    /** Event flag define (touch). */
    private static final int REGIST_FLAG_TOUCH_TOUCH = 0x01;
    /** Event flag define (touchstart). */
    private static final int REGIST_FLAG_TOUCH_TOUCHSTART = 0x02;
    /** Event flag define (touchend). */
    private static final int REGIST_FLAG_TOUCH_TOUCHEND = 0x04;
    /** Event flag define (doubletap). */
    private static final int REGIST_FLAG_TOUCH_DOUBLETAP = 0x08;
    /** Event flag define (touchmove). */
    private static final int REGIST_FLAG_TOUCH_TOUCHMOVE = 0x10;
    /** Event flag define (touchcancel). */
    private static final int REGIST_FLAG_TOUCH_TOUCHCANCEL = 0x20;
    /** Touch profile event flag. (ontouchchange). */
    private static final int REGIST_FLAG_TOUCH_TOUCHCHANGE = 0x0040;
    /**
     * Constructor.
     */
    public WearTouchProfileActivity() {
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock((PowerManager.PARTIAL_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP), "DeviceConnect:TouchWakelockTag");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }

        // Get intent data.
        setRegisterEvent(getIntent());
        setContentView(R.layout.activity_wear_touch_profile);
        mGestureDetector = new GestureDetector(this, mSimpleOnGestureListener);

        // For service destruction suppression.
        Intent i = new Intent(WearConst.ACTION_WEAR_PING_SERVICE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setRegisterEvent(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWakeLock.release();
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull final MotionEvent event) {
        boolean execFlag = false;
        int action = (event.getAction() & MotionEvent.ACTION_MASK);
        String strAction = null;
        switch (action) {
            case MotionEvent.ACTION_DOWN: // 1st touch only.
            case MotionEvent.ACTION_POINTER_DOWN: // Others touch.
                // "ontouch" event processing.
                if ((mRegisterEvent & REGIST_FLAG_TOUCH_TOUCH) != 0) {
                    sendEventData(WearConst.PARAM_TOUCH_TOUCH, event);
                }

                // "ontouchstart" event processing.
                if ((mRegisterEvent & REGIST_FLAG_TOUCH_TOUCHCHANGE) != 0
                    || (mRegisterEvent & REGIST_FLAG_TOUCH_TOUCHSTART) != 0) {
                    strAction = WearConst.PARAM_TOUCH_TOUCHSTART;
                    execFlag = true;
                }
                break;
            case MotionEvent.ACTION_UP: // Last touch remove only.
            case MotionEvent.ACTION_POINTER_UP: // Others touch move.
                // "ontouchend" event processing.
                if ((mRegisterEvent & REGIST_FLAG_TOUCH_TOUCHCHANGE) != 0
                        || (mRegisterEvent & REGIST_FLAG_TOUCH_TOUCHEND) != 0) {
                    strAction = WearConst.PARAM_TOUCH_TOUCHEND;
                    execFlag = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                // "ontouchmove" event processing.
                if ((mRegisterEvent & REGIST_FLAG_TOUCH_TOUCHCHANGE) != 0
                        || (mRegisterEvent & REGIST_FLAG_TOUCH_TOUCHMOVE) != 0) {
                    strAction = WearConst.PARAM_TOUCH_TOUCHMOVE;
                    execFlag = true;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                // "ontouchcancel" event processing.
                if ((mRegisterEvent & REGIST_FLAG_TOUCH_TOUCHCHANGE) != 0
                        || (mRegisterEvent & REGIST_FLAG_TOUCH_TOUCHCANCEL) != 0) {
                    strAction = WearConst.PARAM_TOUCH_TOUCHCANCEL;
                    execFlag = true;
                }
                break;
            default:
                return mGestureDetector.onTouchEvent(event);
        }

        if (execFlag) {
            sendEventData(strAction, event);
        }
        return mGestureDetector.onTouchEvent(event) || super.dispatchTouchEvent(event);
    }

    /**
     * Gesture Listener.
     */
    private final SimpleOnGestureListener mSimpleOnGestureListener = new SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(final MotionEvent event) {
            if ((mRegisterEvent & REGIST_FLAG_TOUCH_TOUCHCHANGE) != 0
                    || (mRegisterEvent & REGIST_FLAG_TOUCH_DOUBLETAP) != 0) {
                sendEventData(WearConst.PARAM_TOUCH_DOUBLETAP, event);
            }
            return super.onDoubleTap(event);
        }
    };

    /**
     * Send event data.
     *
     * @param action Action.
     * @param event MotionEvent.
     */
    private void sendEventData(final String action, final MotionEvent event) {
        int dataCount = event.getPointerCount();
        StringBuilder data = new StringBuilder(String.valueOf(dataCount));
        data.append(",").append(action);
        for (int n = 0; n < dataCount; n++) {
            int pointerId = event.getPointerId(n);
            data.append(",").append(pointerId).append(",").append(event.getX(n)).append(",").append(event.getY(n));
        }

        sendEvent(WearConst.WEAR_TO_DEVICE_TOUCH_DATA, data.toString());
    }

    private void sendEvent(final String path, final String data) {
        synchronized (mIds) {
            for (String id : mIds) {
                ((WearApplication) getApplication()).sendMessage(id, path, data);
            }
        }
    }

    private void setRegisterEvent(Intent intent) {
        String type = intent.getStringExtra(WearConst.PARAM_TOUCH_REGIST);
        String id = intent.getStringExtra(WearConst.PARAM_TOUCH_ID);
        setRegisterEvent(type, id);
    }

    /**
     * Set register event.
     *
     * @param regist Request event.
     */
    private void setRegisterEvent(final String regist, String id) {
        if (WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCH_REGISTER.equals(regist)) {
            if (!mIds.contains(id)) {
                mIds.add(id);
            }
            mRegisterEvent |= REGIST_FLAG_TOUCH_TOUCH;
        } else if (WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHSTART_REGISTER.equals(regist)) {
            if (!mIds.contains(id)) {
                mIds.add(id);
            }
            mRegisterEvent |= REGIST_FLAG_TOUCH_TOUCHSTART;
        } else if (WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHEND_REGISTER.equals(regist)) {
            if (!mIds.contains(id)) {
                mIds.add(id);
            }
            mRegisterEvent |= REGIST_FLAG_TOUCH_TOUCHEND;
        } else if (WearConst.DEVICE_TO_WEAR_TOUCH_ONDOUBLETAP_REGISTER.equals(regist)) {
            if (!mIds.contains(id)) {
                mIds.add(id);
            }
            mRegisterEvent |= REGIST_FLAG_TOUCH_DOUBLETAP;
        } else if (WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHMOVE_REGISTER.equals(regist)) {
            if (!mIds.contains(id)) {
                mIds.add(id);
            }
            mRegisterEvent |= REGIST_FLAG_TOUCH_TOUCHMOVE;
        } else if (WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHCANCEL_REGISTER.equals(regist)) {
            if (!mIds.contains(id)) {
                mIds.add(id);
            }
            mRegisterEvent |= REGIST_FLAG_TOUCH_TOUCHCANCEL;
        } else if (WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHCHANGE_REGISTER.equals(regist)) {
            if (!mIds.contains(id)) {
                mIds.add(id);
            }
            mRegisterEvent |= REGIST_FLAG_TOUCH_TOUCHCHANGE;
        } else if (WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCH_UNREGISTER.equals(regist)) {
            mIds.remove(id);
            mRegisterEvent &= ~(REGIST_FLAG_TOUCH_TOUCH);
            if (mRegisterEvent == 0) {
                finish();
            }
        } else if (WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHSTART_UNREGISTER.equals(regist)) {
            mIds.remove(id);
            mRegisterEvent &= ~(REGIST_FLAG_TOUCH_TOUCHSTART);
            if (mRegisterEvent == 0) {
                finish();
            }
        } else if (WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHEND_UNREGISTER.equals(regist)) {
            mIds.remove(id);
            mRegisterEvent &= ~(REGIST_FLAG_TOUCH_TOUCHEND);
            if (mRegisterEvent == 0) {
                finish();
            }
        } else if (WearConst.DEVICE_TO_WEAR_TOUCH_ONDOUBLETAP_UNREGISTER.equals(regist)) {
            mIds.remove(id);
            mRegisterEvent &= ~(REGIST_FLAG_TOUCH_DOUBLETAP);
            if (mRegisterEvent == 0) {
                finish();
            }
        } else if (WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHMOVE_UNREGISTER.equals(regist)) {
            mIds.remove(id);
            mRegisterEvent &= ~(REGIST_FLAG_TOUCH_TOUCHMOVE);
            if (mRegisterEvent == 0) {
                finish();
            }
        } else if (WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHCANCEL_UNREGISTER.equals(regist)) {
            mIds.remove(id);
            mRegisterEvent &= ~(REGIST_FLAG_TOUCH_TOUCHCANCEL);
            if (mRegisterEvent == 0) {
                finish();
            }
        } else if (WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHCHANGE_UNREGISTER.equals(regist)) {
            mIds.remove(id);
            mRegisterEvent &= ~(REGIST_FLAG_TOUCH_TOUCHCHANGE);
            if (mRegisterEvent == 0) {
                finish();
            }
        }
    }
}
