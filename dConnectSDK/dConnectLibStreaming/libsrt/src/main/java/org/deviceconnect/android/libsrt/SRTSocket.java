package org.deviceconnect.android.libsrt;

import java.net.InetAddress;
import java.net.UnknownHostException;

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
     * 未オープンされた状態を定義.
     */
    private static final int STATE_NOT_OPEN = 1;

    /**
     * オープンされた状態を定義.
     */
    private static final int STATE_CONNECTED = 2;

    /**
     * クローズされた状態を定義.
     */
    private static final int STATE_CLOSED = 3;

    /**
     * SRT サーバソケットの状態.
     */
    private int mState = STATE_NOT_OPEN;

    /**
     * 接続されていない SRT ソケットを作成します。
     *
     * @throws SRTSocketException SRT ソケットの作成に失敗した場合に発生
     */
    public SRTSocket() throws SRTSocketException {
        mNativePtr = NdkHelper.createSrtSocket();
        if (mNativePtr < 0) {
            throw new SRTSocketException("Failed to create a socket.", -1);
        }
    }

    /**
     * SRT ソケットを指定されたアドレスとポート番号に接続します.
     *
     * @param address アドレス
     * @param port ポート番号
     * @throws SRTSocketException 接続に失敗した場合に発生
     */
    public SRTSocket(String address, int port) throws SRTSocketException {
        this();

        try {
            connect(address, port);
        } catch (SRTSocketException e) {
            // コンストラクタで例外が発生したので、SRT ソケットを閉じます。
            NdkHelper.closeSrtSocket(mNativePtr);
            throw e;
        }
    }

    /**
     * コンストラクタ.
     *
     * <p>
     * このコンストラクタは、 SRTServerSocket からのみ呼び出されます。
     * </p>
     *
     * @param nativePtr JNI 側のソケット
     * @param socketAddress ソケットの IPv4 アドレス
     */
    SRTSocket(final long nativePtr, final String socketAddress) {
        mNativePtr = nativePtr;
        mSocketAddress = socketAddress;
        mState = STATE_CONNECTED;
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
     * 指定されたアドレスのサーバーに SRT ソケットを接続します。
     *
     * @param address アドレス
     * @param port ポート番号
     * @throws SRTSocketException 接続に失敗した場合に発生
     */
    public synchronized void connect(String address, int port) throws SRTSocketException {
        if (mState == STATE_CONNECTED) {
            throw new SRTSocketException("SRTSocket is already connected.", -1);
        } else if (mState == STATE_CLOSED) {
            throw new SRTSocketException("SRTSocket is already closed.", -1);
        }

        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            throw new SRTSocketException("The format of the address is invalid.", e, -1);
        }

        int result = NdkHelper.connect(mNativePtr, inetAddress.getHostAddress(), port);
        if (result < 0) {
            throw new SRTSocketException("Failed to create a socket: " + address + ":" + port, -1);
        }

        mState = STATE_CONNECTED;
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
     * このソケットについての統計データを取得します.
     *
     * @return 統計データを受け取るオブジェクト
     */
    public SRTStats getStats() {
        SRTStats stats = new SRTStats();
        NdkHelper.getStats(mNativePtr, stats);
        return stats;
    }

    /**
     * ソケットの接続状態を返します.
     *
     * @return ソケットが接続されている場合は true、それ以外は false
     */
    public boolean isConnected() {
        return mState == STATE_CONNECTED;
    }

    /**
     * ソケットが閉じているかどうかを返します.
     *
     * @return ソケットが閉じている場合は<code>true</code>、そうでない場合は<code>false</code>
     */
    public boolean isClosed() {
        return mState == STATE_CLOSED;
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
     * 指定したバイト配列の先頭から長さ length のデータがペイロードとして格納されます.
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
     * 指定したバイト配列のうち、offset 番目から長さ length のデータがペイロードとして格納されます.
     *
     * @param data ペイロード
     * @param offset オフセット
     * @param length データ長
     * @throws SRTSocketException 送信に失敗した場合
     */
    public synchronized void send(final byte[] data, final int offset, final int length) throws SRTSocketException {
        if (mState == STATE_NOT_OPEN) {
            throw new SRTSocketException("SRTSocket is not connected yet.", -1);
        } else if (mState == STATE_CLOSED) {
            throw new SRTSocketException("SRTSocket is already closed.", -1);
        }

        if (data == null) {
            throw new IllegalArgumentException("data is null.");
        }

        if (offset < 0 || length < offset) {
            throw new IllegalArgumentException("offset is invalid.");
        }

        if (data.length < offset + length) {
            throw new IllegalArgumentException("length is invalid.");
        }

        int result = NdkHelper.sendMessage(mNativePtr, data, offset, length);
        if (result < 0) {
            throw new SRTSocketException("Failed to send a message.", result);
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
        if (mState == STATE_NOT_OPEN) {
            throw new SRTSocketException("SRTSocket is not connected yet.", -1);
        } else if (mState == STATE_CLOSED) {
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
     * @param option オプションのタイプ
     * @param value オプションの値
     * @throws SRTSocketException オプションの設定に失敗した場合に発生
     */
    public void setOption(int option, Object value) throws SRTSocketException {
        if (mState == STATE_CLOSED) {
            throw new SRTSocketException("SRTSocket is already closed.", -1);
        }

        if (value == null) {
            throw new IllegalArgumentException("value is not set.");
        }

        int result = NdkHelper.setSockFlag(mNativePtr, option, value);
        if (result < 0) {
            throw new SRTSocketException("Failed to set a socket option. option=" + option + ", value=" + value, result);
        }
    }

    /**
     * ソケットを閉じます.
     *
     * すでに閉じている場合は何もせずに即座に処理を返します.
     */
    public synchronized void close() {
        if (mState == STATE_CLOSED) {
            return;
        }
        mState = STATE_CLOSED;

        NdkHelper.closeSrtSocket(mNativePtr);
    }
}