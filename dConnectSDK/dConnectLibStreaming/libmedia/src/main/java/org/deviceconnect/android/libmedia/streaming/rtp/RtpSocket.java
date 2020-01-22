package org.deviceconnect.android.libmedia.streaming.rtp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Random;

import org.deviceconnect.android.libmedia.streaming.util.QueueThread;

public class RtpSocket implements RtpPacketize.Callback {
    /**
     * バッファサイズを定義.
     */
    private static final int MAX_BUF_SIZE = 300;

    /**
     * 送信者識別子.
     */
    private int mSsrc;

    /**
     * 送信先のアドレス.
     */
    private InetAddress mRemoteAddress;

    /**
     * 送信先のポート番号.
     */
    private int mRemotePort;

    /**
     * 送信する RTP のデータを一時的にするバッファ.
     */
    private RtpPacket[] mRtpPackets = new RtpPacket[MAX_BUF_SIZE];

    /**
     * マルチキャスト用のソケット.
     */
    private MulticastSocket mSocket;

    /**
     * RTP データ送信用のスレッド.
     */
    private SenderThread mSenderThread;

    /**
     * 送信用の UDP パケット.
     */
    private DatagramPacket mDatagramPacket;

    /**
     * RTCP 送信用のソケット.
     */
    private RtcpSocket mRtcpSocket;

    /**
     * コンストラクタ.
     * @throws IOException ソケットの作成に失敗した場合に発生
     */
    public RtpSocket() throws IOException {
        mSocket = new MulticastSocket();
        mSocket.setTimeToLive(64);
        mSocket.setSoTimeout(5000);
        for (int i = 0; i < MAX_BUF_SIZE; i++) {
            mRtpPackets[i] = new RtpPacket();
        }
        mRtcpSocket = new RtcpSocket();
        mDatagramPacket = new DatagramPacket(new byte[1], 1);
        setSsrc(Math.abs(new Random().nextInt()));
    }

    /**
     * データが破棄されるまでの時間を設定します.
     *
     * TTLのいくつかのデフォルト値
     * <ul>
     * <li>0 は同じホストに制限される。</li>
     * <li>1 は同じサブネットに制限される。</li>
     * <li>32 は同じサイトに制限される。</li>
     * <li>64 は同じ地域に制限される。</li>
     * <li>128 は同じ大陸に制限される。</li>
     * <li>255 は無制限である。</li>
     * </ul>
     *
     * @param ttl time to live
     * @throws IOException 設定に失敗した場合に発生
     */
    public void setTimeToLive(int ttl) throws IOException {
        mSocket.setTimeToLive(ttl);
    }

    /**
     * Socket のタイムアウト時間を設定します.
     *
     * @param timeout タイムアウト時間(ms)
     * @throws IOException 設定に失敗した場合に発生
     */
    public void setSoTimeout(int timeout) throws IOException {
        mSocket.setSoTimeout(timeout);
    }

    /**
     * コンストラクタ.
     *
     * @param dest 接続先のアドレス
     * @param port RTP のポート番号
     * @param rtcpPort RTCP のポート番号
     *
     * @throws IOException ソケットの作成に失敗した場合に発生
     */
    public RtpSocket(InetAddress dest, int port, int rtcpPort) throws IOException {
        this();
        setDestination(dest, port, rtcpPort);
    }

    /**
     * ソケットを開始します.
     */
    public synchronized void open() {
        if (mSenderThread != null) {
            return;
        }

        mSenderThread = new SenderThread();
        mSenderThread.setName("RTP-SENDER");
        mSenderThread.setPriority(Thread.MAX_PRIORITY);
        mSenderThread.start();
    }

    /**
     * ソケットを停止します.
     */
    public synchronized void close() {
        if (mSenderThread != null) {
            try {
                mSocket.close();
            } catch (Exception e) {
                // ignore.
            }
            mSenderThread.terminate();
            mSenderThread = null;
        }

        if (mRtcpSocket != null) {
            mRtcpSocket.close();
        }
    }

    /**
     * 送信先のアドレスとポート番号を設定します.
     *
     * @param dest 送信先のアドレス
     * @param rtpPort RTPを送信するポート番号
     * @param rtcpPort RTCPを送信するポート番号
     */
    public void setDestination(InetAddress dest, int rtpPort, int rtcpPort) {
        mDatagramPacket.setAddress(dest);
        mDatagramPacket.setPort(rtpPort);
        mRemoteAddress = dest;
        mRemotePort = rtpPort;
        mRtcpSocket.setDestination(dest, rtcpPort);
    }

    /**
     * 接続先の RTP と RTCP のポート番号を取得します.
     *
     * @return 接続先の RTP と RTCPのポート番号
     */
    public int[] getRemotePorts() {
        return new int[] {
                mRemotePort, mRtcpSocket.getRemotePort()
        };
    }

    /**
     * RTP と RTCP のソケットのポート番号を取得します.
     *
     * @return RTP と RTCP のポート番号
     */
    public int[] getLocalPorts() {
        return new int[] {
            mSocket.getLocalPort(),
            mRtcpSocket.getLocalPort()
      };
    }

    /**
     * 送信者識別子を取得します.
     *
     * @return 送信者識別子
     */
    public int getSsrc() {
        return mSsrc;
    }

    /**
     * 送信者識別子を設定します.
     *
     * @param ssrc 送信者識別子
     */
    public void setSsrc(int ssrc) {
        mSsrc = ssrc;
        mRtcpSocket.setSsrc(ssrc);
    }

    @Override
    public RtpPacket getRtpPacket() {
        for (RtpPacket packet : mRtpPackets) {
            if (!packet.isUsed()) {
                packet.consume();
                return packet;
            }
        }
        return null;
    }

    @Override
    public void send(RtpPacket packet) {
        if (mSenderThread != null) {
            mSenderThread.add(packet);
        }
    }

    /**
     * RTP 送信用のスレッド.
     */
    private class SenderThread extends QueueThread<RtpPacket> {
        /**
         * 停止フラグ.
         */
        private boolean mStopFlag;

        /**
         * RTP送信用スレッドを停止します.
         */
        private void terminate() {
            mStopFlag = true;

            interrupt();

            try {
                join(500);
            } catch (InterruptedException e) {
                // ignore.
            }
        }

        /**
         * RTP パケットを送信します.
         */
        private void send() throws InterruptedException {
            RtpPacket packet = get();
            try {
                mDatagramPacket.setData(packet.getBuffer());
                mDatagramPacket.setLength(packet.getLength());
                mRtcpSocket.update(packet.getLength(), packet.getTimeStamp());
                mSocket.send(mDatagramPacket);
            } catch (IOException e) {
                // ignore
            } finally {
                packet.release();
            }
        }

        @Override
        public void run() {
            try {
                mSocket.joinGroup(mRemoteAddress);
            } catch (IOException e) {
                // ignore.
            }

            while (!mStopFlag) {
                try {
                    send();
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    // ignore.
                }
            }

            try {
                mSocket.leaveGroup(mRemoteAddress);
            } catch (IOException e) {
                // ignore.
            }
        }
    }
}
