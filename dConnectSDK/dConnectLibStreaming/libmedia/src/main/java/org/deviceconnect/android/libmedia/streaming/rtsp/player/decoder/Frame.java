package org.deviceconnect.android.libmedia.streaming.rtsp.player.decoder;

public class Frame {
    private byte[] mData;
    private int mLength;
    private long mTimestamp;
    private boolean mConsumable;

    public Frame(int length) {
        mData = new byte[length];
    }

    public Frame(byte[] data, long timestamp) {
        this(data, data.length, timestamp);
    }

    public Frame(byte[] data, int length, long timestamp) {
        setData(data, length, timestamp);
    }

    public void setData(byte[] data, int length, long timestamp) {
        if (mData == null || mData.length < length) {
            mData = new byte[length];
        }
        System.arraycopy(data, 0, mData, 0, length);
        mLength = length;
        mTimestamp = timestamp;
    }

    public void append(byte[] data, int length) {
        if (mData.length < mLength + length) {
            extendData(mLength + length);
        }
        System.arraycopy(data, 0, mData, mLength, length);
        mLength += length;
    }

    private void extendData(int size) {
        byte[] data = new byte[size];
        System.arraycopy(mData, 0, data, 0, mLength);
        mData = data;
    }

    /**
     * データを取得します.
     *
     * @return データ
     */
    public byte[] getData() {
        return mData;
    }

    /**
     * データサイズを取得します.
     *
     * @return データサイズ
     */
    public int getLength() {
        return mLength;
    }

    /**
     * タイムスタンプを取得します.
     *
     * @return タイムスタンプ
     */
    public long getTimestamp() {
        return mTimestamp;
    }

    /**
     * フレームバッファの使用中フラグを取得します.
     *
     * @return フレームバッファの使用中フラグ
     */
    public boolean isConsumable() {
        return mConsumable;
    }

    /**
     * フレームバッファを使用します.
     */
    public void consume() {
        mConsumable = true;
    }

    /**
     * フレームバッファを解放します.
     */
    public void release() {
        mConsumable = false;
    }
}
