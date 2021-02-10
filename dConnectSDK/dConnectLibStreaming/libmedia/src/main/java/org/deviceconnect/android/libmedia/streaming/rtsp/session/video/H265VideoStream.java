package org.deviceconnect.android.libmedia.streaming.rtsp.session.video;

import android.graphics.Canvas;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Base64;
import android.util.Log;

import org.deviceconnect.android.libmedia.BuildConfig;
import org.deviceconnect.android.libmedia.streaming.MediaEncoder;
import org.deviceconnect.android.libmedia.streaming.MediaEncoderException;
import org.deviceconnect.android.libmedia.streaming.rtp.RtpPacketize;
import org.deviceconnect.android.libmedia.streaming.rtp.packet.H265Packetize;
import org.deviceconnect.android.libmedia.streaming.sdp.MediaDescription;
import org.deviceconnect.android.libmedia.streaming.sdp.attribute.ControlAttribute;
import org.deviceconnect.android.libmedia.streaming.sdp.attribute.FormatAttribute;
import org.deviceconnect.android.libmedia.streaming.sdp.attribute.RtpMapAttribute;
import org.deviceconnect.android.libmedia.streaming.video.CanvasVideoEncoder;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class H265VideoStream extends VideoStream {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "RTSP-VIDEO";

    /**
     * ペイロードタイプ.
     */
    private static final int PAYLOAD_TYPE = 98;

    /**
     * クロック周波数.
     */
    private static final int CLOCK_FREQUENCY = 90000;

    /**
     * VPS を Base64 でエンコードした文字列.
     */
    private String mVPSString;

    /**
     * PPS を Base64 でエンコードした文字列.
     */
    private String mPPSString;

    /**
     * SPS を Base64 でエンコードした文字列.
     */
    private String mSPSString;

    @Override
    public RtpPacketize createRtpPacketize() {
        H265Packetize packetize = new H265Packetize();
        packetize.setPayloadType(PAYLOAD_TYPE);
        packetize.setClockFrequency(CLOCK_FREQUENCY);
        return packetize;
    }

    @Override
    public void configure() {
        if (mVPSString == null || mPPSString == null || mSPSString == null) {
            final CountDownLatch latch = new CountDownLatch(1);
            final CountDownLatch stop = new CountDownLatch(1);
            final AtomicBoolean result = new AtomicBoolean();

            VideoEncoder videoEncoder = getVideoEncoder();

            VideoEncoder encoder = new TempVideoEncoder();
            encoder.getVideoQuality().set(videoEncoder.getVideoQuality());
            encoder.setCallback(new MediaEncoder.Callback() {
                @Override
                public void onStarted() {
                }

                @Override
                public void onStopped() {
                    stop.countDown();
                }

                @Override
                public void onFormatChanged(MediaFormat newFormat) {
                    ByteBuffer csd0 = newFormat.getByteBuffer("csd-0");

                    if (csd0 == null) {
                        result.set(false);
                        latch.countDown();
                        return;
                    }

                    searchParameterSet(csd0);

                    if (DEBUG) {
                        Log.d(TAG, "### VPS: " + mVPSString);
                        Log.d(TAG, "### SPS: " + mSPSString);
                        Log.d(TAG, "### PPS: " + mPPSString);
                    }

                    result.set(true);
                    latch.countDown();
                }

                @Override
                public void onWriteData(ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo) {
                }

                @Override
                public void onError(MediaEncoderException e) {
                    if (DEBUG) {
                        Log.e(TAG, "Failed to guess a video format.", e);
                    }
                    latch.countDown();
                    stop.countDown();
                }
            });
            encoder.start();

            try {
                latch.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // ignore.
            }

            encoder.stop();

            try {
                stop.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // ignore.
            }

            if (!result.get()) {
                throw new MediaEncoderException("Failed to start a MediaCodec.");
            }
        }
    }

    @Override
    public MediaDescription getMediaDescription() {
        FormatAttribute fmt = new FormatAttribute(PAYLOAD_TYPE);
        fmt.addParameter("sprop-vps", mVPSString);
        fmt.addParameter("sprop-sps", mSPSString);
        fmt.addParameter("sprop-pps", mPPSString);

        MediaDescription mediaDescription = new MediaDescription("video", getDestinationPort(), "RTP/AVP", PAYLOAD_TYPE);
        mediaDescription.addAttribute(new RtpMapAttribute(PAYLOAD_TYPE, "H265", CLOCK_FREQUENCY));
        mediaDescription.addAttribute(fmt);
        mediaDescription.addAttribute(new ControlAttribute("trackID=" + getTrackId()));
        return mediaDescription;
    }

    private static class TempVideoEncoder extends CanvasVideoEncoder {
        TempVideoEncoder() {
            super("video/hevc");
        }
        @Override
        public void draw(Canvas canvas, int width, int height) {
        }
    }

    private void searchParameterSet(ByteBuffer buffer) {
        byte[] b = new byte[buffer.limit()];
        buffer.get(b);
        searchParameterSet(b, b.length);
    }

    /**
     * VPS、SPS、PPS を取り出して、それぞれのパラメータに設定します.
     *
     * @param data MediaFormat から渡されたフォーマットデータ
     * @param dataLength データサイズ
     */
    private void searchParameterSet(byte[] data, int dataLength) {
        for (int i = 0; i + 5 < dataLength; i++) {
            if (data[i] == 0x00 && data[i + 1] == 0x00 && data[i + 2] == 0x00 && data[i + 3] == 0x01) {
                int end = i + 4;
                for (; end + 4 < dataLength; end++) {
                    if (data[end] == 0x00 && data[end + 1] == 0x00 && data[end + 2] == 0x00 && data[end + 3] == 0x01) {
                        break;
                    }
                }

                int type = (data[i + 4] >> 1) & 0x3f;
                int length = end - (i + 4);
                switch (type) {
                    case 32: // VPS
                        mVPSString = Base64.encodeToString(data, i + 4, length, Base64.NO_WRAP);
                        break;
                    case 33: // SPS
                        mSPSString = Base64.encodeToString(data, i + 4, length, Base64.NO_WRAP);
                        break;
                    case 34: // PPS
                        mPPSString = Base64.encodeToString(data, i + 4, length, Base64.NO_WRAP);
                        break;
                }
            }
        }
    }
}
