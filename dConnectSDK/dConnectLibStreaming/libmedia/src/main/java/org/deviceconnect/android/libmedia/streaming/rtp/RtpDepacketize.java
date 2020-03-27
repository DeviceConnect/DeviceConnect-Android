package org.deviceconnect.android.libmedia.streaming.rtp;

import android.util.Log;

import org.deviceconnect.android.libmedia.BuildConfig;

public abstract class RtpDepacketize {

    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "RTP-DEPACKET";

    /**
     * RTP のヘッダーサイズを定義します.
     */
    public static final int RTP_HEADER_LENGTH = RtpPacket.RTP_HEADER_LENGTH;

    /**
     * RTP からデパケットしたデータを通知するためのコールバック.
     */
    private Callback mCallback;

    /**
     * シーケンス番号.
     */
    private int mSequenceNumber = -1;

    /**
     * クロック周波数.
     */
    private int mClock;

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
     * RTP からデパケットしたデータを通知するためのコールバックを設定します.
     *
     * @param callback コールバック
     */
    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    /**
     * RTP パケットを書き込みます.
     *
     * @param data       パケットに格納するデータ
     * @param dataLength パケットに格納するデータサイズ
     */
    public void write(byte[] data, int dataLength) {
        int payloadStart = RTP_HEADER_LENGTH;

        int cc = getCC(data);
        if (cc > 0) {
            payloadStart += cc * 4;
        }

        if (isExtension(data)) {
            int extensionsLength = ((data[payloadStart + 2] & 0xFF) << 8) | (data[payloadStart + 3] & 0xFF);
            payloadStart += 4;
            payloadStart += extensionsLength * 4;
        }

        write(data, payloadStart, dataLength);
    }

    /**
     * RTP パケットを書き込みます.
     *
     * @param data       パケットに格納するデータ
     * @param payloadStart ペイロードの開始位置
     * @param dataLength パケットに格納するデータサイズ
     */
    public abstract void write(byte[] data, int payloadStart, int dataLength);

    /**
     * シーケンス番号の同期確認を行います.
     *
     * @param data RTP パケットデータ
     * @return 同期が取れている場合はtrue、それ以外はfalse
     */
    protected boolean checkSequenceNumber(byte[] data) {
        boolean result = true;
        int s = getSequenceNumber(data);
        if (mSequenceNumber != -1 && (mSequenceNumber + 1) % 65536 != s) {
            if (DEBUG) {
                Log.e(TAG, "Error sequence number: [" + mSequenceNumber + " " + s + "]");
            }
            result = false;
        }
        mSequenceNumber = s;
        return result;
    }

    /**
     * 次のパケットへのマークが付いているか確認します.
     *
     * @param data RTP パケットデータ
     * @return マークが付いている場合は true、それ以外はfalse
     */
    protected boolean isNextPacket(byte[] data) {
        return (data[1] & 0x80) != 0;
    }

    /**
     * RTP バージョンを取得します.
     *
     * @param data RTP パケットデータ
     * @return RTP バージョン
     */
    protected int getVersion(byte[] data) {
        return ((data[0] >> 6) & 0x03);
    }

    /**
     * パディングフラグを取得します.
     *
     * @param data RTP パケットデータ
     * @return パディングデータが存在する場合はtrue、それ以外はfalse
     */
    protected boolean isPadding(byte[] data) {
        return ((data[0] >> 5) & 0x01) != 0;
    }

    /**
     * 拡張フラグを取得します.
     *
     * @param data RTP パケットデータ
     * @return 拡張ヘッダーが存在する場合はtrue、それ以外はfalse
     */
    protected boolean isExtension(byte[] data) {
        return ((data[0] >> 4) & 0x01) != 0;
    }

    /**
     * 寄与送信元(CSRC)識別子の個数を取得します.
     *
     * @param data RTP パケットデータ
     * @return CC の個数
     */
    protected int getCC(byte[] data) {
        return (data[0] & 0x0F);
    }

    /**
     * ペイロードタイプを取得します.
     *
     * @param data RTP パケットデータ
     * @return ペイロードタイプ
     */
    protected int getPayloadType(byte[] data) {
        return data[1] & 0x7F;
    }

    /**
     * RTP ヘッダーからシーケンス番号を取得します.
     *
     * @param data RTP パケットデータ
     * @return シーケンス番号
     */
    protected int getSequenceNumber(byte[] data) {
        return ((data[2] & 0xFF) << 8) | (data[3] & 0xFF);
    }

    /**
     * RTP ヘッダーからタイムスタンプを取得します.
     *
     * @param data RTP パケットデータ
     * @return タイムスタンプ
     */
    protected long getTimestamp(byte[] data) {
        return ((data[4] & 0xFF) << 24) | ((data[5] & 0xFF) << 16) |
                ((data[6] & 0xFF) << 8) | (data[7] & 0xFF);
    }

    /**
     * RTP ヘッダーから同期送信元(SSRC)識別子を取得します.
     *
     * @param data RTP パケットデータ
     * @return 送信者識別子
     */
    protected int getSsrc(byte[] data) {
        return ((data[8] & 0xFF) << 24) | ((data[9] & 0xFF) << 16) |
                ((data[10] & 0xFF) << 8) | (data[11] & 0xFF);
    }

    /**
     * データを通知します.
     *
     * @param data 通知するデータ
     * @param pts タイムスタンプ
     */
    protected void postData(byte[] data, long pts) {
        if (mCallback != null) {
            mCallback.onData(data, pts * 1000 / mClock);
        }
    }

    /**
     * RTP からデパケットしたデータを通知するためのコールバック.
     */
    public interface Callback {
        /**
         * デパケットしたデータを通知します.
         *
         * @param data データ
         * @param pts タイムスタンプ
         */
        void onData(byte[] data, long pts);
    }
}
