/*
 ScreenCastSRTPreviewServer.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractSRTPreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;

/**
 * スクリーンキャストを SRT で配信するサーバー.
 *
 * @author NTT DOCOMO, INC.
 */
class ScreenCastSRTPreviewServer extends AbstractSRTPreviewServer {
    ScreenCastSRTPreviewServer(final Context context, final ScreenCastRecorder recorder, final int port) {
        super(context, recorder);
        setPort(port);
    }

    @Override
    protected VideoEncoder createVideoEncoder() {
        ScreenCastRecorder recorder = (ScreenCastRecorder) getRecorder();
        HostMediaRecorder.Settings settings = recorder.getSettings();
        switch (settings.getPreviewEncoderName()) {
            case H264:
            default:
                return new ScreenCastVideoEncoder(recorder);
            case H265:
                return new ScreenCastVideoEncoder(recorder, "video/hevc");
        }
    }
}
