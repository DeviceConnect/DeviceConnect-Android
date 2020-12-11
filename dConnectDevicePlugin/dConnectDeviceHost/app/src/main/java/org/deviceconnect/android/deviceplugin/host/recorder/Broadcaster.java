package org.deviceconnect.android.deviceplugin.host.recorder;

public interface Broadcaster {
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
    void setOnBroadcasterEventListener(OnBroadcasterEventListener listener);

    /**
     * ブロードキャスト中か確認します.
     *
     * @return ブロードキャスト中の場合は true、それ以外は false
     */
    boolean isRunning();

    /**
     * ブロードキャストを開始します.
     */
    void start();

    /**
     * ブロードキャストを停止します.
     */
    void stop();

    interface OnBroadcasterEventListener {
        void onStarted();
        void onStopped();
        void onError(Exception e);
    }
}
