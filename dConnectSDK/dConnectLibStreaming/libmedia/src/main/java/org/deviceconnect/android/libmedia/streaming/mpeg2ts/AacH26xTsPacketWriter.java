package org.deviceconnect.android.libmedia.streaming.mpeg2ts;

import java.nio.ByteBuffer;

public abstract class AacH26xTsPacketWriter {
    /**
     * PAT、PMT の送信周期を定義.
     */
    private static final int PAT_PMT_SEND_INTERVAL = 10 * 1000;

    /**
     * PAT、PMT 送信時間.
     */
    private long mPatPmtSendTime;

    /**
     * PCR の起点となる時間.
     */
    private long mPcrStartTime;

    /**
     * 映像・音声が含まれている場合は true、それ以外は false.
     */
    private boolean mMixed;

    /**
     * TSパケットを書き込む Writer.
     */
    TsPacketWriter mTsWriter;

    /**
     * TS パケットを送るリスナー.
     */
    private PacketListener mPacketListener;

    /**
     * 初期化を行います.
     *
     * @param sampleRate 音声のサンプルレート
     * @param sampleSizeInBits 音声のビットサイズ
     * @param channels 音声のチャンネル
     * @param fps 映像のフレームレート
     */
    public void initialize(float sampleRate, int sampleSizeInBits, int channels, int fps) {
        mTsWriter = new TsPacketWriter();
        mTsWriter.setCallback(packet -> mPacketListener.onPacketAvailable(packet));

        mMixed = (fps > 0 && sampleRate > 0);
        mPatPmtSendTime = 0;
        mPcrStartTime = System.currentTimeMillis();
    }

    /**
     * TS パケットに変換したデータを通知するリスナーを設定します.
     *
     * @param listener リスナー
     */
    public void setPacketListener(PacketListener listener) {
        mPacketListener = listener;
    }

    /**
     * NALU (Network Abstraction Layer Unit) のデータを TS パケットに書き込みます.
     *
     * @param buffer データ
     * @param pts プレゼンテーションタイム
     */
    public abstract void writeNALU(ByteBuffer buffer, long pts);

    /**
     * AAC のデータを TS パケットに書き込みます.
     *
     * @param buffer データ
     * @param pts プレゼンテーションタイム
     */
    public abstract void writeADTS(ByteBuffer buffer, long pts);

    /**
     * PAT、PMT を一定時間で送信します.
     *
     * <p>
     * 音声のストリームタイプは、AAC で固定で指定しています。
     * </p>
     *
     * @param frameType フレームタイプ
     * @param videoStreamType 映像のストリームタイプ
     */
    void writePatPmt(FrameType frameType, int videoStreamType) {
        writePatPmt(frameType, videoStreamType, TsPacketWriter.STREAM_TYPE_AUDIO_AAC);
    }

    /**
     * PAT、PMT を一定時間で送信します.
     *
     * @param frameType フレームタイプ
     * @param videoStreamType 映像のストリームタイプ
     * @param audioStreamType 音声のストリームタイプ
     */
    void writePatPmt(FrameType frameType, int videoStreamType, int audioStreamType) {
        if (System.currentTimeMillis() - mPatPmtSendTime > PAT_PMT_SEND_INTERVAL) {
            mPatPmtSendTime = System.currentTimeMillis();
            mTsWriter.writePAT();
            mTsWriter.writePMT(mMixed ? FrameType.MIXED : frameType, videoStreamType, audioStreamType);
        }
    }

    /**
     * PCR の時間を計算します.
     *
     * @return PCR の時間
     */
    long getPcr() {
        return (System.currentTimeMillis() - mPcrStartTime) * 90;
    }

    /**
     * TS パケットを通知するリスナー.
     */
    public interface PacketListener {
        /**
         * TS パケットを通知します.
         *
         * <p>
         * packet が null の場合には、データが終了しているので、強制的に TSパケットを送信してください。
         * </p>
         *
         * @param packet TSパケット
         */
        void onPacketAvailable(byte[] packet);
    }
}
