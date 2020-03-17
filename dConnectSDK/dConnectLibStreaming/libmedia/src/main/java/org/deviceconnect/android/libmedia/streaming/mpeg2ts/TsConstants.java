package org.deviceconnect.android.libmedia.streaming.mpeg2ts;

public interface TsConstants {
    // Transport Stream packets are 188 bytes in length
    int TS_PACKET_SIZE = 188;
    int TS_HEADER_SIZE = 4;
    int TS_PAYLOAD_SIZE = TS_PACKET_SIZE - TS_HEADER_SIZE;

    byte SYNC_BYTE = 0x47;

    // Table 2-18 – Stream_id assignments
    int STREAM_ID_VIDEO = 0xE0;
    int STREAM_ID_AUDIO = 0xC0;
    int STREAM_ID_PROGRAM_STREAM_MAP = 0b10111100;
    int STREAM_ID_PRIVATE_STREAM_1 = 0b10111101;
    int STREAM_ID_PADDING_STREAM = 0b10111110;
    int STREAM_ID_PRIVATE_STREAM_2 = 0b10111111;
    int STREAM_ID_ECM_STREAM = 0b11110000;
    int STREAM_ID_EMM_STREAM = 0b11110001;
    int STREAM_ID_DSMCC_STREAM = 0b11110010;
    int STREAM_ID_PROGRAM_STREAM_DIRECTORY = 0b11111111;
    int STREAM_ID_H222_STREAM = 0b11111000;

    // Table 2-29 – Stream type assignments. page 66
    byte STREAM_TYPE_AUDIO_AAC = 0x0F;
    byte STREAM_TYPE_AUDIO_MP3 = 0x03;
    byte STREAM_TYPE_VIDEO_H264 = 0x1B;
    byte STREAM_TYPE_VIDEO_H265 = 0x24;

    int TS_PAT_PID = 0x00;
    int TS_CAT_PID = 0x01;
    int TS_TSDT_PID = 0x02;
    int TS_IPMP_PID = 0x03;
    int TS_PMT_PID = 0x1000;
}
