package org.deviceconnect.android.libsrt.client;

public class Frame {
    /**
     * フレームバッファのID.
     */
    private int mId;

    /**
     * フレームバッファ.
     */
    private byte[] mBuffer;

    /**
     * フレームバッファの使用フラグ.
     * <p>
     * trueの場合は使用中、false の場合には未使用。
     * </p>
     */
    private boolean mConsumable;

    /**
     * フレームバッファサイズ.
     */
    private int mLength;

    /**
     * Presentation Time Stamp.
     */
    private long mPresentationTimeStamp;

    /**
     * フレームの横幅.
     */
    private int mWidth;

    /**
     * フレームの縦幅.
     */
    private int mHeight;

    /**
     * コンストラクタ.
     * @param id フレームバッファのID
     */
    public Frame(int id) {
        this(id, new byte[1024]);
    }

    /**
     * コンストラクタ.
     * @param id フレームバッファのID
     * @param buffer フレームバッファ
     */
    Frame(int id, byte[] buffer) {
        mId = id;
        mBuffer = buffer;
    }

    /**
     * コンストラクタ.
     * @param id フレームバッファのID
     * @param buffer フレームバッファ
     * @param length フレームバッファサイズ
     */
    Frame(int id, byte[] buffer, int length) {
        mId = id;
        setData(buffer, length);
    }

    /**
     * フレームの横幅を取得します.
     *
     * @return フレームの横幅
     */
    public int getWidth() {
        return mWidth;
    }

    /**
     * フレームの縦幅を取得します.
     *
     * @return フレームの縦幅
     */
    public int getHeight() {
        return mHeight;
    }

    /**
     * Presentation Time Stampを取得します.
     *
     * @return Presentation Time Stamp
     */
    public long getPTS() {
        return mPresentationTimeStamp;
    }

    /**
     * Presentation Time Stampを設定します.
     *
     * @param pts Presentation Time Stamp
     */
    void setPTS(long pts) {
        mPresentationTimeStamp = pts;
    }

    /**
     * フレームバッファのIDを取得します.
     * <p>
     * MEMO: JNI から呼び出されるの変更する場合には注意すること。
     * </p>
     * @return フレームバッファのID
     */
    int getId() {
        return mId;
    }

    /**
     * フレームバッファを取得します.
     * <p>
     * MEMO: JNI から呼び出されるの変更する場合には注意すること。
     * </p>
     * @return フレームバッファ
     */
    public byte[] getBuffer() {
        return mBuffer;
    }

    /**
     * フレームバッファのサイズを取得します.
     *
     * @return フレームバッファのサイズ
     */
    public int getBufferLength() {
        return mBuffer == null ? 0 : mBuffer.length;
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
    void consume() {
        mConsumable = true;
    }

    /**
     * フレームバッファを解放します.
     */
    public void release() {
        mConsumable = false;
    }

    /**
     * フレームバッファのサイズを取得します.
     *
     * @return フレームバッファのサイズ
     */
    public int getLength() {
        return mLength;
    }

    /**
     * フレームバッファのサイズを設定します.
     *
     * @param length フレームバッファのサイズ
     */
    void setLength(int length) {
        mLength = length;
    }

    /**
     * フレームバッファにデータを設定します.
     *
     * @param data コピー元のデータ
     * @param dataLength コピー元のデータサイズ
     */
    void setData(byte[] data, int dataLength) {
        if (mBuffer == null || mBuffer.length < dataLength) {
            resizeBuffer(dataLength);
        }
        mLength = dataLength;
        System.arraycopy(data, 0, mBuffer, 0, dataLength);
    }

    /**
     * 指定されたサイズがバッファよりも大きい場合にはリサイズを行います.
     * <p>
     * 現在のバッファの方が大きい場合には何も行いません。
     * </p>
     * @param length バッファサイズ
     */
    void resizeBuffer(int length) {
        if (mBuffer == null || mBuffer.length < length) {
            mBuffer = new byte[length];
        }
    }
}
