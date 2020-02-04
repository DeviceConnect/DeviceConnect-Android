package org.deviceconnect.android.libmedia.streaming.mpeg2ts;

import com.google.common.primitives.Bytes;

import java.nio.ByteBuffer;

class H264TransportPacketWriter {

    // Transport Stream packets are 188 bytes in length
    private static final int TS_PACKET_SIZE 			= 188;
    private static final int TS_HEADER_SIZE				= 4;
    private static final int TS_PAYLOAD_SIZE 			= TS_PACKET_SIZE - TS_HEADER_SIZE;

    // Table 2-29 – Stream type assignments. page 66
    private static final byte STREAM_TYPE_AUDIO_AAC 	= 0x0f;
    private static final byte STREAM_TYPE_AUDIO_MP3 	= 0x03;
    private static final byte STREAM_TYPE_VIDEO_H264 	= 0x1b;


    private static final int TS_PAT_PID 				= 0x0000;	// 0
    private static final int TS_PMT_PID 				= 0x1000;	// 4096
    private static final int TS_AUDIO_PID 				= 0x101;	// 257
    private static final int TS_VIDEO_PID 				= 0x100;	// 256

    // Transport Stream Description Table
    private static final int TS_PAT_TABLE_ID 			= 0x00;
    private static final int TS_PMT_TABLE_ID 			= 0x02;


    // H264 Nalu
    private static byte[] H264_NAL = { 0x00, 0x00, 0x00, 0x01, 0x09, (byte) 0xf0 };

    // ContinuityCounter
    private byte mAudioContinuityCounter = 0;
    private byte mVideoContinuityCounter = 0;
    private int mPatContinuityCounter = 0;
    private int mPmtContinuityCounter = 0;

    private Packet mPacket = new Packet();

    private static class Packet {
        private final byte[] mData = new byte[TS_PACKET_SIZE];
        private int mOffset = 0;

        void add(byte b) {
            mData[mOffset++] = b;
        }

        void add(ByteBuffer buffer, int len) {
            for (int i = 0; i < len; i++) {
                add(buffer.get());
            }
        }

        void reset(final byte b) {
            for (int i = 0; i < TS_PACKET_SIZE; i++) {
                mData[i] = b;
            }
            mOffset = 0;
        }
    }

    public interface Callback {
        void onPacket(final byte[] packet);
    }

    private Callback mCallback;

    public void setCallback(final Callback callback) {
        mCallback = callback;
    }

    private void writePacket(byte b) {
        mPacket.add(b);
    }

    private void writePacket(byte[] buffer, int offset, int length) {
        for (int i = 0; i < length; i++) {
            mPacket.add(buffer[offset + i]);
        }
    }

    private void resetPacket(final byte b) {
        mPacket.reset(b);
    }

    private void notifyPacket() {
        if (mCallback != null) {
            mCallback.onPacket(mPacket.mData);
        }
    }

    private void write_ts_header(int pid, int continuity_counter) {
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
        writePacket((byte) 0x00);	//開始インジケータ
    }

    private void write_pat() {
        resetPacket((byte) 0xFF);

        // header
        write_ts_header(TS_PAT_PID, mPatContinuityCounter);
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
        long crc = mpegts_crc32(mPacket.mData, 5, 12);
        writePacket((byte) ((crc >> 24) & 0xFF));
        writePacket((byte) ((crc >> 16) & 0xFF));
        writePacket((byte) ((crc >> 8) & 0xFF));
        writePacket((byte) ((crc) & 0xFF));
    }

    /*
      only audio , section_length = 18
      audio & video mix, section_length = 23
     */
    private void write_pmt(FrameDataType fType) {
        resetPacket((byte) 0xFF);

        // header
        write_ts_header(TS_PMT_PID, mPmtContinuityCounter );
        mPmtContinuityCounter = (mPmtContinuityCounter + 1) & 0x0F;

         // PMT body
        int section_syntax_indicator = 1;
        int zero = 0;
        int reserved_1 = 3;
        int section_length = (fType == FrameDataType.MIXED) ? 23 : 18;
        int program_number = 1;
        int reserved_2 = 3;
        int version_number = 0;
        int current_next_indicator = 1;
        int section_number = 0;
        int last_section_number = 0;
        int reserved_3 = 7;
        int pcr_pid = (fType == FrameDataType.AUDIO) ? TS_AUDIO_PID : TS_VIDEO_PID;
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
        if ( fType == FrameDataType.VIDEO || fType == FrameDataType.MIXED ) {
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
        if ( fType == FrameDataType.AUDIO || fType == FrameDataType.MIXED ) {

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
        long crc =  mpegts_crc32(mPacket.mData, 5,  (fType == FrameDataType.MIXED) ? 22: 17);
        writePacket((byte) ((crc >> 24) & 0xFF));
        writePacket((byte) ((crc >> 16) & 0xFF));
        writePacket((byte) ((crc >> 8) & 0xFF));
        writePacket((byte) ((crc) & 0xFF));
    }

    private void write_pts_dts(int guard_bits, long value) {
        int pts1 = (int) ((value >> 30) & 0x07);
        int pts2 = (int) ((value >> 15) & 0x7FFF);
        int pts3 = (int) (value & 0x7FFF);

        writePacket((byte) ((guard_bits << 4) | (pts1 << 1) | 0x01));
        writePacket((byte) ((pts2  & 0x7F80) >> 7));
        writePacket((byte) (((pts2 & 0x007F) << 1) | 0x01));
        writePacket((byte) ((pts3  & 0x7F80) >> 7));
        writePacket((byte) (((pts3 & 0x007F) << 1) | 0x01));
    }

    void writeVideoBuffer(boolean isFirstPes, ByteBuffer buffer, int length, long pts, long dts, boolean isFrame, boolean mixed) {
        writeBuffer(mixed ? FrameDataType.MIXED : FrameDataType.VIDEO, isFirstPes, buffer, length, pts, dts, isFrame, false);
    }

    void writeAudioBuffer(boolean isFirstPes, ByteBuffer buffer, int length, long pts, long dts, boolean mixed) {
        writeBuffer(mixed ? FrameDataType.MIXED : FrameDataType.AUDIO, isFirstPes, buffer, length, pts, dts, true, true);
    }

    private void writeBuffer(FrameDataType frameDataType, boolean isFirstPes, ByteBuffer buffer, int length, long pts, long dts, boolean isFrame, boolean isAudio) {
        // write pat table
        write_pat();
        notifyPacket();

        // write pmt table
        write_pmt( frameDataType );
        notifyPacket();

        boolean isFirstTs = true;
        byte[] frameBuf = new byte[length];
        buffer.get(frameBuf);
        int frameBufSize = frameBuf.length;
        int frameBufPtr = 0;
        int pid = isAudio ? TS_AUDIO_PID : TS_VIDEO_PID;

        while (frameBufPtr < frameBufSize) {
            int frameBufRemaining = frameBufSize - frameBufPtr;
            boolean isAdaptationField = (isFirstTs || ( frameBufRemaining < TS_PAYLOAD_SIZE ));

            resetPacket((byte) 0x00);

            // write ts header
            writePacket((byte) 0x47); // sync_byte
            writePacket((byte) ((isFirstTs ? 0x40 : 0x00) | ((pid >> 8) & 0x1f)));
            writePacket((byte) (pid & 0xff));
            writePacket((byte) ((isAdaptationField ? 0x30 : 0x10) | ((isAudio ? mAudioContinuityCounter++ : mVideoContinuityCounter++) & 0xF)));

            if (isFirstTs) {
                if (isFrame) {
                    writePacket((byte) 0x07); // adaptation_field_length
                    writePacket((byte) (isFirstPes ? 0x50 : (isAudio && frameDataType == FrameDataType.MIXED ? 0x50 : 0x10)));

                    /* write PCR */
                    long pcr = pts;
                    writePacket((byte) ((pcr >> 25) & 0xFF));
                    writePacket((byte) ((pcr >> 17) & 0xFF));
                    writePacket((byte) ((pcr >> 9) & 0xFF));
                    writePacket((byte) ((pcr >> 1) & 0xFF));
                    writePacket((byte) 0x00); //(byte) (pcr << 7 | 0x7E); // (6bit) reserved， 0x00
                    writePacket((byte) 0x00);
                } else {
                    writePacket((byte) 0x01); // adaptation_field_length
                    writePacket((byte) (isFirstPes ? 0x40 : (isAudio && frameDataType == FrameDataType.MIXED ? 0x40 : 0x00)));
                }

                /* write PES HEADER */
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
                writePacket((byte) 0x80); 			// 0x80 no flags set,  0x84 just data alignment indicator flag set
                writePacket(PTS_DTS_flags); 		// 0xC0 PTS & DTS,  0x80 PTS,  0x00 no PTS/DTS

                // write pts & dts
                if ( PTS_DTS_flags == (byte)0xc0 ) {
                    writePacket((byte) 0x0A);

                    write_pts_dts(3, pts);
                    write_pts_dts(1, dts);
                } else if ( PTS_DTS_flags == (byte)0x80 ) {
                    writePacket((byte) 0x05);
                    write_pts_dts(2, pts);
                } else {
                    writePacket((byte) 0x00);
                }


                // H264 NAL
                if ( !isAudio && Bytes.indexOf(frameBuf, H264_NAL ) == -1 ) {
                    writePacket(H264_NAL, 0, H264_NAL.length);
                }

            }  else {

                // has adaptation
                if ( isAdaptationField ) {
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
                if ( isAdaptationField ) {

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
            notifyPacket();
        }
    }

    public enum FrameDataType {
        AUDIO,
        VIDEO,
        MIXED
    }

    private static long mpegts_crc32(byte[] data, int offset, int len) {
        long crc = 0xffffffff;
        for (int i = 0; i < len; i++) {
            int pos = (int) ((crc >> 24) ^ data[i + offset]) & 0xff;
            crc = (crc << 8) ^ CRC_TABLE[pos];
        }

        return crc;
    }

    private static final long[] CRC_TABLE = new long[] { 0x00000000, 0x04c11db7,
            0x09823b6e, 0x0d4326d9, 0x130476dc, 0x17c56b6b, 0x1a864db2,
            0x1e475005, 0x2608edb8, 0x22c9f00f, 0x2f8ad6d6, 0x2b4bcb61,
            0x350c9b64, 0x31cd86d3, 0x3c8ea00a, 0x384fbdbd, 0x4c11db70,
            0x48d0c6c7, 0x4593e01e, 0x4152fda9, 0x5f15adac, 0x5bd4b01b,
            0x569796c2, 0x52568b75, 0x6a1936c8, 0x6ed82b7f, 0x639b0da6,
            0x675a1011, 0x791d4014, 0x7ddc5da3, 0x709f7b7a, 0x745e66cd,
            0x9823b6e0, 0x9ce2ab57, 0x91a18d8e, 0x95609039, 0x8b27c03c,
            0x8fe6dd8b, 0x82a5fb52, 0x8664e6e5, 0xbe2b5b58, 0xbaea46ef,
            0xb7a96036, 0xb3687d81, 0xad2f2d84, 0xa9ee3033, 0xa4ad16ea,
            0xa06c0b5d, 0xd4326d90, 0xd0f37027, 0xddb056fe, 0xd9714b49,
            0xc7361b4c, 0xc3f706fb, 0xceb42022, 0xca753d95, 0xf23a8028,
            0xf6fb9d9f, 0xfbb8bb46, 0xff79a6f1, 0xe13ef6f4, 0xe5ffeb43,
            0xe8bccd9a, 0xec7dd02d, 0x34867077, 0x30476dc0, 0x3d044b19,
            0x39c556ae, 0x278206ab, 0x23431b1c, 0x2e003dc5, 0x2ac12072,
            0x128e9dcf, 0x164f8078, 0x1b0ca6a1, 0x1fcdbb16, 0x018aeb13,
            0x054bf6a4, 0x0808d07d, 0x0cc9cdca, 0x7897ab07, 0x7c56b6b0,
            0x71159069, 0x75d48dde, 0x6b93dddb, 0x6f52c06c, 0x6211e6b5,
            0x66d0fb02, 0x5e9f46bf, 0x5a5e5b08, 0x571d7dd1, 0x53dc6066,
            0x4d9b3063, 0x495a2dd4, 0x44190b0d, 0x40d816ba, 0xaca5c697,
            0xa864db20, 0xa527fdf9, 0xa1e6e04e, 0xbfa1b04b, 0xbb60adfc,
            0xb6238b25, 0xb2e29692, 0x8aad2b2f, 0x8e6c3698, 0x832f1041,
            0x87ee0df6, 0x99a95df3, 0x9d684044, 0x902b669d, 0x94ea7b2a,
            0xe0b41de7, 0xe4750050, 0xe9362689, 0xedf73b3e, 0xf3b06b3b,
            0xf771768c, 0xfa325055, 0xfef34de2, 0xc6bcf05f, 0xc27dede8,
            0xcf3ecb31, 0xcbffd686, 0xd5b88683, 0xd1799b34, 0xdc3abded,
            0xd8fba05a, 0x690ce0ee, 0x6dcdfd59, 0x608edb80, 0x644fc637,
            0x7a089632, 0x7ec98b85, 0x738aad5c, 0x774bb0eb, 0x4f040d56,
            0x4bc510e1, 0x46863638, 0x42472b8f, 0x5c007b8a, 0x58c1663d,
            0x558240e4, 0x51435d53, 0x251d3b9e, 0x21dc2629, 0x2c9f00f0,
            0x285e1d47, 0x36194d42, 0x32d850f5, 0x3f9b762c, 0x3b5a6b9b,
            0x0315d626, 0x07d4cb91, 0x0a97ed48, 0x0e56f0ff, 0x1011a0fa,
            0x14d0bd4d, 0x19939b94, 0x1d528623, 0xf12f560e, 0xf5ee4bb9,
            0xf8ad6d60, 0xfc6c70d7, 0xe22b20d2, 0xe6ea3d65, 0xeba91bbc,
            0xef68060b, 0xd727bbb6, 0xd3e6a601, 0xdea580d8, 0xda649d6f,
            0xc423cd6a, 0xc0e2d0dd, 0xcda1f604, 0xc960ebb3, 0xbd3e8d7e,
            0xb9ff90c9, 0xb4bcb610, 0xb07daba7, 0xae3afba2, 0xaafbe615,
            0xa7b8c0cc, 0xa379dd7b, 0x9b3660c6, 0x9ff77d71, 0x92b45ba8,
            0x9675461f, 0x8832161a, 0x8cf30bad, 0x81b02d74, 0x857130c3,
            0x5d8a9099, 0x594b8d2e, 0x5408abf7, 0x50c9b640, 0x4e8ee645,
            0x4a4ffbf2, 0x470cdd2b, 0x43cdc09c, 0x7b827d21, 0x7f436096,
            0x7200464f, 0x76c15bf8, 0x68860bfd, 0x6c47164a, 0x61043093,
            0x65c52d24, 0x119b4be9, 0x155a565e, 0x18197087, 0x1cd86d30,
            0x029f3d35, 0x065e2082, 0x0b1d065b, 0x0fdc1bec, 0x3793a651,
            0x3352bbe6, 0x3e119d3f, 0x3ad08088, 0x2497d08d, 0x2056cd3a,
            0x2d15ebe3, 0x29d4f654, 0xc5a92679, 0xc1683bce, 0xcc2b1d17,
            0xc8ea00a0, 0xd6ad50a5, 0xd26c4d12, 0xdf2f6bcb, 0xdbee767c,
            0xe3a1cbc1, 0xe760d676, 0xea23f0af, 0xeee2ed18, 0xf0a5bd1d,
            0xf464a0aa, 0xf9278673, 0xfde69bc4, 0x89b8fd09, 0x8d79e0be,
            0x803ac667, 0x84fbdbd0, 0x9abc8bd5, 0x9e7d9662, 0x933eb0bb,
            0x97ffad0c, 0xafb010b1, 0xab710d06, 0xa6322bdf, 0xa2f33668,
            0xbcb4666d, 0xb8757bda, 0xb5365d03, 0xb1f740b4 };
}
