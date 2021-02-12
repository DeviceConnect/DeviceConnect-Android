/*
 Frame.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.libuvc;

/**
 * UVCカメラのフレームバッファを格納するクラス.
 *
 * JNI 側で、このクラスは呼び出すので、修正する場合は注意すること。
 *
 * @author NTT DOCOMO, INC.
 */
public class Frame {
    /**
     * パラメータ.
     */
    private Parameter mParameter;

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
     * コンストラクタ.
     * @param param パラメータ
     * @param id フレームバッファのID
     */
    Frame(Parameter param, int id) {
        this(param, id, new byte[1024]);
    }

    /**
     * コンストラクタ.
     * @param param パラメータ
     * @param id フレームバッファのID
     * @param buffer フレームバッファ
     */
    Frame(Parameter param, int id, byte[] buffer) {
        if (param == null) {
            throw new IllegalArgumentException("param is null.");
        }
        mParameter = param;
        mId = id;
        mBuffer = buffer;
    }

    /**
     * フレームタイプを取得します.
     *
     * @return フレームタイプ
     */
    public FrameType getFrameType() {
        return mParameter.getFrameType();
    }

    /**
     * フレームの横幅を取得します.
     *
     * @return フレームの横幅
     */
    public int getWidth() {
        return mParameter.getWidth();
    }

    /**
     * フレームの縦幅を取得します.
     *
     * @return フレームの縦幅
     */
    public int getHeight() {
        return mParameter.getHeight();
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
     * フレームバッファの使用中フラグを取得します.
     *
     * @return フレームバッファの使用中フラグ
     */
    boolean isConsumable() {
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
     * 指定されたサイズがバッファよりも大きい場合にはリサイズを行います.
     * <p>
     * 現在のバッファの方が大きい場合には何も行いません。
     * </p>
     * @param length バッファサイズ
     */
    void resizeBuffer(int length) {
        if (mBuffer.length < length) {
            mBuffer = new byte[length];
        }
    }
}
