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
import org.deviceconnect.android.libmedia.streaming.rtp.packet.H264Packetize;
import org.deviceconnect.android.libmedia.streaming.sdp.MediaDescription;
import org.deviceconnect.android.libmedia.streaming.sdp.attribute.ControlAttribute;
import org.deviceconnect.android.libmedia.streaming.sdp.attribute.FormatAttribute;
import org.deviceconnect.android.libmedia.streaming.sdp.attribute.RtpMapAttribute;
import org.deviceconnect.android.libmedia.streaming.video.CanvasVideoEncoder;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class H264VideoStream extends VideoStream {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "RTSP-VIDEO";

    /**
     * ペイロードタイプ.
     */
    private static final int PAYLOAD_TYPE = 96;

    /**
     * クロック周波数.
     */
    private static final int CLOCK_FREQUENCY = 90000;

    /**
     * PPS を Base64 でエンコードした文字列.
     */
    private String mPPSString;

    /**
     * SPS を Base64 でエンコードした文字列.
     */
    private String mSPSString;

    /**
     * プロファイルレベルの 16 進数にした文字列
     */
    private String mProfileLevel;

    /**
     * PPS のデータ.
     */
    private byte[] mPPS;

    /**
     * SPS のデータ.
     */
    private byte[] mSPS;

    public H264VideoStream() {
        super();
    }

    @Override
    public RtpPacketize createRtpPacketize() {
        H264Packetize packetize = new H264Packetize();
        packetize.setPayloadType(PAYLOAD_TYPE);
        packetize.setClockFrequency(CLOCK_FREQUENCY);
        return packetize;
    }

    @Override
    public void configure() {
        if (mPPSString == null || mSPSString == null) {
            final CountDownLatch latch = new CountDownLatch(1);
            final CountDownLatch stop = new CountDownLatch(1);
            final AtomicBoolean result = new AtomicBoolean();

            VideoEncoder videoEncoder = getVideoEncoder();
            VideoQuality quality = videoEncoder.getVideoQuality();

            boolean isSwapped = videoEncoder.isSwappedDimensions();
            int w = isSwapped ? quality.getVideoHeight() : quality.getVideoWidth();
            int h = isSwapped ? quality.getVideoWidth() : quality.getVideoHeight();

            VideoEncoder encoder = new TempVideoEncoder();
            encoder.getVideoQuality().set(getVideoEncoder().getVideoQuality());
            encoder.getVideoQuality().setVideoWidth(w);
            encoder.getVideoQuality().setVideoHeight(h);
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
                    ByteBuffer sps = newFormat.getByteBuffer("csd-0");
                    ByteBuffer pps = newFormat.getByteBuffer("csd-1");

                    if (sps == null || pps == null) {
                        result.set(false);
                        latch.countDown();
                        return;
                    }

                    mSPS = new byte[sps.capacity() - 4];
                    sps.position(4);
                    sps.get(mSPS, 0, mSPS.length);

                    mPPS = new byte[pps.capacity() - 4];
                    pps.position(4);
                    pps.get(mPPS, 0, mPPS.length);

                    mPPSString = Base64.encodeToString(mPPS, 0, mPPS.length, Base64.NO_WRAP);
                    mSPSString = Base64.encodeToString(mSPS, 0, mSPS.length, Base64.NO_WRAP);
                    mProfileLevel = toHexString(mSPS, 1, 3);

                    if (DEBUG) {
                        Log.d(TAG, "### PPS " + mPPSString);
                        Log.d(TAG, "### SPS " + mSPSString);

                        StringBuilder spsA = new StringBuilder();
                        StringBuilder ppsA = new StringBuilder();
                        for (byte b : mSPS) {
                            spsA.append(String.format("%02X", b));
                        }
                        for (byte b : mPPS) {
                            ppsA.append(String.format("%02X", b));
                        }
                        Log.d(TAG, "### PPS " + ppsA);
                        Log.d(TAG, "### SPS " + spsA);
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
        fmt.addParameter("packetization-mode", "1");
        fmt.addParameter("profile-level-id", mProfileLevel);
        fmt.addParameter("sprop-parameter-sets", mSPSString + "," + mPPSString);

        MediaDescription mediaDescription = new MediaDescription("video", getDestinationPort(), "RTP/AVP", PAYLOAD_TYPE);
        mediaDescription.addAttribute(new RtpMapAttribute(PAYLOAD_TYPE, "H264", CLOCK_FREQUENCY));
        mediaDescription.addAttribute(fmt);
        mediaDescription.addAttribute(new ControlAttribute("trackID=" + getTrackId()));
        return mediaDescription;
    }

    private static String toHexString(byte[] buffer, int start, int len) {
        StringBuilder s = new StringBuilder();
        for (int i = start; i < start + len; i++) {
            s.append(String.format("%02x", (buffer[i] & 0xFF)));
        }
        return s.toString();
    }

    private class TempVideoEncoder extends CanvasVideoEncoder {
        @Override
        public void draw(Canvas canvas, int width, int height) {
        }
    }
}
