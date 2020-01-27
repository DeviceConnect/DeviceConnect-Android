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
     * オーバーレイ削除用アクションを定義.
     */
    String DELETE_PREVIEW_ACTION = "org.deviceconnect.android.deviceplugin.host.DELETE_PREVIEW";

    /**
     * カメラを識別するIDのキー名を定義.
     */
    String EXTRA_CAMERA_ID = "cameraId";

    /**
     * パーミッション結果通知用コールバック.
     */
    interface PermissionCallback {
        /**
         * 許可された場合に呼び出されます.
         */
        void onAllowed();

        /**
         * 拒否された場合に呼び出されます.
         */
        void onDisallowed();
    }

    /**
     * パーミッションの要求結果を通知するコールバックを設定します.
     *
     * @param callback コールバック
     */
    void requestPermission(PermissionCallback callback);

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
     * 全てのサーバを停止します.
     */
    void stopWebServers();
}
