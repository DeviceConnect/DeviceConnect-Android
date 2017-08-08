package org.deviceconnect.android.deviceplugin.fabo.device;

import org.deviceconnect.android.deviceplugin.fabo.param.FaBoShield;

/**
 * FaBoデバイスを操作するためのインターフェース.
 */
public interface FaBoDeviceControl {
    /**
     *  FaBoデバイスの初期化を行います.
     */
    void initialize();

    /**
     * FaBoデバイスとの接続を破棄します.
     */
    void destroy();

    /**
     * 指定されたピンがサポートされているか確認を行います.
     * @param pin サポートされているか確認するピン
     * @return サポートされている場合はtrue、それ以外はfalse
     */
    boolean isPinSupported(final FaBoShield.Pin pin);

    /**
     * アナログピンに対して書き込みを行います.
     * @param pin アナログピン
     * @param value 書き込む値
     */
    void writeAnalog(final FaBoShield.Pin pin, final int value);

    /**
     * デジタルピンに対して書き込みを行います.
     * @param pin デジタルピン
     * @param hl 書き込む値(HIGH or LOW)
     */
    void writeDigital(final FaBoShield.Pin pin, final FaBoShield.Level hl);

    /**
     * アナログピンのデータを取得します.
     * @param pin アナログピン
     * @return アナログ値
     */
    int getAnalog(final FaBoShield.Pin pin);

    /**
     * デジタルピンのデータを取得します.
     * @param pin デジタルピン
     * @return デジタル値
     */
    FaBoShield.Level getDigital(final FaBoShield.Pin pin);

    /**
     * 各PINのモードを設定します.
     * @param pin ピン
     * @param mode モード
     */
    void setPinMode(final FaBoShield.Pin pin, final FaBoShield.Mode mode);

    /**
     * FaBoデバイスの接続状態を取得します.
     * @return 接続状態
     */
    int getStatus();

    /**
     * RobotCarを操作するためのインターフェースを取得します.
     * @return IRobotCarを実装したクラス
     */
    IRobotCar getRobotCar();

    /**
     * MouseCarを操作するためのインターフェースを取得します.
     * @return IMouseCarを実装したクラス
     */
    IMouseCar getMouseCar();

    /**
     * ADXL345を操作するためのインターフェースを取得します.
     * @return IADXL345を実装したクラス
     */
    IADXL345 getADXL345();

    /**
     * ADT7410を操作するためのインターフェースを取得します.
     * @return IADT7410を実装したクラス
     */
    IADT7410 getADT7410();

    /**
     * HTS221を操作するためのインターフェースを取得します.
     * @return IHTS221を実装したクラス
     */
    IHTS221 getHTS221();

    /**
     * VCNL4010を操作するためのインターフェースを取得します.
     * @return VCNL4010を実装したクラス
     */
    IVCNL4010 getVCNL4010();

    /**
     * ISL29034を操作するためのインターフェースを取得します.
     * @return ISL29034を実装したクラス
     */
    IISL29034 getISL29034();

    /**
     * MPL115を操作するためのインターフェースを取得します.
     * @return MPL115を実装したクラス
     */
    IMPL115 getMPL115();

    /**
     * LIDARLite v3を操作するためのインターフェースを取得します.
     * @return ILIDARLiteV3を実装したクラス
     */
    ILIDARLiteV3 getLIDARLite();

    /**
     * FaBoデバイスの接続状態通知リスナーを設定します.
     * @param listener リスナー
     */
    void setOnFaBoDeviceControlListener(final OnFaBoDeviceControlListener listener);

    /**
     * GPIOのイベント通知リスナーを追加します.
     * @param listener 追加するリスナー
     */
    void addOnGPIOListener(final OnGPIOListener listener);

    /**
     * GPIOのイベント通知リスナーを削除します.
     * @param listener 削除するリスナー
     */
    void removeOnGPIOListener(final OnGPIOListener listener);

    /**
     * FaBoデバイスの接続状態を通知するためのリスナー.
     */
    interface  OnFaBoDeviceControlListener {
        /**
         * FaBoが接続された時のイベントを通知します.
         */
        void onConnected();

        /**
         * FaBoが切断された時のイベントを通知します.
         */
        void onDisconnected();

        /**
         * 接続に失敗した時のイベントを通知します.
         */
        void onFailedConnected();
    }

    /**
     * GPIOのイベントを通知するリスナー.
     */
    interface OnGPIOListener {
        /**
         * アナログのデータを受信したことを通知します.
         */
        void onAnalog();

        /**
         * デジタルのデータを受信したことを通知します.
         */
        void onDigital();
    }
}
