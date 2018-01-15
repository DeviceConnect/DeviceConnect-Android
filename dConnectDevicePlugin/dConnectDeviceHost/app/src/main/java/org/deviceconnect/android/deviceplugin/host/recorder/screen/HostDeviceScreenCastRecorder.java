/*
 HostDeviceScreenCast.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.hardware.display.VirtualDisplay;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.util.DisplayMetrics;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDevicePhotoRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.PreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.util.MixedReplaceMediaServer;
import org.deviceconnect.android.provider.FileManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Host Device Screen Cast.
 *
 * @author NTT DOCOMO, INC.
 */
@TargetApi(21)
public class HostDeviceScreenCastRecorder extends AbstractPreviewServerProvider implements HostDevicePhotoRecorder {

    static final String RESULT_DATA = "result_data";

    static final String EXTRA_CALLBACK = "callback";

    private static final String ID = "screen";

    private static final String NAME = "AndroidHost Screen";

    private static final String MIME_TYPE = "video/x-mjpeg";

    private static final double DEFAULT_MAX_FPS = 10.0d;

    /** ファイル名に付けるプレフィックス. */
    private static final String FILENAME_PREFIX = "android_screen_";

    /** ファイルの拡張子. */
    private static final String FILE_EXTENSION = ".jpg";

    /** 日付のフォーマット. */
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyyMMdd_kkmmss", Locale.JAPAN);

    /**
     * マイムタイプ一覧を定義.
     */
    private List<String> mMimeTypes = new ArrayList<String>() {
        {
            add(MIME_TYPE);
        }
    };

    private final List<PictureSize> mSupportedPreviewSizes = new ArrayList<>();
    private final List<PictureSize> mSupportedPictureSizes = new ArrayList<>();
    private final Context mContext;
    private final Logger mLogger = Logger.getLogger("host.dplugin");
    private final ScreenCastManager mScreenCastMgr;
    private final ScreenCastRTSPPreviewServer mScreenCastRTSPServer;
    //private final MixedReplaceMediaServer mServer;

    private FileManager mFileMgr;
    private PictureSize mPreviewSize;
    private PictureSize mPictureSize;
    private long mFrameInterval;
    private double mMaxFps = DEFAULT_MAX_FPS;
    private RecorderState mState = RecorderState.INACTTIVE;
    private BroadcastReceiver mConfigChangeReceiver;

    public HostDeviceScreenCastRecorder(final Context context, final FileManager fileMgr) {
        super(context, 2001);
        mContext = context;
        mFileMgr = fileMgr;

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        PictureSize size = new PictureSize(metrics.widthPixels, metrics.heightPixels);
        initSupportedPreviewSizes(size);

        setMaxFrameRate(mMaxFps);

        mScreenCastMgr = new ScreenCastManager(context);
        mScreenCastRTSPServer = new ScreenCastRTSPPreviewServer(context, this, mScreenCastMgr);
    }

    private void initSupportedPreviewSizes(final PictureSize originalSize) {
        final int num = 4;
        final int w = originalSize.getWidth();
        final int h = originalSize.getHeight();
        mSupportedPreviewSizes.clear();
        for (int i = 1; i <= num; i++) {
            float scale = i / ((float) num);
            PictureSize previewSize = new PictureSize((int) (w * scale), (int) (h * scale));
            mSupportedPreviewSizes.add(previewSize);
            mSupportedPictureSizes.add(previewSize);
        }
        mPreviewSize = mSupportedPreviewSizes.get(0);
        mPictureSize = mSupportedPictureSizes.get(num - 1);
    }

    @Override
    public List<PreviewServer> getServers() {
        List<PreviewServer> servers = new ArrayList<>();
        servers.add(mScreenCastRTSPServer);
        return servers;
    }

    @Override
    public void initialize() {
        // Nothing to do.
    }

    @Override
    public void clean() {
        stopWebServers();
    }

    @Override
    public void stopWebServers() {
        super.stopWebServers();
        mScreenCastMgr.clean();
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public RecorderState getState() {
        return mState;
    }

    @Override
    public PictureSize getPictureSize() {
        return mPictureSize;
    }

    @Override
    public void setPictureSize(final PictureSize size) {
        mPictureSize = size;
    }

    @Override
    public PictureSize getPreviewSize() {
        return mPreviewSize;
    }

    @Override
    public synchronized void setPreviewSize(final PictureSize size) {
        mPreviewSize = size;
    }

    @Override
    public double getMaxFrameRate() {
        return mMaxFps;
    }

    @Override
    public void setMaxFrameRate(double frameRate) {
        mMaxFps = frameRate;
        mFrameInterval =  1000L / (long) mMaxFps;
    }

    @Override
    public int getPreviewBitRate() {
        return 0; // TODO ビットレートの取得
    }

    @Override
    public void setPreviewBitRate(int bitRate) {
        // TODO ビットレートの設定
    }

    @Override
    public List<PictureSize> getSupportedPreviewSizes() {
        return mSupportedPreviewSizes;
    }

    @Override
    public List<PictureSize> getSupportedPictureSizes() {
        return mSupportedPictureSizes;
    }

    @Override
    public List<String> getSupportedMimeTypes() {
        return mMimeTypes;
    }

    @Override
    public boolean isSupportedPictureSize(int width, int height) {
        if (mSupportedPictureSizes != null) {
            for (PictureSize size : mSupportedPictureSizes) {
                if (width == size.getWidth() && height == size.getHeight()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isSupportedPreviewSize(int width, int height) {
        if (mSupportedPreviewSizes != null) {
            for (PictureSize size : mSupportedPreviewSizes) {
                if (width == size.getWidth() && height == size.getHeight()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isBack() {
        return false;
    }

    @Override
    public void turnOnFlashLight() {

    }

    @Override
    public void turnOffFlashLight() {

    }

    @Override
    public boolean isFlashLightState() {
        return false;
    }

    @Override
    public boolean isUseFlashLight() {
        return false;
    }

    @Override
    public void takePhoto(final OnPhotoEventListener listener) {
        mScreenCastMgr.takePhoto(listener);
    }

}
