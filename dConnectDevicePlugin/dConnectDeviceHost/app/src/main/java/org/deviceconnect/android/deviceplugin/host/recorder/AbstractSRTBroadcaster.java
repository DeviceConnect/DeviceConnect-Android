package org.deviceconnect.android.deviceplugin.host.recorder;

import org.deviceconnect.android.libmedia.streaming.MediaEncoderException;
import org.deviceconnect.android.libmedia.streaming.audio.AudioEncoder;
import org.deviceconnect.android.libmedia.streaming.audio.MicAACLATMEncoder;
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
    private OnEventListener mOnBroadcasterEventListener;

    public AbstractSRTBroadcaster(HostMediaRecorder recorder, String broadcastURI) {
       super(recorder, broadcastURI);
    }

    /**
     * SRT 配信に使用する VideoEncoder のインスタンスを作成します.
     *
     * @return SRT 配信に使用する VideoEncoder
     */
    protected abstract VideoEncoder createVideoEncoder();

    @Override
    public void setOnEventListener(OnEventListener listener) {
        mOnBroadcasterEventListener = listener;
    }

    @Override
    public boolean isRunning() {
        return mSrtClient != null && mSrtClient.isRunning();
    }

    @Override
    public void start(OnStartCallback callback) {
        HostMediaRecorder.Settings settings = getRecorder().getSettings();

        VideoEncoder videoEncoder = createVideoEncoder();
        setVideoQuality(videoEncoder.getVideoQuality());

        AudioEncoder audioEncoder = null;
        if (settings.isAudioEnabled()) {
            audioEncoder = new MicAACLATMEncoder();
            setAudioQuality(audioEncoder.getAudioQuality());
        }

        mSrtClient = new SRTClient(getBroadcastURI());
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
            }

            @Override
            public void onConnected() {
            }

            @Override
            public void onDisconnected() {
                AbstractSRTBroadcaster.this.stop();
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
        if (mSrtClient != null) {
            mSrtClient.setMute(mute);
        }
    }

    @Override
    public boolean isMute() {
        return mSrtClient != null && mSrtClient.isMute();
    }

    @Override
    public void onConfigChange() {
        super.onConfigChange();

        if (mSrtClient != null) {
            mSrtClient.restartVideoEncoder();
        }
    }
}
