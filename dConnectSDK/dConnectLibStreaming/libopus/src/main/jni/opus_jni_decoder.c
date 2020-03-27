//
// Created by toshiya mitsuhashi on 2019-07-29.
//

#include <jni.h>
#include <android/log.h>
#include <opus_jni.h>
#include <string.h>

JNIEXPORT jlong JNICALL
Java_org_deviceconnect_opuscodec_OpusDecoder_opusDecoderCreate(
        JNIEnv* env,
        jobject instance,
        jint    samplingRate,
        jint    channels
) {
    int error;
    OpusDecoder *decoder = opus_decoder_create(samplingRate, channels, &error);
    if (decoder != NULL) {
        return (jlong) decoder;
    } else {
        return 0;
    }
}

JNIEXPORT jint JNICALL
Java_org_deviceconnect_opuscodec_OpusDecoder_opusDecode(
        JNIEnv*     env,
        jobject     instance,
        jlong       pointer,
        jbyteArray  opusFrame,
        jint        opusFrameSize,
        jshortArray pcmBuffer,
        jint        pcmBufferSize
) {
    OpusDecoder *decoder = (OpusDecoder *) pointer;
    if (pointer == 0) {
        return 0;
    }

    jbyte *n_opusFrame = (*env)->GetByteArrayElements(env, opusFrame, NULL);
    jshort *n_pcmBuffer = (*env)->GetShortArrayElements(env, pcmBuffer, NULL);

    int ret = opus_decode(decoder, (const unsigned char *) n_opusFrame, opusFrameSize,
                      n_pcmBuffer, pcmBufferSize, 0);

    (*env)->ReleaseShortArrayElements(env, pcmBuffer, n_pcmBuffer, 0);
    (*env)->ReleaseByteArrayElements(env, opusFrame, n_opusFrame, 0);

    return ret;
}

JNIEXPORT void JNICALL
Java_org_deviceconnect_opuscodec_OpusDecoder_opusDecoderDestroy(
        JNIEnv* env,
        jobject instance,
        jlong   pointer
) {
    if (pointer != 0) {
        opus_decoder_destroy((OpusDecoder *) pointer);
    }
}
