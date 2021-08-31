/*
 Camera2PreviewServerProvider.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.content.Context;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.PreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.util.OverlayManager;

import java.util.List;

/**
 * カメラのプレビュー配信用サーバを管理するクラス.
 *
 * @author NTT DOCOMO, INC.
 */
class Camera2PreviewServerProvider extends AbstractPreviewServerProvider {
    /**
     * オーバーレイを管理するクラス.
     */
    private final OverlayManager mOverlayManager;

    /**
     * コンストラクタ.
     *
     * @param context  コンテキスト
     * @param recorder レコーダ
     */
    Camera2PreviewServerProvider(final Context context, final Camera2Recorder recorder) {
        super(context, recorder);

        mOverlayManager = new OverlayManager(context, recorder);

        List<String> previewList = recorder.getSettings().getPreviewServerList();
        for (String name : previewList) {
            HostMediaRecorder.StreamingSettings s = recorder.getSettings().getPreviewServer(name);
            if (s != null) {
                String mimeType = s.getMimeType();
                if ("video/x-mjpeg".equalsIgnoreCase(mimeType)) {
                    addServer(new Camera2MJPEGPreviewServer(context, recorder, false));
                } else if ("video/x-rtp".equalsIgnoreCase(mimeType)) {
                    addServer(new Camera2RTSPPreviewServer(context, recorder));
                } else if ("video/MP2T".equalsIgnoreCase(mimeType)) {
                    addServer(new Camera2SRTPreviewServer(context, recorder));
                }
            }
        }
    }

    @Override
    public List<PreviewServer> startServers() {
        List<PreviewServer> servers = super.startServers();
        if (!servers.isEmpty()) {
            mOverlayManager.registerBroadcastReceiver();
        }
        return servers;
    }

    @Override
    public void stopServers() {
        mOverlayManager.destroy();
        super.stopServers();
    }

    @Override
    public void onConfigChange() {
        super.onConfigChange();
        mOverlayManager.onConfigChange();
    }
}
