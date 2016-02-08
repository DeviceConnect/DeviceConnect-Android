package org.deviceconnect.android.deviceplugin.webrtc.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.AttributeSet;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.webrtc.BuildConfig;
import org.deviceconnect.android.deviceplugin.webrtc.util.MixedReplaceMediaServer;
import org.webrtc.EglBase;
import org.webrtc.GlShader;
import org.webrtc.GlUtil;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class MySurfaceViewRenderer extends SurfaceViewRenderer {

    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "WebRTC";

    private YuvConverter mYuvConverter;

    private ByteBuffer mByteBuffer;
    private MixedReplaceMediaServer mServer;

    private boolean mReleased;

    public MySurfaceViewRenderer(Context context) {
        super(context);
    }

    public MySurfaceViewRenderer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void release() {
        mReleased = true;
        mByteBuffer = null;
        mServer.stop();
        mYuvConverter.release();
    }

    public void createYuvConvertor(EglBase.Context context, int port) {
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

    @Override
    public void renderFrame(VideoRenderer.I420Frame frame) {
        if (mReleased) {
            return;
        }

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

        super.renderFrame(frame);
    }

    private int[] mTestBuffer;

    private void convertYuvToRGB(VideoRenderer.I420Frame frame) {
        if (mTestBuffer == null || mTestBuffer.length != frame.width * frame.height) {
            mTestBuffer = new int[frame.width * frame.height];
        }
/*
        mYuvConverter.test(mTestBuffer, frame.width, frame.height, frame.width, frame.textureId, frame.yuvStrides, frame.yuvPlanes, frame.samplingMatrix);

        Bitmap bitmap = Bitmap.createBitmap(frame.width, frame.height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(mTestBuffer, 0, frame.width, 0, 0, frame.width, frame.height);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
        mServer.offerMedia(out.toByteArray());
*/

        if (frame.yuvPlanes == null || frame.yuvPlanes[0] == null) {
            return;
        }

        android.graphics.YuvImage remoteImage = ConvertTo(frame.width, frame.height, frame.yuvStrides, frame.yuvPlanes);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        remoteImage.compressToJpeg(new Rect(0, 0, frame.width, frame.height), 50, out);
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

    private static void copyPlane(final ByteBuffer src, final ByteBuffer dst) {
        src.position(0).limit(src.capacity());
        dst.put(src);
        dst.position(0).limit(dst.capacity());
    }

    public static android.graphics.YuvImage ConvertTo(final int width, final int height, final int[] yuvStrides, final ByteBuffer[] yuvPlanes) {

        if (yuvStrides[0] != width)
            return convertLineByLine(width, height, yuvStrides, yuvPlanes);
        if (yuvStrides[1] != width/2)
            return convertLineByLine(width, height, yuvStrides, yuvPlanes);
        if (yuvStrides[2] != width/2)
            return convertLineByLine(width, height, yuvStrides, yuvPlanes);

        byte[] bytes = new byte[yuvStrides[0] * height +
                yuvStrides[1] * height / 2 +
                yuvStrides[2] * height / 2];
        ByteBuffer tmp = ByteBuffer.wrap(bytes, 0, width*height);
        copyPlane(yuvPlanes[0], tmp);

        byte[] tmparray = new byte[width / 2 * height / 2];
        tmp = ByteBuffer.wrap(tmparray, 0, width / 2 * height / 2);

        copyPlane(yuvPlanes[2], tmp);
        for (int row = 0; row < height / 2; row++) {
            for (int col = 0; col < width / 2; col++) {
                bytes[width * height + row * width + col * 2] = tmparray[row * width / 2 + col];
            }
        }
        copyPlane(yuvPlanes[1], tmp);
        for (int row = 0; row < height / 2; row++) {
            for (int col = 0; col < width / 2; col++) {
                bytes[width * height + row * width + col * 2 + 1] = tmparray[row * width / 2 + col];
            }
        }
        return new YuvImage(bytes, android.graphics.ImageFormat.NV21, width, height, null);
    }

    public static android.graphics.YuvImage convertLineByLine(final int width, final int height, final int[] yuvStrides, final ByteBuffer[] yuvPlanes) {
        byte[] bytes = new byte[width * height * 3 / 2];
        byte[] yuvPlanes0 = yuvPlanes[0].array();
        byte[] yuvPlanes1 = yuvPlanes[1].array();
        byte[] yuvPlanes2 = yuvPlanes[2].array();

        int i = 0;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                bytes[i++] = yuvPlanes0[col + row * yuvStrides[0]];
            }
        }
        for (int row = 0; row < height / 2; row++) {
            for (int col = 0; col < width / 2; col++) {
                bytes[i++] = yuvPlanes2[col + row * yuvStrides[2]];
                bytes[i++] = yuvPlanes1[col + row * yuvStrides[1]];
            }
        }
        return new YuvImage(bytes, android.graphics.ImageFormat.NV21, width, height, null);
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
                mServer.offerMedia(out.toByteArray());
            }
        }
    }

    public Bitmap getBitmapImageFromYUV(byte[] data, int width, int height) {
        YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yuvimage.compressToJpeg(new Rect(0, 0, width, height), 80, baos);
        byte[] jdata = baos.toByteArray();
        BitmapFactory.Options bitmapFatoryOptions = new BitmapFactory.Options();
        bitmapFatoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bmp = BitmapFactory.decodeByteArray(jdata, 0, jdata.length, bitmapFatoryOptions);
        return bmp;
    }

    private static class YuvConverter {
        private final EglBase eglBase;
        private final GlShader shader;
        private boolean released = false;

        // Vertex coordinates in Normalized Device Coordinates, i.e.
        // (-1, -1) is bottom-left and (1, 1) is top-right.
        private static final FloatBuffer DEVICE_RECTANGLE =
                GlUtil.createFloatBuffer(new float[] {
                        -1.0f, -1.0f,  // Bottom left.
                        1.0f, -1.0f,  // Bottom right.
                        -1.0f,  1.0f,  // Top left.
                        1.0f,  1.0f,  // Top right.
                });

        // Texture coordinates - (0, 0) is bottom-left and (1, 1) is top-right.
        private static final FloatBuffer TEXTURE_RECTANGLE =
                GlUtil.createFloatBuffer(new float[] {
                        0.0f, 0.0f,  // Bottom left.
                        1.0f, 0.0f,  // Bottom right.
                        0.0f, 1.0f,  // Top left.
                        1.0f, 1.0f   // Top right.
                });

        private static final String VERTEX_SHADER =
                "varying vec2 interp_tc;\n"
                        + "attribute vec4 in_pos;\n"
                        + "attribute vec4 in_tc;\n"
                        + "\n"
                        + "uniform mat4 texMatrix;\n"
                        + "\n"
                        + "void main() {\n"
                        + "    gl_Position = in_pos;\n"
                        + "    interp_tc = (texMatrix * in_tc).xy;\n"
                        + "}\n";

        private static final String FRAGMENT_SHADER =
                "#extension GL_OES_EGL_image_external : require\n"
                        + "precision mediump float;\n"
                        + "varying vec2 interp_tc;\n"
                        + "\n"
                        + "uniform samplerExternalOES oesTex;\n"
                        // Difference in texture coordinate corresponding to one
                        // sub-pixel in the x direction.
                        + "uniform vec2 xUnit;\n"
                        // Color conversion coefficients, including constant term
                        + "uniform vec4 coeffs;\n"
                        + "\n"
                        + "void main() {\n"
                        // Since the alpha read from the texture is always 1, this could
                        // be written as a mat4 x vec4 multiply. However, that seems to
                        // give a worse framerate, possibly because the additional
                        // multiplies by 1.0 consume resources. TODO(nisse): Could also
                        // try to do it as a vec3 x mat3x4, followed by an add in of a
                        // constant vector.
                        + "  gl_FragColor.r = coeffs.a + dot(coeffs.rgb,\n"
                        + "      texture2D(oesTex, interp_tc - 1.5 * xUnit).rgb);\n"
                        + "  gl_FragColor.g = coeffs.a + dot(coeffs.rgb,\n"
                        + "      texture2D(oesTex, interp_tc - 0.5 * xUnit).rgb);\n"
                        + "  gl_FragColor.b = coeffs.a + dot(coeffs.rgb,\n"
                        + "      texture2D(oesTex, interp_tc + 0.5 * xUnit).rgb);\n"
                        + "  gl_FragColor.a = coeffs.a + dot(coeffs.rgb,\n"
                        + "      texture2D(oesTex, interp_tc + 1.5 * xUnit).rgb);\n"
                        + "}\n";

        private int texMatrixLoc;
        private int xUnitLoc;
        private int coeffsLoc;

        YuvConverter (EglBase.Context sharedContext) {
            eglBase = EglBase.create(sharedContext, EglBase.CONFIG_PIXEL_RGBA_BUFFER);
            eglBase.createDummyPbufferSurface();
            eglBase.makeCurrent();

            shader = new GlShader(VERTEX_SHADER, FRAGMENT_SHADER);
            shader.useProgram();
            texMatrixLoc = shader.getUniformLocation("texMatrix");
            xUnitLoc = shader.getUniformLocation("xUnit");
            coeffsLoc = shader.getUniformLocation("coeffs");
            GLES20.glUniform1i(shader.getUniformLocation("oesTex"), 0);
            GlUtil.checkNoGLES2Error("Initialize fragment shader uniform values.");
            // Initialize vertex shader attributes.
            shader.setVertexAttribArray("in_pos", 2, DEVICE_RECTANGLE);
            // If the width is not a multiple of 4 pixels, the texture
            // will be scaled up slightly and clipped at the right border.
            shader.setVertexAttribArray("in_tc", 2, TEXTURE_RECTANGLE);
            eglBase.detachCurrent();
        }

        private void test(int[] buf,
                  int width, int height, int stride, int textureId, int[] strides, ByteBuffer[] planes, float[] transformMatrix) {

            if (planes == null) {
                Log.e("ABC", "AAAAAA planes = null.");
                // TODO
                return;
            }
/*
            Log.e("ABC", "AAAAAA width, height = (" + width + ", " + height + ")");
            Log.e("ABC", "AAAAAA strides[0] = " + strides[0]);
            Log.e("ABC", "AAAAAA strides[1] = " + strides[1]);
            Log.e("ABC", "AAAAAA strides[2] = " + strides[2]);
            Log.e("ABC", "AAAAAA stride = " + stride);
            Log.e("ABC", "AAAAAA planes[0] = " + planes[0].limit());
            Log.e("ABC", "AAAAAA planes[1] = " + planes[1].limit());
            Log.e("ABC", "AAAAAA planes[2] = " + planes[2].limit());
            Log.e("ABC", "AAAAAA mBuffer = " + buf.length);
*/
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int index1 = y * width + x;
                    int index2 = index1 / 4;
                    int yy = planes[0].get(index1);
                    int uu = planes[1].get(index2);
                    int vv = planes[2].get(index2);

                    int y1192 = 1192 * yy;
                    int r = (y1192 + 1634 * vv);
                    int g = (y1192 - 833 * vv - 400 * uu);
                    int b = (y1192 + 2066 * uu);

                    if (r < 0) {
                        r = 0;
                    } else if (r > 262143) {
                        r = 262143;
                    }
                    if (g < 0) {
                        g = 0;
                    } else if (g > 262143) {
                        g = 262143;
                    }
                    if (b < 0) {
                        b = 0;
                    } else if (b > 262143) {
                        b = 262143;
                    }

                    buf[y * width + x] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
                }
            }
        }



        private synchronized void convert(ByteBuffer buf,
                                  int width, int height, int stride, int textureId, float[] transformMatrix) {
            if (released) {
                throw new IllegalStateException(
                        "YuvConverter.convert called on released object");
            }

            // We draw into a buffer laid out like
            //
            //    +---------+
            //    |         |
            //    |  Y      |
            //    |         |
            //    |         |
            //    +----+----+
            //    | U  | V  |
            //    |    |    |
            //    +----+----+
            //
            // In memory, we use the same stride for all of Y, U and V. The
            // U data starts at offset |height| * |stride| from the Y data,
            // and the V data starts at at offset |stride/2| from the U
            // data, with rows of U and V data alternating.
            //
            // Now, it would have made sense to allocate a pixel buffer with
            // a single byte per pixel (EGL10.EGL_COLOR_BUFFER_TYPE,
            // EGL10.EGL_LUMINANCE_BUFFER,), but that seems to be
            // unsupported by devices. So do the following hack: Allocate an
            // RGBA buffer, of width |stride|/4. To render each of these
            // large pixels, sample the texture at 4 different x coordinates
            // and store the results in the four components.
            //
            // Since the V data needs to start on a boundary of such a
            // larger pixel, it is not sufficient that |stride| is even, it
            // has to be a multiple of 8 pixels.

            if (stride % 8 != 0) {
                throw new IllegalArgumentException(
                        "Invalid stride, must be a multiple of 8");
            }
            if (stride < width){
                throw new IllegalArgumentException(
                        "Invalid stride, must >= width");
            }

            int y_width = (width+3) / 4;
            int uv_width = (width+7) / 8;
            int uv_height = (height+1)/2;
            int total_height = height + uv_height;
            int size = stride * total_height;

            if (buf.capacity() < size) {
                throw new IllegalArgumentException("YuvConverter.convert called with too small buffer");
            }
            // Produce a frame buffer starting at top-left corner, not
            // bottom-left.
            transformMatrix =
                    RendererCommon.multiplyMatrices(transformMatrix,
                            RendererCommon.verticalFlipMatrix());

            // Create new pBuffferSurface with the correct size if needed.
            if (eglBase.hasSurface()) {
                if (eglBase.surfaceWidth() != stride/4 ||
                        eglBase.surfaceHeight() != total_height){
                    eglBase.releaseSurface();
                    eglBase.createPbufferSurface(stride/4, total_height);
                }
            } else {
                eglBase.createPbufferSurface(stride/4, total_height);
            }

            eglBase.makeCurrent();

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
            GLES20.glUniformMatrix4fv(texMatrixLoc, 1, false, transformMatrix, 0);

            // Draw Y
            GLES20.glViewport(0, 0, y_width, height);
            // Matrix * (1;0;0;0) / width. Note that opengl uses column major order.
            GLES20.glUniform2f(xUnitLoc,
                    transformMatrix[0] / width,
                    transformMatrix[1] / width);
            // Y'UV444 to RGB888, see
            // https://en.wikipedia.org/wiki/YUV#Y.27UV444_to_RGB888_conversion.
            // We use the ITU-R coefficients for U and V */
            GLES20.glUniform4f(coeffsLoc, 0.299f, 0.587f, 0.114f, 0.0f);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

            // Draw U
            GLES20.glViewport(0, height, uv_width, uv_height);
            // Matrix * (1;0;0;0) / (2*width). Note that opengl uses column major order.
            GLES20.glUniform2f(xUnitLoc,
                    transformMatrix[0] / (2.0f*width),
                    transformMatrix[1] / (2.0f*width));
            GLES20.glUniform4f(coeffsLoc, -0.169f, -0.331f, 0.499f, 0.5f);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

            // Draw V
            GLES20.glViewport(stride/8, height, uv_width, uv_height);
            GLES20.glUniform4f(coeffsLoc, 0.499f, -0.418f, -0.0813f, 0.5f);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

            GLES20.glReadPixels(0, 0, stride/4, total_height, GLES20.GL_RGBA,
                    GLES20.GL_UNSIGNED_BYTE, buf);

            GlUtil.checkNoGLES2Error("YuvConverter.convert");

            // Unbind texture. Reportedly needed on some devices to get
            // the texture updated from the camera.
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
            eglBase.detachCurrent();
        }

        synchronized void release() {
            released = true;
            eglBase.makeCurrent();
            shader.release();
            eglBase.release();
        }
    }

}
