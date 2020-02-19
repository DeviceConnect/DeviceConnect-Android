package org.deviceconnect.android.libsrt.client.decoder;

/**
 * フレームデータをデコードするクラスのインターフェース.
 */
public interface Decoder {
    /**
     * 送られてくるフレームデータを受け取ります.
     *
     * @param data フレームデータ
     * @param dataLength データサイズ
     */
    void onReceived(byte[] data, int dataLength, long pts);

    /**
     * デコーダの後始末を行います.
     */
    void onReleased();
}
