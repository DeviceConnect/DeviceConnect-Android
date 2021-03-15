#ifndef UVC_COMMON_H
#define UVC_COMMON_H

#include <stdio.h>
#include <android/log.h>

#ifdef DEBUG
#define LOG_TAG "UVC-JNI"
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


#define SAFE_FREE(p) { free(p); p = NULL; }


/**
 * 操作の結果を定義.
 */
typedef enum _uvc_result {
    /**
     * 操作に成功.
     */
    UVC_SUCCESS = 0,

    /**
     * 操作に失敗.
     */
    UVC_ERROR = -1,

    /**
     * 既に UVC カメラが動作している場合のエラー定義.
     */
    UVC_ALREADY_RUNNING = -2,

    /**
     * メモリ不足の場合のエラー定義.
     */
    UVC_OUT_OF_MEMORY = -3,

    /**
     * パラメータが不正な場合のエラー定義.
     */
    UVC_PARAMETER_INVALID = -4,
} uvc_result;

/**
 * UVC用のBOOL定義.
 */
enum {
    UVC_TRUE = 1,
    UVC_FALSE = 0
};


#endif //UVC_COMMON_H
