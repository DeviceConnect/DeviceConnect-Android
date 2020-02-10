package org.deviceconnect.android.libmedia.streaming.mpeg2ts;

import com.google.common.primitives.Bytes;

public class TsPacketWriter {

    // Transport Stream packets are 188 bytes in length
    private static final int TS_PACKET_SIZE = 188;
    private static final int TS_HEADER_SIZE = 4;
    private static final int TS_PAYLOAD_SIZE = TS_PACKET_SIZE - TS_HEADER_SIZE;

    // Table 2-29 – Stream type assignments. page 66
    private static final byte STREAM_TYPE_AUDIO_AAC = 0x0f;
    private static final byte STREAM_TYPE_AUDIO_MP3 = 0x03;
    private static final byte STREAM_TYPE_VIDEO_H264 = 0x1b;

    /**
     * PAT の PID を定義.
     */
    private static final int TS_PAT_PID = 0x0000;    // 0

    /**
     * PMT の PID を定義.
     */
    private static final int TS_PMT_PID = 0x1000;    // 4096

    /**
     * 音声の PID を定義.
     */
    private static final int TS_AUDIO_PID = 0x101;    // 257

    /**
     * 映像の PID を定義.
     */
    private static final int TS_VIDEO_PID = 0x100;    // 256

    // Transport Stream Description Table
    private static final int TS_PAT_TABLE_ID = 0x00;
    private static final int TS_PMT_TABLE_ID = 0x02;

    // H264 Nalu
    private static byte[] H264_NAL = {0x00, 0x00, 0x00, 0x01, 0x09, (byte) 0xf0};

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

    private Callback mCallback;

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
        writePacket((byte) 0x00);    //開始インジケータ
    }

    /**
     * PAT を作成します.
     */
    void writePAT() {
        resetPacket((byte) 0xFF);

        // header
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

        notifyPacket(true);
    }

    /**
     * PMT を作成します.
     *
     * <p>
     *     only audio , section_length = 18
     *     audio & video mix, section_length = 23
     * </p>
     *
     * @param fType フレームタイプ
     */
    void writePMT(FrameType fType) {
        resetPacket((byte) 0xFF);

        // header
        writeTsHeader(TS_PMT_PID, mPmtContinuityCounter);
        mPmtContinuityCounter = (mPmtContinuityCounter + 1) & 0x0F;

        // PMT body
        int section_syntax_indicator = 1;
        int zero = 0;
        int reserved_1 = 3;
        int section_length = (fType == FrameType.MIXED) ? 23 : 18;
        int program_number = 1;
        int reserved_2 = 3;
        int version_number = 0;
        int current_next_indicator = 1;
        int section_number = 0;
        int last_section_number = 0;
        int reserved_3 = 7;
        int pcr_pid = (fType == FrameType.AUDIO) ? TS_AUDIO_PID : TS_VIDEO_PID;
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
        if (fType == FrameType.VIDEO || fType == FrameType.MIXED) {
            int stream_type = 0x1b;
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
        if (fType == FrameType.AUDIO || fType == FrameType.MIXED) {
            int stream_type = 0x0f;
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
        long crc = CrcUtil.crc32(mPacket.mData, 5, (fType == FrameType.MIXED) ? 22 : 17);
        writePacket((byte) ((crc >> 24) & 0xFF));
        writePacket((byte) ((crc >> 16) & 0xFF));
        writePacket((byte) ((crc >> 8) & 0xFF));
        writePacket((byte) ((crc) & 0xFF));

        notifyPacket(true);
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

    private void writeBuffer(FrameType frameType, boolean isFirstPes, byte[] frameBuf, int frameBufSize, long pts, long dts, boolean isFrame, boolean isAudio) {
        boolean isFirstTs = true;
        int frameBufPtr = 0;
        int pid = isAudio ? TS_AUDIO_PID : TS_VIDEO_PID;

        while (frameBufPtr < frameBufSize) {
            int frameBufRemaining = frameBufSize - frameBufPtr;
            boolean isAdaptationField = (isFirstTs || (frameBufRemaining < TS_PAYLOAD_SIZE));

            resetPacket((byte) 0x00);

            // write ts header
            writePacket((byte) 0x47); // sync_byte
            writePacket((byte) ((isFirstTs ? 0x40 : 0x00) | ((pid >> 8) & 0x1f)));
            writePacket((byte) (pid & 0xff));
            writePacket((byte) ((isAdaptationField ? 0x30 : 0x10) | ((isAudio ? mAudioContinuityCounter++ : mVideoContinuityCounter++) & 0xF)));

            if (isFirstTs) {
                if (isFrame) {
                    writePacket((byte) 0x07); // adaptation_field_length
                    writePacket((byte) (isFirstPes ? 0x50 : (isAudio && frameType == FrameType.MIXED ? 0x50 : 0x10)));

                    // write PCR
                    long pcr = pts;
                    writePacket((byte) ((pcr >> 25) & 0xFF));
                    writePacket((byte) ((pcr >> 17) & 0xFF));
                    writePacket((byte) ((pcr >> 9) & 0xFF));
                    writePacket((byte) ((pcr >> 1) & 0xFF));
                    writePacket((byte) 0x00); //(byte) (pcr << 7 | 0x7E); // (6bit) reserved， 0x00
                    writePacket((byte) 0x00);
                } else {
                    writePacket((byte) 0x01); // adaptation_field_length
                    writePacket((byte) (isFirstPes ? 0x40 : (isAudio && frameType == FrameType.MIXED ? 0x40 : 0x00)));
                }

                // write PES HEADER
                writePacket((byte) 0x00);
                writePacket((byte) 0x00);
                writePacket((byte) 0x01);
                writePacket(isAudio ? (byte) 0xc0 : (byte) 0xe0);

                int header_size = 5 + 5;

                // PES パケット長
                if (isAudio) {
                    int pes_size = frameBufSize + header_size + 3;
                    writePacket((byte) ((pes_size >> 8) & 0xFF));
                    writePacket((byte) (pes_size & 0xFF));
                } else {
                    writePacket((byte) 0x00); // 0x00==無制限
                    writePacket((byte) 0x00); // 16:
                }

                // PES ヘッダーの識別
                byte PTS_DTS_flags = isFrame ? (byte) 0xc0 : (byte) 0x00;
                writePacket((byte) 0x80);          // 0x80 no flags set,  0x84 just data alignment indicator flag set
                writePacket(PTS_DTS_flags);        // 0xC0 PTS & DTS,  0x80 PTS,  0x00 no PTS/DTS

                // write pts & dts
                if (PTS_DTS_flags == (byte) 0xc0) {
                    writePacket((byte) 0x0A);

                    writePtsDts(3, pts);
                    writePtsDts(1, dts);
                } else if (PTS_DTS_flags == (byte) 0x80) {
                    writePacket((byte) 0x05);
                    writePtsDts(2, pts);
                } else {
                    writePacket((byte) 0x00);
                }

                // H264 NAL
                if (!isAudio && Bytes.indexOf(frameBuf, H264_NAL) == -1) {
                    writePacket(H264_NAL, 0, H264_NAL.length);
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
                writePacket(frameBuf, frameBufPtr, tsBufRemaining);
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
                        tsBuf[start + i] = (byte) 0xff;
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

                System.arraycopy(frameBuf, frameBufPtr, tsBuf, offset + paddingSize, frameBufRemaining);
                frameBufPtr += frameBufRemaining;
            }

            isFirstTs = false;

            notifyPacket(frameBufPtr >= frameBufSize);
        }
    }

    /**
     * 映像のデータを書き込みます.
     *
     * @param isFirstPes 最初のパケットフラグ
     * @param buffer 映像データのバッファ
     * @param length 映像データのバッファサイズ
     * @param pts PTS
     * @param dts DTS
     * @param isFrame フレームフラグ
     * @param mixed 映像、音声が混合の場合はtrue、それ以外はfalse
     */
    void writeVideoBuffer(boolean isFirstPes, byte[] buffer, int length, long pts, long dts, boolean isFrame, boolean mixed) {
        writeBuffer(mixed ? FrameType.MIXED : FrameType.VIDEO, isFirstPes, buffer, length, pts, dts, isFrame, false);
    }

    /**
     * 音声のデータを書き込みます.
     *
     * @param isFirstPes 最初のパケットフラグ
     * @param buffer 音声データのバッファ
     * @param length 音声データのバッファサイズ
     * @param pts PTS
     * @param dts DTS
     * @param mixed 映像、音声が混合の場合はtrue、それ以外はfalse
     */
    void writeAudioBuffer(boolean isFirstPes, byte[] buffer, int length, long pts, long dts, boolean mixed) {
        writeBuffer(mixed ? FrameType.MIXED : FrameType.AUDIO, isFirstPes, buffer, length, pts, dts, true, true);
    }
}
