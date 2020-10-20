/*
 ThetaCameraPreviewServerProvider.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.core.preview.omni;

import org.deviceconnect.android.deviceplugin.theta.core.preview.AbstractPreviewServerProvider;
import org.deviceconnect.android.deviceplugin.theta.core.preview.PreviewServer;
import org.deviceconnect.android.deviceplugin.theta.core.preview.omni.projector.Projector;

import java.util.List;

/**
 * カメラのプレビュー配信用サーバを管理するクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public class OmnidirectionalImagePreviewServerProvider extends AbstractPreviewServerProvider {
    /**
     * コンストラクタ.
     */
    public OmnidirectionalImagePreviewServerProvider(Projector projector) {
        super();

        addServer(new OmnidirectionalImageMJPEGPreviewServer(projector, false, 9200));
        addServer(new OmnidirectionalImageMJPEGPreviewServer(projector, true,  9210));
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
