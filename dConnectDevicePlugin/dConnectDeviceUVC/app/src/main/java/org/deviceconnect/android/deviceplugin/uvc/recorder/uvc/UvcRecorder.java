package org.deviceconnect.android.deviceplugin.uvc.recorder.uvc;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.uvc.recorder.AbstractMediaRecorder;
import org.deviceconnect.android.deviceplugin.uvc.recorder.BroadcasterProvider;
import org.deviceconnect.android.deviceplugin.uvc.recorder.MediaRecorder;
import org.deviceconnect.android.deviceplugin.uvc.recorder.PreviewServerProvider;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;
import org.deviceconnect.android.libuvc.Parameter;
import org.deviceconnect.android.libuvc.UVCCamera;

import java.io.IOException;

public abstract class UvcRecorder extends AbstractMediaRecorder {
    private final UVCCamera mUVCCamera;
    private final UvcSettings mSettings;
    private final UvcSurfaceDrawingThread mUvcSurfaceDrawingThread;
    private final UvcBroadcasterProvider mUvcBroadcasterProvider;
    private final UvcPreviewServerProvider mUvcPreviewServerProvider;

    public UvcRecorder(Context context, UVCCamera camera) {
        super(context);

        if (camera == null) {
            throw new IllegalArgumentException("UVCCamera is null.");
        }

        mUVCCamera = camera;
        mSettings = createSettings();
        mUvcSurfaceDrawingThread = new UvcSurfaceDrawingThread(this);
        mUvcBroadcasterProvider = new UvcBroadcasterProvider(context, this);
        mUvcPreviewServerProvider = new UvcPreviewServerProvider(context, this);
    }

    public UVCCamera getUVCCamera() {
        return mUVCCamera;
    }

    protected abstract UvcSettings createSettings();

    @Override
    public Settings getSettings() {
        return mSettings;
    }

    @Override
    public BroadcasterProvider getBroadcasterProvider() {
        return mUvcBroadcasterProvider;
    }

    @Override
    public PreviewServerProvider getServerProvider() {
        return mUvcPreviewServerProvider;
    }

    @Override
    public EGLSurfaceDrawingThread getSurfaceDrawingThread() {
        return mUvcSurfaceDrawingThread;
    }

    @Override
    public void requestPermission(MediaRecorder.PermissionCallback callback) {
    }

    public String getSettingsName() {
        return mUVCCamera.getDeviceId() + "-" + getId();
    }

    public class UvcSettings extends MediaRecorder.Settings {

        public UvcSettings(Context context) {
            super(context, getSettingsName());
        }

        /**
         * 設定されているパラメータに一致するパラメータを取得します.
         *
         * @return パラメータ
         * @throws IOException カメラ情報の取得に失敗した場合に例外が発生
         */
        public Parameter getParameter() throws IOException {
            return null;
        }
    }
}
