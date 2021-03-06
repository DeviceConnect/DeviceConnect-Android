package org.deviceconnect.android.libmedia.streaming.rtsp.player.decoder.video;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import org.deviceconnect.android.libmedia.BuildConfig;
import org.deviceconnect.android.libmedia.streaming.rtp.RtpDepacketize;
import org.deviceconnect.android.libmedia.streaming.rtp.depacket.H265Depacketize;
import org.deviceconnect.android.libmedia.streaming.rtsp.player.decoder.Frame;
import org.deviceconnect.android.libmedia.streaming.sdp.Attribute;
import org.deviceconnect.android.libmedia.streaming.sdp.MediaDescription;
import org.deviceconnect.android.libmedia.streaming.sdp.attribute.FormatAttribute;
import org.deviceconnect.android.libmedia.streaming.sdp.attribute.RtpMapAttribute;
import org.deviceconnect.android.libmedia.streaming.util.H265Parser;
import org.deviceconnect.android.libmedia.streaming.util.HexUtil;

import java.nio.ByteBuffer;

public class H265Decoder extends VideoDecoder {
    /**
     * デバッグフラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * デバッグ用タグ.
     */
    private static final String TAG = "H265-DECODE";

    /**
     * MediaCodec に渡すマイムタイプ.
     */
    private static final String MIME_TYPE_H265 = "video/hevc";

    private byte[] mVPS;
    private byte[] mSPS;
    private byte[] mPPS;

    private int mWidth = 960;
    private int mHeight = 540;

    @Override
    protected void configure(MediaDescription md) {
        int clockFrequency = 90000;

        for (Attribute attribute : md.getAttributes()) {
            if (attribute instanceof RtpMapAttribute) {
                RtpMapAttribute rma = (RtpMapAttribute) attribute;
                clockFrequency = rma.getRate();
            } else if (attribute instanceof FormatAttribute) {
                FormatAttribute fa = (FormatAttribute) attribute;
                String vps = fa.getParameters().get("sprop-vps");
                if (vps != null) {
                    mVPS = Base64.decode(vps, Base64.NO_WRAP);
                }
                String sps = fa.getParameters().get("sprop-sps");
                if (sps != null) {
                    mSPS = Base64.decode(sps, Base64.NO_WRAP);

                    try {
                        H265Parser.Sps s = H265Parser.parseSps(mPPS);
                        mWidth = s.getWidth();
                        mHeight = s.getHeight();
                    } catch (Exception e) {
                        // ignore.
                    }
                }
                String pps = fa.getParameters().get("sprop-pps");
                if (pps != null) {
                    mPPS = Base64.decode(pps, Base64.NO_WRAP);
                }

                if (DEBUG) {
                    if (mVPS != null) {
                        Log.d(TAG, "### VPS: " + HexUtil.hexToString(mVPS));
                    }
                    if (mSPS != null) {
                        Log.d(TAG, "### SPS: " + HexUtil.hexToString(mSPS));
                    }
                    if (mPPS != null) {
                        Log.d(TAG, "### PPS: " + HexUtil.hexToString(mPPS));
                    }
                }
            }
        }

        setClockFrequency(clockFrequency);

        if (mVPS != null && mSPS != null && mPPS != null) {
            addFrame(new Frame(createSPS_PPS(mVPS, mSPS, mPPS), 0));
        }
    }

    @Override
    protected RtpDepacketize createDepacketize() {
        RtpDepacketize rtpDepacketize = new H265Depacketize();
        rtpDepacketize.setCallback((data, length, pts) -> {
            Frame frame = getFrame();
            frame.setData(data, length, pts);
            addFrame(frame);
        });
        return rtpDepacketize;
    }

    @Override
    protected MediaFormat createMediaFormat() {
        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE_H265, mWidth, mHeight);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            format.setInteger(MediaFormat.KEY_COLOR_RANGE, MediaFormat.COLOR_RANGE_FULL);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            format.setInteger(MediaFormat.KEY_OPERATING_RATE, Short.MAX_VALUE);
        }

        ByteBuffer csd0 = ByteBuffer.allocateDirect(mVPS.length + mSPS.length + mPPS.length +  12);
        csd0.put(createSPS_PPS(mVPS, mSPS, mPPS));
        csd0.flip();

        format.setByteBuffer("csd-0", csd0);

        return format;
    }

    @Override
    protected int getFlags(byte[] data, int dataLength) {
        int type = (data[4] >> 1) & 0x3F;
        if (type == 32 || type == 33 || type == 34) {
            return MediaCodec.BUFFER_FLAG_CODEC_CONFIG;
        }
        return 0;
    }

    private byte[] createSPS_PPS(byte[] vps, byte[] sps, byte[] pps) {
        byte[] config = new byte[4 + vps.length + 4 + sps.length + 4 + pps.length];
        config[0] = 0x00;
        config[1] = 0x00;
        config[2] = 0x00;
        config[3] = 0x01;
        System.arraycopy(vps, 0, config, 4, vps.length);
        config[vps.length + 4] = 0x00;
        config[vps.length + 5] = 0x00;
        config[vps.length + 6] = 0x00;
        config[vps.length + 7] = 0x01;
        System.arraycopy(sps, 0, config, 8 + vps.length, sps.length);
        config[vps.length + sps.length + 8] = 0x00;
        config[vps.length + sps.length + 9] = 0x00;
        config[vps.length + sps.length + 10] = 0x00;
        config[vps.length + sps.length + 11] = 0x01;
        System.arraycopy(pps, 0, config, 12 + vps.length + sps.length, pps.length);
        return config;
    }
}
