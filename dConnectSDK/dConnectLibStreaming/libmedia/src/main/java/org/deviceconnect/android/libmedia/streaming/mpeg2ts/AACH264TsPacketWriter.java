package org.deviceconnect.android.libmedia.streaming.mpeg2ts;

import java.nio.ByteBuffer;
import java.util.List;

public class AACH264TsPacketWriter {
    /**
     * PAT、PMT の送信周期を定義.
     */
    private static final int PAT_PMT_SEND_INTERVAL = 2000;

    /**
     * P フレーム（差分)のタイプを定義.
     */
    private static final int H264NT_SLICE  = 1;

    /**
     * I フレームのタイプを定義.
     */
    private static final int H264NT_SLICE_IDR = 5;

    /**
     * H264 のスタートコードを定義.
     */
    private static final byte[] H264_START_CODE = {0x00, 0x00, 0x00, 0x01};

    /**
     * TSパケットを書き込む Writer.
     */
    private final TsPacketWriter mTsWriter;

    /**
     * 最初の PES 送信フラグ.
     */
    private boolean mFirstPes = true;

    /**
     * TS パケットを送るリスナー.
     */
    private PacketListener mPacketListener;

    /**
     * 映像の FPS.
     */
    private int mFps;

    /**
     * 音声のサンプルレート.
     */
    private float mSampleRate;

    /**
     * 音声のビットサイズ.
     */
    private int mSampleSizeInBits;

    /**
     * 音声のチャンネル数.
     */
    private int mChannel;

    /**
     * 映像の 1 フレームの時間.
     */
    private long mIncVideoPts;

    /**
     * 音声の 1 フレームの時間.
     */
    private long mIncAudioPts;

    /**
     * 映像・音声が含まれている場合は true、それ以外は false.
     */
    private boolean mMixed;

    /**
     * データを一時的に格納するバッファ.
     */
    private byte[] mFrameBuffer = new byte[4096];

    /**
     * PAT、PMT 送信時間.
     */
    private long mPatPmtSendTime;


    public AACH264TsPacketWriter() {
        mTsWriter = new TsPacketWriter();
        mTsWriter.setCallback(packet -> mPacketListener.onPacketAvailable(packet));
    }

    public void setPacketListener(PacketListener packetListener) {
        this.mPacketListener = packetListener;
    }

    /**
     * パケットライターの初期化を行います.
     *
     * @param sampleRate 音声のサンプリングレート
     * @param sampleSizeInBits 音声のビットサイズ(1byte or 2byte)
     * @param channels 音声のチャンネル数 (1 or 2)
     * @param fps 映像のFPS
     */
    public void initialize(float sampleRate, int sampleSizeInBits, int channels, int fps) {
        mSampleRate = sampleRate;
        mSampleSizeInBits = sampleSizeInBits;
        mChannel = channels;
        mFps = fps;

        if (fps > 0) {
            // PTS ステップサイズ.
            // ・映像：1000 / fps（単位：ms）
            // ・ミリ秒への変換：h264の設定によると90HZであるため、PTS/DTSのミリ秒への変換式は次のとおり。ms = pts / 90
            mIncVideoPts = (long) (1000 / fps) * 90;
        }

        if (sampleRate > 0) {
            // ・音声: 1つのAACフレームに対応するサンプリングサンプル数/サンプリング周波数（単位: s)
            mIncAudioPts = (long) (1024 * (1000 / sampleRate)) * 90;
        }

        mMixed = (fps > 0 && sampleRate > 0);

        // 初期化フラグ
        mFirstPes = true;

        mPatPmtSendTime = 0;
    }

    /**
     * 複数の NALU のフレームを書き込みます.
     *
     * @param buffer 複数の NALU のフレームが格納されたバッファ
     * @param pts プレゼンテーションタイム
     */
    public void writeNALUs(final ByteBuffer buffer, long pts) {
        // TODO 現在は使用していないが、スタートコードの検索ごとに List を作るの無駄なので、使わないようにすること。
        List<Integer> offsets = ByteUtil.kmp(buffer, H264_START_CODE);
        buffer.position(0);
        int totalLength = buffer.remaining();
        for (int i = 0; i < offsets.size(); i++) {
            final int unitOffset = offsets.get(i);
            final int unitLength;
            if (i == offsets.size() - 1) {
                unitLength = totalLength - unitOffset;
            } else {
                int nextOffset = offsets.get(i + 1);
                unitLength = nextOffset - unitOffset;
            }
            int type = buffer.get(unitOffset + H264_START_CODE.length) & 0x1F;
            boolean isFrame = type == H264NT_SLICE || type == H264NT_SLICE_IDR;

            buffer.position(unitOffset);
            buffer.limit(unitOffset + unitLength);
            writeNALU(buffer, pts);

            if (isFrame) {
                pts += mIncVideoPts;
            }
        }
    }

    /**
     * NALU のフレームを書き込みます.
     *
     * @param buffer NALU のフレームが格納されたバッファ
     * @param pts プレゼンテーションタイム
     */
    public synchronized void writeNALU(final ByteBuffer buffer, final long pts) {
        writePatPmt(FrameType.VIDEO);

        int offset = buffer.position();
        int length = buffer.limit() - offset;
        int type = buffer.get(H264_START_CODE.length) & 0x1F;
        boolean isFrame = type == H264NT_SLICE || type == H264NT_SLICE_IDR;
        buffer.position(offset);
        mTsWriter.writeVideoBuffer(mFirstPes, put(buffer, length), length, pts, pts, isFrame, mMixed);
        mFirstPes = false;
    }

    /**
     * ADTS のフレームを書き込みます.
     *
     * @param buffer ADTS のフレームが格納されたバッファ
     * @param pts プレゼンテーションタイム
     */
    public synchronized void writeADTS(final ByteBuffer buffer, final long pts) {
        writePatPmt(FrameType.AUDIO);

        int length = buffer.limit() - buffer.position();
        mTsWriter.writeAudioBuffer(mFirstPes, put(buffer, length), length, pts, pts, mMixed);
        mFirstPes = false;
    }

    /**
     * PAT、PMT を一定時間で送信します.
     *
     * @param frameType フレームタイプ
     */
    private void writePatPmt(FrameType frameType) {
        if (System.currentTimeMillis() - mPatPmtSendTime > PAT_PMT_SEND_INTERVAL) {
            mPatPmtSendTime = System.currentTimeMillis();
            mTsWriter.writePAT();
            mTsWriter.writePMT(mMixed ? FrameType.MIXED : frameType);
        }
    }

    /**
     * Buffer のデータを byte[] にコピーして取得します.
     *
     * <p>
     * 返却される byte[] は、フィールド変数に指定されているので、注意。
     * </p>
     *
     * @param buffer コピー元のバッファ
     * @param length コピー元のバッファサイズ
     * @return コピーしたbyte[]
     */
    private byte[] put(ByteBuffer buffer, int length) {
        if (mFrameBuffer.length < length) {
            mFrameBuffer = new byte[length];
        }
        buffer.get(mFrameBuffer, 0, length);
        return mFrameBuffer;
    }

    /**
     * TS パケットを通知するリスナー.
     */
    public interface PacketListener {
        /**
         * TS パケットを通知します.
         *
         * @param packet TSパケット
         */
        void onPacketAvailable(byte[] packet);
    }
}
