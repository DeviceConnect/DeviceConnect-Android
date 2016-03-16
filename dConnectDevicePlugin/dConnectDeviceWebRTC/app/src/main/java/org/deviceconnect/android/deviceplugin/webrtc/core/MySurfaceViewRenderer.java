package org.deviceconnect.android.deviceplugin.webrtc.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;

import org.deviceconnect.android.deviceplugin.webrtc.BuildConfig;
import org.deviceconnect.android.deviceplugin.webrtc.util.ImageUtils;
import org.deviceconnect.android.deviceplugin.webrtc.util.MixedReplaceMediaServer;
import org.deviceconnect.android.deviceplugin.webrtc.util.YuvConverter;
import org.webrtc.EglBase;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MySurfaceViewRenderer extends SurfaceViewRenderer {

    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "WebRTC";

    public static final String TYPE_LOCAL = "local";
    public static final String TYPE_REMOTE = "remote";

    private String mType = null;

    private YuvConverter mYuvConverter;

    private ByteBuffer mByteBuffer;
    private MixedReplaceMediaServer mServer = null;

    private int mFrameHeight = 0;
    private int mFrameWidth = 0;

    private boolean mReleased;

    private Bitmap mBitmap;
    private ByteArrayOutputStream mOutputStream = new ByteArrayOutputStream();

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

        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }

        if (mYuvConverter != null) {
            mYuvConverter.release();
            mYuvConverter = null;
        }
    }

    public void createYuvConverter(EglBase.Context context) {
        mYuvConverter = new YuvConverter(context);
    }

    public String getUrl() {
        if (mServer == null) {
            return null;
        }
        return mServer.getUrl(mType);
    }

    public int getFrameHeight() {
        return mFrameHeight;
    }

    public int getFrameWidth() {
        return mFrameWidth;
    }

    public String getMimeType() {
        if (mServer == null) {
            return null;
        }
        return mServer.getMimeType();
    }

    public void setType(final String type) {
        mType = type;
    }

    public void setWebServer(final MixedReplaceMediaServer server) {
        mServer = server;
    }

    @Override
    public void renderFrame(VideoRenderer.I420Frame frame) {
        if (mReleased) {
            return;
        }

        if (mServer != null && mYuvConverter != null) {
            if (!frame.yuvFrame) {
                convertTextureToYUV(frame);
            } else {
                convertYuvToRGB(frame);
            }
        }

        super.renderFrame(frame);
    }

    private void convertYuvToRGB(VideoRenderer.I420Frame frame) {
        if (frame.yuvPlanes == null || frame.yuvPlanes[0] == null) {
            return;
        }

        if (mBitmap == null || mBitmap.getWidth() != frame.width || mBitmap.getHeight() != frame.height) {
            mBitmap = Bitmap.createBitmap(frame.width, frame.height, Bitmap.Config.ARGB_8888);
        }

        ImageUtils.decodeYUV420SP3(mBitmap, frame.yuvPlanes, frame.width, frame.height, frame.yuvStrides);

        mOutputStream.reset();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 20, mOutputStream);
        mFrameHeight = frame.height;
        mFrameWidth = frame.width;
        mServer.offerMedia(mType, mOutputStream.toByteArray());
    }

    private void convertTextureToYUV(VideoRenderer.I420Frame frame) {
        if (mByteBuffer == null) {
            int uv_height = (frame.height + 1) / 2;
            int total_height = frame.height + uv_height;
            int size = frame.width * total_height;
            mByteBuffer = ByteBuffer.allocateDirect(size);
            mByteBuffer.order(ByteOrder.nativeOrder());
        }

        if (mBitmap == null || mBitmap.getWidth() != frame.width || mBitmap.getHeight() != frame.height) {
            mBitmap = Bitmap.createBitmap(frame.width, frame.height, Bitmap.Config.ARGB_8888);
        }

        mYuvConverter.convert(mByteBuffer, frame.width, frame.height, frame.width, frame.textureId, frame.samplingMatrix);

        ImageUtils.decodeYUV420SP2(mBitmap, mByteBuffer, frame.width, frame.height);

        mOutputStream.reset();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 20, mOutputStream);
        mFrameHeight = frame.height;
        mFrameWidth = frame.width;
        mServer.offerMedia(mType, mOutputStream.toByteArray());
    }
}
