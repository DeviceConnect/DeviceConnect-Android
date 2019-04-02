#ifndef RTP_COMMON_H
#define RTP_COMMON_H

#include <stdio.h>
#include <android/log.h>

#ifdef DEBUG
#define LOG_TAG "RTP-JNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#else
#define LOGI(...)
#define LOGD(...)
#define LOGE(...)
#define LOGW(...)
#endif

// MTUのサイズ
#define MTU 1300

// パケットの最大サイズ
#define MAX_PACKET_SIZE (MTU - 28)

// RTP ヘッダーのサイズ
#define RTP_HEADER_LENGTH 12

// メモリを解放した後いに変数にNULLを格納する
#define SAFE_FREE(p) { free(p); p = NULL; }

#endif //RTP_COMMON_H
