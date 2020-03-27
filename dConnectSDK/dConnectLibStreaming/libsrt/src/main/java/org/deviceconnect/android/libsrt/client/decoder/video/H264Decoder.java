package org.deviceconnect.android.libsrt.client.decoder.video;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import org.deviceconnect.android.libmedia.streaming.util.H264Parser;
import org.deviceconnect.android.libsrt.BuildConfig;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class H264Decoder extends VideoDecoder {
    /**
     * デバッグ用フラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * デバッグ用タグ.
     */
    private static final String TAG = "H264Decoder";

    /**
     * MediaCodec に渡すマイムタイプ.
     */
    private String mMimeType = "video/avc";

    /**
     * MediaCodec に渡す横幅.
     */
    private int mWidth;

    /**
     * MediaCodec に渡す縦幅.
     */
    private int mHeight;

    /**
     * SPSの情報を格納するバッファ.
     */
    private ByteBuffer mCsd0;

    /**
     * PPSの情報を格納するバッファ.
     */
    private ByteBuffer mCsd1;

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
                int type = data[i + 4] & 0x1F;
                switch (type) {
                    case 7: // SPS
                        mCsd0 = createCSD(data, i + 4, end);
                        break;
                    case 8: // PPS
                        mCsd1 = createCSD(data, i + 4, end);
                        break;
                }
            }
        }

        return mCsd0 != null && mCsd1 != null;
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

        format.setByteBuffer("csd-0", mCsd0);
        format.setByteBuffer("csd-1", mCsd1);

        if (DEBUG) {
            Log.d(TAG, "H264Decoder::createMediaCodec: " + format);
        }

        MediaCodec mediaCodec = MediaCodec.createDecoderByType(mMimeType);
        mediaCodec.configure(format, getSurface(), null, 0);
        mediaCodec.setVideoScalingMode(MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT);
        mediaCodec.start();
        return mediaCodec;
    }

    @Override
    protected int getFlags(byte[] data, int dataLength) {
        int type = data[4] & 0x1F;
        if (type == 0x07 || type == 0x08) {
            return MediaCodec.BUFFER_FLAG_CODEC_CONFIG;
        }
        return 0;
    }

    /**
     * csd-0 または csd-1 のデータを作成します.
     *
     * @param data csd が含まれているデータ
     * @param start csd の開始位置
     * @param end csd の終了位置
     * @return csd のデータ
     */
    private ByteBuffer createCSD(byte[] data, int start, int end) {
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

        ByteBuffer csd = ByteBuffer.allocateDirect(end - start).order(ByteOrder.nativeOrder());
        csd.put(data, start, end - start);
        csd.flip();
        return csd;
    }

    /**
     * SPS のデータから映像の解像度を取得します.
     */
    private void parseSps() {
        byte[] spsData = new byte[mCsd0.limit()];
        mCsd0.get(spsData);
        mCsd0.flip();

        H264Parser.Sps sps = H264Parser.parseSps(spsData, 0);
        mWidth = sps.getWidth();
        mHeight = sps.getHeight();
    }
}
