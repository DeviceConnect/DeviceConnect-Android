package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.ImageReader;
import android.os.Build;
import android.view.Surface;
import android.view.WindowManager;

import org.deviceconnect.android.deviceplugin.host.recorder.util.MediaProjectionProvider;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class ScreenCastManager {
    private final Context mContext;
    private final WindowManager mWindowManager;
    private final MediaProjectionProvider mMediaProjectionProvider;

    ScreenCastManager(Context context, MediaProjectionProvider client) {
        mContext = context;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mMediaProjectionProvider = client;
    }

    /**
     * 画面が回転して解像度のスワップが必要か確認します.
     *
     * @return 解像度のスワップが必要な場合はtrue、それ以外はfalse
     */
    boolean isSwappedDimensions() {
        switch (getDisplayRotation()) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                return false;
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
            default:
                return true;
        }
    }

    /**
     * 画面の回転を取得します.
     *
     * @return 画面の回転
     */
    int getDisplayRotation() {
        return mWindowManager.getDefaultDisplay().getRotation();
    }

    /**
     * MediaProjection の後始末を行います.
     */
    synchronized void clean() {
        mMediaProjectionProvider.stop();
    }

    /**
     * Surface に端末の画面をキャストするクラスを作成します.
     *
     * @param outputSurface キャスト先の Surface
     * @param width         キャスト先の Surface の横幅
     * @param height        キャスト先の Surface の縦幅
     * @return SurfaceScreenCast のインスタンス
     */
    SurfaceScreenCast createScreenCast(final Surface outputSurface, int width, int height) {
        if (mMediaProjectionProvider.getMediaProjection() == null) {
            throw new IllegalStateException("Media Projection is not allowed.");
        }
        return new SurfaceScreenCast(mContext, mMediaProjectionProvider.getMediaProjection(), outputSurface, width, height);
    }

    /**
     * ImageReader に端末の画面をキャストするクラスを作成します.
     *
     * @param imageReader キャスト先の ImageReader
     * @param width       キャスト先の ImageReader の横幅
     * @param height      キャスト先の ImageReader の縦幅
     * @return ImageScreenCast のインスタンス
     */
    ImageScreenCast createScreenCast(final ImageReader imageReader, int width, int height) {
        if (mMediaProjectionProvider.getMediaProjection() == null) {
            throw new IllegalStateException("Media Projection is not allowed.");
        }
        return new ImageScreenCast(mContext, mMediaProjectionProvider.getMediaProjection(), imageReader, width, height);
    }
}
