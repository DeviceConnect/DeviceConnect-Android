//
// Created by toshiya mitsuhashi on 2019-07-22.
//

#include <jni.h>
#include <android/log.h>
#include <opus_jni.h>
#include <string.h>
#include "opus_jni.h"

JNIEXPORT jlong JNICALL
Java_org_deviceconnect_opuscodec_OpusEncoder_opusEncoderCreate(
        JNIEnv* env,
        jobject instance,
        jint    samplingRate,
        jint    channels,
        jint    bitRate,
        jint    frameSize,
        jint    mode
) {
    int error;
    OpusEncoder* encoder = opus_encoder_create(samplingRate, channels, mode, &error);
    if (encoder != NULL) {
        opus_encoder_ctl(encoder, OPUS_SET_BITRATE(bitRate));
        return (jlong) encoder;
    } else {
        return 0;
    }
}

JNIEXPORT jint JNICALL
Java_org_deviceconnect_opuscodec_OpusEncoder_opusEncode(
        JNIEnv*     env,
        jobject     instance,
        jlong       pointer,
        jshortArray pcmBuffer,
        jint        pcmBufferLength,
        jbyteArray  opusFrame
) {
    OpusEncoder *encoder = (OpusEncoder *) pointer;
    if (encoder == NULL) {
        return 0;
    }

    jshort *n_pcmBuffer = (*env)->GetShortArrayElements(env, pcmBuffer, NULL);
    jbyte *n_opusFrame = (*env)->GetByteArrayElements(env, opusFrame, NULL);

    int ret = opus_encode(encoder, n_pcmBuffer, pcmBufferLength, (unsigned char *) n_opusFrame,
            MAX_PAYLOAD_BYTES);

    (*env)->ReleaseShortArrayElements(env, pcmBuffer, n_pcmBuffer, 0);
    (*env)->ReleaseByteArrayElements(env, opusFrame, n_opusFrame, 0);

    return ret;
}

JNIEXPORT void JNICALL
Java_org_deviceconnect_opuscodec_OpusEncoder_opusEncoderDestroy(
        JNIEnv* env,
        jobject instance,
        jlong   pointer
) {
    if (pointer != 0) {
        opus_encoder_destroy((OpusEncoder *) pointer);
    }
}