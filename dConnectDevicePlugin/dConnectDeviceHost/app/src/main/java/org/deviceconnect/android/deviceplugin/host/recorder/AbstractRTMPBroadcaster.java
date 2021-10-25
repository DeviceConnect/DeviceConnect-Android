package org.deviceconnect.android.deviceplugin.host.recorder;

import org.deviceconnect.android.libmedia.streaming.MediaEncoderException;
import org.deviceconnect.android.libmedia.streaming.audio.AudioEncoder;
import org.deviceconnect.android.libmedia.streaming.rtmp.RtmpClient;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;

public abstract class AbstractRTMPBroadcaster extends AbstractBroadcaster {
    /**
     * RTMP 配信クライアント.
     */
    private RtmpClient mRtmpClient;

    /**
     * イベントを通知するためのリスナー.
     */
    private Broadcaster.OnEventListener mOnBroadcasterEventListener;

    public AbstractRTMPBroadcaster(HostMediaRecorder recorder, String encoderId) {
        super(recorder, encoderId);
    }

    @Override
    public String getMimeType() {
        return "video/x-rtmp";
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
        String broadcastURI = getBroadcastURI();
        if (broadcastURI == null) {
            callback.onFailed(new RuntimeException("broadcastURI is not set."));
            return;
        }

        VideoEncoder videoEncoder = createVideoEncoder();
        if (videoEncoder != null) {
            setVideoQuality(videoEncoder.getVideoQuality());
        }

        AudioEncoder audioEncoder = createAudioEncoder();
        if (audioEncoder != null) {
            setAudioQuality(audioEncoder.getAudioQuality());
        }

        HostMediaRecorder.EncoderSettings settings = getEncoderSettings();

        mRtmpClient = new RtmpClient(broadcastURI);
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
        super.setMute(mute);

        if (mRtmpClient != null) {
            mRtmpClient.setMute(mute);
        }
    }

    @Override
    public boolean isMuted() {
        return mRtmpClient != null && mRtmpClient.isMute();
    }

    @Override
    protected VideoEncoder getVideoEncoder() {
        return mRtmpClient != null ? mRtmpClient.getVideoEncoder() : null;
    }

    @Override
    protected AudioEncoder getAudioEncoder() {
        return mRtmpClient != null ? mRtmpClient.getAudioEncoder() : null;
    }
}
