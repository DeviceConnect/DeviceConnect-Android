package org.deviceconnect.android.libmedia.streaming.rtp;

public class RtpPacket {
    /**
     * RTP のヘッダーサイズを定義します.
     */
    public static final int RTP_HEADER_LENGTH = 12;

    /**
     * Maximum Transmission Unit(MTU).
     */
    public static final int MTU = 1300;

    /**
     * RTP データを格納するバッファ.
     */
    private final byte[] mBuffer = new byte[MTU];

    /**
     * RTP パケットのサイズ.
     */
    private int mLength;

    /**
     * 使用フラグ.
     */
    private boolean mUsed;

    /**
     * タイムスタンプ.
     */
    private long mTimeStamp;

    /**
     * RTP パケットのデータを取得します.
     *
     * @return RTP パケットのデータ
     */
    public byte[] getBuffer() {
        return mBuffer;
    }

    /**
     * RTP パケットのデータサイズを取得します.
     *
     * @return RTP パケットのデータサイズ
     */
    public int getLength() {
        return mLength;
    }

    /**
     * RTP パケットのデータサイズを設定します.
     *
     * @param length RTP パケットのデータサイズ
     */
    public void setLength(int length) {
        mLength = length;
    }

    /**
     * RTP のタイムスタンプを取得します.
     *
     * @return RTP のタイムスタンプ
     */
    public long getTimeStamp() {
        return mTimeStamp;
    }

    /**
     * RTP のタイムスタンプを設定します.
     *
     * @param timeStamp タイムスタンプ
     */
    public void setTimeStamp(long timeStamp) {
        mTimeStamp = timeStamp;
    }

    /**
     * 使用中か確認します.
     *
     * @return 使用中の場合はtrue、それ以外はfalse
     */
    public synchronized boolean isUsed() {
        return mUsed;
    }

    /**
     * 使用を開始します.
     */
    public synchronized void consume() {
        mUsed = true;
    }

    /**
     * 使用をやめます.
     */
    public synchronized void release() {
        mUsed = false;
    }
}
