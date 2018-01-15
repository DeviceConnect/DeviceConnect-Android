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

    void requestPermission(PermissionCallback callback);

    interface PermissionCallback {
        void onAllowed();
        void onDisallowed();
    }

    List<PreviewServer> getServers();

    void stopWebServers();

}
