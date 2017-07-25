package org.deviceconnect.android.deviceplugin.fabo.device;

/**
 * MPL115を操作するためのインターフェース.
 */
public interface IMPL115 {
    /**
     * 気圧センサーの値を取得します.
     * @param listener 値を受け取るリスナー
     */
    void readAtmosphericPressure(final OnAtmosphericPressureListener listener);

    /**
     * 気圧センサーの値を受け取るリスナー.
     */
    interface OnAtmosphericPressureListener {
        /**
         * 気圧の値を受け取ります.
         * @param hpa 気圧(ヘクトパスカル)
         * @param temperature 温度
         */
        void onData(final double hpa, final double temperature);

        /**
         * 気圧の取得に失敗通知を受け取ります.
         * @param message エラーメッセージ
         */
        void onError(final String message);
    }

}
