package org.deviceconnect.android.deviceplugin.host.connection;

public class HostTraffic {
    int mNetworkType;
    long mRx;
    long mTx;
    long mBitrateRx;
    long mBitrateTx;

    /**
     * ネットワークタイプを取得します.
     *
     * @return ネットワークタイプ
     */
    public int getNetworkType() {
        return mNetworkType;
    }

    /**
     * 受信したバイト数を取得します.
     *
     * @return 受信したバイト数
     */
    public long getRx() {
        return mRx;
    }

    /**
     * 送信したバイト数を取得します.
     *
     * @return 送信したバイト数
     */
    public long getTx() {
        return mTx;
    }

    /**
     * 受信ビットレートを取得します.
     *
     * @return 受信ビットレート
     */
    public long getBitrateRx() {
        return mBitrateRx;
    }

    /**
     * 送信ビットレートを取得します.
     *
     * @return 送信ビットレート
     */
    public long getBitrateTx() {
        return mBitrateTx;
    }

    @Override
    public String toString() {
        return "networkType: " + mNetworkType + "\n"
                +  "rx: " + mRx + "\n"
                +  "tx: " + mTx + "\n"
                +  "BitrateRx: " + mBitrateRx + "\n"
                +  "BitrateTx: " + mBitrateTx + "\n";
    }
}
