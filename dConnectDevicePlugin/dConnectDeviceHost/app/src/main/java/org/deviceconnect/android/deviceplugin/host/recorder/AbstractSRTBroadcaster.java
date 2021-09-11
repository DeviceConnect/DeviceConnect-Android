package org.deviceconnect.android.deviceplugin.host.recorder;

import org.deviceconnect.android.libmedia.streaming.MediaEncoderException;
import org.deviceconnect.android.libmedia.streaming.audio.AudioEncoder;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;
import org.deviceconnect.android.libsrt.broadcast.SRTClient;

public abstract class AbstractSRTBroadcaster extends AbstractBroadcaster {
    /**
     * RTMP 配信クライアント.
     */
    private SRTClient mSrtClient;

    /**
     * イベントを通知するためのリスナー.
     */
    private Broadcaster.OnEventListener mOnBroadcasterEventListener;

    public AbstractSRTBroadcaster(HostMediaRecorder recorder, String id) {
       super(recorder, id);
    }

    @Override
    public String getMimeType() {
        return "video/MP2T";
    }

    @Override
    public void setOnEventListener(Broadcaster.OnEventListener listener) {
        mOnBroadcasterEventListener = listener;
    }

    @Override
    public boolean isRunning() {
        return mSrtClient != null && mSrtClient.isRunning();
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

        mSrtClient = new SRTClient(getBroadcastURI());
        mSrtClient.setMaxRetryCount(settings.getRetryCount());
        mSrtClient.setRetryInterval(settings.getRetryInterval());
        mSrtClient.setVideoEncoder(videoEncoder);
        mSrtClient.setAudioEncoder(audioEncoder);
        mSrtClient.setOnEventListener(new SRTClient.OnEventListener() {
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

                AbstractSRTBroadcaster.this.stop();
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
        mSrtClient.start();
    }

    @Override
    public void stop() {
        if (mSrtClient != null) {
            mSrtClient.stop();
            mSrtClient = null;
        }
    }

    @Override
    public void setMute(boolean mute) {
        super.setMute(mute);

        if (mSrtClient != null) {
            mSrtClient.setMute(mute);
        }
    }

    @Override
    public boolean isMuted() {
        return mSrtClient != null && mSrtClient.isMute();
    }

    @Override
    public void onConfigChange() {
        super.onConfigChange();

        if (mSrtClient != null) {
            mSrtClient.restartVideoEncoder();
        }
    }

    @Override
    protected VideoEncoder getVideoEncoder() {
        return mSrtClient != null ? mSrtClient.getVideoEncoder() : null;
    }

    @Override
    protected AudioEncoder getAudioEncoder() {
        return mSrtClient != null ? mSrtClient.getAudioEncoder() : null;
    }
}
