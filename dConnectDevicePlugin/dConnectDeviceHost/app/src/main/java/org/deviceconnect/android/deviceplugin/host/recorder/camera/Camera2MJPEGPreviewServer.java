/*
 CameraPreviewServer.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractMJPEGPreviewServer;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGEncoder;

/**
 * カメラのプレビューをMJPEG形式で配信するサーバー.
 */
class Camera2MJPEGPreviewServer extends AbstractMJPEGPreviewServer {
    Camera2MJPEGPreviewServer(Context context, Camera2Recorder recorder, boolean useSSL) {
        super(context, recorder, useSSL);
        setPort(recorder.getSettings().getPort(getMimeType()) + (useSSL ?  1 : 0));
    }

    @Override
    protected MJPEGEncoder createSurfaceMJPEGEncoder() {
        return new CameraMJPEGEncoder((Camera2Recorder) getRecorder());
    }
}
