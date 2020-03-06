package org.deviceconnect.android.libmedia.streaming.rtsp.player.decoder.video;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Base64;
import android.util.Log;
import android.view.Surface;

import org.deviceconnect.android.libmedia.BuildConfig;
import org.deviceconnect.android.libmedia.streaming.rtp.RtpDepacketize;
import org.deviceconnect.android.libmedia.streaming.rtp.depacket.H264Depacketize;
import org.deviceconnect.android.libmedia.streaming.rtsp.player.decoder.Frame;
import org.deviceconnect.android.libmedia.streaming.sdp.Attribute;
import org.deviceconnect.android.libmedia.streaming.sdp.MediaDescription;
import org.deviceconnect.android.libmedia.streaming.sdp.attribute.FormatAttribute;
import org.deviceconnect.android.libmedia.streaming.sdp.attribute.RtpMapAttribute;
import org.deviceconnect.android.libmedia.streaming.util.H264Parser;
import org.deviceconnect.android.libmedia.streaming.util.QueueThread;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * H264をデコードしてSurfaceに描画するクラス.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class H264Decoder extends VideoDecoder {
    /**
     * デバッグフラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * デバッグ用タグ.
     */
    private static final String TAG = "H264-DECODE";

    /**
     * RTP をデコードするためのクラス.
     */
    private RtpDepacketize mDepacketize;

    /**
     * デコードを行うMediaCodec.
     */
    private MediaCodec mMediaCodec;

    /**
     * SPSの情報を格納するバッファ.
     */
    private ByteBuffer mCsd0;

    /**
     * PPSの情報を格納するバッファ.
     */
    private ByteBuffer mCsd1;

    /**
     * デコード処理を行うスレッド.
     */
    private WorkThread mWorkThread;

    /**
     * 描画先のSurface.
     */
    private Surface mSurface;

    /**
     * SPS (Sequence Parameter Set)情報.
     * <p>
     * ストリームのデコードに必要な、基本的な情報。
     * <ul>
     * <li>profile_idc（プロファイル）</li>
     * <li>level_idc（レベル）</li>
     * <li>num_ref_frames（参照フレーム数）</li>
     * <li>pic_width_in_mbs_minus1（幅）</li>
     * <li>pic_height_in_map_units_minus1（高さ）</li>
     * <li>frame_mbs_only_flag（インターレース）</li>
     * <li>mb_adaptive_frame_field_flag（MBAFF）</li>
     * <li>direct_8x8_inference_flag</li>
     * <li>frame_cropping_flag</li>
     * <li>VUI（Video Usability Information)</li>
     * </ul>
     * </p>
     */
    private byte[] mSPS;

    /**
     * PPS (Picture Parameter Set)情報.
     * <p>
     * 個別のピクチャ（フレーム）をデコードする上で必要な情報。
     * <ul>
     * <li>entropy_coding_mode_flag（CAVLC/CABAC）</li>
     * <li>weighted_pred_flag（いわゆるweightp）</li>
     * <li>weighted_bipred_flag（いわゆるweightb）</li>
     * <li>pic_init_qp_minus26 （QP）</li>
     * <li>chroma_qp_index_offset</li>
     * <li>deblocking_filter_control_present_flag</li>
     * <li>constrained_intra_pred_flag</li>
     * <li>スライスとピクチャの対応情報</li>
     * </ul>
     * </p>
     */
    private byte[] mPPS;

    @Override
    public void onInit(MediaDescription md) {

        // https://tools.ietf.org/html/rfc6184

        int clockFrequency = 90000;

        for (Attribute attribute : md.getAttributes()) {
            if (attribute instanceof RtpMapAttribute) {
                RtpMapAttribute rma = (RtpMapAttribute) attribute;
                clockFrequency = rma.getRate();
            } else if (attribute instanceof FormatAttribute) {
                FormatAttribute fa = (FormatAttribute) attribute;
                String a = fa.getParameters().get("sprop-parameter-sets");
                if (a != null) {
                    String[] base = a.split(",");
                    if (base.length == 2) {
                        mSPS = Base64.decode(base[0], Base64.NO_WRAP);
                        mPPS = Base64.decode(base[1], Base64.NO_WRAP);
                        setSPS_PPS(mSPS, mPPS);

                        if (DEBUG) {
                            StringBuilder sps = new StringBuilder();
                            StringBuilder pps = new StringBuilder();
                            for (byte b : mSPS) {
                                sps.append(String.format("%02X", b));
                            }
                            for (byte b : mPPS) {
                                pps.append(String.format("%02X", b));
                            }
                            Log.e(TAG, "### SPS " + sps);
                            Log.e(TAG, "### PPS " + pps);
                        }
                    }
                }
            }
        }

        try {
            createMediaCodec();
        } catch (IOException e) {
            postError(e);
            return;
        }

        if (mSPS != null && mPPS != null) {
            mWorkThread.add(new Frame(createSPS_PPS(mSPS, mPPS), 0));
        }

        mDepacketize = new H264Depacketize();
        mDepacketize.setClockFrequency(clockFrequency);
        mDepacketize.setCallback((data, pts) -> {
            if (mWorkThread != null) {
                mWorkThread.add(new Frame(data, pts));
            }
        });
    }

    @Override
    public void setSurface(final Surface surface) {
        mSurface = surface;
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

        if (mDepacketize != null) {
            mDepacketize = null;
        }

        mSPS = null;
        mPPS = null;
    }

    private byte[] createSPS_PPS(byte[] sps, byte[] pps) {
        byte[] sps_pps = new byte[4 + sps.length + 4 + pps.length];
        sps_pps[0] = 0x00;
        sps_pps[1] = 0x00;
        sps_pps[2] = 0x00;
        sps_pps[3] = 0x01;
        System.arraycopy(sps, 0, sps_pps, 4, sps.length);
        sps_pps[sps.length + 4] = 0x00;
        sps_pps[sps.length + 5] = 0x00;
        sps_pps[sps.length + 6] = 0x00;
        sps_pps[sps.length + 7] = 0x01;
        System.arraycopy(pps, 0, sps_pps, 8 + sps.length, pps.length);
        return sps_pps;
    }

    private void setSPS_PPS(byte[] sps, byte[] pps) {
        if (sps != null) {
            mCsd0 = ByteBuffer.allocateDirect(sps.length).order(ByteOrder.nativeOrder());
            mCsd0.put(sps, 0, sps.length);
            mCsd0.flip();
        }

        if (pps != null) {
            mCsd1 = ByteBuffer.allocateDirect(pps.length).order(ByteOrder.nativeOrder());
            mCsd1.put(pps, 0, pps.length);
            mCsd1.flip();
        }
    }

    /**
     * MediaCodecを作成します.
     *
     * @throws IOException MediaCodecの作成に失敗した場合に発生
     */
    private void createMediaCodec() throws IOException {
        MediaFormat format = MediaFormat.createVideoFormat("video/avc", 0, 0);
        if (mCsd0 != null) {
            format.setByteBuffer("csd-0", mCsd0);
        }

        if (mCsd1 != null) {
            format.setByteBuffer("csd-1", mCsd1);
        }

        mMediaCodec = MediaCodec.createDecoderByType("video/avc");
        mMediaCodec.configure(format, mSurface, null, 0);
        mMediaCodec.setVideoScalingMode(MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
        mMediaCodec.start();

        mWorkThread = new WorkThread();
        mWorkThread.setName("H264-DECODE");
        mWorkThread.start();
    }

    /**
     * 送られてきたデータをMediaCodecに渡してデコードを行うスレッド.
     */
    private class WorkThread extends QueueThread<Frame> {
        /**
         * データの終了フラグ.
         */
        private boolean isEOS = false;

        /**
         * スレッドのクローズ処理を行います.
         */
        void close() {
            isEOS = true;

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
                long startMs = System.currentTimeMillis();

                while (!isInterrupted()) {
                    Frame frame = get();

                    if (!isEOS) {
                        int inIndex = mMediaCodec.dequeueInputBuffer(10000);
                        if (inIndex >= 0) {
                            ByteBuffer buffer = mMediaCodec.getInputBuffer(inIndex);
                            if (buffer == null) {
                                continue;
                            }

                            buffer.clear();
                            buffer.put(frame.getData(), 0, frame.getLength());
                            buffer.flip();

                            int flags = 0;
                            if (frame.getLength() > 4) {
                                int type = frame.getData()[4] & 0x1F;
                                if (type == 0x07 || type == 0x08) {
                                    flags = MediaCodec.BUFFER_FLAG_CODEC_CONFIG;
                                    if (type == 0x07) {
                                        try {
                                            H264Parser.Sps sps = H264Parser.parseSps(frame.getData(), 4);
                                            postSizeChanged(sps.getWidth(), sps.getHeight());
                                        } catch (Exception e) {
                                            if (DEBUG) {
                                                Log.e(TAG, "", e);
                                            }
                                        }
                                    }
                                }
                            }

                            mMediaCodec.queueInputBuffer(inIndex, 0, frame.getLength(), frame.getTimestamp(), flags);
                        }
                    }

                    int outIndex = mMediaCodec.dequeueOutputBuffer(info, 10000);
                    if (outIndex > 0) {
                        while (info.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
                            try {
                                sleep(10);
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                        mMediaCodec.releaseOutputBuffer(outIndex, true);
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
                    Log.w(TAG, "H264 encode occurred an exception.");
                }
                postError(e);
            }
        }
    }
}
