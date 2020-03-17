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

    /**
     * UDP にデータを送信します.
     */
    private void send() throws InterruptedException {
        byte[] packet = get();
        if (packet == null) {
            return;
        }

        try {
            mDatagramPacket.setData(packet);
            mDatagramPacket.setLength(packet.length);
            mSocket.send(mDatagramPacket);
        } catch (IOException e) {
            // ignore.
        }
    }

    @Override
    public void run() {
        while (!mStopFlag) {
            try {
                send();
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                // ignore.
            }
        }
    }
}
