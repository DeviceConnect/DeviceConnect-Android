package org.deviceconnect.android.deviceplugin.fabo.device;

/**
 * ILIDARLiteV3を操作するためのインターフェース.
 */
public interface ILIDARLiteV3 {

    /**
     * LIDARLite v3から距離を取得します.
     * @param listener 加速度の通知を受けるリスナー
     */
    void read(final OnLIDARLiteListener listener);

    /**
     * LIDARLite v3から距離センサーを開始します.
     * @param listener 加速度の通知を受けるリスナー
     */
    void startRead(final OnLIDARLiteListener listener);

    /**
     * LIDARLite v3から距離センサーを停止します.
     * @param listener 加速度の通知を受けるリスナー
     */
    void stopRead(final OnLIDARLiteListener listener);


    /**
     * LIDARLite v3からのデータを受け取るリスナー.
     */
    interface OnLIDARLiteListener {
        /**
         * 距離センサー開始成功通知を受け取ります.
         */
        void onStarted();

        /**
         * 距離センサーを受け取ります.
         * @param distance 距離
         */
        void onData(final int distance);

        /**
         * 距離センサーでエラーが発生したことを受け取ります.
         * @param message エラーメッセージ
         */
        void onError(final String message);
    }
}
