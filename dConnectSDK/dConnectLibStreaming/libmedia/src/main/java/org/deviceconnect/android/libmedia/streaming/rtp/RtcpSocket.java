package org.deviceconnect.android.libmedia.streaming.rtp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class RtcpSocket {
    /**
     * RTCP パケットのサイズを定義します.
     */
    private static final int PACKET_LENGTH = 28;

    /**
     * 送信者識別子.
     */
    private int mSsrc;

    /**
     * マルチキャスト用のソケット.
     */
    private MulticastSocket mSocket;

    /**
     * UDP 送信パケット.
     */
    private DatagramPacket mPacket;

    /**
     * 送信したデータの送信量.
     */
    private int mOctetCount;

    /**
     * 送信した RTP パケット数.
     */
    private int mPacketCount;

    /**
     * RTCP のパケットを格納するバッファ.
     */
    private final byte[] mBuffer = new byte[RtpPacket.MTU];

    /**
     * RTCP 送信時間.
     */
    private long mSendTime;

    /**
     * RTCP 送信インターバル.
     */
    private int mInterval = 3000;

    /**
     * コンストラクタ.
     * @throws IOException ソケットの作成に失敗した場合に発生します.
     */
    public RtcpSocket() throws IOException {
        mSocket = new MulticastSocket();
        mSocket.setTimeToLive(64);
        mPacket = new DatagramPacket(mBuffer, 1);
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    /**
     * RTCP 送信用のソケットを閉じます.
     */
    public void close() {
        try {
            mSocket.close();
        } catch (Exception e) {
            // ignore.
        }
    }

    /**
     * RTCP を送信するインターバルを設定します.
     *
     * @param interval インターバル
     */
    public void setInterval(int interval) {
        if (interval <= 0) {
            throw new IllegalArgumentException("interval is negative.");
        }
        mInterval = interval;
    }

    /**
     * 送信者識別子を設定します.
     *
     * @param ssrc 送信者識別子
     */
    public void setSsrc(int ssrc) {
        mSsrc = ssrc;
    }

    /**
     * 送信先のアドレスとポート番号を設定します.
     *
     * @param dest 送信先のアドレス
     * @param port 送信先のポート番号
     */
    public void setDestination(InetAddress dest, int port) {
        mPacket.setAddress(dest);
        mPacket.setPort(port);
    }

    /**
     * 送信先のポート番号を取得します.
     *
     * @return 送信先のポート番号
     */
    public int getRemotePort() {
        return mPacket.getPort();
    }

    /**
     * ソケットのポート番号を取得します.
     *
     * @return ソケットのポート番号
     */
    public int getLocalPort() {
        return mSocket.getLocalPort();
    }

    /**
     * RTP データの情報を更新します.
     *
     * @param dataLength データタイプ
     * @param rtpts タイムスタンプ
     */
    public void update(int dataLength, long rtpts) {
        mPacketCount++;
        mOctetCount += dataLength;

        if (System.currentTimeMillis() - mSendTime > mInterval) {
            send(rtpts);
            mSendTime = System.currentTimeMillis();
        }
    }

    /**
     * RTCP のパケットを送信します.
     *
     * @param timestamp タイムスタンプ
     */
    private void send(long timestamp) {
        int packetLen = PACKET_LENGTH / 4 - 1;

        long ntpts = System.nanoTime();
        long hb = ntpts / 1000000000;
        long lb = ((ntpts - hb * 1000000000) * 4294967296L) / 1000000000;

        mBuffer[0] = (byte) (2 << 6);
        mBuffer[1] = (byte) 200;
        mBuffer[2] = (byte) ((packetLen >> 8) & 0xFF);
        mBuffer[3] = (byte) ((packetLen) & 0xFF);
        mBuffer[4] = (byte) ((mSsrc >> 24) & 0xFF);
        mBuffer[5] = (byte) ((mSsrc >> 16) & 0xFF);
        mBuffer[6] = (byte) ((mSsrc >> 8) & 0xFF);
        mBuffer[7] = (byte) ((mSsrc) & 0xFF);
        mBuffer[8] = (byte) ((hb >> 24) & 0xFF);
        mBuffer[9] = (byte) ((hb >> 16) & 0xFF);
        mBuffer[10] = (byte) ((hb >> 8) & 0xFF);
        mBuffer[11] = (byte) ((hb) & 0xFF);
        mBuffer[12] = (byte) ((lb >> 24) & 0xFF);
        mBuffer[13] = (byte) ((lb >> 16) & 0xFF);
        mBuffer[14] = (byte) ((lb >> 8) & 0xFF);
        mBuffer[15] = (byte) ((lb) & 0xFF);
        mBuffer[16] = (byte) ((timestamp >> 24) & 0xFF);
        mBuffer[17] = (byte) ((timestamp >> 16) & 0xFF);
        mBuffer[18] = (byte) ((timestamp >> 8) & 0xFF);
        mBuffer[19] = (byte) ((timestamp) & 0xFF);
        mBuffer[20] = (byte) ((mPacketCount >> 24) & 0xFF);
        mBuffer[21] = (byte) ((mPacketCount >> 16) & 0xFF);
        mBuffer[22] = (byte) ((mPacketCount >> 8) & 0xFF);
        mBuffer[23] = (byte) ((mPacketCount) & 0xFF);
        mBuffer[24] = (byte) ((mOctetCount >> 24) & 0xFF);
        mBuffer[25] = (byte) ((mOctetCount >> 16) & 0xFF);
        mBuffer[26] = (byte) ((mOctetCount >> 8) & 0xFF);
        mBuffer[27] = (byte) ((mOctetCount) & 0xFF);

        try {
            mPacket.setLength(PACKET_LENGTH);
            mSocket.send(mPacket);
        } catch (IOException e) {
            // ignore.
        }
    }
}
