package org.deviceconnect.android.libmedia.streaming.mpeg2ts;

import com.google.common.primitives.Bytes;

import java.nio.ByteBuffer;

import static org.deviceconnect.android.libmedia.streaming.mpeg2ts.TransportPacket.TS_AUDIO_PID;
import static org.deviceconnect.android.libmedia.streaming.mpeg2ts.TransportPacket.TS_HEADER_SIZE;
import static org.deviceconnect.android.libmedia.streaming.mpeg2ts.TransportPacket.TS_PACKET_SIZE;
import static org.deviceconnect.android.libmedia.streaming.mpeg2ts.TransportPacket.TS_PAYLOAD_SIZE;
import static org.deviceconnect.android.libmedia.streaming.mpeg2ts.TransportPacket.TS_VIDEO_PID;

class H264TransportPacketWriter {

    // H264 Nalu
    private static byte[] H264_NAL = { 0x00, 0x00, 0x00, 0x01, 0x09, (byte) 0xf0 };

    // ContinuityCounter
    private byte mAudioContinuityCounter = 0;
    private byte mVideoContinuityCounter = 0;

    private TransportPacket mPacket = new TransportPacket();

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
        notifyPacket(mPacket);
    }

    private void notifyPacket(final TransportPacket p) {
        if (mCallback != null) {
            mCallback.onPacket(p.mData);
        }
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
                if (!isAudio && isFrame) {
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
}
