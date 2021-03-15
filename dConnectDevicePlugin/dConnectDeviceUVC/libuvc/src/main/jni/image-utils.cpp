/*
 image-utils.cpp
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
#include <jni.h>
#include <unistd.h>
#include <malloc.h>
#include <math.h>
#include <string.h>
#include <android/bitmap.h>

#include "common.h"

/**
 * JNI のパッケージ名、クラス名を定義.
 */
#define JNI_METHOD_NAME(name) \
    Java_org_deviceconnect_android_libuvc_ImageUtils_##name


#ifdef __cplusplus
extern "C" {
#endif


/**
 * JPEGヘッダーで使用されるハフマンテーブルのサイズ.
 */
static const size_t huff_tbl_size = 432;

/**
 * JPEGヘッダーで使用されるハフマンテーブル.
 */
static const uint8_t huff_tbl[huff_tbl_size] = {
        // dc_huff_tbl_0
        0xff, 0xc4, 0x00, 0x1f, 0x00, 0x00, 0x01, 0x05, 0x01, 0x01,
        0x01, 0x01, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
        0x09, 0x0a, 0x0b,

        // ac_huff_tbl_0
        0xff, 0xc4, 0x00, 0xb5, 0x10, 0x00, 0x02, 0x01, 0x03, 0x03,
        0x02, 0x04, 0x03, 0x05, 0x05, 0x04, 0x04, 0x00, 0x00, 0x01,
        0x7d, 0x01, 0x02, 0x03, 0x00, 0x04, 0x11, 0x05, 0x12, 0x21,
        0x31, 0x41, 0x06, 0x13, 0x51, 0x61, 0x07, 0x22, 0x71, 0x14,
        0x32, 0x81, 0x91, 0xa1, 0x08, 0x23, 0x42, 0xb1, 0xc1, 0x15,
        0x52, 0xd1, 0xf0, 0x24, 0x33, 0x62, 0x72, 0x82, 0x09, 0x0a,
        0x16, 0x17, 0x18, 0x19, 0x1a, 0x25, 0x26, 0x27, 0x28, 0x29,
        0x2a, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3a, 0x43, 0x44,
        0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x53, 0x54, 0x55, 0x56,
        0x57, 0x58, 0x59, 0x5a, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68,
        0x69, 0x6a, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7a,
        0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89, 0x8a, 0x92, 0x93,
        0x94, 0x95, 0x96, 0x97, 0x98, 0x99, 0x9a, 0xa2, 0xa3, 0xa4,
        0xa5, 0xa6, 0xa7, 0xa8, 0xa9, 0xaa, 0xb2, 0xb3, 0xb4, 0xb5,
        0xb6, 0xb7, 0xb8, 0xb9, 0xba, 0xc2, 0xc3, 0xc4, 0xc5, 0xc6,
        0xc7, 0xc8, 0xc9, 0xca, 0xd2, 0xd3, 0xd4, 0xd5, 0xd6, 0xd7,
        0xd8, 0xd9, 0xda, 0xe1, 0xe2, 0xe3, 0xe4, 0xe5, 0xe6, 0xe7,
        0xe8, 0xe9, 0xea, 0xf1, 0xf2, 0xf3, 0xf4, 0xf5, 0xf6, 0xf7,
        0xf8, 0xf9, 0xfa,

        // dc_huff_tbl_1
        0xff, 0xc4, 0x00, 0x1f, 0x01, 0x00, 0x03, 0x01, 0x01, 0x01,
        0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
        0x09, 0x0a, 0x0b,

        // ac_huff_tbl_1
        0xff, 0xc4, 0x00, 0xb5, 0x11, 0x00, 0x02, 0x01, 0x02, 0x04,
        0x04, 0x03, 0x04, 0x07, 0x05, 0x04, 0x04, 0x00, 0x01, 0x02,
        0x77, 0x00, 0x01, 0x02, 0x03, 0x11, 0x04, 0x05, 0x21, 0x31,
        0x06, 0x12, 0x41, 0x51, 0x07, 0x61, 0x71, 0x13, 0x22, 0x32,
        0x81, 0x08, 0x14, 0x42, 0x91, 0xa1, 0xb1, 0xc1, 0x09, 0x23,
        0x33, 0x52, 0xf0, 0x15, 0x62, 0x72, 0xd1, 0x0a, 0x16, 0x24,
        0x34, 0xe1, 0x25, 0xf1, 0x17, 0x18, 0x19, 0x1a, 0x26, 0x27,
        0x28, 0x29, 0x2a, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3a, 0x43,
        0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x53, 0x54, 0x55,
        0x56, 0x57, 0x58, 0x59, 0x5a, 0x63, 0x64, 0x65, 0x66, 0x67,
        0x68, 0x69, 0x6a, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79,
        0x7a, 0x82, 0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89, 0x8a,
        0x92, 0x93, 0x94, 0x95, 0x96, 0x97, 0x98, 0x99, 0x9a, 0xa2,
        0xa3, 0xa4, 0xa5, 0xa6, 0xa7, 0xa8, 0xa9, 0xaa, 0xb2, 0xb3,
        0xb4, 0xb5, 0xb6, 0xb7, 0xb8, 0xb9, 0xba, 0xc2, 0xc3, 0xc4,
        0xc5, 0xc6, 0xc7, 0xc8, 0xc9, 0xca, 0xd2, 0xd3, 0xd4, 0xd5,
        0xd6, 0xd7, 0xd8, 0xd9, 0xda, 0xe2, 0xe3, 0xe4, 0xe5, 0xe6,
        0xe7, 0xe8, 0xe9, 0xea, 0xf2, 0xf3, 0xf4, 0xf5, 0xf6, 0xf7,
        0xf8, 0xf9, 0xfa
};


/**
 * YUVのピクセルフォーマットを定義します。
 * <p>
 * Java側でも同じ定義をしていますので、修正する場合には注意してください。
 * </p>
 */
enum {
    FMT_YUY2 = 0,
    FMT_NV12 = 1,
    FMT_M420 = 2,
    FMT_I420 = 3
};


/**
 * 指定された値が0〜255に収まるようにクリップします.
 *
 * @param a 変換する値
 * @return 0〜255に収まるように変換された値
 */
static uint8_t clip(double a) {
    if (a < 0) {
        return 0;
    } else if (a > 255) {
        return 255;
    } else {
        return (uint8_t) a;
    }
}


/**
 * YUVの値をRGBに変換します.
 *
 * @param Y 輝度信号
 * @param U 青色成分の差分信号
 * @param V 赤色成分の差分信号
 * @return RGB
 */
static uint32_t yuv2rgb(uint8_t Y, uint8_t U, uint8_t V) {
    uint8_t R = clip(1.164 * (Y - 16) + 1.596 * (V - 128));
    uint8_t G = clip(1.164 * (Y - 16) - 0.391 * (U - 128) - 0.813 * (V - 128));
    uint8_t B = clip(1.164 * (Y - 16) + 2.018 * (U - 128));
    return 0xFF000000 | (B << 16) | (G << 8) | R;
}


/**
 * YUY2の値をRGBのバッファに格納します.
 *
 * @param src YUY2のデータ
 * @param width 横幅
 * @param height 縦幅
 * @param dst 出力先のバッファ
 */
static void yuy2toRGB(uint8_t *src, uint32_t width, uint32_t height, uint32_t *dst) {
    for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x += 2) {
            uint8_t Y0 = *src++;
            uint8_t U = *src++;
            uint8_t Y1 = *src++;
            uint8_t V = *src++;
            *dst++ = yuv2rgb(Y0, U, V);
            *dst++ = yuv2rgb(Y1, U, V);
        }
    }
}


/**
 * NV12の値をRGBのバッファに格納します.
 *
 * @param src NV12のデータ
 * @param width 横幅
 * @param height 縦幅
 * @param dst 出力先のバッファ
 */
static void nv12toRGB(uint8_t *src, uint32_t width, uint32_t height, uint32_t *dst) {
    uint8_t *uv = (src + width * height);
    for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x ++) {
            int index = y / 2 + x / 2;
            uint8_t Y = *src++;
            uint8_t U = *(uv + index);
            uint8_t V = *(uv + index + 1);
            *dst++ = yuv2rgb(Y, U, V);
        }
    }
}


/**
 * M420の値をRGBのバッファに格納します.
 *
 * @param src M420のデータ
 * @param width 横幅
 * @param height 縦幅
 * @param dst 出力先のバッファ
 */
static void m420toRGB(uint8_t *src, uint32_t width, uint32_t height, uint32_t *dst) {
    for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x ++) {
            int yIndex = x + (y + y / 2) * width;
            int uvIndex = x / 2 + 2 * width;
            uint8_t Y = *(src + yIndex);
            uint8_t U = *(src + yIndex + uvIndex);
            uint8_t V = *(src + yIndex + uvIndex + 1);
            *dst++ = yuv2rgb(Y, U, V);
        }
    }
}


/**
 * I420の値をRGBのバッファに格納します.
 *
 * @param src I420のデータ
 * @param width 横幅
 * @param height 縦幅
 * @param dst 出力先のバッファ
 */
static void i420toRGB(uint8_t *src, uint32_t width, uint32_t height, uint32_t *dst) {
    uint8_t *uv = (src + width * height);
    for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x ++) {
            uint8_t Y = *src++;
            uint8_t U = *(uv + y);
            uint8_t V = *(uv + y + 1);
            *dst++ = yuv2rgb(Y, U, V);
        }
    }
}


//////////////////////////////


JNIEXPORT jboolean JNICALL JNI_METHOD_NAME(nativeDecodeYUV)(JNIEnv *env, jclass clazz, jobject bitmap, jbyteArray yuvArray, jint width, jint height, jint type) {
    AndroidBitmapInfo info;
    uint32_t *pixels;

    if (bitmap == NULL) {
        return JNI_FALSE;
    }

    if (AndroidBitmap_getInfo(env, bitmap, &info) < 0) {
        return JNI_FALSE;
    }

    if (info.width != width || info.height != height) {
        return JNI_FALSE;
    }

    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        return JNI_FALSE;
    }

    if (AndroidBitmap_lockPixels(env, bitmap, (void **) &pixels) < 0) {
        return JNI_FALSE;
    }

    jboolean b;
    jbyte *yuv = env->GetByteArrayElements(yuvArray, &b);
    if (yuv == NULL) {
        AndroidBitmap_unlockPixels(env, bitmap);
        return JNI_FALSE;
    }

    jboolean result = JNI_TRUE;
    switch (type) {
        case FMT_YUY2:
            yuy2toRGB((uint8_t *) yuv, (uint32_t) width, (uint32_t) height, pixels);
            break;
        case FMT_NV12:
            nv12toRGB((uint8_t *) yuv, (uint32_t) width, (uint32_t) height, pixels);
            break;
        case FMT_M420:
            m420toRGB((uint8_t *) yuv, (uint32_t) width, (uint32_t) height, pixels);
            break;
        case FMT_I420:
            i420toRGB((uint8_t *) yuv, (uint32_t) width, (uint32_t) height, pixels);
            break;
        default:
            result = JNI_FALSE;
            break;
    }

    env->ReleaseByteArrayElements(yuvArray, yuv, 0);

    AndroidBitmap_unlockPixels(env, bitmap);

    return result;
}


JNIEXPORT void JNICALL JNI_METHOD_NAME(nativeCopyJpeg)(JNIEnv *env, jclass clazz, jbyteArray srcArray, jint srcLength, jbyteArray destArray) {
    jboolean isCopy;
    uint8_t *src = (uint8_t *) env->GetByteArrayElements(srcArray, &isCopy);
    if (src == NULL) {
        return;
    }

    uint8_t *dest = (uint8_t *) env->GetByteArrayElements(destArray, &isCopy);
    if (dest == NULL) {
        env->ReleaseByteArrayElements(srcArray, (jbyte *) src, 0);
        return;
    }

    memcpy(dest, src, 2);
    memcpy((dest + 2), huff_tbl, huff_tbl_size);
    memcpy((dest + 2 + huff_tbl_size), src + 2, (size_t) srcLength - 2);

    env->ReleaseByteArrayElements(srcArray, (jbyte *) src, 0);
    env->ReleaseByteArrayElements(destArray, (jbyte *) dest, 0);
}


JNIEXPORT void JNICALL JNI_METHOD_NAME(nativeDecodeYUV420SP)(JNIEnv *env, jclass clazz, jobject bitmap, jbyteArray yuv420spArray, jint width, jint height) {
    AndroidBitmapInfo info;
    int *pixels;

    if (bitmap == NULL) {
        return;
    }

    if (AndroidBitmap_getInfo(env, bitmap, &info) < 0) {
        return;
    }

    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        return;
    }

    if (AndroidBitmap_lockPixels(env, bitmap, (void **) &pixels) < 0) {
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


#ifdef __cplusplus
}
#endif
