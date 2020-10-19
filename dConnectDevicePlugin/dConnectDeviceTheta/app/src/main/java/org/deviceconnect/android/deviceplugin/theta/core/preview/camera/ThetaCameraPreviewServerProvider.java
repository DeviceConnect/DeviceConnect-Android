/*
 ThetaCameraPreviewServerProvider.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.core.preview.camera;

import org.deviceconnect.android.deviceplugin.theta.core.ThetaDevice;
import org.deviceconnect.android.deviceplugin.theta.core.preview.AbstractPreviewServer;
import org.deviceconnect.android.deviceplugin.theta.core.preview.AbstractPreviewServerProvider;
import org.deviceconnect.android.deviceplugin.theta.core.preview.PreviewServer;

import java.util.List;

/**
 * カメラのプレビュー配信用サーバを管理するクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaCameraPreviewServerProvider extends AbstractPreviewServerProvider {
    /**
     * コンストラクタ.
     *
     * @param recorder レコーダ
     */
    public ThetaCameraPreviewServerProvider(final ThetaDevice recorder) {
        super();
        addServer(new ThetaCameraMJPEGPreviewServer(false, recorder, 9100));
        addServer(new ThetaCameraMJPEGPreviewServer(true, recorder, 9110));
    }

    @Override
    public List<PreviewServer> startServers() {
        return super.startServers();
    }

    @Override
    public void stopServers() {
        super.stopServers();
    }

    @Override
    public void onConfigChange() {
        super.onConfigChange();
    }

}
