package org.deviceconnect.android.deviceplugin.host.recorder.screen;


import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.view.Surface;


@TargetApi(21)
class ImageScreenCast extends AbstractScreenCast {

    private final ImageReader mImageReader;

    ImageScreenCast(final Context context,
                    final MediaProjection mediaProjection,
                    final ImageReader imageReader,
                    int width, int height) {
        super(context, mediaProjection, width, height);
        mImageReader = imageReader;
    }

    @Override
    protected Surface getSurface() {
        return mImageReader.getSurface();
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

        Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height,
                Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(planes[0].getBuffer());
        img.close();

        return Bitmap.createBitmap(bitmap, 0, 0, width, height, null, true);
    }
}
