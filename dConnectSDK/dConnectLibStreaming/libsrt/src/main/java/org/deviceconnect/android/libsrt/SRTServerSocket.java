package org.deviceconnect.android.libsrt;

import java.io.IOException;

/**
 * SRTサーバーのソケット.
 *
 * このクラスはスレッドセーフではありません.
 */
public class SRTServerSocket {

    private static final int DEFAULT_BACKLOG = 5;

    /**
     * NDK 側で確保した SRTSOCKET の値.
     */
    private long mNativeSocket;

    private final String mServerAddress;

    private final int mServerPort;

    private final int mBacklog;

    private boolean mOpened;

    public SRTServerSocket(final String serverAddress,
                           final int serverPort,
                           final int backlog) {
        mServerAddress = serverAddress;
        mServerPort = serverPort;
        mBacklog = backlog;
    }

    public SRTServerSocket(final String serverAddress,
                           final int serverPort) {
        this(serverAddress, serverPort, DEFAULT_BACKLOG);
    }

    public SRTServerSocket(final int serverPort) {
        this("0.0.0.0", serverPort);
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
     * SRTServerSocket を開きます.
     *
     * @throws IOException SRTServerSocket を開くのに失敗した場合に発生
     */
    public void open() throws IOException {
        if (!mOpened) {
            mNativeSocket = NdkHelper.createSrtSocket(mServerAddress, mServerPort, mBacklog);
            if (mNativeSocket < 0) {
                throw new IOException("Failed to create server socket: " + mServerAddress + ":" + mServerPort);
            }
            mOpened = true;
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
     * @throws IOException SRTServerSocket の接続が切断されたり、クライアントの接続に失敗した場合に発生
     */
    public SRTSocket accept() throws IOException {
        if (!mOpened) {
            throw new IOException("already closed");
        }

        long ptr = NdkHelper.accept(mNativeSocket);
        if (ptr <= 0) {
            throw new IOException("Failed to accept a client.");
        }

        String address = NdkHelper.getPeerName(ptr);
        if (address == null) {
            throw new IOException("Failed to get client address");
        }
        return new SRTSocket(ptr, address);
    }

    /**
     * SRTSocketServer を閉じます.
     */
    public void close() {
        if (mOpened) {
            mOpened = false;
            NdkHelper.closeSrtSocket(mNativeSocket);
        }
    }
}
