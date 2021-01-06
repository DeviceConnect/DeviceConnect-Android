package org.deviceconnect.android.libmedia.streaming.rtsp.player.decoder.video;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import org.deviceconnect.android.libmedia.BuildConfig;
import org.deviceconnect.android.libmedia.streaming.rtp.RtpDepacketize;
import org.deviceconnect.android.libmedia.streaming.rtp.depacket.H264Depacketize;
import org.deviceconnect.android.libmedia.streaming.rtsp.player.decoder.Frame;
import org.deviceconnect.android.libmedia.streaming.sdp.Attribute;
import org.deviceconnect.android.libmedia.streaming.sdp.MediaDescription;
import org.deviceconnect.android.libmedia.streaming.sdp.attribute.FormatAttribute;
import org.deviceconnect.android.libmedia.streaming.sdp.attribute.RtpMapAttribute;
import org.deviceconnect.android.libmedia.streaming.util.H264Parser;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

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
     * MediaCodec に渡すマイムタイプ.
     */
    private static final String MIME_TYPE_H264 = "video/avc";

    private ByteBuffer mCsd0;
    private ByteBuffer mCsd1;
    private byte[] mSPS;
    private byte[] mPPS;

    private int mWidth = 960;
    private int mHeight = 540;

    private Frame mCurrentFrame;

    @Override
    protected void configure(MediaDescription md) {
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

                        try {
                            H264Parser.Sps s = H264Parser.parseSps(mSPS);
                            mWidth = s.getWidth();
                            mHeight = s.getHeight();
                        } catch (Exception e) {
                            // ignore.
                        }

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

        setClockFrequency(clockFrequency);

        if (mSPS != null && mPPS != null) {
            addFrame(new Frame(createSPS_PPS(mSPS, mPPS), 0));
        }
    }

    @Override
    protected RtpDepacketize createDepacketize() {
        RtpDepacketize rtpDepacketize = new H264Depacketize();
        rtpDepacketize.setCallback((data, length, pts) -> {
            int type = data[4] & 0x1F;
            if (type == 0x09) {
                // AU (Access Unit) delimiter が使用されている場合は、
                // 次の AU がくるまでは同じフレームとして処理を行います。
                if (mCurrentFrame != null) {
                    addFrame(mCurrentFrame);
                }
                mCurrentFrame = getFrame();
                mCurrentFrame.setData(data, length, pts);
            } else {
                if (mCurrentFrame == null) {
                    // AU (Access Unit) delimiter が送られてきていないので
                    // フレームをそのまま追加します。
                    Frame frame = getFrame();
                    frame.setData(data, length, pts);
                    addFrame(frame);
                } else {
                    mCurrentFrame.append(data, length);
                }
            }
        });
        return rtpDepacketize;
    }

    @Override
    protected MediaFormat createMediaFormat() {
        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE_H264, mWidth, mHeight);
        if (mCsd0 != null) {
            format.setByteBuffer("csd-0", mCsd0);
        }

        if (mCsd1 != null) {
            format.setByteBuffer("csd-1", mCsd1);
        }
        return format;
    }

    @Override
    protected int getFlags(byte[] data, int dataLength) {
        int type = data[4] & 0x1F;
        if (type == 0x07 || type == 0x08) {
            return MediaCodec.BUFFER_FLAG_CODEC_CONFIG;
        }
        return 0;
    }

    private byte[] createSPS_PPS(byte[] sps, byte[] pps) {
        byte[] config = new byte[4 + sps.length + 4 + pps.length];
        config[0] = 0x00;
        config[1] = 0x00;
        config[2] = 0x00;
        config[3] = 0x01;
        System.arraycopy(sps, 0, config, 4, sps.length);
        config[sps.length + 4] = 0x00;
        config[sps.length + 5] = 0x00;
        config[sps.length + 6] = 0x00;
        config[sps.length + 7] = 0x01;
        System.arraycopy(pps, 0, config, 8 + sps.length, pps.length);
        return config;
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
}
