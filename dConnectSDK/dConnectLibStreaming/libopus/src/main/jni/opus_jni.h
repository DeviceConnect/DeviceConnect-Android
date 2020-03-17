//
// Created by toshiya mitsuhashi on 2019-07-23.
//

#ifndef MICTORTP_OPUS_JNI_H
#define MICTORTP_OPUS_JNI_H

#include <opus.h>
#include <jni.h>

#define MAX_PAYLOAD_BYTES       1500
#define MAX_FRAME_SIZE          (960 * 6)

#ifdef DEBUG
#define LOG_D(TAG, ...) \
        __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOG_E(TAG, ...) \
        __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#else
#define LOG_D(TAG, FORMAT, ...)
#define LOG_E(TAG, FORMAT, ...)
#endif

#endif //MICTORTP_OPUS_JNI_H
