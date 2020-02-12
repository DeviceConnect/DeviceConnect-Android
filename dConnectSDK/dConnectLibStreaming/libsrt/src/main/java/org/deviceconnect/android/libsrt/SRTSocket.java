package org.deviceconnect.android.libsrt;

/**
 * SRTソケット.
 */
public class SRTSocket {

    /**
     * JNI 側のソケット.
     */
    private long mNativePtr;

    /**
     * ソケットの IPv4 アドレス.
     *
     * 例) "192.168.1.2"
     */
    private String mSocketAddress;

    /**
     * ソケットが閉じているかどうかのフラグ
     */
    private boolean mClosed;

    /**
     * コンストラクタ.
     *
     * @param nativePtr JNI 側のソケット
     * @param socketAddress ソケットの IPv4 アドレス
     */
    SRTSocket(final long nativePtr,
              final String socketAddress) {
        mNativePtr = nativePtr;
        mSocketAddress = socketAddress;
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
     * ソケットの IPv4 アドレスを返します.
     *
     * @return ソケットの IPv4 アドレス
     */
    public String getRemoteSocketAddress() {
        return mSocketAddress;
    }

    /**
     * 通信に関する統計情報をログ出力します.
     */
    public void dumpStats() {
        NdkHelper.dumpStats(mNativePtr);
    }

    /**
     * ソケットが閉じているかどうかを返します.
     *
     * @return ソケットが閉じている場合は<code>true</code>、そうでない場合は<code>false</code>
     */
    public boolean isClosed() {
        return mClosed;
    }

    /**
     * SRTパケットを送信します.
     *
     * 指定したバイト配列のすべてのデータがペイロードとして格納されます.
     *
     * @param data ペイロード
     * @throws SRTSocketException 送信に失敗した場合
     */
    public synchronized void send(byte[] data) throws SRTSocketException {
        send(data, 0, data.length);
    }

    /**
     * SRTパケットを送信します.
     *
     * 指定したバイト配列の先頭から長さ dataLength のデータがペイロードとして格納されます.
     *
     * @param data ペイロード
     * @param length データ長
     * @throws SRTSocketException 送信に失敗した場合
     */
    public synchronized void send(byte[] data, int length) throws SRTSocketException {
        send(data, 0, length);
    }

    /**
     * SRTパケットを送信します.
     *
     * 指定したバイト配列のうち、offset 番目から長さ dataLength のデータがペイロードとして格納されます.
     *
     * @param data ペイロード
     * @param offset オフセット
     * @param length データ長
     * @throws SRTSocketException 送信に失敗した場合
     */
    public synchronized void send(final byte[] data, final int offset, final int length) throws SRTSocketException {
        if (mClosed) {
            throw new SRTSocketException(0);
        }
        int result = NdkHelper.sendMessage(mNativePtr, data, offset, length);
        if (result < 0) {
            throw new SRTSocketException(result);
        }
    }

    /**
     * SRTパケットを受信します.
     *
     * @param data 受信するためのバッファ
     * @param dataLength 受信するデータ長
     * @return 受信したデータ長
     * @throws SRTSocketException 受信に失敗した場合
     */
    public synchronized int recv(byte[] data, int dataLength) throws SRTSocketException {
        if (mClosed) {
            throw new SRTSocketException("SRTSocket is already closed.", -1);
        }

        if (data == null) {
            throw new IllegalArgumentException("data is null.");
        }

        if (data.length < dataLength) {
            throw new IllegalArgumentException("dataLength is invalid.");
        }

        int result = NdkHelper.recvMessage(mNativePtr, data, dataLength);
        if (result < 0) {
            throw new SRTSocketException("Failed to received a message.", result);
        }
        return result;
    }

    /**
     * SRT ソケットにオプションを設定します.
     *
     * @param inputBW 入力ビットレート
     * @param oheaBW bandwidth overhead above input rate
     * @throws SRTSocketException
     */
    public void setOptions(long inputBW, int oheaBW) throws SRTSocketException {
        if (mClosed) {
            throw new SRTSocketException(0);
        }
        NdkHelper.setSrtOptions(mNativePtr, inputBW, oheaBW);
    }

    /**
     * ソケットを閉じます.
     *
     * すでに閉じている場合は何もせずに即座に処理を返します.
     */
    public synchronized void close() {
        if (mClosed) {
            return;
        }
        mClosed = true;

        NdkHelper.closeSrtSocket(mNativePtr);
    }
}