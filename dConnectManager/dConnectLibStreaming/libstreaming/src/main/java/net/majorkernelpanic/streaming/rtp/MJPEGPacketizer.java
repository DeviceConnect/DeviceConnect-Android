package net.majorkernelpanic.streaming.rtp;

import java.io.IOException;
import java.io.InputStream;

public class MJPEGPacketizer extends AbstractPacketizer implements Runnable {
    private Thread mThread = null;
    private Statistics stats = new Statistics();

    private int mWidth;
    private int mHeight;

    @Override
    public void run() {

        // See section 3.1 of RFC 2435

        try {
            while (!Thread.interrupted()) {
                long oldTime = System.nanoTime();

                sendFrame();

                long duration = System.nanoTime() - oldTime;

                ts += duration;

                stats.push(duration);
            }
        } catch (IOException e) {
            // ignore.
        } catch (InterruptedException e) {
            // ignore.
        }
    }

    @Override
    public void start() {
        if (mThread == null) {
            mThread = new Thread(this);
            mThread.setPriority(Thread.MAX_PRIORITY);
            mThread.start();
        }
    }

    @Override
    public void stop() {
        if (mThread != null) {
            mThread.interrupt();
            try {
                mThread.join();
            } catch (InterruptedException e) {
                // ignore.
            }
            mThread = null;
        }
    }

    public void setSize(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    private void sendFrame() throws IOException, InterruptedException {
        int offset = 0;
        byte type = 0;
        byte q = (byte) 255;
        byte width = (byte) ((mWidth >> 3) & 0xFF);
        byte height = (byte) ((mHeight >> 3) & 0xFF);

        mQuantizationNum = 0;
        mDRI = 0;

        parseJpegHeader(is);

        if (mDRI != 0) {
            type |= RTP_JPEG_RESTART;
        }

        int bytesLeft = is.available();

        while (bytesLeft > 0) {
            int headerLen = rtphl;

            byte[] buffer = socket.requestBuffer();
            socket.updateTimestamp(ts);

            buffer[headerLen++] = 0; // typespec
            buffer[headerLen++] = (byte) ((offset >> 16) & 0xFF);
            buffer[headerLen++] = (byte) ((offset >> 8) & 0xFF);
            buffer[headerLen++] = (byte) ((offset) & 0xFF);
            buffer[headerLen++] = (byte) (type & 0xFF);
            buffer[headerLen++] = (byte) (q & 0xFF);
            buffer[headerLen++] = (byte) (width & 0xFF);
            buffer[headerLen++] = (byte) (height & 0xFF);

            if (mDRI != 0) {
                buffer[headerLen++] = (byte) ((mDRI >> 8) & 0xFF);
                buffer[headerLen++] = (byte) ((mDRI) & 0xFF);
                buffer[headerLen++] = (byte) 0xFF;
                buffer[headerLen++] = (byte) 0xFF;
            }

            if (offset == 0 && mQuantizationNum > 0) {
                int length = 64 * mQuantizationNum;
                buffer[headerLen++] = 0; // mbz
                buffer[headerLen++] = 0; // precision
                buffer[headerLen++] = (byte) ((length >> 8) & 0xFF);
                buffer[headerLen++] = (byte) ((length) & 0xFF);
                for (int i = 0; i < mQuantizationNum; i++) {
                    System.arraycopy(mQuantizationTable[i], 0, buffer, headerLen, 64);
                    headerLen += 64;
                }
            }

            int len = is.read(buffer, headerLen, buffer.length - headerLen);

            if (bytesLeft - len <= 0) {
                socket.markNextPacket();
            }

            super.send(len + headerLen);

            offset += len;
            bytesLeft -= len;
        }
    }

    private static final byte RTP_JPEG_RESTART = 0x40;

    private static final byte SOI = (byte) 0xD8;
    private static final byte EOI = (byte) 0xD9;
    private static final byte DQT = (byte) 0xDB;
    private static final byte SOF = (byte) 0xC0;
    private static final byte DHT = (byte) 0xC4;
    private static final byte SOS = (byte) 0xDA;
    private static final byte DRI = (byte) 0xDD;

    private byte[][] mQuantizationTable = new byte[4][64];
    private int mQuantizationNum = 0;
    private int mDRI = 0;

    private void parseDQT(InputStream in, int len) throws IOException {
        byte[] d = new byte[len];
        int l = in.read(d, 0, len);
        if (l != len) {
            throw new IOException("");
        }

        for (int i = 0; i < len; i += 65) {
            if ((d[i] & 0xF0) != 0) {
                throw new IOException("");
            }
            System.arraycopy(d, i + 1, mQuantizationTable[d[i] & 0xF], 0, 64);
            mQuantizationNum++;
        }
    }

    private void parseJpegHeader(InputStream in) throws IOException {
        while (in.available() > 0) {
            byte d = (byte) (in.read() & 0xFF);
            if (d == (byte) 0xFF) {
                d = (byte) (in.read() & 0xFF);
                if (d != SOI) {
                    int len = (((in.read() & 0xFF) << 8) | (in.read() & 0xFF));
                    switch (d) {
                        case DQT: // Quantization Table
                            parseDQT(in, len - 2);
                            break;

                        case DRI:
                            mDRI = (((in.read() & 0xFF) << 8) | (in.read() & 0xFF));
                            break;

                        case EOI: // End of Image
                        case SOF: // Start of Frame
                        case DHT: // Huffman Table
                        default:  // Other
                            is.skip(len - 2);
                            break;

                        case SOS: // Start of Scan
                            is.skip(len - 2);
                            return;
                    }
                }
            }
        }
    }
}
