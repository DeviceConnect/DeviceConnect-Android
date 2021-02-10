package org.deviceconnect.android.deviceplugin.host.sensor;

public class HostKeyEvent {
    public static final String STATE_KEY_UP = "up";
    public static final String STATE_KEY_DOWN = "down";
    public static final String STATE_KEY_CHANGE = "change";

    private String mState;
    private String mConfig;
    private int mId;
    private long mTimestamp;

    public HostKeyEvent(String state, int id, String config) {
        mState = state;
        mId = id;
        mConfig = config;
        mTimestamp = System.currentTimeMillis();
    }

    public String getState() {
        return mState;
    }

    public String getConfig() {
        return mConfig;
    }

    public void setId(int id) {
        mId = id;
    }

    public int getId() {
        return mId;
    }

    public long getTimestamp() {
        return mTimestamp;
    }
}
