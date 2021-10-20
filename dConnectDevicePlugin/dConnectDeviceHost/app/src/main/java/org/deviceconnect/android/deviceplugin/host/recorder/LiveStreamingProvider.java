package org.deviceconnect.android.deviceplugin.host.recorder;

import java.util.List;

public interface LiveStreamingProvider {
    /**
     * 配信用のサーバを作成します.
     *
     * @param encoderId サーバの識別子
     * @param encoderSettings エンコード設定
     * @return サーバ
     */
    LiveStreaming createLiveStreaming(String encoderId, HostMediaRecorder.EncoderSettings encoderSettings);

    /**
     * 追加します.
     *
     * @param liveStreaming 追加するプレビュー配信サーバ
     */
    void addLiveStreaming(LiveStreaming liveStreaming);

    /**
     * 削除します.
     *
     * @param encoderId サーバの識別子
     */
    void removeLiveStreaming(String encoderId);

    /**
     * サポートしているプレビュー配信用サーバのリストを取得します.
     * @return プレビュー配信用サーバのリスト
     */
    List<LiveStreaming> getLiveStreamingList();

    /**
     * プレビューで配信するマイムタイプを取得します.
     *
     * @return プレビューで配信するマイムタイプ
     */
    List<String> getSupportedMimeType();

    /**
     * プレビューサーバが動作している確認します.
     *
     * @return 動作中の場合は true、それ以外は false
     */
    boolean isRunning();

    /**
     * 全てのプレビュー配信サーバを開始します.
     *
     * レスポンスのリストが空の場合には、全てのプレビュー配信サーバの起動に失敗しています。
     *
     * @return 起動に成功したプレビュー配信サーバのリスト
     */
    List<LiveStreaming> start();

    /**
     * 全てのプレビュー配信サーバを停止します.
     */
    void stop();

    /**
     * 映像のエンコーダーに対して sync frame の即時生成を要求する.
     */
    void requestSyncFrame();

    /**
     * 映像のエンコーダーに対してビットレートの更新を要求する。
     */
    void requestBitRate();

    /**
     * 映像のエンコーダーに対して JPEG の品質の更新を要求する。
     */
    void requestJpegQuality();

    /**
     * 設定が変更されたことを通知します.
     */
    void onConfigChange();

    /**
     * Recorder をミュート状態にする.
     */
    void setMute(boolean mute);

    /**
     * イベントを通知するリスナーを設定します.
     *
     * @param listener リスナー
     */
    void setOnEventListener(OnEventListener listener);

    /**
     * 配信用のサーバを開放します.
     */
    void release();

    /**
     * プレビュー配信サーバのイベントを通知するリスナー.
     */
    interface OnEventListener {
        /**
         * プレビュー配信サーバを開始したことを通知します.
         *
         * @param servers 開始したサーバのリスト
         */
        void onStarted(List<LiveStreaming> servers);

        /**
         * プレビュー配信サーバを停止したことを通知します.
         */
        void onStopped();

        /**
         * プレビュー配信サーバでエラーが発生したことを通知します.
         *
         * @param server エラーが発生したサーバ
         * @param e エラー原因の例外
         */
        void onError(LiveStreaming server, Exception e);
    }
}
