package org.deviceconnect.android.libmedia.streaming.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class UDPSenderThread extends QueueThread<byte[]> {
    /**
     * 停止フラグ.
     */
    private boolean mStopFlag;

    /**
     * マルチキャスト用のソケット.
     */
    private MulticastSocket mSocket;

    /**
     * 送信用の UDP パケット.
     */
    private DatagramPacket mDatagramPacket;

    /**
     * 送信サイズ.
     */
    private long mSentSize;

    /**
     * BPS (bits per second).
     */
    private long mBPS;

    /**
     * コンストラクタ.
     * @throws IOException ソケットの作成に失敗した場合に発生
     */
    public UDPSenderThread() throws IOException {
        mSocket = new MulticastSocket();
        mSocket.setTimeToLive(64);
        mSocket.setSoTimeout(5000);
        mDatagramPacket = new DatagramPacket(new byte[1], 1);
    }

    /**
     * コンストラクタ.
     * @throws IOException ソケットの作成に失敗した場合に発生
     */
    public UDPSenderThread(String address, int port) throws IOException {
        this();
        setDestination(address, port);
    }

    /**
     * コンストラクタ.
     * @throws IOException ソケットの作成に失敗した場合に発生
     */
    public UDPSenderThread(InetAddress dest, int port) throws IOException {
        this();
        setDestination(dest, port);
    }

    /**
     * データが破棄されるまでの時間を設定します.
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
     * 送信先のアドレスとポート番号を設定します.
     *
     * @param address アドレス
     * @param port ポート番号
     * @throws UnknownHostException
     */
    public void setDestination(String address, int port) throws UnknownHostException {
        setDestination(InetAddress.getByName(address), port);
    }

    /**
     * 送信先のアドレスとポート番号を設定します.
     *
     * @param dest アドレス
     * @param port ポート番号
     */
    public void setDestination(InetAddress dest, int port) {
        mDatagramPacket.setAddress(dest);
        mDatagramPacket.setPort(port);
    }

    /**
     * 送信したデータサイズを取得します.
     *
     * @return 送信したデータサイズ
     */
    public long getSentSize() {
        return mSentSize;
    }

    /**
     * 送信したデータサイズをリセットします.
     */
    public void resetSentSize() {
        mSentSize = 0;
    }

    /**
     * 送信したデータの BPS (bits per second) を取得します.
     *
     * @return BPS (bits per second)
     */
    public long getBPS() {
        return mBPS;
    }

    /**
     * スレッドの停止処理を行います.
     */
    public void terminate() {
        mStopFlag = true;

        try {
            mSocket.close();
        } catch (Exception e) {
            // ignore.
        }

        interrupt();

        try {
            join(500);
        } catch (InterruptedException e) {
            // ignore.
        }
    }

    @Override
    public void run() {
        long sentSize = 0;
        long startTime = System.currentTimeMillis();
        while (!mStopFlag) {
            try {
                byte[] packet = get();
                if (packet == null) {
                    return;
                }

                try {
                    mDatagramPacket.setData(packet);
                    mDatagramPacket.setLength(packet.length);
                    mSocket.send(mDatagramPacket);

                    mSentSize += packet.length;
                    sentSize += packet.length;
                    if (System.currentTimeMillis() - startTime >= 1000) {
                        mBPS = sentSize * 8;
                        sentSize = 0;
                        startTime = System.currentTimeMillis();
                    }
                } catch (IOException e) {
                    // ignore.
                }
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                // ignore.
            }
        }
    }
}
