package org.deviceconnect.android.libmedia.streaming.rtp;

import org.deviceconnect.android.libmedia.streaming.util.UDPReceiverThread;

public class RtpReceiver {
    /**
     * RTP を受信するためのポート番号.
     */
    private int mRtpPort;

    /**
     * RTCP を受信するためのポート番号.
     */
    private int mRtcpPort;

    /**
     * RTP 受信用のスレッド.
     */
    private UDPReceiverThread mReceiverThread;

    /**
     * RTCP 受信用のスレッド.
     */
    private UDPReceiverThread mRtcpReceiverThread;

    /**
     * 受信した RTP、RTCP を通知するコールバック.
     */
    private Callback mCallback;

    /**
     * コンストラクタ.
     *
     * @param rtpPort RTPポート番号
     * @param rtcpPort RTCPポート番号
     */
    public RtpReceiver(int rtpPort, int rtcpPort) {
        mRtpPort = rtpPort;
        mRtcpPort = rtcpPort;
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
     * 受信した RTP・RTCP パケットを通知するコールバックを設定します.
     *
     * @param callback コールバック
     */
    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    /**
     * RTP・RTCP パケットの受信用のソケットを開きます.
     */
    public synchronized void open() {
        if (mReceiverThread != null || mRtcpReceiverThread != null) {
            return;
        }

        mReceiverThread = new UDPReceiverThread(mRtpPort, RtpPacket.MTU) {
            @Override
            public void onReceived(byte[] data, int dataLength) {
                postOnRtpReceived(data, dataLength);
            }
            @Override
            public void onError(Exception e) {
                postOnError(e);
            }
        };
        mReceiverThread.setName("RTP-RECEIVER-" + mRtpPort);
        mReceiverThread.setPriority(Thread.MAX_PRIORITY);
        mReceiverThread.start();

        if (mRtcpPort > 0) {
            mRtcpReceiverThread = new UDPReceiverThread(mRtcpPort, RtpPacket.MTU) {
                @Override
                public void onReceived(byte[] data, int dataLength) {
                    postOnRtcpReceived(data, dataLength);
                }

                @Override
                public void onError(Exception e) {
                    postOnError(e);
                }
            };
            mRtcpReceiverThread.setName("RTCP-RECEIVER-" + mRtcpPort);
            mRtcpReceiverThread.start();
        }
    }

    /**
     * RTP・RTCP パケットの受信用のソケットを閉じます.
     */
    public synchronized void close() {
        if (mReceiverThread != null) {
            mReceiverThread.terminate();
            mReceiverThread = null;
        }

        if (mRtcpReceiverThread != null) {
            mRtcpReceiverThread.terminate();
            mRtcpReceiverThread = null;
        }
    }

    /**
     * RTP受信ポート番号の取得.
     * @return ポート番号
     */
    public int getRtpPort() {
        return mRtpPort;
    }

    /**
     * RTCP受信ポート番号の取得.
     * @return ポート番号
     */
    public int getRtcpPort() {
        return mRtcpPort;
    }

    /**
     * エラーを通知します.
     *
     * @param e エラー原因の例外
     */
    private void postOnError(Exception e) {
        if (mCallback != null) {
            mCallback.onError(e);
        }
    }

    /**
     * 受信した RTP のデータを通知します.
     *
     * @param data RTP のデータ
     * @param dataLength RTP のデータサイズ
     */
    private void postOnRtpReceived(byte[] data, int dataLength) {
        if (mCallback != null) {
            mCallback.onRtpReceived(data, dataLength);
        }
    }

    /**
     * 受信した RTCP のデータを通知します.
     *
     * @param data RTCP のデータ
     * @param dataLength RTCP のデータサイズ
     */
    private void postOnRtcpReceived(byte[] data, int dataLength) {
        if (mCallback != null) {
            mCallback.onRtcpReceived(data, dataLength);
        }
    }

    public interface Callback {
        /**
         * RTP・RTCP の受信で発生したエラーを通知します.
         *
         * @param e エラー原因の例外
         */
        void onError(Exception e);

        /**
         * 受信した RTP パケットを通知します.
         *
         * <p>
         * RTP を受信しているので、この通知の中では重い処理を行わないこと。
         * </p>
         *
         * @param data RTP パケットデータ
         * @param dataLength RTP パケットデータサイズ
         */
        void onRtpReceived(byte[] data, int dataLength);

        /**
         * 受信した RTCP パケットを通知します.
         *
         * @param data RTCP パケットデータ
         * @param dataLength RTCP パケットデータサイズ
         */
        void onRtcpReceived(byte[] data, int dataLength);
    }
}
