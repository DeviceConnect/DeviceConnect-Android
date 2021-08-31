package org.deviceconnect.android.deviceplugin.host.recorder.audio;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;

public class AudioPreviewServer extends AbstractPreviewServer {
    /**
     * コンストラクタ.
     *
     * @param context  コンテキスト
     * @param recorder プレビューで表示するレコーダ
     */
    public AudioPreviewServer(Context context, HostMediaRecorder recorder) {
        super(context, recorder, recorder.getId() + "-audio");
    }

    @Override
    public String getMimeType() {
        return null;
    }

    @Override
    public String getUri() {
        return null;
    }

    @Override
    public void startWebServer(OnWebServerStartCallback callback) {
    }

    @Override
    public void stopWebServer() {
    }

    @Override
    public boolean requestSyncFrame() {
        return false;
    }

    @Override
    public long getBPS() {
        return 0;
    }
}
