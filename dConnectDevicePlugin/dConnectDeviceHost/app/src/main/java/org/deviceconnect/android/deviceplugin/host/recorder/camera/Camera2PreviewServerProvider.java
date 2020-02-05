/*
 Camera2PreviewServerProvider.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.util.RecorderSettingData;

/**
 * カメラのプレビュー配信用サーバを管理するクラス.
 *
 * @author NTT DOCOMO, INC.
 */
class Camera2PreviewServerProvider extends AbstractPreviewServerProvider {
    /**
     * Notification の識別子を定義.
     *
     * <p>
     * カメラは、前と後ろが存在するので、BASE_NOTIFICATION_ID + カメラIDのハッシュ値 を識別子とします。
     * </p>
     */
    private static final int BASE_NOTIFICATION_ID = 1001;

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     * @param recorder レコーダ
     */
    Camera2PreviewServerProvider(final Context context, final Camera2Recorder recorder) {
        super(context, recorder, BASE_NOTIFICATION_ID + recorder.getId().hashCode());

        Camera2MJPEGPreviewServer mjpegServer = new Camera2MJPEGPreviewServer(context, recorder);
        mjpegServer.setQuality(RecorderSettingData.getInstance(getContext()).readPreviewQuality(recorder.getId()));
        addServer(mjpegServer);
        addServer(new Camera2RTSPPreviewServer(context, recorder));
        addServer(new Camera2SRTPreviewServer(context, recorder));
    }
}
