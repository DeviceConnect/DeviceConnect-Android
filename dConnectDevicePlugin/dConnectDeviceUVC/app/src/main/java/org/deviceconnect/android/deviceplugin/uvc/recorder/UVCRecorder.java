package org.deviceconnect.android.deviceplugin.uvc.recorder;

import org.deviceconnect.android.deviceplugin.uvc.core.UVCDevice;
import org.deviceconnect.android.deviceplugin.uvc.core.UVCDeviceManager;
import org.deviceconnect.android.deviceplugin.uvc.recorder.preview.MJPEGPreviewServer;
import org.deviceconnect.android.deviceplugin.uvc.recorder.preview.PreviewServer;
import org.deviceconnect.android.deviceplugin.uvc.recorder.preview.RTSPPreviewServer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;

public class UVCRecorder implements MediaRecorder {

    private static final String RECORDER_ID = "0";
    private static final String RECORDER_MIME_TYPE_MJPEG = "video/x-mjpeg";

    private final List<PreviewServer> mPreviewServers = new ArrayList<>();
    private final UVCDeviceManager mDeviceMgr;
    private UVCDevice mDevice;

    /**
     * コンストラクタ.
     *
     * @param manager ファイル管理クラス
     * @param device UVCカメラ
     */
    public UVCRecorder(final UVCDeviceManager manager, final UVCDevice device) {
        mDeviceMgr = manager;
        mDevice = device;
    }

    public void setDevice(UVCDevice device) {
        mDevice = device;
    }

    @Override
    public void initialize() {
        mPreviewServers.clear();
        mPreviewServers.add(new MJPEGPreviewServer(mDeviceMgr, mDevice, 40000));
        mPreviewServers.add(new RTSPPreviewServer(mDeviceMgr, mDevice, 40001));
    }

    @Override
    public void clean() {
        stopPreview();
    }

    @Override
    public String getId() {
        return RECORDER_ID;
    }

    @Override
    public String getName() {
        return mDevice.getName();
    }

    @Override
    public String getMimeType() {
        return RECORDER_MIME_TYPE_MJPEG;
    }

    @Override
    public void setMimeType(String mimeType) {
    }

    @Override
    public State getState() {
        return null;
    }

    @Override
    public Size getPictureSize() {
        return null;
    }

    @Override
    public void setPictureSize(Size size) {
    }

    @Override
    public Size getPreviewSize() {
        return new Size(mDevice.getPreviewWidth(), mDevice.getPreviewHeight());
    }

    @Override
    public void setPreviewSize(Size size) {
        mDevice.setPreviewSize(size.getWidth(), size.getHeight());
    }

    @Override
    public double getMaxFrameRate() {
        return mDevice.getFrameRate();
    }

    @Override
    public void setMaxFrameRate(double frameRate) {
        mDevice.setPreviewFrameRate(frameRate);
    }

    @Override
    public int getPreviewBitRate() {
        return 0;
    }

    @Override
    public void setPreviewBitRate(int bitRate) {
    }

    @Override
    public List<Size> getSupportedPictureSizes() {
        List<Size> result = new ArrayList<>();
        for (UVCDevice.PreviewOption option : mDevice.getPreviewOptions()) {
            result.add(new Size(option.getWidth(), option.getHeight()));
        }
        return result;
    }

    @Override
    public List<Size> getSupportedPreviewSizes() {
        List<Size> result = new ArrayList<>();
        for (UVCDevice.PreviewOption option : mDevice.getPreviewOptions()) {
            result.add(new Size(option.getWidth(), option.getHeight()));
        }
        return result;
    }

    @Override
    public List<String> getSupportedMimeTypes() {
        List<String> result = new ArrayList<>();
        result.add(RECORDER_MIME_TYPE_MJPEG);
        for (PreviewServer server : mPreviewServers) {
            result.add(server.getMimeType());
        }
        return result;
    }

    @Override
    public void takePhoto(OnPhotoEventListener listener) {
    }

    @Override
    public List<PreviewServer> getServers() {
        return mPreviewServers;
    }

    @Override
    public List<PreviewServer> startPreview() {
        List<PreviewServer> results = new ArrayList<>();

        CountDownLatch lock = new CountDownLatch(mPreviewServers.size());
        for (PreviewServer server : mPreviewServers) {
            server.start(new PreviewServer.OnWebServerStartCallback() {
                @Override
                public void onStart(@NonNull String uri) {
                    results.add(server);
                    lock.countDown();
                }

                @Override
                public void onFail() {
                    lock.countDown();
                }
            });
        }
        try {
            if (!lock.await(10, TimeUnit.SECONDS)) {
                // TODO タイムアウト処理
            }
        } catch (Exception e) {
            // ignore.
        }
        return results;
    }

    @Override
    public void stopPreview() {
        for (PreviewServer server : mPreviewServers) {
            server.stop();
        }
    }

    @Override
    public boolean isStartedPreview() {
        for (PreviewServer server : mPreviewServers) {
            if (server.isStarted()) {
                return true;
            }
        }
        return false;
    }
}
