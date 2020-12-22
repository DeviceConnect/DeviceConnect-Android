/*
 PreviewServerProvider.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder;

import java.util.List;

public interface PreviewServerProvider {
    /**
     * プレビューで配信するマイムタイプを取得します.
     *
     * @return プレビューで配信するマイムタイプ
     */
    List<String> getSupportedMimeType();

    /**
     * サポートしているプレビュー配信サーバを追加します.
     *
     * @param server 追加するプレビュー配信サーバ
     */
    void addServer(PreviewServer server);

    /**
     * サポートしているプレビュー配信用サーバのリストを取得します.
     * @return プレビュー配信用サーバのリスト
     */
    List<PreviewServer> getServers();

    /**
     * 指定されたマイムタイプに対応するプレビュー配信サーバを取得します.
     *
     * <p>
     * マイムタイプに対応したプレビュー配信サーバが存在しない場合は null を返却します。
     * </p>
     *
     * @param mimeType マイムタイプ
     * @return プレビュー配信サーバ
     */
    PreviewServer getServerByMimeType(String mimeType);

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
    List<PreviewServer> startServers();

    /**
     * 全てのプレビュー配信サーバを停止します.
     */
    void stopServers();

    /**
     * 全てのサーバの映像のエンコーダーに対して sync frame の即時生成を要求する.
     *
     * @return 実際に即時生成を受け付けたサーバのリスト
     */
    List<PreviewServer> requestSyncFrame();

    /**
     * 設定が変更されたことを通知します.
     */
    void onConfigChange();

    /**
     * Recorder をミュート状態にする.
     */
    void setMute(boolean mute);
}
