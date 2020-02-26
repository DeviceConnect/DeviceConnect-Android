package org.deviceconnect.android.deviceplugin.host.recorder.util;

import android.util.Log;

import androidx.annotation.NonNull;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.libmedia.streaming.IMediaMuxer;
import org.deviceconnect.android.libmedia.streaming.MediaEncoderException;
import org.deviceconnect.android.libmedia.streaming.MediaStreamer;
import org.deviceconnect.android.libmedia.streaming.audio.AudioEncoder;
import org.deviceconnect.android.libmedia.streaming.muxer.RtmpMuxer;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;

@SuppressWarnings("unused")
public class LiveStreamingClient implements MediaStreamer.OnEventListener {
    private static final String TAG = "LiveStreamingClient";
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private MediaStreamer mMediaStreamer;
    private EventListener mEventListener;
    private boolean mStreaming = false;
    private boolean mError = false;
    private final String mBroadcastURI;

    public LiveStreamingClient(final String broadcastURI, EventListener eventListener) {
        if (DEBUG) {
            Log.d(TAG, "LiveStreamingClient()");
            Log.d(TAG, "broadcastURI : " + broadcastURI);
        }
        IMediaMuxer muxer = new RtmpMuxer(broadcastURI);
        mMediaStreamer = new MediaStreamer(muxer);
        mEventListener = null;
        mMediaStreamer.setOnEventListener(this);
        mBroadcastURI = broadcastURI;
        mEventListener = eventListener;
    }

    public void setVideoEncoder(final @NonNull VideoEncoder videoEncoder, Integer width, Integer height, Integer bitrate, Integer frameRate) {
        if (DEBUG) {
            Log.d(TAG, "setVideoEncoder()");
            Log.d(TAG, "videoEncoder : " + videoEncoder);
            Log.d(TAG, "width : " + width);
            Log.d(TAG, "height : " + height);
            Log.d(TAG, "bitrate : " + bitrate);
            Log.d(TAG, "frameRate : " + frameRate);
            Log.d(TAG, "mMediaStreamer : " + mMediaStreamer);
        }
        if (mMediaStreamer != null) {
            VideoQuality videoQuality = videoEncoder.getVideoQuality();
            if (width != null) {
                videoQuality.setVideoWidth(width);
            }
            if (height != null) {
                videoQuality.setVideoHeight(height);
            }
            if (width != null) {
                videoQuality.setBitRate(bitrate);
            }
            if (width != null) {
                videoQuality.setFrameRate(frameRate);
            }
            mMediaStreamer.setVideoEncoder(videoEncoder);
        }
    }

    public void setAudioEncoder(final AudioEncoder audioEncoder) {
        if (DEBUG) {
            Log.d(TAG, "setAudioEncoder()");
            Log.d(TAG, "audioEncoder : " + audioEncoder);
            Log.d(TAG, "mMediaStreamer : " + mMediaStreamer);
        }
        if (mMediaStreamer != null) {
            mMediaStreamer.setAudioEncoder(audioEncoder);
        }
    }

    public void start() {
        if (DEBUG) {
            Log.d(TAG, "start()");
            Log.d(TAG, "mMediaStreamer : " + mMediaStreamer);
        }
        if (mMediaStreamer != null) {
            mMediaStreamer.start();
            mStreaming = true;
        }
    }

    public void stop() {
        if (DEBUG) {
            Log.d(TAG, "stop()");
            Log.d(TAG, "mMediaStreamer : " + mMediaStreamer);
        }
        if (mMediaStreamer != null) {
            mMediaStreamer.stop();
            mStreaming = false;
        }
    }

    public void setMute(final boolean value) {
        if (DEBUG) {
            Log.d(TAG, "setMute()");
            Log.d(TAG, "mMediaStreamer : " + mMediaStreamer);
            Log.d(TAG, "value : " + value);
        }
        if (mMediaStreamer != null) {
            final AudioEncoder audioEncoder = mMediaStreamer.getAudioEncoder();
            if (DEBUG) {
                Log.d(TAG, "audioEncoder : " + audioEncoder);
            }
            if (audioEncoder != null) {
                audioEncoder.setMute(value);
            }
        }
    }

    public boolean isStreaming() {
        if (DEBUG) {
            Log.d(TAG, "isStreaming()");
            Log.d(TAG, "mStreaming : " + mStreaming);
        }
        return mStreaming;
    }

    /**
     * オーディオのミュート状態を判定する
     * @return ミュート状態ならばtrue,ミュート状態でないまたは取得不能時はfalse
     */
    public boolean isMute() {
        if (DEBUG) {
            Log.d(TAG, "mute()");
            Log.d(TAG, "mMediaStreamer : " + mMediaStreamer);
        }
        if (mMediaStreamer != null) {
            final AudioEncoder audioEncoder = mMediaStreamer.getAudioEncoder();
            if (DEBUG) {
                Log.d(TAG, "audioEncoder : " + audioEncoder);
            }
            if (audioEncoder != null) {
                return audioEncoder.isMute();
            }
        }
        return false;
    }

    public String getBroadcastURI() {
        return mBroadcastURI;
    }

    public int getVideoWidth() {
        if (mMediaStreamer != null) {
            VideoEncoder videoEncoder = mMediaStreamer.getVideoEncoder();
            if (videoEncoder != null) {
                VideoQuality videoQuality = videoEncoder.getVideoQuality();
                if (videoQuality != null) {
                    return videoQuality.getVideoWidth();
                }
            }
        }
        return 0;
    }

    public int getVideoHeight() {
        if (mMediaStreamer != null) {
            VideoEncoder videoEncoder = mMediaStreamer.getVideoEncoder();
            if (videoEncoder != null) {
                VideoQuality videoQuality = videoEncoder.getVideoQuality();
                if (videoQuality != null) {
                    return videoQuality.getVideoHeight();
                }
            }
        }
        return 0;
    }

    public int getBitrate() {
        if (mMediaStreamer != null) {
            VideoEncoder videoEncoder = mMediaStreamer.getVideoEncoder();
            if (videoEncoder != null) {
                VideoQuality videoQuality = videoEncoder.getVideoQuality();
                if (videoQuality != null) {
                    return videoQuality.getBitRate();
                }
            }
        }
        return 0;
    }

    public int getFrameRate() {
        if (mMediaStreamer != null) {
            VideoEncoder videoEncoder = mMediaStreamer.getVideoEncoder();
            if (videoEncoder != null) {
                VideoQuality videoQuality = videoEncoder.getVideoQuality();
                if (videoQuality != null) {
                    return videoQuality.getFrameRate();
                }
            }
        }
        return 0;
    }

    public String getMimeType() {
        if (mMediaStreamer != null) {
            VideoEncoder videoEncoder = mMediaStreamer.getVideoEncoder();
            if (videoEncoder != null) {
                VideoQuality videoQuality = videoEncoder.getVideoQuality();
                if (videoQuality != null) {
                    return videoQuality.getMimeType();
                }
            }
        }
        return null;
    }

    public boolean isError() {
        return mError;
    }

    @Override
    public void onStarted() {
        if (DEBUG) {
            Log.d(TAG, "onStarted()");
        }
        mStreaming = true;
        if (mEventListener != null) {
            mEventListener.onStart();
        }
    }

    @Override
    public void onStopped() {
        if (DEBUG) {
            Log.d(TAG, "onStopped()");
        }
        mStreaming = false;
        if (mEventListener != null) {
            mEventListener.onStop();
        }
    }

    @Override
    public void onError(MediaEncoderException e) {
        if (DEBUG) {
            Log.d(TAG, "onError()");
        }
        e.printStackTrace();
        mError = true;
        if (mEventListener != null) {
            mEventListener.onError(e);
        }
    }

    public interface EventListener {
        void onStart();
        void onStop();
        void onError(MediaEncoderException mediaEncoderException);
    }
}
