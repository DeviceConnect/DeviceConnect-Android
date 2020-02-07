package org.deviceconnect.android.libmedia.streaming.mpeg2ts;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.deviceconnect.android.libmedia.streaming.mpeg2ts.TransportPacket.TS_AUDIO_PID;
import static org.deviceconnect.android.libmedia.streaming.mpeg2ts.TransportPacket.TS_PAT_PID;
import static org.deviceconnect.android.libmedia.streaming.mpeg2ts.TransportPacket.TS_PAT_TABLE_ID;
import static org.deviceconnect.android.libmedia.streaming.mpeg2ts.TransportPacket.TS_PMT_PID;
import static org.deviceconnect.android.libmedia.streaming.mpeg2ts.TransportPacket.TS_PMT_TABLE_ID;
import static org.deviceconnect.android.libmedia.streaming.mpeg2ts.TransportPacket.TS_VIDEO_PID;


/**
 * H.264 形式の映像配信用 Transport Stream を生成するクラス.
 */
public class H264TransportStreamWriter {

    public interface PacketListener {
        void onPacketAvailable(byte[] packet);
    }

    private class TableTimerTask extends TimerTask {

        @Override
        public void run() {
            createPAT(mPAT);
            notifyPacket(mPAT);

            createPMT(mPMT, mFrameDataType);
            notifyPacket(mPMT);
        }
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

    /**
     * PAT, PMT を定期的に送信するタイマー.
     */
    private Timer mTableTimer;

    /**
     * 起動済みフラグ.
     */
    private boolean mIsStarted;

    /**
     * 入力するフレームのタイプ
     */
    private FrameDataType mFrameDataType = FrameDataType.MIXED;

    /**
     * PAT パケット.
     */
    private final TransportPacket mPAT = new TransportPacket();

    /**
     * PMT パケット.
     */
    private final TransportPacket mPMT = new TransportPacket();

    /**
     * PAT パケットのカウンター.
     */
    private int mPatContinuityCounter = 0;

    /**
     * PMT パケットのカウンター.
     */
    private int mPmtContinuityCounter = 0;

    public H264TransportStreamWriter() {
        tsWriter = new H264TransportPacketWriter();
        tsWriter.setCallback(packet -> notifyPacket(packet));
    }

    public void setStreamEnabled(final boolean hasVideo, final boolean hasAudio) {
        if (!hasVideo && !hasAudio) {
            throw new IllegalArgumentException();
        }
        if (hasVideo && hasAudio) {
            mFrameDataType = FrameDataType.MIXED;
        } else if (hasVideo) {
            mFrameDataType = FrameDataType.VIDEO;
        } else {
            mFrameDataType = FrameDataType.AUDIO;
        }
    }

    private boolean isMixed() {
        return mFrameDataType == FrameDataType.MIXED;
    }

    private void notifyPacket(TransportPacket packet) {
        notifyPacket(packet.mData);
    }

    private void notifyPacket(byte[] packet) {
        bufferListener.onPacketAvailable(packet);
    }

    private void createPAT(final TransportPacket p) {
        p.reset((byte) 0xFF);

        // header
        p.header(TS_PAT_PID, mPatContinuityCounter);
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

        p.add((byte) TS_PAT_TABLE_ID);
        p.add((byte) ((section_syntax_indicator << 7) | (zero << 6) | (reserved_1 << 4) | ((section_length >> 8) & 0x0F)));
        p.add((byte) (section_length & 0xFF));
        p.add((byte) ((transport_stream_id >> 8) & 0xFF));
        p.add((byte) (transport_stream_id & 0xFF));
        p.add((byte) ((reserved_2 << 6) | (version_number << 1) | (current_next_indicator & 0x01)));
        p.add((byte) (section_number & 0xFF));
        p.add((byte) (last_section_number & 0xFF));
        p.add((byte) ((program_number >> 8) & 0xFF));

        p.add((byte) (program_number & 0xFF));
        p.add((byte) ((reserved_3 << 5) | ((program_id >> 8) & 0x1F)));
        p.add((byte) (program_id & 0xFF));

        // set crc32
        long crc = CrcUtil.mpegts_crc32(p.mData, 5, 12);
        p.add((byte) ((crc >> 24) & 0xFF));
        p.add((byte) ((crc >> 16) & 0xFF));
        p.add((byte) ((crc >> 8) & 0xFF));
        p.add((byte) ((crc) & 0xFF));
    }

    private void createPMT(final TransportPacket p, final FrameDataType fType) {
        p.reset((byte) 0xFF);

        // header
        p.header(TS_PMT_PID, mPmtContinuityCounter);
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

        p.add((byte) TS_PMT_TABLE_ID);
        p.add((byte) ((section_syntax_indicator << 7) | (zero << 6) | (reserved_1 << 4) | ((section_length >> 8) & 0x0F)));
        p.add((byte) (section_length & 0xFF));
        p.add((byte) ((program_number >> 8) & 0xFF));
        p.add((byte) (program_number & 0xFF));
        p.add((byte) ((reserved_2 << 6) | (version_number << 1) | (current_next_indicator & 0x01)));
        p.add((byte) section_number);
        p.add((byte) last_section_number);
        p.add((byte) ((reserved_3 << 5) | ((pcr_pid >> 8) & 0xFF)));
        p.add((byte) (pcr_pid & 0xFF));
        p.add((byte) ((reserved_4 << 4) | ((program_info_length >> 8) & 0xFF)));
        p.add((byte) (program_info_length & 0xFF));


        // set video stream info
        if ( fType == FrameDataType.VIDEO || fType == FrameDataType.MIXED ) {
            int stream_type = 0x1b;
            int reserved_5 = 7;
            int elementary_pid = TS_VIDEO_PID;
            int reserved_6 = 15;
            int ES_info_length = 0;

            p.add((byte) stream_type);
            p.add((byte) ((reserved_5 << 5) | ((elementary_pid >> 8) & 0x1F)));
            p.add((byte) (elementary_pid & 0xFF));
            p.add((byte) ((reserved_6 << 4) | ((ES_info_length >> 4) & 0x0F)));
            p.add((byte) (ES_info_length & 0xFF));
        }


        // set audio stream info
        if ( fType == FrameDataType.AUDIO || fType == FrameDataType.MIXED ) {

            int stream_type = 0x0f;
            int reserved_5 = 7;
            int elementary_pid = TS_AUDIO_PID;
            int reserved_6 = 15;
            int ES_info_length = 0;

            p.add((byte) stream_type);
            p.add((byte) ((reserved_5 << 5) | ((elementary_pid >> 8) & 0x1F)));
            p.add((byte) (elementary_pid & 0xFF));
            p.add((byte) ((reserved_6 << 4) | ((ES_info_length >> 4) & 0x0F)));
            p.add((byte) (ES_info_length & 0xFF));
        }


        // set crc32
        long crc =  CrcUtil.mpegts_crc32(p.mData, 5,  (fType == FrameDataType.MIXED) ? 22: 17);
        p.add((byte) ((crc >> 24) & 0xFF));
        p.add((byte) ((crc >> 16) & 0xFF));
        p.add((byte) ((crc >> 8) & 0xFF));
        p.add((byte) ((crc) & 0xFF));
    }

    public synchronized void start() {
        if (mIsStarted) {
            return;
        }
        mIsStarted = true;

        mTableTimer = new Timer();
        mTableTimer.schedule(new TableTimerTask(), 0, 1000);
    }

    public synchronized void stop() {
        if (!mIsStarted) {
            return;
        }
        mIsStarted = false;

        mTableTimer.cancel();
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

    public synchronized void pushVideoBuffer(final ByteBuffer buffer) {
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

            boolean mixed = isMixed();
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

    public synchronized void pushAudioBuffer(final ByteBuffer buffer, final int length) {
        saveBaseTime(true);
        if (!headFrameAudio && !syncPtsBase) {
            audioPts.reset((System.currentTimeMillis() - ctime) * 90);
            syncPtsBase = true;
        }

        long pts = audioPts.getPts();

        boolean mixed = isMixed();
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
