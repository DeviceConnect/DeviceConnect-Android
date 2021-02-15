/*
 Scanner.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.libusb.descriptor;

import java.io.IOException;

/**
 * byte 配列をスキャンするためのクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public class Scanner {
    /**
     * スキャンする byte 配列.
     */
    private final byte[] mBuffer;

    /**
     * スキャンするサイズ.
     */
    private final int mLength;

    /**
     * スキャンしている位置.
     */
    private int mOffset;

    /**
     * コンストラクタ.
     *
     * @param buffer スキャンするbyte配列
     */
    public Scanner(final byte[] buffer) {
        this(buffer, buffer.length);
    }

    /**
     * コンストラクタ.
     *
     * @param buffer スキャンするbyte配列
     * @param length スキャンサイズ
     */
    public Scanner(final byte[] buffer, final int length) {
        if (buffer == null) {
            throw new IllegalArgumentException("buffer is null.");
        }

        if (buffer.length < length) {
            throw new IllegalArgumentException("buffer size is smaller than length.");
        }

        mBuffer = buffer;
        mLength = length;
        mOffset = 0;
    }

    /**
     * 現在のスキャン位置を取得します.
     *
     * @return 現在のスキャン位置
     */
    public int getOffset() {
        return mOffset;
    }

    /**
     * 1byte取得して、スキャン位置を1進めます.
     *
     * @return 1byte
     * @throws IOException スキャン位置がスキャンサイズを超えた場合に発生
     */
    public byte readByte() throws IOException {
        checkBound(mOffset + 1);
        try {
            return (byte) (mBuffer[mOffset] & 0xFF);
        } finally {
            mOffset++;
        }
    }

    /**
     * 2byte取得して、スキャン位置を2進めます.
     *
     * @return short
     * @throws IOException スキャン位置がスキャンサイズを超えた場合に発生
     */
    public short readShort() throws IOException {
        checkBound(mOffset + 2);
        try {
            return (short) (((mBuffer[mOffset + 1] & 0xFF) << 8) | (mBuffer[mOffset] & 0xFF));
        } finally {
            mOffset += 2;
        }
    }

    /**
     * 4byte取得して、スキャン位置を4進めます.
     *
     * @return int
     * @throws IOException スキャン位置がスキャンサイズを超えた場合に発生
     */
    public int readInt() throws IOException {
        checkBound(mOffset + 4);
        try {
            return ((mBuffer[mOffset + 3] & 0xFF) << 24) | ((mBuffer[mOffset + 2] & 0xFF) << 16) |
                    ((mBuffer[mOffset + 1] & 0xFF) << 8) | (mBuffer[mOffset] & 0xFF);
        } finally {
            mOffset += 4;
        }
    }

    /**
     * 指定された位置にスキャン位置を移動させます
     *
     * @param position 移動する位置
     * @throws IOException スキャン位置がスキャンサイズを超えた場合に発生
     */
    public void seek(final int position) throws IOException {
        if (position < 0 || position > mLength) {
            throw new IOException("Scanner's buffer is out of bound. offset=" + mOffset);
        }
        mOffset = position;
    }

    /**
     * 指定されたサイズ分スキャン位置を移動します.
     *
     * @param len 移動する位置
     * @throws IOException スキャン位置がスキャンサイズを超えた場合に発生
     */
    public void skip(final int len) throws IOException {
        if (len < 0) {
            throw new IOException("len is negative.");
        }
        checkBound(mOffset + len);
        mOffset += len;
    }

    /**
     * スキャン位置が最後まで到達しているか確認します.
     *
     * @return 最後まで到達している場合はtrue、それ以外はfalse
     */
    public boolean isFinish() {
        return mOffset >= mLength;
    }

    /**
     * スキャン位置をリセットします.
     */
    public void reset() {
        mOffset = 0;
    }

    /**
     * 指定された位置がスキャンサイズを超えていないか確認します.
     *
     * @param offset スキャン位置
     * @throws IOException スキャンサイズを超えている場合に発生
     */
    private void checkBound(int offset) throws IOException {
        if (offset > mLength) {
            throw new IOException("Scanner's buffer is out of bound. offset=" + offset);
        }
    }
}
