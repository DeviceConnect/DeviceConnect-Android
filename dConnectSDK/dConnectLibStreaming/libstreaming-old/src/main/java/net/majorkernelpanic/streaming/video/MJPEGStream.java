package net.majorkernelpanic.streaming.video;

import net.majorkernelpanic.streaming.rtp.MJPEGPacketizer;

import java.io.IOException;
import java.io.InputStream;

public class MJPEGStream extends VideoStream {

    private InputStream mInputStream;
    private int mWidth;
    private int mHeight;

    public MJPEGStream(final VideoQuality quality) {
        mPacketizer = new MJPEGPacketizer();
        mWidth = quality.resX;
        mHeight = quality.resY;
    }

    public void setInputStream(InputStream in) {
        mInputStream = in;
    }

    @Override
    public synchronized String getSessionDescription() throws IllegalStateException {
        return "m=video " + String.valueOf(getDestinationPorts()[0]) + " RTP/AVP 96\r\n" +
                "a=rtpmap:96 JPEG/90000\r\n" +
                "a=fmtp:96 width=" + mWidth + "; height=" + mHeight + ";\r\n";
    }

    @Override
    protected void encodeWithMediaCodec() throws IOException {
        ((MJPEGPacketizer) mPacketizer).setSize(mWidth, mHeight);
        mPacketizer.setInputStream(mInputStream);
        mPacketizer.start();
        mStreaming = true;
    }

    @Override
    public synchronized void start() throws IllegalStateException, IOException {
        if (!mStreaming) {
            configure();
            super.start();
        }
    }
}
