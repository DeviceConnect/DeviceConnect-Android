/*
 ScreenCastSRTPreviewServer.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractSRTPreviewServer;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;

/**
 * スクリーンキャストを SRT で配信するサーバー.
 *
 * @author NTT DOCOMO, INC.
 */
class ScreenCastSRTPreviewServer extends AbstractSRTPreviewServer {
    ScreenCastSRTPreviewServer(ScreenCastRecorder recorder, String encoderId) {
        super(recorder, encoderId);
    }

    @Override
    protected VideoEncoder createVideoEncoder() {
        ScreenCastRecorder recorder = (ScreenCastRecorder) getRecorder();
        switch (getEncoderSettings().getPreviewEncoderName()) {
            case H264:
            default:
                return new ScreenCastVideoEncoder(recorder);
            case H265:
                return new ScreenCastVideoEncoder(recorder, "video/hevc");
        }
    }
}
