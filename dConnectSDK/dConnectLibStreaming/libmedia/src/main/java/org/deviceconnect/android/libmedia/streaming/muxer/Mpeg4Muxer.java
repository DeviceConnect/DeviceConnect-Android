package org.deviceconnect.android.libmedia.streaming.muxer;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;

import org.deviceconnect.android.libmedia.streaming.IMediaMuxer;
import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Mpeg4Muxer implements IMediaMuxer {
    private MediaMuxer mMuxer;
    private int mVideoTrackIndex;
    private int mAudioTrackIndex;
    private boolean mMuxerStarted = false;

    public Mpeg4Muxer(String outputPath) {
        try {
            mMuxer = new MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException ioe) {
            throw new RuntimeException("MediaMuxer creation failed", ioe);
        }
        mVideoTrackIndex = -1;
        mAudioTrackIndex = -1;
    }

    @Override
    public boolean onPrepare(VideoQuality quality, AudioQuality audioQuality) {
        mMuxerStarted = false;
        return true;
    }

    @Override
    public void onVideoFormatChanged(MediaFormat newFormat) {
        mVideoTrackIndex = mMuxer.addTrack(newFormat);
        startMixer();
    }

    @Override
    public void onWriteVideoData(ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo) {
        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            bufferInfo.size = 0;
        }

        if (bufferInfo.size != 0) {
            if (!mMuxerStarted) {
                return;
            }
            encodedData.position(bufferInfo.offset);
            encodedData.limit(bufferInfo.offset + bufferInfo.size);
            mMuxer.writeSampleData(mVideoTrackIndex, encodedData, bufferInfo);
        }
    }

    @Override
    public void onAudioFormatChanged(MediaFormat newFormat) {
        mAudioTrackIndex = mMuxer.addTrack(newFormat);
        startMixer();
    }

    @Override
    public void onWriteAudioData(ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo) {
        if (bufferInfo.size != 0) {
            if (!mMuxerStarted) {
                return;
            }
            encodedData.position(bufferInfo.offset);
            encodedData.limit(bufferInfo.offset + bufferInfo.size);
            mMuxer.writeSampleData(mAudioTrackIndex, encodedData, bufferInfo);
        }
    }

    @Override
    public void onReleased() {
        if (mMuxer != null) {
            mMuxer.stop();
            mMuxer.release();
            mMuxer = null;
        }
    }

    private void startMixer() {
        if (mMuxerStarted) {
            return;
        }
        mMuxer.start();
        mMuxerStarted = true;
    }
}
