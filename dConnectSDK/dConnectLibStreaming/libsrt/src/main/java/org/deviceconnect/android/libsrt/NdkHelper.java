package org.deviceconnect.android.libsrt;

/**
 * NDK へのアクセスを行うクラス.
 */
class NdkHelper {
    private static boolean mLoadLibrary = true;
    static {
        try {
            System.loadLibrary("srt");
            System.loadLibrary("srt-native-interface");
        } catch(java.lang.UnsatisfiedLinkError e) {
            mLoadLibrary = false;
        }
    }

    static boolean isLoadLibrary() {
        return mLoadLibrary;
    }
    static void checkLoadLibrary() throws SRTSocketException {
        if (!mLoadLibrary) {
            throw new SRTSocketException("Failed to load SRT Library, check your device architecture.", -1);
        }
    }

    private NdkHelper() {
    }

    static native void startup();
    static native void cleanup();

    /**
     * SRT ソケットを作成します.
     *
     * @return SRTソケットへのポインタ
     */
    static long createSrtSocket() throws SRTSocketException {
        checkLoadLibrary();
        return createSrtSocketImpl();
    }
    /**
     * SRT ソケットを作成します.
     *
     * @return SRTソケットへのポインタ
     */
    static native long createSrtSocketImpl();

    /**
     * SRT ソケットを閉じます.
     *
     * @param nativePtr SRTソケットへのポインタ
     */
    static native void closeSrtSocket(long nativePtr);

    /**
     * SRT ソケットをバインドします.
     *
     * @param nativePtr SRTソケットへのポインタ
     * @param address アドレス
     * @param port ポート番号
     * @return バインド結果(-1 の場合はバインドに失敗)
     */
    static native int bind(long nativePtr, String address, int port);

    /**
     * SRT ソケットを Listen 状態にします.
     *
     * @param nativePtr SRTソケットへのポインタ
     * @param backlog サーバにacceptされていないクライアントからの接続要求を保持しておくキューの最大値
     * @return listenの結果(-1 の場合は listen に失敗)
     */
    static native int listen(long nativePtr, int backlog);

    /**
     * SRT ソケットに接続が送られてくるのを待ちます.
     *
     * <p>
     * スレッドがブロッキングされるので、スレッドから呼び出すこと。
     * </p>
     *
     * @param nativePtr SRTソケットへのポインタ
     * @return 接続された SRT ソケットへのポインタ
     */
    static native long accept(long nativePtr);

    /**
     * 指定されたアドレスとポート番号に接続します.
     *
     * @param nativePtr SRTソケットへのポインタ
     * @param address 接続先のアドレス
     * @param port 接続先のポート番号
     * @return 接続結果(-1 の場合は接続に失敗)
     */
    static native int connect(long nativePtr, String address, int port);

    /**
     * SRT ソケットにオプションを設定します.
     * @param nativePtr SRTソケットへのポインタ
     * @param opt オプションのタイプ
     * @param value オプションの値
     * @return オプションの設定結果(-1 の場合はオプション設定に失敗)
     */
    static native int setSockFlag(long nativePtr, int opt, Object value);

    /**
     * メッセージを送信します.
     *
     * @param nativePtr SRTソケットへのポインタ
     * @param data 送信するデータ
     * @param offset オフセット
     * @param length データサイズ
     * @return 送信結果(-1 の場合はメッセージ送信に失敗)
     */
    static native int sendMessage(long nativePtr, byte[] data, int offset, int length);

    /**
     * メッセージを受信します.
     *
     * @param nativePtr SRTソケットへのポインタ
     * @param data データを格納する配列
     * @param length データを格納する配列のサイズ
     * @return 受信サイズ(-1 の場合はバインドに失敗)
     */
    static native int recvMessage(long nativePtr, byte[] data, int length);

    /**
     * 接続先のアドレスを取得します.
     *
     * @param nativePtr SRTソケットへのポインタ
     * @return アドレス
     */
    static native String getPeerName(long nativePtr);

    /**
     * 指定したソケットについての統計データを取得します.
     *
     * @param nativePtr SRTソケットへのポインタ
     * @param stats 統計データを受け取るオブジェクト
     */
    static native void getStats(long nativePtr, SRTStats stats);

}
