/*
 PreviewServerProvider.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.core.preview;


import java.util.List;

public interface PreviewServerProvider {

    /**
     * カメラを識別するIDのキー名を定義.
     */
    String EXTRA_CAMERA_ID = "cameraId";

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
     * <p>
     * マイムタイプに対応したプレビュー配信サーバが存在しない場合は null を返却します。
     * </p>
     * @param mimeType マイムタイプ
     * @return プレビュー配信サーバ
     */
    PreviewServer getServerForMimeType(String mimeType);

    /**
     * 全てのサーバを開始します.
     *
     * @return 起動したプレビュー配信サーバのリスト
     */
    List<PreviewServer> startServers();

    /**
     * 全てのサーバを停止します.
     */
    void stopServers();


    /**
     * 設定が変更されたことを通知します.
     */
    void onConfigChange();

}
