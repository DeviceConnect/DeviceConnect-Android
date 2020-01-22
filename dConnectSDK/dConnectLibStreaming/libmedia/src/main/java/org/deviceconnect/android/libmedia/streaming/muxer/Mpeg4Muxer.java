package org.deviceconnect.android.libmedia.streaming.muxer;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.deviceconnect.android.libmedia.streaming.IMediaMuxer;
import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;

public class Mpeg4Muxer implements IMediaMuxer {
    private static final boolean DEBUG = false;
    private static final String TAG = "MPEG4-MUXER";

    private MediaMuxer mMuxer;
    private int mTrackIndex;
    private boolean mMuxerStarted = false;

    public Mpeg4Muxer(String outputPath) {
        try {
            mMuxer = new MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException ioe) {
            throw new RuntimeException("MediaMuxer creation failed", ioe);
        }
        mTrackIndex = -1;
    }

    @Override
    public boolean onPrepare(VideoQuality quality, AudioQuality audioQuality) {
        mMuxerStarted = false;
        return true;
    }

    @Override
    public void onVideoFormatChanged(MediaFormat newFormat) {
        mTrackIndex = mMuxer.addTrack(newFormat);
        mMuxer.start();
        mMuxerStarted = true;
    }

    @Override
    public void onWriteVideoData(ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo) {
        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            bufferInfo.size = 0;
        }

        if (bufferInfo.size != 0) {
            if (!mMuxerStarted) {
                throw new RuntimeException("muxer hasn't started");
            }
            encodedData.position(bufferInfo.offset);
            encodedData.limit(bufferInfo.offset + bufferInfo.size);
            mMuxer.writeSampleData(mTrackIndex, encodedData, bufferInfo);
        }
    }

    @Override
    public void onAudioFormatChanged(MediaFormat newFormat) {
    }

    @Override
    public void onWriteAudioData(ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo) {
    }

    @Override
    public void onReleased() {
        if (mMuxer != null) {
            mMuxer.stop();
            mMuxer.release();
            mMuxer = null;
        }
    }
}
