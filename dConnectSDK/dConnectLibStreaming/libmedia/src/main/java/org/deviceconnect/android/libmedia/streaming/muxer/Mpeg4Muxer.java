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
    private boolean mVideoEnabled = false;
    private boolean mAudioEnabled = false;
    private long mPresentationTimeUs;
    private final Object mLockObject = new Object();

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
    public boolean onPrepare(VideoQuality videoQuality, AudioQuality audioQuality) {
        mMuxerStarted = false;
        mVideoEnabled = videoQuality != null;
        mAudioEnabled = audioQuality != null;
        mPresentationTimeUs = 0;
        return true;
    }

    @Override
    public void onVideoFormatChanged(MediaFormat newFormat) {
        mVideoTrackIndex = mMuxer.addTrack(newFormat);
        startMixer();

        synchronized (mLockObject) {
            if (!mMuxerStarted && mMuxer != null) {
                try {
                    mLockObject.wait(5000);
                } catch (Exception e) {
                    // ignore.
                }
            }
        }
    }

    @Override
    public void onWriteVideoData(ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo) {
        writeData(mVideoTrackIndex, encodedData, bufferInfo);
    }

    @Override
    public void onAudioFormatChanged(MediaFormat newFormat) {
        mAudioTrackIndex = mMuxer.addTrack(newFormat);
        startMixer();

        synchronized (mLockObject) {
            if (!mMuxerStarted && mMuxer != null) {
                try {
                    mLockObject.wait(5000);
                } catch (Exception e) {
                    // ignore.
                }
            }
        }
    }

    @Override
    public void onWriteAudioData(ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo) {
        writeData(mAudioTrackIndex, encodedData, bufferInfo);
    }

    @Override
    public void onReleased() {
        synchronized (mLockObject) {
            mLockObject.notifyAll();
        }

        if (mMuxer != null) {
            try {
                mMuxer.stop();
            } catch (Exception e) {
                // ignore.
            }

            try {
                mMuxer.release();
            } catch (Exception e) {
                // ignore.
            }
            mMuxer = null;
        }
    }

    private synchronized void writeData(int trackIndex, ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo) {
        if (!mMuxerStarted) {
            return;
        }

        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            bufferInfo.size = 0;
        }

        if (bufferInfo.size != 0) {
            bufferInfo.presentationTimeUs = getPresentationTime(bufferInfo);
            encodedData.position(bufferInfo.offset);
            encodedData.limit(bufferInfo.offset + bufferInfo.size);
            mMuxer.writeSampleData(trackIndex, encodedData, bufferInfo);
        }
    }

    private long getPresentationTime(MediaCodec.BufferInfo bufferInfo) {
        if (mPresentationTimeUs == 0) {
            mPresentationTimeUs = bufferInfo.presentationTimeUs;
        }
        return bufferInfo.presentationTimeUs - mPresentationTimeUs;
    }

    private synchronized void startMixer() {
        if (mMuxerStarted) {
            return;
        }

        if (mAudioEnabled && mAudioTrackIndex == -1) {
            return;
        }

        if (mVideoEnabled && mVideoTrackIndex == -1) {
            return;
        }

        mMuxer.start();
        mMuxerStarted = true;

        synchronized (mLockObject) {
            mLockObject.notifyAll();
        }
    }
}
