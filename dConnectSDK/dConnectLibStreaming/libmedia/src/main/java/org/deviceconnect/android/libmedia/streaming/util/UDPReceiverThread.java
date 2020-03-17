package org.deviceconnect.android.libmedia.streaming.util;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * UDP 受信を行うためのスレッド.
 */
public abstract class UDPReceiverThread extends Thread {
    /**
     * 停止フラグ.
     */
    private boolean mStopFlag;

    /**
     * UDP 受信用のソケット.
     */
    private DatagramSocket mDatagramSocket;

    /**
     * 受信データを格納するバッファ.
     */
    private final byte[] mData;

    /**
     * UDP のポート番号.
     */
    private int mPort;

    /**
     * タイムアウト時間.
     */
    private int mTimeout = 5000;

    /**
     * コンストラクタ.
     *
     * @param port ポート番号
     */
    public UDPReceiverThread(int port) {
        this(port, 4096);
    }

    /**
     * コンストラクタ.
     *
     * @param port ポート番号
     */
    public UDPReceiverThread(int port, int bufferSize) {
        mPort = port;
        mData = new byte[bufferSize];
    }

    /**
     * Socket のタイムアウト時間を設定します.
     *
     * @param timeout タイムアウト時間(ms)
     */
    public void setSoTimeout(int timeout) {
        mTimeout = timeout;
    }

    /**
     * スレッドの停止処理を行います.
     */
    public void terminate() {
        mStopFlag = true;

        try {
            mDatagramSocket.close();
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
     * 受信した UDP のデータを通知します.
     *
     * @param data       受信したデータ
     * @param dataLength 受信したデータサイズ
     */
    public abstract void onReceived(byte[] data, int dataLength);

    /**
     * UDP の受信に失敗したエラーを通知します.
     *
     * @param e エラー原因の例外
     */
    public void onError(Exception e) {
    }

    @Override
    public void run() {
        DatagramPacket packet = new DatagramPacket(mData, mData.length);

        try {
            mDatagramSocket = new DatagramSocket(mPort);
            mDatagramSocket.setSoTimeout(mTimeout);
        } catch (SocketException e) {
            onError(e);
            return;
        }

        while (!mStopFlag) {
            try {
                mDatagramSocket.receive(packet);
                onReceived(packet.getData(), packet.getLength());
            } catch (Exception e) {
                // ignore.
            }
        }
    }
}
