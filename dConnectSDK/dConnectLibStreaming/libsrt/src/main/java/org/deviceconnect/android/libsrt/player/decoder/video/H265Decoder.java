package org.deviceconnect.android.libsrt.player.decoder.video;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import org.deviceconnect.android.libmedia.streaming.util.H265Parser;
import org.deviceconnect.android.libsrt.BuildConfig;

import java.io.IOException;
import java.nio.ByteBuffer;

public class H265Decoder extends VideoDecoder {
    /**
     * デバッグ用フラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * デバッグ用タグ.
     */
    private static final String TAG = "H265Decoder";

    /**
     * MediaCodec に渡すマイムタイプ.
     */
    private String mMimeType = "video/hevc";

    private byte[] mVPS;
    private byte[] mSPS;
    private byte[] mPPS;

    /**
     * MediaCodec に渡す横幅.
     */
    private int mWidth;

    /**
     * MediaCodec に渡す縦幅.
     */
    private int mHeight;

    @Override
    protected boolean searchConfig(byte[] data, int dataLength) {
        for (int i = 0; i + 5 < dataLength; i++) {
            if (data[i] == 0x00 && data[i + 1] == 0x00 && data[i + 2] == 0x00 && data[i + 3] == 0x01) {
                int end = i + 4;
                for (; end + 4 < dataLength; end++) {
                    if (data[end] == 0x00 && data[end + 1] == 0x00 && data[end + 2] == 0x00 && data[end + 3] == 0x01) {
                        break;
                    }
                }

                int type = (data[i + 4] >> 1) & 0x3f;
                switch (type) {
                    case 32: // VPS
                        mVPS = createCSD(data, i, end);
                        break;
                    case 33: // SPS
                        mSPS = createCSD(data, i, end);
                        break;
                    case 34: // PPS
                        mPPS = createCSD(data, i, end);
                        break;
                }
            }
        }
        return mVPS != null && mSPS != null && mPPS != null;
    }

    @Override
    protected MediaCodec createMediaCodec() throws IOException {
        parseSps();

        MediaFormat format = MediaFormat.createVideoFormat(mMimeType, mWidth, mHeight);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            format.setInteger(MediaFormat.KEY_COLOR_RANGE, MediaFormat.COLOR_RANGE_FULL);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            format.setInteger(MediaFormat.KEY_OPERATING_RATE, Short.MAX_VALUE);
        }

        ByteBuffer csd0 = ByteBuffer.allocateDirect(mVPS.length + mSPS.length + mPPS.length);
        csd0.put(mVPS);
        csd0.position(mVPS.length);
        csd0.put(mSPS);
        csd0.position(mVPS.length + mSPS.length);
        csd0.put(mPPS);

        format.setByteBuffer("csd-0", csd0);

        if (DEBUG) {
            Log.d(TAG, "H265Decoder::createMediaCodec: " + format);
        }

        MediaCodec mediaCodec = MediaCodec.createDecoderByType(mMimeType);
        mediaCodec.configure(format, getSurface(), null, 0);
        mediaCodec.setVideoScalingMode(MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT);
        mediaCodec.start();
        return mediaCodec;
    }

    @Override
    protected int getFlags(byte[] data, int dataLength) {
        int type = (data[4] >> 1) & 0x3F;
        if (type == 32 || type == 33 || type == 34) {
            return MediaCodec.BUFFER_FLAG_CODEC_CONFIG;
        }
        return 0;
    }

    private byte[] createCSD(byte[] data, int start, int end) {
        int length = end - start;
        byte[] csd = new byte[length];
        System.arraycopy(data, start, csd, 0, length);
        if (DEBUG) {
            StringBuilder builder = new StringBuilder();
            builder.append("csd: ");
            for (int i = start; i < end; i++) {
                if (i > start) {
                    builder.append(",");
                }
                builder.append(String.format("%02X", data[i]));
            }
            Log.d(TAG, builder.toString());
        }
        return csd;
    }

    /**
     * SPS のデータから映像の解像度を取得します.
     */
    private void parseSps() {
        try {
            mWidth = 320;
            mHeight = 480;
            H265Parser.Sps sps = H265Parser.parseSps(mSPS, 4);
            mWidth = sps.getWidth();
            mHeight = sps.getHeight();
        } catch (Exception e) {
            // ignore.
        }
    }
}
