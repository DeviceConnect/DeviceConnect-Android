package org.deviceconnect.android.libmedia.streaming.mjpeg;


public abstract class MJPEGEncoder {
    /**
     * MJPEG の設定を格納するクラス.
     */
    private final MJPEGQuality mMJPEGQuality = new MJPEGQuality();

    /**
     * JPEG を通知するためのコールバック.
     */
    private Callback mCallback;

    /**
     * MJPEG の設定を取得します.
     *
     * @return MJPEG の設定
     */
    public MJPEGQuality getMJPEGQuality() {
        return mMJPEGQuality;
    }

    /**
     * エンコードを開始します.
     * @throws MJPEGEncoderException MJPEGのエンコードの開始に失敗した場合
     */
    public abstract void start() throws MJPEGEncoderException;

    /**
     * エンコードを停止します.
     */
    public abstract void stop();

    /**
     * JPEG のデータを通知します.
     *
     * @param jpeg JPEGデータ
     */
    protected void postJPEG(byte[] jpeg) {
        if (mCallback != null) {
            mCallback.onJpeg(jpeg);
        }
    }

    /**
     * JPEG を通知するためのコールバックを設定します.
     *
     * @param callback JPEG を通知するためのコールバック
     */
    void setCallback(Callback callback) {
        mCallback = callback;
    }

    /**
     * JPEG を通知するためのコールバック.
     */
    interface Callback {
        /**
         * JPEG を通知します.
         * @param jpeg JPEGデータ
         */
        void onJpeg(byte[] jpeg);
    }
}
