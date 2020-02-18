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

    /**
     * サーバーソケットのオープン状態を表すフラグ.
     *
     * サーバーソケットがオープンしている場合は true、それ以外は false。
     */
    private boolean mOpened;

    public SRTServerSocket(final int serverPort) {
        this("0.0.0.0", serverPort);
    }

    public SRTServerSocket(final String serverAddress, final int serverPort) {
        this(serverAddress, serverPort, DEFAULT_BACKLOG);
    }

    public SRTServerSocket(final String serverAddress, final int serverPort, final int backlog) {
        mServerAddress = serverAddress;
        mServerPort = serverPort;
        mBacklog = backlog;
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
    public synchronized void open() throws IOException {
        if (!mOpened) {
            mNativeSocket = NdkHelper.createSrtSocket();
            if (mNativeSocket < 0) {
                throw new IOException("Failed to create server socket: " + mServerAddress + ":" + mServerPort);
            }

            // TODO pre オプション は、listen する前に設定する必要があります。
            //      ここだと設定ができないので、場所を変えた方が良いかもしれない。
            NdkHelper.setSockFlag(mNativeSocket, SRT.SRTO_SENDER, 1);
            NdkHelper.setSockFlag(mNativeSocket, SRT.SRTO_MAXBW, 0L);

            int result = NdkHelper.bind(mNativeSocket, mServerAddress, mServerPort);
            if (result < 0) {
                NdkHelper.closeSrtSocket(mNativeSocket);
                throw new IOException("Failed to create server socket: " + mServerAddress + ":" + mServerPort);
            }

            result = NdkHelper.listen(mNativeSocket, mBacklog);
            if (result < 0) {
                NdkHelper.closeSrtSocket(mNativeSocket);
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
     * SRT サーバーソケットにオプションを設定します.
     *
     * @param option オプションのタイプ
     * @param value オプションの値
     * @throws IOException 設定に失敗した場合に発生
     */
    public void setOption(int option, Object value) throws IOException {
        if (!mOpened) {
            throw new IOException("already closed");
        }

        int result = NdkHelper.setSockFlag(mNativeSocket, option, value);
        if (result < 0) {
            throw new IOException("Failed to set a socket flag.");
        }
    }

    /**
     * SRTSocketServer を閉じます.
     */
    public synchronized void close() {
        if (mOpened) {
            mOpened = false;
            NdkHelper.closeSrtSocket(mNativeSocket);
        }
    }
}
