package org.deviceconnect.android.deviceplugin.host.recorder.screen;


import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.WindowManager;

import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceRecorder;


@TargetApi(21)
class ImageScreenCast extends AbstractScreenCast {

    private final ImageReader mImageReader;

    ImageScreenCast(final Context context,
                    final MediaProjection mediaProjection,
                    final ImageReader imageReader,
                    final HostDeviceRecorder.PictureSize size) {
        super(context, mediaProjection, size);
        mImageReader = imageReader;
    }

    @Override
    protected VirtualDisplay createVirtualDisplay() {
        HostDeviceRecorder.PictureSize size = mDisplaySize;
        int w = size.getWidth();
        int h = size.getHeight();

        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        if (dm.widthPixels > dm.heightPixels) {
            if (w < h) {
                w = size.getHeight();
                h = size.getWidth();
            }
        } else {
            if (w > h) {
                w = size.getHeight();
                h = size.getWidth();
            }
        }

        return mMediaProjection.createVirtualDisplay(
                "Android Host Screen",
                w,
                h,
                mDisplayDensityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(),
                getDisplayCallback(),
                new Handler(Looper.getMainLooper()));
    }

    synchronized Bitmap getScreenshot() {
        try {
            if (mImageReader == null) {
                return null;
            }
            Image image = mImageReader.acquireLatestImage();
            if (image == null) {
                return null;
            }
            return decodeToBitmap(image);
        } catch (Exception e) {
            return null;
        }
    }

    private Bitmap decodeToBitmap(final Image img) {
        Image.Plane[] planes = img.getPlanes();
        if (planes[0].getBuffer() == null) {
            return null;
        }

        int width = img.getWidth();
        int height = img.getHeight();

        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;

        Bitmap bitmap = Bitmap.createBitmap(
                width + rowPadding / pixelStride, height,
                Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(planes[0].getBuffer());
        img.close();

        return Bitmap.createBitmap(bitmap, 0, 0, width, height, null, true);
    }

}
