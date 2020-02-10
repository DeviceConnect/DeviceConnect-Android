package org.deviceconnect.android.libmedia.streaming.mpeg2ts;

import java.nio.ByteBuffer;
import java.util.Arrays;

class TsPacket {

    static final int TS_PACKET_SIZE = 188;
    static final int TS_HEADER_SIZE = 4;
    static final int TS_PAYLOAD_SIZE = TS_PACKET_SIZE - TS_HEADER_SIZE;

    static final int TS_PAT_PID = 0x0000;    // 0
    static final int TS_PMT_PID = 0x1000;    // 4096
    static final int TS_AUDIO_PID = 0x101;    // 257
    static final int TS_VIDEO_PID = 0x100;    // 256

    static final int TS_PAT_TABLE_ID = 0x00;
    static final int TS_PMT_TABLE_ID = 0x02;

    // Table 2-29 – Stream type assignments. page 66
    private static final byte STREAM_TYPE_AUDIO_AAC = 0x0f;
    private static final byte STREAM_TYPE_AUDIO_MP3 = 0x03;
    private static final byte STREAM_TYPE_VIDEO_H264 = 0x1b;

    final byte[] mData = new byte[TS_PACKET_SIZE];
    int mOffset = 0;

    void add(byte b) {
        mData[mOffset++] = b;
    }

    void add(ByteBuffer buffer, int len) {
        for (int i = 0; i < len; i++) {
            add(buffer.get());
        }
    }

    void add(byte[] buffer, int offset, int length) {
        for (int i = 0; i < length; i++) {
            add(buffer[offset + i]);
        }
    }

    void reset(final byte b) {
        Arrays.fill(mData, b);
        mOffset = 0;
    }

    void header(int pid, int continuity_counter) {
        byte sync_byte = 0x47;
        int transport_error_indicator = 0;
        int payload_unit_start_indicator = 1;
        int transport_priority = 0;
        int transport_scrambling_control = 0;
        int adaptation_field_control = 1;

        add(sync_byte);
        add((byte) ((transport_error_indicator << 7) | (payload_unit_start_indicator << 6) | (transport_priority << 5) | ((pid >> 8) & 0x1F)));
        add((byte) (pid & 0xff));
        add((byte) ((transport_scrambling_control << 6) | (adaptation_field_control << 4) | (continuity_counter & 0x0F)));
        add((byte) 0x00);    // adaptation field length
    }
}
