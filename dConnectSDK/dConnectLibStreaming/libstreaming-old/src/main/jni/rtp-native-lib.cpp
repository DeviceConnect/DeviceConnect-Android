#include <jni.h>
#include "common.h"
#include "rtp-socket.h"
#include "rtcp-socket.h"

/**
 * JNI のパッケージ名、クラス名を定義.
 */
#define JNI_METHOD_NAME(name) \
    Java_org_deviceconnect_android_streaming_rtp_RtpSocketNative_##name


#ifdef __cplusplus
extern "C" {
#endif

struct user_context {
    struct rtp_socket_info *rtp_info;
    struct rtcp_socket_info *rtcp_info;
};


static void user_context_close(struct user_context *context) {
    if (context) {
        rtp_socket_close(context->rtp_info);
        rtcp_socket_close(context->rtcp_info);
        context->rtp_info = NULL;
        context->rtcp_info = NULL;
    }
    SAFE_FREE(context);
}


JNIEXPORT jlong JNI_METHOD_NAME(open)(JNIEnv *env, jclass clazz, jstring dest_address, jint dest_port, jint rtcp_port) {
    LOGI("Java_org_deviceconnect_android_streaming_rtp_RtpSocketNative_open(%d, %d)", dest_port, rtcp_port);

    struct user_context *context = (struct user_context *) calloc(1, sizeof(struct user_context));
    if (context == NULL) {
        return NULL;
    }


    const char *hostString = env->GetStringUTFChars(dest_address, JNI_FALSE);
    if (hostString == NULL) {
        SAFE_FREE(context);
        return NULL;
    }

    context->rtp_info = rtp_socket_open((const uint8_t *) hostString, (uint16_t) dest_port);
    context->rtcp_info = rtcp_socket_open((const uint8_t *) hostString, (uint16_t) rtcp_port);

    env->ReleaseStringUTFChars(dest_address, hostString);

    if (context->rtp_info == NULL || context->rtcp_info == NULL) {
        user_context_close(context);
        return NULL;
    }

    context->rtp_info->rtcp_info = context->rtcp_info;

    return (jlong) context;
}


JNIEXPORT void JNI_METHOD_NAME(setPayloadType)(JNIEnv *env, jclass clazz, jlong nativePtr, jint payload_type) {
    LOGI("Java_org_deviceconnect_android_streaming_rtp_RtpSocketNative_setPayloadType(%d)", payload_type);

    struct user_context *context = (struct user_context *) nativePtr;
    if (context) {
        context->rtp_info->payload_type = (uint8_t) payload_type;
    }
}


JNIEXPORT void JNI_METHOD_NAME(setClockFrequency)(JNIEnv *env, jclass clazz, jlong nativePtr, jlong clock) {
    LOGI("Java_org_deviceconnect_android_streaming_rtp_RtpSocketNative_setClockFrequency(%d)", clock);

    struct user_context *context = (struct user_context *) nativePtr;
    if (context) {
        context->rtp_info->clock = (uint64_t) clock;
    }
}


JNIEXPORT void JNI_METHOD_NAME(setTTL)(JNIEnv *env, jclass clazz, jlong nativePtr, jint ttl) {
    LOGI("Java_org_deviceconnect_android_streaming_rtp_RtpSocketNative_setTTL(%d)", ttl);

    struct user_context *context = (struct user_context *) nativePtr;
    if (context) {
        rtp_socket_set_ttl(context->rtp_info, (uint8_t) ttl);
        rtcp_socket_set_ttl(context->rtcp_info, (uint8_t) ttl);
    }
}


JNIEXPORT void JNI_METHOD_NAME(setSSRC)(JNIEnv *env, jclass clazz, jlong nativePtr, jint ssrc) {
    LOGI("Java_org_deviceconnect_android_streaming_rtp_RtpSocketNative_setSSRC(%d)", ssrc);

    struct user_context *context = (struct user_context *) nativePtr;
    if (context) {
        rtp_socket_set_ssrc(context->rtp_info, (uint32_t) ssrc);
        rtcp_socket_set_ssrc(context->rtcp_info, (uint32_t) ssrc);
    }
}


JNIEXPORT void JNI_METHOD_NAME(send)(JNIEnv *env, jclass clazz, jlong nativePtr, jint packet_type, jbyteArray byteArray, jint length, jlong rtpts, jlong ntpts) {
//    LOGI("Java_org_deviceconnect_android_streaming_rtp_RtpSocketNative_send()");

    struct user_context *context = (struct user_context *) nativePtr;
    if (context == NULL) {
        return;
    }

    if (context->rtp_info == NULL || context->rtcp_info == NULL) {
        return;
    }

    jboolean isCopy;
    uint8_t *data = (uint8_t *) env->GetByteArrayElements(byteArray, &isCopy);
    if (data == NULL) {
        LOGE("Failed to get a buffer from java vm.");
        return;
    }

    uint64_t tps = rtp_compute_ts(context->rtp_info, (uint64_t) rtpts);

    context->rtcp_info->ntpts = (uint64_t) ntpts;
    context->rtcp_info->rtpts = tps;

    struct rtp_message message;
    message.packet_type = (rtp_packet_type) packet_type;
    message.data = data;
    message.length = (uint32_t) length;
    message.timestamp = tps;

    rtp_socket_send_message(context->rtp_info, &message);
}


JNIEXPORT void JNI_METHOD_NAME(close)(JNIEnv *env, jclass clazz, jlong nativePtr) {
    LOGI("Java_org_deviceconnect_android_streaming_rtp_RtpSocketNative_close()");

    user_context_close((struct user_context *) nativePtr);
}



#ifdef __cplusplus
}
#endif