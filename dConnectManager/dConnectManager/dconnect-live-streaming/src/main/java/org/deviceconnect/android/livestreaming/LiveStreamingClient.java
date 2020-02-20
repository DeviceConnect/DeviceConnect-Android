package org.deviceconnect.android.livestreaming;

import android.util.Log;

import org.deviceconnect.android.libmedia.streaming.IMediaMuxer;
import org.deviceconnect.android.libmedia.streaming.MediaEncoderException;
import org.deviceconnect.android.libmedia.streaming.MediaStreamer;
import org.deviceconnect.android.libmedia.streaming.audio.AudioEncoder;
import org.deviceconnect.android.libmedia.streaming.muxer.RtmpMuxer;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;

@SuppressWarnings("unused WeakerAccess")
public class LiveStreamingClient implements MediaStreamer.OnEventListener {
    private static final String TAG = "dConnectLibStreaming";
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private MediaStreamer mMediaStreamer;
    private EventListener mEventListener;
    private boolean mStreaming = false;

    public LiveStreamingClient(final String url) {
        if (DEBUG) {
            Log.d(TAG, "LiveStreamingClient()");
            Log.d(TAG, "url : " + url);
        }
        IMediaMuxer muxer = new RtmpMuxer(url);
        mMediaStreamer = new MediaStreamer(muxer);
        mEventListener = null;
        mMediaStreamer.setOnEventListener(this);
    }

    public LiveStreamingClient(final String url, EventListener eventListener) {
        this(url);
        mEventListener = eventListener;
    }

    public void setVideoEncoder(final VideoEncoder videoEncoder) {
        if (DEBUG) {
            Log.d(TAG, "setVideoEncoder()");
            Log.d(TAG, "videoEncoder : " + videoEncoder);
            Log.d(TAG, "mMediaStreamer : " + mMediaStreamer);
        }
        if (mMediaStreamer != null) {
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
