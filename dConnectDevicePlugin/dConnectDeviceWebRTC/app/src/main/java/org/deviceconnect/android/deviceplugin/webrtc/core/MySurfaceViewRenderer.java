package org.deviceconnect.android.deviceplugin.webrtc.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.AttributeSet;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.webrtc.BuildConfig;
import org.deviceconnect.android.deviceplugin.webrtc.util.MixedReplaceMediaServer;
import org.deviceconnect.android.deviceplugin.webrtc.util.YuvConverter;
import org.webrtc.EglBase;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class MySurfaceViewRenderer extends SurfaceViewRenderer {

    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "WebRTC";

    private YuvConverter mYuvConverter;

    private ByteBuffer mByteBuffer;
    private MixedReplaceMediaServer mServer;

    private int mFrameHeight = 0;
    private int mFrameWidth = 0;

    private boolean mReleased;

    public MySurfaceViewRenderer(Context context) {
        super(context);
    }

    public MySurfaceViewRenderer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void release() {
        if (mReleased) {
            return;
        }

        mReleased = true;
        mByteBuffer = null;

        if (mServer != null) {
            mServer.stop();
            mServer = null;
        }

        if (mYuvConverter != null) {
            mYuvConverter.release();
            mYuvConverter = null;
        }
    }

    public void createYuvConverter(EglBase.Context context, int port) {
        mYuvConverter = new YuvConverter(context);
        mServer = new MixedReplaceMediaServer();
        mServer.setPort(port);
        String url = mServer.start();
        if (DEBUG) {
            Log.i(TAG, "url is " + url);
        }
    }

    public String getUrl() {
        return mServer.getUrl();
    }

    public int getFrameHeight() {
        return mFrameHeight;
    }

    public int getFrameWidth() {
        return mFrameWidth;
    }

    public String getMimeType() {
        return mServer.getMimeType();
    }

    @Override
    public void renderFrame(VideoRenderer.I420Frame frame) {
        if (mReleased) {
            return;
        }

        if (mServer != null && mYuvConverter != null) {
            if (frame != null) {
                if (!frame.yuvFrame) {
                    convertTextureToYUV(frame);
                } else {
                    convertYuvToRGB(frame);
                }
            } else {
                if (DEBUG) {
                    Log.e(TAG, "renderFrame: frame is null.");
                }
            }
        }

        super.renderFrame(frame);
    }

    private void convertYuvToRGB(VideoRenderer.I420Frame frame) {
        if (frame.yuvPlanes == null || frame.yuvPlanes[0] == null) {
            return;
        }

        YuvImage remoteImage = mYuvConverter.convertToYuvImage(frame.width, frame.height, frame.yuvStrides, frame.yuvPlanes);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        remoteImage.compressToJpeg(new Rect(0, 0, frame.width, frame.height), 50, out);
        mFrameHeight = frame.height;
        mFrameWidth = frame.width;
        mServer.offerMedia(out.toByteArray());

        if (frame.yuvPlanes[0] != null) {
            frame.yuvPlanes[0].rewind();
        }
        if (frame.yuvPlanes[1] != null) {
            frame.yuvPlanes[1].rewind();
        }
        if (frame.yuvPlanes[2] != null) {
            frame.yuvPlanes[2].rewind();
        }
    }

    private void convertTextureToYUV(VideoRenderer.I420Frame frame) {
        if (mYuvConverter != null) {
            if (mByteBuffer == null) {
                int uv_height = (frame.height + 1) / 2;
                int total_height = frame.height + uv_height;
                int size = frame.width * total_height;
                mByteBuffer = ByteBuffer.allocate(size);
            }

            mYuvConverter.convert(mByteBuffer, frame.width, frame.height, frame.width, frame.textureId, frame.samplingMatrix);

            Bitmap bitmap = getBitmapImageFromYUV(mByteBuffer.array(), frame.width, frame.height);
            if (bitmap != null) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
                mFrameHeight = frame.height;
                mFrameWidth = frame.width;
                mServer.offerMedia(out.toByteArray());
            }
        }
    }

    public Bitmap getBitmapImageFromYUV(byte[] data, int width, int height) {
        YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yuvimage.compressToJpeg(new Rect(0, 0, width, height), 50, baos);
        byte[] jdata = baos.toByteArray();
        BitmapFactory.Options bitmapFatoryOptions = new BitmapFactory.Options();
        bitmapFatoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeByteArray(jdata, 0, jdata.length, bitmapFatoryOptions);
    }

}
