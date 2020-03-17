package org.deviceconnect.android.libmedia.streaming.rtsp.player.decoder;

import android.util.Log;

public class Frame {
    private byte[] mData;
    private int mLength;
    private long mTimestamp;

    public Frame(byte[] data, long timestamp) {
        mData = data;
        mLength = mData.length;
        mTimestamp = timestamp;
    }

    public byte[] getData() {
        return mData;
    }

    public int getLength() {
        return mLength;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public void print() {
        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 20 && i < mLength; i++) {
                sb.append(String.format("%02X", mData[i]));
            }
            Log.e("FRAME", "#### [" + mData.length + "]: " + sb.toString());
        } catch (Exception e) {
            // ignore.
        }
    }
}
