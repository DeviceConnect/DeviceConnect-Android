package org.deviceconnect.android.libmedia.streaming.rtsp.player.decoder.audio;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import org.deviceconnect.android.libmedia.BuildConfig;
import org.deviceconnect.android.libmedia.streaming.rtp.RtpDepacketize;
import org.deviceconnect.android.libmedia.streaming.rtp.depacket.AACLATMDepacketize;
import org.deviceconnect.android.libmedia.streaming.rtsp.player.decoder.Decoder;
import org.deviceconnect.android.libmedia.streaming.rtsp.player.decoder.Frame;
import org.deviceconnect.android.libmedia.streaming.sdp.Attribute;
import org.deviceconnect.android.libmedia.streaming.sdp.MediaDescription;
import org.deviceconnect.android.libmedia.streaming.sdp.attribute.FormatAttribute;
import org.deviceconnect.android.libmedia.streaming.sdp.attribute.RtpMapAttribute;
import org.deviceconnect.android.libmedia.streaming.util.QueueThread;

import java.io.IOException;
import java.nio.ByteBuffer;

public class AACLATMDecoder extends AudioDecoder {
    /**
     * デバッグフラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * デバッグ用タグ.
     */
    private static final String TAG = "AACLATM-DECODE";

    /**
     * RTP パケットを解析するクラス.
     */
    private RtpDepacketize mDepacketize;

    /**
     * デコードを行うMediaCodec.
     */
    private MediaCodec mMediaCodec;

    /**
     * 音声再生処理を行うスレッド.
     */
    private WorkThread mWorkThread;

    @Override
    public void onInit(MediaDescription md) {
        // https://tools.ietf.org/html/rfc5691

        for (Attribute attribute : md.getAttributes()) {
            if (attribute instanceof RtpMapAttribute) {
                RtpMapAttribute rtpMapAttribute = (RtpMapAttribute) attribute;
                setSamplingRate(rtpMapAttribute.getRate());
                try {
                    String p = rtpMapAttribute.getParameters();
                    if (p != null) {
                        setChannelCount(Integer.parseInt(p.trim()));
                    }
                } catch (NumberFormatException e) {
                    // ignore.
                }
            } else if (attribute instanceof FormatAttribute) {
                FormatAttribute formatAttribute = (FormatAttribute) attribute;
                String streamType = formatAttribute.getParameters().get("streamType");
                String profileLevelId = formatAttribute.getParameters().get("profile-level-id");
                String mode = formatAttribute.getParameters().get("mode");

                // TODO
            }
        }

        try {
            createAudioTrack();
        } catch (Exception e) {
            postError(e);
            return;
        }

        try {
            createMediaCodec();
        } catch (IOException e) {
            postError(e);
            return;
        }

        mDepacketize = new AACLATMDepacketize();
        mDepacketize.setClockFrequency(getSamplingRate());
        mDepacketize.setCallback((data, pts) -> {
            if (mWorkThread != null) {
                mWorkThread.add(new Frame(data, pts));
            }
        });
    }

    @Override
    public void onRtpReceived(MediaDescription md, byte[] data, int dataLength) {
        if (mDepacketize != null) {
            mDepacketize.write(data, dataLength);
        }
    }

    @Override
    public void onRtcpReceived(MediaDescription md, byte[] data, int dataLength) {
    }

    @Override
    public void onRelease() {
        if (mMediaCodec != null) {
            try {
                mMediaCodec.stop();
            } catch (Exception e) {
                // ignore.
            }

            try {
                mMediaCodec.release();
            } catch (Exception e) {
                // ignore.
            }

            mMediaCodec = null;
        }

        if (mWorkThread != null) {
            mWorkThread.close();
            mWorkThread = null;
        }

        releaseAudioTrack();

        if (mDepacketize != null) {
            mDepacketize = null;
        }
    }

    private void createMediaCodec() throws IOException {
        if (mMediaCodec != null) {
            if (DEBUG) {
                Log.w(TAG, "MediaCodec is already running.");
            }
            return;
        }

        if (mWorkThread != null) {
            if (DEBUG) {
                Log.w(TAG, "WorkThread is already running.");
            }
            return;
        }

        MediaFormat format = MediaFormat.createAudioFormat("audio/mp4a-latm",
                getSamplingRate(), getChannelCount());

        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);

        mMediaCodec = MediaCodec.createDecoderByType("audio/mp4a-latm");
        mMediaCodec.configure(format, null, null, 0);
        mMediaCodec.start();

        mWorkThread = new WorkThread();
        mWorkThread.setName("AACLATM-DECODE");
        mWorkThread.start();
    }

    /**
     * 送られてきたデータをMediaCodecに渡してデコードを行うスレッド.
     */
    private class WorkThread extends QueueThread<Frame> {
        /**
         * データの終了フラグ.
         */
        private boolean mStopFlag = false;

        /**
         * スレッドのクローズ処理を行います.
         */
        void close() {
            mStopFlag = true;

            interrupt();

            try {
                join(500);
            } catch (InterruptedException e) {
                // ignore.
            }
        }

        @Override
        public void run() {
            try {
                MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

                while (!mStopFlag) {
                    Frame frame = get();

                    int inIndex = mMediaCodec.dequeueInputBuffer(10000);
                    if (inIndex >= 0 && !mStopFlag) {
                        ByteBuffer buffer = mMediaCodec.getInputBuffer(inIndex);
                        if (buffer == null) {
                            continue;
                        }

                        buffer.clear();
                        buffer.put(frame.getData(), 0, frame.getLength());
                        buffer.flip();

                        mMediaCodec.queueInputBuffer(inIndex, 0, frame.getLength(), frame.getTimestamp(), 0);
                    }

                    int outIndex = mMediaCodec.dequeueOutputBuffer(info, 10000);
                    if (outIndex > 0 && !mStopFlag) {
                        if (info.size > 0) {
                            writeAudioData(mMediaCodec.getOutputBuffer(outIndex), info.offset, info.size, info.presentationTimeUs);
                        }
                        mMediaCodec.releaseOutputBuffer(outIndex, false);
                    } else {
                        switch (outIndex) {
                            case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                                if (DEBUG) {
                                    Log.d(TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
                                }
                                break;

                            case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                                if (DEBUG) {
                                    Log.d(TAG, "INFO_OUTPUT_FORMAT_CHANGED");
                                    Log.d(TAG, "New format " + mMediaCodec.getOutputFormat());
                                }
                                break;

                            case MediaCodec.INFO_TRY_AGAIN_LATER:
                                Thread.sleep(1);
                                break;

                            default:
                                break;
                        }
                    }

                    // All decoded frames have been rendered, we can stop playing now
                    if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        if (DEBUG) {
                            Log.d(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
                        }
                        break;
                    }
                }
            } catch (InterruptedException e) {
                // ignore.
            } catch (Exception e) {
                if (DEBUG) {
                    Log.w(TAG, "AAC encode occurred an exception.");
                }
            }
        }
    }
}
