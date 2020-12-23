package org.deviceconnect.android.deviceplugin.host.recorder;

public interface Broadcaster {
    /**
     * マイムタイプを取得します.
     *
     * @return マイムタイプ
     */
    String getMimeType();

    /**
     * ブロードキャスト先の URI を取得します.
     *
     * @return ブロードキャスト先の URI
     */
    String getBroadcastURI();

    /**
     * ブロードキャストのイベントを通知するリスナーを設定します.
     *
     * @param listener リスナー
     */
    void setOnEventListener(OnEventListener listener);

    /**
     * ブロードキャスト中か確認します.
     *
     * @return ブロードキャスト中の場合は true、それ以外は false
     */
    boolean isRunning();

    /**
     * ブロードキャストを開始します.
     */
    void start(OnStartCallback callback);

    /**
     * ブロードキャストを停止します.
     */
    void stop();

    /**
     * ミュート設定を行います.
     *
     * @param mute ミュートにする場合にはtrue、それ以外はfalse
     */
    void setMute(boolean mute);

    /**
     * ミュート設定を取得します.
     *
     * @return ミュートの場合はtrue、それ以外はfalse
     */
    boolean isMute();

    /**
     * 設定が変更されたことを通知します.
     */
    void onConfigChange();

    interface OnStartCallback {
        void onSuccess();
        void onFailed(Exception e);
    }

    interface OnEventListener {
        void onStarted();
        void onStopped();
        void onError(Exception e);
    }
}
