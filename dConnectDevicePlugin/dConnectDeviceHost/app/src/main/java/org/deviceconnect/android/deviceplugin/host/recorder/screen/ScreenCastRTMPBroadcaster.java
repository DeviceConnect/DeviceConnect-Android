package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import org.deviceconnect.android.deviceplugin.host.recorder.Broadcaster;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.libmedia.streaming.MediaEncoderException;
import org.deviceconnect.android.libmedia.streaming.audio.AudioEncoder;
import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;
import org.deviceconnect.android.libmedia.streaming.audio.MicAACLATMEncoder;
import org.deviceconnect.android.libmedia.streaming.rtmp.RtmpClient;
import org.deviceconnect.android.libmedia.streaming.video.CameraVideoQuality;

public class ScreenCastRTMPBroadcaster implements Broadcaster {
    /**
     * 配信先の URI.
     */
    private final String mBroadcastURI;

    /**
     * 配信を行うレコーダ.
     */
    private final ScreenCastRecorder mRecorder;

    /**
     * RTMP 配信クライアント.
     */
    private RtmpClient mRtmpClient;

    /**
     * イベントを通知するためのリスナー.
     */
    private OnEventListener mOnBroadcasterEventListener;

    public ScreenCastRTMPBroadcaster(ScreenCastRecorder recorder, String broadcastURI) {
        mRecorder = recorder;
        mBroadcastURI = broadcastURI;
    }

    @Override
    public String getMimeType() {
        return null;
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
        return mRtmpClient != null && mRtmpClient.isRunning();
    }

    @Override
    public void start(OnStartCallback callback) {
        HostMediaRecorder.Settings settings = mRecorder.getSettings();

        ScreenCastVideoEncoder videoEncoder = new ScreenCastVideoEncoder(mRecorder);
        CameraVideoQuality videoQuality = (CameraVideoQuality) videoEncoder.getVideoQuality();
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

        mRtmpClient = new RtmpClient(mBroadcastURI);
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
            }

            @Override
            public void onConnected() {
            }

            @Override
            public void onDisconnected() {
                ScreenCastRTMPBroadcaster.this.stop();
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
            if (mRtmpClient.isRunning()) {
                mRtmpClient.restartAudioEncoder();
            }
        }
    }

    @Override
    public boolean isMute() {
        if (mRtmpClient != null) {
            return mRtmpClient.isMute();
        }
        return false;
    }

    @Override
    public void onConfigChange() {

    }
}
