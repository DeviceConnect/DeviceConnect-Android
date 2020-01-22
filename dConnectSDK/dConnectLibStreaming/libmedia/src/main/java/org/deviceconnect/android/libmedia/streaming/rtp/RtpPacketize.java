package org.deviceconnect.android.libmedia.streaming.rtp;

import java.util.Random;

public abstract class RtpPacketize {
    /**
     * RTP のヘッダーサイズを定義します.
     */
    public static final int RTP_HEADER_LENGTH = RtpPacket.RTP_HEADER_LENGTH;

    /**
     * 最大のパケットサイズを定義します.
     */
    public static final int MAX_PACKET_SIZE = RtpPacket.MTU - 28;

    /**
     * RTP ヘッダーに格納するシーケンス番号.
     */
    private int mSequenceNumber = new Random().nextInt() % Short.MAX_VALUE;

    /**
     * RTP のバージョン.
     */
    private int mVersion = 2;

    /**
     * パディング.
     */
    private int mPadding;

    /**
     * 拡張フラグ.
     */
    private int mExtension;

    /**
     * 寄与送信元(CSRC)識別子.
     */
    private int mCC;

    /**
     * RTP ヘッダーに格納するペイロードタイプ.
     */
    private int mPayloadType;

    /**
     * 同期送信元(SSRC)識別子.
     */
    private int mSsrc;

    /**
     * クロック周波数.
     */
    private int mClock;

    /**
     * 変換された RTP パケットを通知するコールバック.
     */
    private Callback mCallback;

    /**
     * 変換された RTP パケットを通知するコールバックを設定します.
     *
     * @param callback コールバック
     */
    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    /**
     * RTPに格納するペイロードタイプを設定します.
     *
     * @param payloadType ペイロードタイプ
     */
    public void setPayloadType(int payloadType) {
        mPayloadType = payloadType;
    }

    /**
     * ペイロードタイプを取得します.
     *
     * @return ペイロードタイプ
     */
    public int getPayloadType() {
        return mPayloadType;
    }

    /**
     * 送信者識別子を取得します.
     *
     * @return 送信者識別子
     */
    public int getSsrc() {
        return mSsrc;
    }

    /**
     * クロック周波数を取得します.
     *
     * @return クロック周波数
     */
    public int getClockFrequency() {
        return mClock;
    }

    /**
     * クロック周波数を設定します.
     *
     * @param clock クロック周波数
     */
    public void setClockFrequency(int clock) {
        mClock = clock;
    }

    /**
     * 送信者識別子を設定します.
     *
     * @param ssrc 送信者識別子
     */
    public void setSsrc(int ssrc) {
        mSsrc = ssrc;
    }

    /**
     * RTP パケットを書き込みます.
     *
     * @param data パケットに格納するデータ
     * @param dataLength パケットに格納するデータサイズ
     * @param pts プレゼンテーションタイム
     */
    public abstract void write(byte[] data, int dataLength, long pts);

    /**
     * RTP ヘッダーのデータをバッファに書き込みます.
     *
     * @param buffer RTPヘッダーのデータを書き込むバッファ
     * @param timestamp タイムスタンプ
     */
    protected void writeRtpHeader(byte[] buffer, long timestamp) {
        buffer[0] = (byte) (mVersion << 6 | mPadding << 5 | mExtension << 4 | mCC);
        buffer[1] = (byte) (mPayloadType & 0x7F);
        buffer[2] = (byte) ((mSequenceNumber >> 8) & 0xFF);
        buffer[3] = (byte) ((mSequenceNumber) & 0xFF);
        buffer[4] = (byte) ((timestamp >> 24) & 0xFF);
        buffer[5] = (byte) ((timestamp >> 16) & 0xFF);
        buffer[6] = (byte) ((timestamp >> 8) & 0xFF);
        buffer[7] = (byte) ((timestamp) & 0xFF);
        buffer[8] = (byte) ((mSsrc >> 24) & 0xFF);
        buffer[9] = (byte) ((mSsrc >> 16) & 0xFF);
        buffer[10] = (byte) ((mSsrc >> 8) & 0xFF);
        buffer[11] = (byte) ((mSsrc) & 0xFF);
    }

    /**
     * タイプスタンプを更新します.
     *
     * @param timestamp タイプスタンプ
     * @return 更新されたタイムスタンプ
     */
    protected int updateTimestamp(long timestamp) {
        return (int) ((timestamp / 100L) * (mClock / 1000L) / 10000L);
    }

    /**
     * RTPヘッダーにパケット終了のマークをつけます.
     *
     * @param buffer RTPヘッダーのデータを書き込むバッファ
     */
    protected void writeNextPacket(byte[] buffer) {
        buffer[1] |= 0x80;
    }

    /**
     * RTPのデータを書き込むためのバッファを取得します.
     *
     * @return RTPを格納するバッファ
     */
    protected RtpPacket getRtpPacket() {
        return mCallback.getRtpPacket();
    }

    /**
     * RTP のデータを送信します.
     *
     * @param packet 送信するRTPデータが格納されたバッファ
     * @param length RTPデータサイズ
     * @param timestamp タイムスタンプ
     */
    protected void send(RtpPacket packet, int length, long timestamp) {
        packet.setLength(length);
        packet.setTimeStamp(timestamp);
        mSequenceNumber++;
        mCallback.send(packet);
    }

    /**
     * 変換された RTP パケットを通知するコールバック.
     */
    public interface Callback {
        /**
         * RTP データを格納するパケットを取得します.
         *
         * @return RTP データを格納するパケット
         */
        RtpPacket getRtpPacket();

        /**
         * 書き込まれたデータを RTP パケットに変換されたデータが送られてきます.
         *
         * @param packet RTPパケット
         */
        void send(RtpPacket packet);
    }
}
