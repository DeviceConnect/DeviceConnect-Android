package org.deviceconnect.android.libmedia.streaming.mpeg2ts;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * H.264 形式の映像配信用 Transport Stream を生成するクラス.
 */
public class H264TransportStreamWriter {

    public interface PacketListener {
        void onPacketAvailable(byte[] packet);
    }

    private static final int H264NT_SLICE  = 1;

    private static final int H264NT_SLICE_IDR = 5;

    private static final byte[] H264_START_CODE = {0x00, 0x00, 0x00, 0x01};

    private final H264TransportPacketWriter tsWriter;

    private PacketListener bufferListener;

    private final Queue<FrameData> mVideoQueue = new ConcurrentLinkedQueue<>();

    private final Queue<FrameData> mAudioQueue = new ConcurrentLinkedQueue<>();

    private PTS videoPts;

    private PTS audioPts;

    private boolean isFirstVideoPes = true;

    private boolean isFirstAudioPes = true;

    private long ctime;

    private boolean headFrameAudio;

    private boolean syncPtsBase = false;

    public H264TransportStreamWriter() {
        tsWriter = new H264TransportPacketWriter();
        tsWriter.setCallback(packet -> bufferListener.onPacketAvailable(packet));
    }

    public void setBufferListener(final PacketListener bufferListener) {
        this.bufferListener = bufferListener;
    }

    public void initialize(float sampleRate, int sampleSizeInBits, int channels, int fps) {
        /*
         * ptsステップサイズ.
         *
         * ・音声: 1つのAACフレームに対応するサンプリングサンプル数/サンプリング周波数（単位: s)
         * ・映像：1000 / fps（単位：ms）
         * ・ミリ秒への変換：h264の設定によると90HZであるため、PTS/DTSのミリ秒への変換式は次のとおり。ms = pts / 90
         */
        this.videoPts = new PTS((long) (1000 / fps) * 90);
        if (sampleRate > 0) {
            this.audioPts = new PTS((90000 << 10) / (int) sampleRate);
        }
    }

    public synchronized void pushVideoBuffer(final ByteBuffer buffer, final boolean mixed) {
        saveBaseTime(false);
        if (headFrameAudio && !syncPtsBase) {
            videoPts.reset((System.currentTimeMillis() - ctime) * 90);
            syncPtsBase = true;
        }

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
            long pts = videoPts.getPts();
            buffer.position(unitOffset);

            FrameData v = new FrameData(buffer, unitLength, pts, isFirstVideoPes, isFrame, mixed);
            if (mixed) {
                mVideoQueue.add(v);
            } else {
                tsWriter.writeVideoBuffer(v.isFirstPes, v.frame, v.length, v.pts, v.pts, v.isFrame, v.isMixed);
            }
            isFirstVideoPes = false;

            writeBuffer();
        }
    }

    public synchronized void pushAudioBuffer(final ByteBuffer buffer, final int length, final boolean mixed) {
        saveBaseTime(true);
        if (!headFrameAudio && !syncPtsBase) {
            audioPts.reset((System.currentTimeMillis() - ctime) * 90);
            syncPtsBase = true;
        }

        long pts = audioPts.getPts();

        FrameData a = new FrameData(buffer, length, pts, isFirstAudioPes, true, mixed);
        if (mixed) {
            mAudioQueue.add(a);
        } else {
            tsWriter.writeAudioBuffer(a.isFirstPes, a.frame, a.length, a.pts, a.pts, a.isMixed);
        }


        isFirstAudioPes = false;

        writeBuffer();
    }

    private void writeBuffer() {
        try {
            while (!mAudioQueue.isEmpty() && !mVideoQueue.isEmpty()) {
                if (mAudioQueue.peek().pts > mVideoQueue.peek().pts) {
                    FrameData v = mVideoQueue.poll();
                    tsWriter.writeVideoBuffer(v.isFirstPes, v.frame, v.length, v.pts, v.pts, v.isFrame, v.isMixed);
                } else {
                    FrameData a = mAudioQueue.poll();
                    tsWriter.writeAudioBuffer(a.isFirstPes, a.frame, a.length, a.pts, a.pts, a.isMixed);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void saveBaseTime(boolean isAudio) {
        if (ctime == 0) {
            headFrameAudio = isAudio;
            ctime = System.currentTimeMillis();
        }
    }

    private static class FrameData {
        final long pts;
        final ByteBuffer frame;
        final int length;
        final boolean isFirstPes;
        final boolean isFrame;
        final boolean isMixed;

        public FrameData(ByteBuffer buffer, int frameLength, long pts, boolean isFirstPes, boolean isFrame, boolean isMixed) {
            if (buffer == null) {
                throw new IllegalArgumentException("frame is null");
            }

            byte[] array = new byte[frameLength];
            buffer.rewind();
            buffer.get(array, 0, frameLength);
            this.frame = ByteBuffer.wrap(array);
            this.frame.rewind();
            this.length = frameLength;

            this.pts = pts;
            this.isFirstPes = isFirstPes;
            this.isFrame = isFrame;
            this.isMixed = isMixed;
        }
    }

    private static class PTS {
        long time;
        final long inc;

        PTS(long inc) {
            this.inc = inc;
        }

        void reset(long time) {
            this.time = time;
        }

        long getPts() {
            return time += inc;
        }
    }
}
