package org.deviceconnect.android.deviceplugin.host.recorder;

import org.deviceconnect.android.libmedia.streaming.MediaEncoderException;
import org.deviceconnect.android.libmedia.streaming.audio.AudioEncoder;
import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;
import org.deviceconnect.android.libmedia.streaming.rtmp.RtmpClient;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;

public abstract class AbstractRTMPBroadcaster extends AbstractBroadcaster {

    /**
     * RTMP 配信クライアント.
     */
    private RtmpClient mRtmpClient;

    /**
     * イベントを通知するためのリスナー.
     */
    private Broadcaster.OnEventListener mOnBroadcasterEventListener;

    public AbstractRTMPBroadcaster(HostMediaRecorder recorder, String broadcastURI, String name) {
        super(recorder, broadcastURI, name);
    }

    @Override
    public void setOnEventListener(Broadcaster.OnEventListener listener) {
        mOnBroadcasterEventListener = listener;
    }

    @Override
    public boolean isRunning() {
        return mRtmpClient != null && mRtmpClient.isRunning();
    }

    @Override
    public void start(OnStartCallback callback) {
        VideoEncoder videoEncoder = createVideoEncoder();
        if (videoEncoder != null) {
            setVideoQuality(videoEncoder.getVideoQuality());
        }

        AudioEncoder audioEncoder = createAudioEncoder();
        if (audioEncoder != null) {
            setAudioQuality(audioEncoder.getAudioQuality());
        }

        HostMediaRecorder.StreamingSettings settings = getStreamingSettings();

        mRtmpClient = new RtmpClient(getBroadcastURI());
        mRtmpClient.setMaxRetryCount(settings.getRetryCount());
        mRtmpClient.setRetryInterval(settings.getRetryInterval());
        mRtmpClient.setVideoEncoder(videoEncoder);
        mRtmpClient.setAudioEncoder(audioEncoder);
        mRtmpClient.setOnEventListener(new RtmpClient.OnEventListener() {
            @Override
            public void onStarted() {
                if (callback != null) {
                    callback.onSuccess();
                }

                if (mOnBroadcasterEventListener != null) {
                    mOnBroadcasterEventListener.onStarted();
                }
            }

            @Override
            public void onStopped() {
                if (mOnBroadcasterEventListener != null) {
                    mOnBroadcasterEventListener.onStopped();
                }
            }

            @Override
            public void onError(MediaEncoderException e) {
                if (callback != null) {
                    callback.onFailed(e);
                }

                if (mOnBroadcasterEventListener != null) {
                    mOnBroadcasterEventListener.onError(e);
                }

                AbstractRTMPBroadcaster.this.stop();
            }

            @Override
            public void onConnected() {
            }

            @Override
            public void onDisconnected() {
            }

            @Override
            public void onNewBitrate(long bitrate) {
            }
        });
        mRtmpClient.start();
    }

    @Override
    public void stop() {
        if (mRtmpClient != null) {
            mRtmpClient.stop();
            mRtmpClient = null;
        }
    }

    @Override
    public void setMute(boolean mute) {
        if (mRtmpClient != null) {
            mRtmpClient.setMute(mute);
        }
    }

    @Override
    public boolean isMute() {
        return mRtmpClient != null && mRtmpClient.isMute();
    }

    @Override
    public void onConfigChange() {
        super.onConfigChange();

        if (mRtmpClient != null) {
            mRtmpClient.restartVideoEncoder();
        }
    }

    @Override
    protected VideoQuality getVideoQuality() {
        if (mRtmpClient != null) {
            VideoEncoder videoEncoder = mRtmpClient.getVideoEncoder();
            if (videoEncoder != null) {
                return videoEncoder.getVideoQuality();
            }
        }
        return null;
    }

    @Override
    protected AudioQuality getAudioQuality() {
        if (mRtmpClient != null) {
            AudioEncoder audioEncoder = mRtmpClient.getAudioEncoder();
            if (audioEncoder != null) {
                return audioEncoder.getAudioQuality();
            }
        }
        return null;
    }
}
