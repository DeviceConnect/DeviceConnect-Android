#include <jni.h>
#include <math.h>

#include <android/log.h>
#include <android/bitmap.h>

#ifdef __cplusplus
extern "C" {
#endif /* __cplusplus */

static jint roundUp(jint x, jint alignment) {
    return (jint) ceil(x / (double) alignment) * alignment;
}

JNIEXPORT void JNICALL Java_org_deviceconnect_android_deviceplugin_webrtc_util_ImageUtils_nativeEncodeYV12
        (JNIEnv *env, jclass clazz, jbyteArray yuv420spArray, jintArray argbArray, jint width, jint height) {

    jboolean b;
    jbyte* yuv420sp = env->GetByteArrayElements(yuv420spArray, &b);
    jint* argb = env->GetIntArrayElements(argbArray, &b);

    int frameSize = width * height;
    int yStride = roundUp(width, 16);
    int uvStride = roundUp(yStride / 2, 16);
    int uvSize = uvStride * height / 2;

    int yIndex = 0;
    int uIndex = frameSize;
    int vIndex = frameSize + uvSize;

    int diff = (uvStride - width / 2) / 2;

    int R, G, B, Y, U, V;
    int index = 0;
    for (int j = 0; j < height; j++) {
        for (int i = 0; i < width; i++) {

            R = (argb[index] & 0xff0000) >> 16;
            G = (argb[index] & 0xff00) >> 8;
            B = (argb[index] & 0xff);

            Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
            U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
            V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

            yuv420sp[yIndex++] = (jbyte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
            if (j % 2 == 0 && index % 2 == 0) {
                yuv420sp[uIndex++] = (jbyte) ((V < 0) ? 0 : ((V > 255) ? 255 : V));
                yuv420sp[vIndex++] = (jbyte) ((U < 0) ? 0 : ((U > 255) ? 255 : U));
            }

            index++;
        }
        vIndex += diff;
        uIndex += diff;
    }

    env->ReleaseByteArrayElements(yuv420spArray, yuv420sp, 0);
    env->ReleaseIntArrayElements(argbArray, argb, 0);
}


JNIEXPORT void JNICALL Java_org_deviceconnect_android_deviceplugin_webrtc_util_ImageUtils_nativeDecodeYUV420SP
        (JNIEnv *env, jclass clazz, jobject bitmap, jbyteArray yuv420spArray, jint width, jint height) {
    AndroidBitmapInfo info;
    int *pixels;
    int ret;
    int i, j;

    if (bitmap == NULL) {
        return;
    }

    if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
        return;
    }

    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        return;
    }

    if ((ret = AndroidBitmap_lockPixels(env, bitmap, (void **) &pixels)) < 0) {
        return;
    }

    jboolean b;
    jbyte *yuv420sp = env->GetByteArrayElements(yuv420spArray, &b);

    jint frameSize = width * height;
    for (int j = 0, yp = 0; j < height; j++) {
        int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
        for (int i = 0; i < width; i++, yp++) {
            int y = (0xff & ((int) yuv420sp[yp])) - 16;
            if (y < 0) y = 0;
            if ((i & 1) == 0) {
                v = (0xff & yuv420sp[uvp++]) - 128;
                u = (0xff & yuv420sp[uvp++]) - 128;
            }
            int y1192 = 1192 * y;
            int r = (y1192 + 1634 * v);
            int g = (y1192 - 833 * v - 400 * u);
            int b = (y1192 + 2066 * u);
            if (r < 0) r = 0;
            else if (r > 262143) r = 262143;
            if (g < 0) g = 0;
            else if (g > 262143) g = 262143;
            if (b < 0) b = 0;
            else if (b > 262143) b = 262143;
            pixels[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
        }
    }

    env->ReleaseByteArrayElements(yuv420spArray, yuv420sp, 0);

    AndroidBitmap_unlockPixels(env, bitmap);
}

JNIEXPORT void JNICALL Java_org_deviceconnect_android_deviceplugin_webrtc_util_ImageUtils_nativeDecodeYUV420SP2
    (JNIEnv *env, jclass clazz, jobject bitmap, jobject buffer, jint width, jint height) {
    AndroidBitmapInfo info;
    int *pixels;
    int ret;
    int i, j;

    if (bitmap == NULL) {
        return;
    }

    if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
        return;
    }

    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        return;
    }

    if ((ret = AndroidBitmap_lockPixels(env, bitmap, (void **) &pixels)) < 0) {
        return;
    }

    jbyte *yuv420sp = (jbyte *)env->GetDirectBufferAddress(buffer);

    jint frameSize = width * height;
    for (int j = 0, yp = 0; j < height; j++) {
        int up = frameSize + (j >> 1) * width + width / 2;
        int vp = frameSize + (j >> 1) * width;
        int u = 0, v = 0;
        for (int i = 0; i < width; i++, yp++) {
            int y = (0xff & ((int) yuv420sp[yp])) - 16;
            if (y < 0) y = 0;
            if ((i & 1) == 0) {
                v = (0xff & yuv420sp[vp++]) - 128;
                u = (0xff & yuv420sp[up++]) - 128;
            }
            int y1192 = 1192 * y;
            int r = (y1192 + 1634 * v);
            int g = (y1192 - 833 * v - 400 * u);
            int b = (y1192 + 2066 * u);
            if (r < 0) r = 0;
            else if (r > 262143) r = 262143;
            if (g < 0) g = 0;
            else if (g > 262143) g = 262143;
            if (b < 0) b = 0;
            else if (b > 262143) b = 262143;
            pixels[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
        }
    }

    AndroidBitmap_unlockPixels(env, bitmap);
}

JNIEXPORT void JNICALL Java_org_deviceconnect_android_deviceplugin_webrtc_util_ImageUtils_nativeDecodeYUV420SP3
       (JNIEnv *env, jclass clazz, jobject bitmap, jobjectArray buffers, jint width, jint height) {
    AndroidBitmapInfo info;
    int *pixels;
    int ret;
    int i, j;

    if (bitmap == NULL) {
        return;
    }

    if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
        return;
    }

    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        return;
    }

    if ((ret = AndroidBitmap_lockPixels(env, bitmap, (void **) &pixels)) < 0) {
        return;
    }

    jobject buffer0 = env->GetObjectArrayElement(buffers, 0);
    jobject buffer1 = env->GetObjectArrayElement(buffers, 1);
    jobject buffer2 = env->GetObjectArrayElement(buffers, 2);

    jbyte *ydata = (jbyte *) env->GetDirectBufferAddress(buffer0);
    jbyte *vdata = (jbyte *) env->GetDirectBufferAddress(buffer1);
    jbyte *udata = (jbyte *) env->GetDirectBufferAddress(buffer2);

    jint frameSize = width * height;
    for (int j = 0, yp = 0; j < height; j++) {
        int up = (j >> 2) * width;
        int vp = (j >> 2) * width;
        int u = 0, v = 0;
        for (int i = 0; i < width; i++, yp++) {
            int y = (0xff & ((int) ydata[yp])) - 16;
            if (y < 0) y = 0;
            if ((i & 1) == 0) {
                v = (0xff & vdata[vp++]) - 128;
                u = (0xff & udata[up++]) - 128;
            }
            int y1192 = 1192 * y;
            int r = (y1192 + 1634 * v);
            int g = (y1192 - 833 * v - 400 * u);
            int b = (y1192 + 2066 * u);
            if (r < 0) r = 0;
            else if (r > 262143) r = 262143;
            if (g < 0) g = 0;
            else if (g > 262143) g = 262143;
            if (b < 0) b = 0;
            else if (b > 262143) b = 262143;
            pixels[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
        }
    }

    env->DeleteLocalRef(buffer0);
    env->DeleteLocalRef(buffer1);
    env->DeleteLocalRef(buffer2);

    AndroidBitmap_unlockPixels(env, bitmap);
}

JNIEXPORT void JNICALL Java_org_deviceconnect_android_deviceplugin_webrtc_util_ImageUtils_nativeDecodeYUV420SP4
    (JNIEnv *env, jclass clazz, jobject bitmap, jobjectArray buffers, jint width, jint height, jintArray stridesArray) {
    AndroidBitmapInfo info;
    int *pixels;
    int ret;
    int i, j;

    if (bitmap == NULL) {
        return;
    }

    if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
        return;
    }

    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        return;
    }

    if ((ret = AndroidBitmap_lockPixels(env, bitmap, (void **) &pixels)) < 0) {
        return;
    }

    jobject buffer0 = env->GetObjectArrayElement(buffers, 0);
    jobject buffer1 = env->GetObjectArrayElement(buffers, 1);
    jobject buffer2 = env->GetObjectArrayElement(buffers, 2);

    jbyte *ydata = (jbyte *) env->GetDirectBufferAddress(buffer0);
    jbyte *vdata = (jbyte *) env->GetDirectBufferAddress(buffer1);
    jbyte *udata = (jbyte *) env->GetDirectBufferAddress(buffer2);

    jboolean b;
    jint *strides = env->GetIntArrayElements(stridesArray, &b);

    jint frameSize = width * height;
    for (int j = 0, yp = 0; j < height; j++) {
        int up = (j >> 2) * strides[2];
        int vp = (j >> 2) * strides[1];
        int u = 0, v = 0;
        for (int i = 0; i < width; i++, yp++) {
            int y = (0xff & ((int) ydata[i + j * strides[0]])) - 16;
            if (y < 0) y = 0;
            if ((i & 1) == 0) {
                v = (0xff & vdata[vp++]) - 128;
                u = (0xff & udata[up++]) - 128;
            }
            int y1192 = 1192 * y;
            int r = (y1192 + 1634 * v);
            int g = (y1192 - 833 * v - 400 * u);
            int b = (y1192 + 2066 * u);
            if (r < 0) r = 0;
            else if (r > 262143) r = 262143;
            if (g < 0) g = 0;
            else if (g > 262143) g = 262143;
            if (b < 0) b = 0;
            else if (b > 262143) b = 262143;
            pixels[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
        }
    }

    env->ReleaseIntArrayElements(stridesArray, strides, 0);

    env->DeleteLocalRef(buffer0);
    env->DeleteLocalRef(buffer1);
    env->DeleteLocalRef(buffer2);

    AndroidBitmap_unlockPixels(env, bitmap);
}


#ifdef __cplusplus
}
#endif /* __cplusplus */
