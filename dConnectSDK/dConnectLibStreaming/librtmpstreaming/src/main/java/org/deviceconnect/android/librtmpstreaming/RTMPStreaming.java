package org.deviceconnect.android.librtmpstreaming;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import net.ossrs.rtmp.ConnectCheckerRtmp;
import net.ossrs.rtmp.SrsFlvMuxer;

import java.io.IOException;
import java.nio.ByteBuffer;

public class RTMPStreaming {

    private static final String TAG = "RTMPStreaming";

    private SrsFlvMuxer mSrsFlvMuxer;

    private MediaFormat mVideoMediaFormat;
    private MediaFormat mAudioMediaFormat;
    private String mRtmpUri;
    private RTMPStreamingCallback mCallback;

    private VideoEncoderCore mVideoEncoder;
    private AudioEncoder mAudioEncoder;

    private Boolean isConnected = false;
    private Surface inputSurface;

    public interface RTMPStreamingCallback {
        void onConnect();
        void onFailure(String message);
    }

    /**
     * 映像用MediaFormatを設定.設定しない場合は映像配信しない.
     * @param videoMediaFormat 映像用MediaFormat
     */
    public void setVideoMediaFormat(MediaFormat videoMediaFormat) {
        if (videoMediaFormat != null) {
            mVideoEncoder = new VideoEncoderCore();
            mVideoMediaFormat = videoMediaFormat;
            mVideoEncoder.setMediaFormat(videoMediaFormat);
        }
        else {
            mVideoEncoder = null;
            mVideoMediaFormat = null;
        }
    }

    /**
     * 音声用MediaFormatを設定.設定しない場合は映像配信しない.
     * @param audioMediaFormat 音声用MediaFormat
     */
    public void setAudioMediaFormat(MediaFormat audioMediaFormat) {
        if (audioMediaFormat != null) {
            mAudioEncoder = new AudioEncoder();
            mAudioMediaFormat = audioMediaFormat;
            mAudioEncoder.setMediaFormat(audioMediaFormat);
        }
        else {
            mAudioEncoder = null;
            mAudioMediaFormat = null;
        }
    }

    /**
     * 配信先のRTMP URIを設定する.
     * @param rtmpUri RTMP URI
     */
    public void setRtmpUri(String rtmpUri) {
        mRtmpUri = rtmpUri;
    }

    /**
     * RTMP配信状態を返すコールバックを登録する.
     * @param callback RTMP配信状態を返すコールバック
     */
    public void setCallback(RTMPStreamingCallback callback) {
        mCallback = callback;
    }

    /**
     * RTMP配信処理を開始する.
     * @throws IllegalStateException 事前に設定する値が設定されていない.
     * @throws IOException MediaCodecが作成できなかった場合等.
     */
    public void start() throws IllegalStateException, IOException, Exception {

        final long presentTimeUs = System.nanoTime() / 1000;

        // VideoEncoder
        if (mVideoEncoder != null) {
            mVideoEncoder.setPresentTimeUs(presentTimeUs);
            mVideoEncoder.setCallback(new VideoEncoderCore.VideoEncodeCallback() {
                @Override
                public void onReceiveSampleData(ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo) {
                    synchronized (isConnected) {
                        if (!isConnected) {
                            return;
                        }
                    }
                    mSrsFlvMuxer.sendVideo(encodedData, bufferInfo);
                }

                @Override
                public void onOutputFormatChanged(MediaFormat newFormat) {
                    synchronized (isConnected) {
                        if (!isConnected) {
                            return;
                        }
                    }
                    mSrsFlvMuxer.setSpsPPs(newFormat.getByteBuffer("csd-0"), newFormat.getByteBuffer("csd-1"));
                }

                @Override
                public void onFailure(String errorMessage) {
                    mCallback.onFailure(errorMessage);
                    stop();
                }

                @Override
                public void onStopped() {
                    stopVideoEncoderProc();
                }
            });
            mVideoEncoder.start();

            inputSurface = mVideoEncoder.getInputSurface();
        }

        // AndioEncoder
        if (mAudioEncoder != null) {
            mAudioEncoder.setPresentTimeUs(presentTimeUs);
            mAudioEncoder.setCallback(new AudioEncoder.Callback() {
                @Override
                public void onAudioFormat(MediaFormat mediaFormat) {
                    synchronized (isConnected) {
                        if (!isConnected) {
                            return;
                        }
                    }
//                    mSrsFlvMuxer.setSpsPPs(mediaFormat.getByteBuffer("csd-0"), mediaFormat.getByteBuffer("csd-1"));
                }

                @Override
                public void onReceiveAacData(ByteBuffer aacBytes, MediaCodec.BufferInfo bufferInfo) {
                    synchronized (isConnected) {
                        if (!isConnected) {
                            return;
                        }
                    }
                    mSrsFlvMuxer.sendAudio(aacBytes, bufferInfo);
                }

                @Override
                public void onStopped() {
                    stopAudioEncoderProc();
                }
            });
            mAudioEncoder.start();
        }

        mSrsFlvMuxer = new SrsFlvMuxer(new ConnectCheckerRtmp() {
            /*--- ConnectCheckerRtmp ---*/

            @Override
            public void onConnectionSuccessRtmp() {
                synchronized (isConnected) {
                    isConnected = true;
                    mCallback.onConnect();
                }
            }

            @Override
            public void onConnectionFailedRtmp(String reason) {
                synchronized (isConnected) {
                    isConnected = false;
                }
                mCallback.onFailure(reason);
            }

            @Override
            public void onDisconnectRtmp() {
                synchronized (isConnected) {
                    isConnected = false;
                }
            }

            @Override
            public void onAuthErrorRtmp() {
                synchronized (isConnected) {
                    isConnected = false;
                }
            }

            @Override
            public void onAuthSuccessRtmp() {

            }
        });

        if (mVideoMediaFormat != null) {
            int videoWidth = mVideoMediaFormat.getInteger(MediaFormat.KEY_WIDTH);
            int videoHeight = mVideoMediaFormat.getInteger(MediaFormat.KEY_HEIGHT);
            mSrsFlvMuxer.setVideoResolution(videoWidth, videoHeight);
        }
        mSrsFlvMuxer.start(mRtmpUri);
    }

    public void stop() {
        if (mVideoEncoder != null) {
            mVideoEncoder.stop();
            // stop処理完了後にCallbackによりstopVideoEncoderProc()が実行される.
        }
        if (mAudioEncoder != null) {
            mAudioEncoder.stop();
            // stop処理完了後にCallbackによりstopAudioEncoderProc()が実行される.
        }
        mSrsFlvMuxer.stop();
    }

    /**
     * stop()後のVideoEncoderの処理.
     */
    private void stopVideoEncoderProc() {
        setVideoMediaFormat(null);
    }

    /**
     * stop()後のAudioEncoderの処理.
     */
    private void stopAudioEncoderProc() {
        setAudioMediaFormat(null);
    }

    /**
     * MediaCodecが作成した永続的なSurfaceを返す
     * @return MediaCodecが作成した永続的なSurface
     */
    public Surface getInputSurface() {
        return inputSurface;
    }

    /**
     * PCM音声データをAudioEncoderへ書き込む.
     * @param pcmBytes PCM音声データ
     * @param size PCM音声データサイズ
     */
    public void sendInputPCMBytes(byte[] pcmBytes, int size) throws Exception {
        if (mAudioEncoder == null) {
            throw new Exception("mAudioEncoder is null");
        }
        mAudioEncoder.inputPCMData(pcmBytes, size);
    }


    public static class Builder {

        private MediaFormat mVideoMediaFormat;

        private MediaFormat mAudioMediaFormat;

        private String mRtmpUri;

        private RTMPStreamingCallback mCallback;


        public RTMPStreaming build() {
            RTMPStreaming rtmpStreaming = new RTMPStreaming();
            rtmpStreaming.setVideoMediaFormat(mVideoMediaFormat);
            rtmpStreaming.setAudioMediaFormat(mAudioMediaFormat);
            rtmpStreaming.setRtmpUri(mRtmpUri);
            rtmpStreaming.setCallback(mCallback);
            return rtmpStreaming;
        }

        public Builder setVideoMediaFormat(MediaFormat videoMediaFormat) {
            mVideoMediaFormat = videoMediaFormat;
            return this;
        }

        public Builder setAudioMediaFormat(MediaFormat audioMediaFormat) {
            mAudioMediaFormat = audioMediaFormat;
            return this;
        }

        public Builder setRtmpUri(String rtmpUri) {
            mRtmpUri = rtmpUri;
            return this;
        }

        public Builder setCallback(RTMPStreamingCallback callback) {
            mCallback = callback;
            return this;
        }
    }
}
