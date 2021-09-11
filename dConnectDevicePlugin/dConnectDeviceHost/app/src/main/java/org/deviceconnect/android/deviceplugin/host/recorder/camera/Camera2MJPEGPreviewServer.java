/*
 CameraPreviewServer.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractMJPEGPreviewServer;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGEncoder;

/**
 * カメラのプレビューをMJPEG形式で配信するサーバー.
 */
class Camera2MJPEGPreviewServer extends AbstractMJPEGPreviewServer {
    Camera2MJPEGPreviewServer(Camera2Recorder recorder, String encoderId) {
        super(recorder, encoderId);
    }

    @Override
    protected MJPEGEncoder createSurfaceMJPEGEncoder() {
        return new CameraMJPEGEncoder((Camera2Recorder) getRecorder());
    }
}
