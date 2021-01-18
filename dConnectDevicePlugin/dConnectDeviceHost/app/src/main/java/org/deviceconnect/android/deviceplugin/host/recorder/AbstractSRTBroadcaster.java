package org.deviceconnect.android.deviceplugin.host.recorder;

import org.deviceconnect.android.libmedia.streaming.MediaEncoderException;
import org.deviceconnect.android.libmedia.streaming.audio.AudioEncoder;
import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;
import org.deviceconnect.android.libmedia.streaming.audio.MicAACLATMEncoder;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;
import org.deviceconnect.android.libsrt.broadcast.SRTClient;

public abstract class AbstractSRTBroadcaster implements Broadcaster {
    /**
     * 配信先の URI.
     */
    private final String mBroadcastURI;

    /**
     * カメラを操作するレコーダ.
     */
    private final HostMediaRecorder mRecorder;

    /**
     * RTMP 配信クライアント.
     */
    private SRTClient mSrtClient;

    /**
     * イベントを通知するためのリスナー.
     */
    private OnEventListener mOnBroadcasterEventListener;

    public AbstractSRTBroadcaster(HostMediaRecorder recorder, String broadcastURI) {
        mRecorder = recorder;
        mBroadcastURI = broadcastURI;
    }

    protected abstract VideoEncoder createVideoEncoder();

    public HostMediaRecorder getRecorder() {
        return mRecorder;
    }

    @Override
    public String getMimeType() {
        return "";
    }

    @Override
    public String getBroadcastURI() {
        return mBroadcastURI;
    }

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
        HostMediaRecorder.Settings settings = mRecorder.getSettings();

        VideoEncoder videoEncoder = createVideoEncoder();
        VideoQuality videoQuality = videoEncoder.getVideoQuality();
        videoQuality.setVideoWidth(settings.getPreviewSize().getWidth());
        videoQuality.setVideoHeight(settings.getPreviewSize().getHeight());
        videoQuality.setIFrameInterval(settings.getPreviewKeyFrameInterval());
        videoQuality.setFrameRate(settings.getPreviewMaxFrameRate());
        videoQuality.setBitRate(settings.getPreviewBitRate());

        AudioEncoder audioEncoder = null;
        if (settings.isAudioEnabled()) {
            audioEncoder = new MicAACLATMEncoder();
            AudioQuality audioQuality = audioEncoder.getAudioQuality();
            audioQuality.setBitRate(settings.getPreviewAudioBitRate());
            audioQuality.setSamplingRate(settings.getPreviewSampleRate());
        }

        mSrtClient = new SRTClient(mBroadcastURI);
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
        if (mSrtClient != null) {
            mSrtClient.restartVideoEncoder();
        }
    }
}
