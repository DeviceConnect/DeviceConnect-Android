package org.deviceconnect.android.libmedia.streaming.mpeg2ts;

import com.google.common.primitives.Bytes;

import org.deviceconnect.android.libmedia.streaming.util.CrcUtil;

import java.nio.ByteBuffer;

class TsPacketWriter {

    // Transport Stream packets are 188 bytes in length
    private static final int TS_PACKET_SIZE = 188;
    private static final int TS_HEADER_SIZE = 4;
    private static final int TS_PAYLOAD_SIZE = TS_PACKET_SIZE - TS_HEADER_SIZE;

    // Table 2-29 – Stream type assignments. page 66
    static final byte STREAM_TYPE_AUDIO_AAC = 0x0F;
    static final byte STREAM_TYPE_AUDIO_MP3 = 0x03;
    static final byte STREAM_TYPE_VIDEO_H264 = 0x1B;
    static final byte STREAM_TYPE_VIDEO_H265 = 0x24;

    // Table 2-18 – Stream_id assignments
    static final byte STREAM_ID_VIDEO = (byte) 0xE0;
    static final byte STREAM_ID_AUDIO = (byte) 0xC0;

    private static final int TS_PAT_PID = 0x0000;    // 0
    private static final int TS_PMT_PID = 0x1000;    // 4096
    private static final int TS_AUDIO_PID = 0x101;   // 257
    private static final int TS_VIDEO_PID = 0x100;   // 256

    // Transport Stream Description Table
    private static final int TS_PAT_TABLE_ID = 0x00;
    private static final int TS_PMT_TABLE_ID = 0x02;

    // H264, H265 AUD NALU
    private static byte[] H264_NAL = {0x00, 0x00, 0x00, 0x01, 0x09, (byte) 0xF0};
    private static byte[] H265_NAL = {0x00, 0x00, 0x00, 0x01, 0x46, 0x01, 0x50};

    // ContinuityCounter
    private byte mAudioContinuityCounter = 0;
    private byte mVideoContinuityCounter = 0;
    private int mPatContinuityCounter = 0;
    private int mPmtContinuityCounter = 0;

    private TsPacket mPacket = new TsPacket();

    public interface Callback {
        /**
         * TS パケットを通知します.
         *
         * @param packet パケット
         */
        void onPacket(final byte[] packet);
    }

    /**
     * 書き込みが完了した TS パケットを通知するコールバック.
     */
    private Callback mCallback;

    /**
     * 書き込みが完了した TS パケットを通知するコールバックを設定します.
     *
     * @param callback コールバック
     */
    public void setCallback(final Callback callback) {
        mCallback = callback;
    }

    /**
     * TS パケットに指定された値を書き込みます.
     *
     * @param b 書き込む値
     */
    private void writePacket(byte b) {
        mPacket.add(b);
    }

    /**
     * TS パケットに指定されたバッファを書き込みます.
     * @param buffer 書き込むバッファ
     * @param offset 書き込むバッファのオフセット
     * @param length 書き込むバッファのサイズ
     */
    private void writePacket(byte[] buffer, int offset, int length) {
        mPacket.add(buffer, offset, length);
    }

    /**
     * TS パケットのデータを指定されたパケットで初期化します.
     *
     * @param b 初期化する値
     */
    private void resetPacket(final byte b) {
        mPacket.reset(b);
    }

    /**
     * TS パケットを送信します.
     *
     * @param flush パケットを強制送信する場合はtrue、それ以外はfalse
     */
    private void notifyPacket(boolean flush) {
        if (mCallback != null) {
            mCallback.onPacket(mPacket.mData);
            if (flush) {
                mCallback.onPacket(null);
            }
        }
    }

    /**
     * TS パケットのヘッダーを作成します.
     *
     * @param pid PID
     * @param continuity_counter カウンター
     */
    private void writeTsHeader(int pid, int continuity_counter) {
        byte sync_byte = 0x47;
        int transport_error_indicator = 0;
        int payload_unit_start_indicator = 1;
        int transport_priority = 0;
        int transport_scrambling_control = 0;
        int adaptation_field_control = 1;

        writePacket(sync_byte);
        writePacket((byte) ((transport_error_indicator << 7) | (payload_unit_start_indicator << 6) | (transport_priority << 5) | ((pid >> 8) & 0x1F)));
        writePacket((byte) (pid & 0xff));
        writePacket((byte) ((transport_scrambling_control << 6) | (adaptation_field_control << 4) | (continuity_counter & 0x0F)));
        writePacket((byte) 0x00);
    }

    /**
     * PAT を作成します.
     */
    void writePAT() {
        resetPacket((byte) 0xFF);

        // TS Header
        writeTsHeader(TS_PAT_PID, mPatContinuityCounter);
        mPatContinuityCounter = (mPatContinuityCounter + 1) & 0x0F;

        // PAT body
        int section_syntax_indicator = 1;
        int zero = 0;
        int reserved_1 = 3;
        int section_length = 13;
        int transport_stream_id = 1;
        int reserved_2 = 3;
        int version_number = 0;
        int current_next_indicator = 1;
        int section_number = 0;
        int last_section_number = 0;
        int program_number = 1;
        int reserved_3 = 7;
        int program_id = TS_PMT_PID;

        writePacket((byte) TS_PAT_TABLE_ID);
        writePacket((byte) ((section_syntax_indicator << 7) | (zero << 6) | (reserved_1 << 4) | ((section_length >> 8) & 0x0F)));
        writePacket((byte) (section_length & 0xFF));
        writePacket((byte) ((transport_stream_id >> 8) & 0xFF));
        writePacket((byte) (transport_stream_id & 0xFF));
        writePacket((byte) ((reserved_2 << 6) | (version_number << 1) | (current_next_indicator & 0x01)));
        writePacket((byte) (section_number & 0xFF));
        writePacket((byte) (last_section_number & 0xFF));
        writePacket((byte) ((program_number >> 8) & 0xFF));
        writePacket((byte) (program_number & 0xFF));
        writePacket((byte) ((reserved_3 << 5) | ((program_id >> 8) & 0x1F)));
        writePacket((byte) (program_id & 0xFF));

        // set crc32
        long crc = CrcUtil.crc32(mPacket.mData, 5, 12);
        writePacket((byte) ((crc >> 24) & 0xFF));
        writePacket((byte) ((crc >> 16) & 0xFF));
        writePacket((byte) ((crc >> 8) & 0xFF));
        writePacket((byte) ((crc) & 0xFF));

        notifyPacket(false);
    }

    /**
     * PMT を作成します.
     *
     * <p>
     *     only audio , section_length = 18
     *     audio & video mix, section_length = 23
     * </p>
     *
     * @param frameType フレームタイプ
     * @param videoStreamType 映像ストリームのタイプ
     * @param audioStreamType 音声ストリームのタイプ
     */
    void writePMT(FrameType frameType, int videoStreamType, int audioStreamType) {
        resetPacket((byte) 0xFF);

        // TS Header
        writeTsHeader(TS_PMT_PID, mPmtContinuityCounter);
        mPmtContinuityCounter = (mPmtContinuityCounter + 1) & 0x0F;

        // PMT body
        int section_syntax_indicator = 1;
        int zero = 0;
        int reserved_1 = 3;
        int section_length = (frameType == FrameType.MIXED) ? 23 : 18;
        int program_number = 1;
        int reserved_2 = 3;
        int version_number = 0;
        int current_next_indicator = 1;
        int section_number = 0;
        int last_section_number = 0;
        int reserved_3 = 7;
        int pcr_pid = (frameType == FrameType.AUDIO) ? TS_AUDIO_PID : TS_VIDEO_PID;
        int reserved_4 = 15;
        int program_info_length = 0;

        writePacket((byte) TS_PMT_TABLE_ID);
        writePacket((byte) ((section_syntax_indicator << 7) | (zero << 6) | (reserved_1 << 4) | ((section_length >> 8) & 0x0F)));
        writePacket((byte) (section_length & 0xFF));
        writePacket((byte) ((program_number >> 8) & 0xFF));
        writePacket((byte) (program_number & 0xFF));
        writePacket((byte) ((reserved_2 << 6) | (version_number << 1) | (current_next_indicator & 0x01)));
        writePacket((byte) section_number);
        writePacket((byte) last_section_number);
        writePacket((byte) ((reserved_3 << 5) | ((pcr_pid >> 8) & 0xFF)));
        writePacket((byte) (pcr_pid & 0xFF));
        writePacket((byte) ((reserved_4 << 4) | ((program_info_length >> 8) & 0xFF)));
        writePacket((byte) (program_info_length & 0xFF));

        // set video stream info
        if (frameType == FrameType.VIDEO || frameType == FrameType.MIXED) {
            int stream_type = videoStreamType;
            int reserved_5 = 7;
            int elementary_pid = TS_VIDEO_PID;
            int reserved_6 = 15;
            int ES_info_length = 0;

            writePacket((byte) stream_type);
            writePacket((byte) ((reserved_5 << 5) | ((elementary_pid >> 8) & 0x1F)));
            writePacket((byte) (elementary_pid & 0xFF));
            writePacket((byte) ((reserved_6 << 4) | ((ES_info_length >> 4) & 0x0F)));
            writePacket((byte) (ES_info_length & 0xFF));
        }

        // set audio stream info
        if (frameType == FrameType.AUDIO || frameType == FrameType.MIXED) {
            int stream_type = audioStreamType;
            int reserved_5 = 7;
            int elementary_pid = TS_AUDIO_PID;
            int reserved_6 = 15;
            int ES_info_length = 0;

            writePacket((byte) stream_type);
            writePacket((byte) ((reserved_5 << 5) | ((elementary_pid >> 8) & 0x1F)));
            writePacket((byte) (elementary_pid & 0xFF));
            writePacket((byte) ((reserved_6 << 4) | ((ES_info_length >> 4) & 0x0F)));
            writePacket((byte) (ES_info_length & 0xFF));
        }

        // set crc32
        long crc = CrcUtil.crc32(mPacket.mData, 5, (frameType == FrameType.MIXED) ? 22 : 17);
        writePacket((byte) ((crc >> 24) & 0xFF));
        writePacket((byte) ((crc >> 16) & 0xFF));
        writePacket((byte) ((crc >> 8) & 0xFF));
        writePacket((byte) ((crc) & 0xFF));

        notifyPacket(false);
    }

    /**
     * PTS、DTS のデータを書き込みます.
     *
     * @param guard_bits
     * @param value PTS、DTS の値
     */
    private void writePtsDts(int guard_bits, long value) {
        int pts1 = (int) ((value >> 30) & 0x07);
        int pts2 = (int) ((value >> 15) & 0x7FFF);
        int pts3 = (int) (value & 0x7FFF);

        writePacket((byte) ((guard_bits << 4) | (pts1 << 1) | 0x01));
        writePacket((byte) ((pts2 & 0x7F80) >> 7));
        writePacket((byte) (((pts2 & 0x007F) << 1) | 0x01));
        writePacket((byte) ((pts3 & 0x7F80) >> 7));
        writePacket((byte) (((pts3 & 0x007F) << 1) | 0x01));
    }

    /**
     * PES のデータを書き込みます.
     *
     * @param pes PES データ
     */
    void writePES(PES pes) {
        boolean isFirstTs = true;
        int frameBufPtr = 0;
        int pid = pes.isAudio() ? TS_AUDIO_PID : TS_VIDEO_PID;

        while (frameBufPtr < pes.mDataLength) {
            int frameBufRemaining = pes.mDataLength - frameBufPtr;
            boolean isAdaptationField = (isFirstTs || (frameBufRemaining < TS_PAYLOAD_SIZE));

            // TS パケットを 0x00 で初期化しておく
            resetPacket((byte) 0x00);

            // write ts header
            writePacket((byte) 0x47); // sync_byte
            writePacket((byte) ((isFirstTs ? 0x40 : 0x00) | ((pid >> 8) & 0x1F)));
            writePacket((byte) (pid & 0xFF));
            writePacket((byte) ((isAdaptationField ? 0x30 : 0x10) | ((pes.isAudio() ? mAudioContinuityCounter++ : mVideoContinuityCounter++) & 0xF)));

            if (isFirstTs) {
                if (pes.isFrame()) {
                    writePacket((byte) 0x07); // adaptation_field_length
                    writePacket((byte) 0x50); // random_access_indicator, PCR_flag

                    // write PCR
                    writePacket((byte) ((pes.mPCR >> 25) & 0xFF));
                    writePacket((byte) ((pes.mPCR >> 17) & 0xFF));
                    writePacket((byte) ((pes.mPCR >> 9) & 0xFF));
                    writePacket((byte) ((pes.mPCR >> 1) & 0xFF));
                    writePacket((byte) 0x00); //(byte) (pcr << 7 | 0x7E); // (6bit) reserved， 0x00
                    writePacket((byte) 0x00);
                } else {
                    writePacket((byte) 0x01); // adaptation_field_length
                    writePacket((byte) ((pes.isAudio() || pes.isFrame() ? 0x40 : 0x00)));
                }

                // write packet_start_code_prefix
                writePacket((byte) 0x00);
                writePacket((byte) 0x00);
                writePacket((byte) 0x01);
                writePacket((byte) (pes.mStreamId & 0xFF));

                // PES パケットサイズ
                if (pes.isAudio()) {
                    int header_size = pes.hasDTS() ? 10 : 5;
                    int pes_size = pes.mDataLength + header_size + 3;
                    writePacket((byte) ((pes_size >> 8) & 0xFF));
                    writePacket((byte) (pes_size & 0xFF));
                } else {
                    writePacket((byte) 0x00); // 0x00==無制限
                    writePacket((byte) 0x00);
                }

                // PES ヘッダーの識別
                byte PTS_DTS_flags = pes.isFrame() ? (byte) (pes.hasDTS() ? 0xC0 : 0x80) : (byte) 0x00;
                writePacket((byte) 0x80);     // 0x80 no flags set,  0x84 just data alignment indicator flag set
                writePacket(PTS_DTS_flags);   // 0xC0 PTS & DTS,  0x80 PTS,  0x00 no PTS/DTS

                // write pts & dts
                if (PTS_DTS_flags == (byte) 0xC0) {
                    writePacket((byte) 0x0A);
                    writePtsDts(3, pes.mPTS);
                    writePtsDts(1, pes.mDTS);
                } else if (PTS_DTS_flags == (byte) 0x80) {
                    writePacket((byte) 0x05);
                    writePtsDts(2, pes.mPTS);
                } else {
                    writePacket((byte) 0x00);
                }

                if (!pes.isAudio()) {
                    // TODO この記述は必要か確認すること。
                    switch (pes.mStreamType) {
                        case STREAM_TYPE_VIDEO_H264:
                            if (Bytes.indexOf(pes.mData, H264_NAL) == -1) {
                                writePacket(H264_NAL, 0, H264_NAL.length);
                            }
                            break;
                        case STREAM_TYPE_VIDEO_H265:
                            if (Bytes.indexOf(pes.mData, H265_NAL) == -1) {
                                writePacket(H265_NAL, 0, H265_NAL.length);
                            }
                            break;
                        default:
                            // not implements
                            break;
                    }
                }
            } else {
                // has adaptation
                if (isAdaptationField) {
                    writePacket((byte) 1);
                    writePacket((byte) 0x00);
                } else {
                    // no adaptation
                    // ts_header + ts_payload
                }
            }

            // fill data
            int tsBufRemaining = TS_PACKET_SIZE - mPacket.mOffset;
            if (frameBufRemaining >= tsBufRemaining) {
                writePacket(pes.mData, frameBufPtr, tsBufRemaining);
                frameBufPtr += tsBufRemaining;
            } else {
                int paddingSize = tsBufRemaining - frameBufRemaining;
                byte[] tsBuf = mPacket.mData;
                int offset = mPacket.mOffset;

                // 0x30  0011 0000
                // 0x10  0001 0000
                // has adaptation
                if (isAdaptationField) {
                    int adaptationFieldLength = (tsBuf[4] & 0xFF);
                    int start = TS_HEADER_SIZE + adaptationFieldLength + 1;
                    int end = offset - 1;

                    // move
                    for (int i = end; i >= start; i--) {
                        tsBuf[i + paddingSize] = tsBuf[i];
                    }

                    // fill data, 0xff
                    for (int i = 0; i < paddingSize; i++) {
                        tsBuf[start + i] = (byte) 0xFF;
                    }

                    tsBuf[4] += paddingSize;

                    // no adaptation
                } else {
                    // set adaptation
                    tsBuf[3] |= 0x20;
                    tsBuf[4] = (byte) paddingSize;
                    tsBuf[5] = 0;

                    for (int i = 0; i < paddingSize; i++) {
                        tsBuf[6 + i] = (byte) 0xFF;
                    }
                }

                System.arraycopy(pes.mData, frameBufPtr, tsBuf, offset + paddingSize, frameBufRemaining);
                frameBufPtr += frameBufRemaining;
            }

            isFirstTs = false;

            notifyPacket(frameBufPtr >= pes.mDataLength);
        }
    }

    static class PES {
        private int mStreamId;
        private int mStreamType;
        private byte[] mData = new byte[1024];
        private int mDataLength;
        private long mPCR;
        private long mPTS;
        private long mDTS;
        private boolean mFrame;

        boolean isFrame() {
            return mFrame;
        }

        boolean isAudio() {
            return mStreamId == STREAM_ID_AUDIO;
        }

        boolean hasDTS() {
            return mDTS > 0;
        }

        void setStreamId(int streamId) {
            mStreamId = streamId;
        }

        void setStreamType(int streamType) {
            mStreamType = streamType;
        }

        void setData(ByteBuffer buffer, int length) {
            if (mData == null || mData.length < length) {
                mData = new byte[length];
            }
            buffer.get(mData, 0, length);
            mDataLength = length;
        }

        void setPCR(long PCR) {
            mPCR = PCR;
        }

        void setPTS(long PTS) {
            mPTS = PTS;
        }

        void setDTS(long DTS) {
            mDTS = DTS;
        }

        void setFrame(boolean frame) {
            mFrame = frame;
        }
    }
}
