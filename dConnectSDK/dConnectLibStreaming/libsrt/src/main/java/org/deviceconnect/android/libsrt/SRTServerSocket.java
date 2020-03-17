package org.deviceconnect.android.libsrt;

import java.io.Closeable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * SRTサーバーのソケット.
 *
 * このクラスはスレッドセーフではありません.
 */
public class SRTServerSocket implements Closeable {
    /**
     * backlog のデフォルト値を定義.
     */
    private static final int DEFAULT_BACKLOG = 5;

    /**
     * NDK 側で確保した SRTSOCKET の値.
     */
    private long mNativeSocket;

    private String mServerAddress;
    private int mServerPort;

    /**
     * 着信接続のキューの要求された最大長.
     */
    private int mBacklog = DEFAULT_BACKLOG;

    /**
     * 未オープンされた状態を定義.
     */
    private static final int STATE_NOT_OPEN = 1;

    /**
     * オープンされた状態を定義.
     */
    private static final int STATE_OPEN = 2;

    /**
     * クローズされた状態を定義.
     */
    private static final int STATE_CLOSED = 3;

    /**
     * SRT サーバソケットの状態.
     */
    private int mState = STATE_NOT_OPEN;

    /**
     * コンストラクタ.
     *
     * アンバウンドのサーバー・ソケットを作成します。
     *
     * @throws SRTSocketException ソケットの作成に失敗した場合に発生
     */
    public SRTServerSocket() throws SRTSocketException {
        mNativeSocket = NdkHelper.createSrtSocket();
        if (mNativeSocket < 0) {
            throw new SRTSocketException("Failed to create server socket: " + mServerAddress + ":" + mServerPort, -1);
        }
    }

    /**
     * コンストラクタ.
     *
     * 指定されたポートにバインドされたサーバー・ソケットを作成します。
     *
     * @param serverPort サーバのポート番号
     * @throws SRTSocketException ソケットの作成に失敗した場合に発生
     */
    public SRTServerSocket(final int serverPort) throws SRTSocketException {
        this("0.0.0.0", serverPort);
    }

    /**
     * コンストラクタ.
     *
     * <p>
     * backlog はデフォルトで、5 を設定します。
     * </p>
     *
     * @param serverAddress サーバのアドレス
     * @param serverPort サーバのポート番号
     * @throws SRTSocketException ソケットの作成に失敗した場合に発生
     */
    public SRTServerSocket(final String serverAddress, final int serverPort) throws SRTSocketException {
        this(serverAddress, serverPort, DEFAULT_BACKLOG);
    }

    /**
     * コンストラクタ.
     *
     * @param serverAddress サーバのアドレス
     * @param serverPort サーバのポート番号
     * @param backlog サーバにacceptされていないクライアントからの接続要求を保持しておくキューの最大値
     * @throws SRTSocketException ソケットの作成に失敗した場合に発生
     */
    public SRTServerSocket(final String serverAddress, final int serverPort, final int backlog) throws SRTSocketException {
        this();

        if (backlog <= 0) {
            mBacklog = DEFAULT_BACKLOG;
        } else {
            mBacklog = backlog;
        }

        try {
            bind(serverAddress, serverPort);
        } catch (SRTSocketException e) {
            NdkHelper.closeSrtSocket(mNativeSocket);
            throw e;
        }
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
     * サーバの IP アドレスを取得します.
     *
     * @return サーバの IP アドレス
     */
    public String getServerAddress() {
        return mServerAddress;
    }

    /**
     * サーバのポート番号を取得します.
     *
     * @return ポート番号
     */
    public int getServerPort() {
        return mServerPort;
    }

    /**
     * SRTServerSocket を特定のアドレス(IPアドレスおよびポート番号)にバインドします.
     *
     * <p>
     * 内部では、srt_bind と srt_listen の処理が行われます。
     * </p>
     *
     * @throws SRTSocketException SRTServerSocket をバインドするのに失敗した場合に発生
     */
    public synchronized void bind(String serverAddress, int serverPort) throws SRTSocketException {
        if (mState == STATE_NOT_OPEN) {
            InetAddress inetAddress;
            try {
                inetAddress = InetAddress.getByName(serverAddress);
            } catch (UnknownHostException e) {
                throw new IllegalArgumentException("The format of the address is invalid.", e);
            }

            if (serverPort <= 0 || serverPort > 65535) {
                throw new IllegalArgumentException("serverPort is invalid.");
            }

            mServerAddress = inetAddress.getHostAddress();
            mServerPort = serverPort;

            int result = NdkHelper.bind(mNativeSocket, mServerAddress, mServerPort);
            if (result < 0) {
                throw new SRTSocketException("Failed to create server socket: " + mServerAddress + ":" + mServerPort, result);
            }

            result = NdkHelper.listen(mNativeSocket, mBacklog);
            if (result < 0) {
                throw new SRTSocketException("Failed to create server socket: " + mServerAddress + ":" + mServerPort, result);
            }
            mState = STATE_OPEN;
        } else if (mState == STATE_OPEN) {
            throw new SRTSocketException("SRTServerSocket is already opened.", -1);
        } else if (mState == STATE_CLOSED) {
            throw new SRTSocketException("SRTServerSocket is already closed.", -1);
        }
    }

    /**
     * SRT クライアントからの接続を待ちます
     *
     * <p>
     * クライアントからの接続があるまで、スレッドをブロッキングします。
     * </p>
     *
     * @return SRTServerSocket に接続された SRTSocket のクライアント
     * @throws SRTSocketException SRTServerSocket の接続が切断されたり、クライアントの接続に失敗した場合に発生
     */
    public SRTSocket accept() throws SRTSocketException {
        if (mState == STATE_CLOSED) {
            throw new SRTSocketException("SRTServerSocket is already closed.", -1);
        } else if (mState == STATE_NOT_OPEN) {
            throw new SRTSocketException("SRTServerSocket has not been opened yet.", -1);
        }

        long ptr = NdkHelper.accept(mNativeSocket);
        if (ptr <= 0) {
            throw new SRTSocketException("Failed to accept a client.", -1);
        }

        String address = NdkHelper.getPeerName(ptr);
        if (address == null) {
            throw new SRTSocketException("Failed to get client address", -1);
        }
        return new SRTSocket(ptr, address);
    }

    /**
     * SRT サーバーソケットにオプションを設定します.
     *
     * @param option オプションのタイプ
     * @param value オプションの値
     * @throws SRTSocketException 設定に失敗した場合に発生
     */
    public void setOption(int option, Object value) throws SRTSocketException {
        if (mState == STATE_CLOSED) {
            throw new SRTSocketException("already closed", -1);
        }

        if (value == null) {
            throw new IllegalArgumentException("value is not set.");
        }

        int result = NdkHelper.setSockFlag(mNativeSocket, option, value);
        if (result < 0) {
            throw new SRTSocketException("Failed to set a socket flag.", result);
        }
    }

    /**
     * SRT ソケットにオプションをまとめて設定します.
     *
     * @param options オプション設定の一覧
     * @throws SRTSocketException オプションの設定に失敗した場合に発生
     */
    public void setOptions(Map<Integer, Object> options) throws SRTSocketException {
        for (Map.Entry<Integer, Object> entry : options.entrySet()) {
            Object value = entry.getValue();
            if (value != null) {
                setOption(entry.getKey(), value);
            }
        }
    }

    /**
     * SRTSocketServer を閉じます.
     */
    public synchronized void close() {
        if (mState != STATE_CLOSED) {
            mState = STATE_CLOSED;
            NdkHelper.closeSrtSocket(mNativeSocket);
        }
    }
}
