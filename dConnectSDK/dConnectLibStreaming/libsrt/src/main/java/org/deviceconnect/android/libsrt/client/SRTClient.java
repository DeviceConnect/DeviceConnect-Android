package org.deviceconnect.android.libsrt.client;

import org.deviceconnect.android.libsrt.SRTSocket;

import java.io.IOException;

/**
 * SRT サーバと通信するクラス.
 */
public class SRTClient {

    /**
     * SRT ソケット.
     */
    private SRTSocket mSRTSocket;

    /**
     * 接続先のアドレス.
     */
    private String mAddress;

    /**
     * 接続先のポート番号.
     */
    private int mPort;

    /**
     * SRT サーバとの接続を監視するスレッド.
     */
    private SRTSessionThread mSRTSessionThread;

    /**
     * SRT クライアントのイベントを通知するリスナー.
     */
    private OnEventListener mOnEventListener;

    /**
     * コンストラクタ.
     * @param address 接続先のアドレス
     * @param port 接続先のポート番号
     */
    public SRTClient(String address, int port) {
        mAddress = address;
        mPort = port;
    }

    /**
     * SRT クライアントのイベントを通知するリスナーを設定します.
     *
     * @param listener リスナー
     */
    public void setOnEventListener(OnEventListener listener) {
        mOnEventListener = listener;
    }

    /**
     * 通信を開始します.
     */
    public synchronized void start() {
        if (mSRTSessionThread != null) {
            return;
        }
        mSRTSessionThread = new SRTSessionThread();
        mSRTSessionThread.start();
    }

    /**
     * 通信を停止します.
     */
    public synchronized void stop() {
        if (mSRTSessionThread != null) {
            mSRTSessionThread.terminate();
            mSRTSessionThread = null;
        }
    }

    /**
     * SRT サーバとの通信を監視するスレッド.
     */
    private class SRTSessionThread extends Thread {
        /**
         * バッファサイズを定義.
         */
        private static final int BUFFER_SIZE = 1500;

        SRTSessionThread() {
            setName("SRT-CLIENT");
        }

        /**
         * スレッドの終了処理を行います.
         */
        void terminate() {
            if (mSRTSocket != null) {
                try {
                    mSRTSocket.close();
                } catch (Exception e) {
                    // ignore.
                }
            }

            interrupt();

            try {
                join(200);
            } catch (InterruptedException e) {
                // ignore.
            }
        }

        @Override
        public void run() {
            try {
                mSRTSocket = new SRTSocket(mAddress, mPort);
            } catch (IOException e) {
                postOnError(e);
                return;
            }

            postOnConnected();

            try {
                int len;
                byte[] buffer = new byte[BUFFER_SIZE];

                while (!isInterrupted()) {
                    len = mSRTSocket.recv(buffer, BUFFER_SIZE);
                    if (len > 0) {
                        postOnRead(buffer, len);
                    }
                }
            } catch (IOException e) {
                // ignore.
            } finally {
                if (mSRTSocket != null) {
                    try {
                        mSRTSocket.close();
                    } catch (Exception e) {
                        // ignore.
                    }
                }

                postOnDisconnected();
            }
        }
    }

    private void postOnRead(byte[] data, int dataLength) {
        if (mOnEventListener != null) {
            mOnEventListener.onRead(data, dataLength);
        }
    }

    private void postOnConnected() {
        if (mOnEventListener != null) {
            mOnEventListener.onConnected();
        }
    }

    private void postOnDisconnected() {
        if (mOnEventListener != null) {
            mOnEventListener.onDisconnected();
        }
    }

    private void postOnError(IOException e) {
        if (mOnEventListener != null) {
            mOnEventListener.onError(e);
        }
    }

    public interface OnEventListener {
        void onConnected();
        void onDisconnected();
        void onRead(byte[] data, int dataLength);
        void onError(IOException e);
    }
}
