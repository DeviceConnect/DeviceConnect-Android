package org.deviceconnect.android.deviceplugin.host.sensor;

import android.view.MotionEvent;

public class HostTouchEvent {
    public static final String STATE_TOUCH_START = "start";
    public static final String STATE_TOUCH_END = "end";
    public static final String STATE_TOUCH_DOUBLE_TAP = "doubletap";
    public static final String STATE_TOUCH_MOVE = "move";
    public static final String STATE_TOUCH_CANCEL = "cancel";
    public static final String STATE_TOUCH_CHANGE = "change";

    private final String mState;
    private final int mCount;
    private final int[] mX;
    private final int[] mY;
    private final int[] mId;
    private final long mTimestamp;

    public HostTouchEvent(String state, MotionEvent event) {
        mState = state;
        mCount = event.getPointerCount();
        mId = new int[mCount];
        mX = new int[mCount];
        mY = new int[mCount];
        for (int i = 0; i < mCount; i++) {
            mId[i] = event.getPointerId(i);
            mX[i] = (int) event.getX(i);
            mY[i] = (int) event.getY(i);
        }
        mTimestamp = System.currentTimeMillis();
    }

    public HostTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                mState = STATE_TOUCH_START;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mState = STATE_TOUCH_END;
                break;
            case MotionEvent.ACTION_MOVE:
                mState = STATE_TOUCH_MOVE;
                break;
            case MotionEvent.ACTION_CANCEL:
                mState = STATE_TOUCH_CANCEL;
                break;
            default:
                mState = "unknown";
                break;
        }

        mCount = event.getPointerCount();
        mId = new int[mCount];
        mX = new int[mCount];
        mY = new int[mCount];
        for (int i = 0; i < mCount; i++) {
            mId[i] = event.getPointerId(i);
            mX[i] = (int) event.getX(i);
            mY[i] = (int) event.getY(i);
        }
        mTimestamp = System.currentTimeMillis();
    }

    public String getState() {
        return mState;
    }

    public int getCount() {
        return mCount;
    }

    public int getId(int index) {
        return mId[index];
    }

    public int getX(int index) {
        return mX[index];
    }

    public int getY(int index) {
        return mY[index];
    }

    public long getTimestamp() {
        return mTimestamp;
    }
}
