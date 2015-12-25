#include <jni.h>
#include <android/log.h>
#include <math.h>

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


#ifdef __cplusplus
}
#endif /* __cplusplus */
