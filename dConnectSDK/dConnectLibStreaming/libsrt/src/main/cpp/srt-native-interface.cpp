#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <srt/srt.h>

#define DEBUG

#ifdef DEBUG
#include <android/log.h>
#define LOG_TAG "SRT-JNI"
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

/**
* JNI のパッケージ名、クラス名を定義.
*/
#define JNI_METHOD_NAME(name) Java_org_deviceconnect_android_libsrt_NdkHelper_##name

#ifdef __cplusplus
extern "C" {
#endif


JNIEXPORT void JNICALL
JNI_METHOD_NAME(startup)(JNIEnv *env, jclass clazz) {
    LOGI("Java_org_deviceconnect_android_libsrt_NdkHelper_startup()");
    srt_startup();
}


JNIEXPORT void JNICALL
JNI_METHOD_NAME(cleanup)(JNIEnv *env, jclass clazz) {
    LOGI("Java_org_deviceconnect_android_libsrt_NdkHelper_cleanup()");
    srt_cleanup();
}


JNIEXPORT jlong JNICALL
JNI_METHOD_NAME(createSrtSocket)(JNIEnv *env, jclass clazz, jstring address, jint port, jint backlog) {
    LOGI("Java_org_deviceconnect_android_libsrt_NdkHelper_createSrtSocket()");

    int result;
    int server_socket = srt_create_socket();
    if (server_socket == SRT_INVALID_SOCK) {
        LOGE("srt_socket: %s", srt_getlasterror_str());
        return -1;
    }

    const char *addressString = env->GetStringUTFChars(address, nullptr);
    struct sockaddr_in sa;
    sa.sin_family = AF_INET;
    sa.sin_port = htons(port);
    if (inet_pton(AF_INET, addressString, &sa.sin_addr) != 1) {
        LOGE("inet_pton error.");
        env->ReleaseStringUTFChars(address, addressString);
        srt_close(server_socket);
        return -1;
    }
    env->ReleaseStringUTFChars(address, addressString);

    int64_t maxBW = 0;
    srt_setsockflag(server_socket, SRTO_MAXBW, &maxBW, sizeof maxBW);

    result = srt_bind(server_socket, (struct sockaddr *) &sa, sizeof sa);
    if (result == SRT_ERROR) {
        LOGE("srt_bind: %s", srt_getlasterror_str());
        srt_close(server_socket);
        return -1;
    }

    result = srt_listen(server_socket, backlog);
    if (result == SRT_ERROR) {
        LOGE("srt_listen: %s\n", srt_getlasterror_str());
        srt_close(server_socket);
        return -1;
    }

    return server_socket;
}


JNIEXPORT void JNICALL
JNI_METHOD_NAME(closeSrtSocket)(JNIEnv *env, jclass clazz, jlong ptr) {
    LOGI("Java_org_deviceconnect_android_libsrt_NdkHelper_closeSrtSocket()");

    int result = srt_close((int) ptr);
    if (result == SRT_ERROR) {
        LOGE("srt_close: %s\n", srt_getlasterror_str());
    }
}


JNIEXPORT void JNICALL
JNI_METHOD_NAME(accept)(JNIEnv *env, jclass clazz, jlong ptr, jobject socket) {
    LOGI("Java_org_deviceconnect_android_libsrt_NdkHelper_accept()");

    struct sockaddr addr;
    int addrlen;
    int accepted_socket = srt_accept((int) ptr, &addr, &addrlen);
    if (accepted_socket == SRT_INVALID_SOCK) {
        LOGE("srt_accept: %s\n", srt_getlasterror_str());
        return;
    }

    int64_t inputBW = 2 * 1024 * 1024;
    int ohdadBW = 50;
    srt_setsockflag(accepted_socket, SRTO_INPUTBW, &inputBW, sizeof inputBW);
    srt_setsockflag(accepted_socket, SRTO_OHEADBW, &ohdadBW, sizeof ohdadBW);

    // クライアント側のソケットへのポインタ
    jclass socketCls = env->FindClass("org/deviceconnect/android/libsrt/SRTSocket");
    jfieldID socketPtr = env->GetFieldID(socketCls, "mNativePtr", "J");
    env->SetLongField(socket, socketPtr, accepted_socket);

    // クライアントのIPアドレス
    char buf[15];
    sprintf(buf, "%d.%d.%d.%d", addr.sa_data[2], addr.sa_data[3], addr.sa_data[4], addr.sa_data[5]);
    jstring address = env->NewStringUTF(buf);
    jfieldID addressField = env->GetFieldID(socketCls, "mSocketAddress", "Ljava/lang/String;");
    env->SetObjectField(socket, addressField, address);
}


JNIEXPORT jint JNICALL
JNI_METHOD_NAME(sendMessage)(JNIEnv *env, jclass clazz, jlong ptr, jbyteArray byteArray, jint offset, jint length) {
    jboolean isCopy;
    jbyte* data = env->GetByteArrayElements(byteArray, &isCopy);
    if (data == nullptr) {
        return -1;
    }

    SRT_MSGCTRL mc = srt_msgctrl_default;
    int result = srt_sendmsg2((int) ptr, (const char *) &data[offset], length, &mc);
    if (result <= SRT_ERROR) {
        LOGE("srt_send: %s\n", srt_getlasterror_str());
    }
    env->ReleaseByteArrayElements(byteArray, data, 0);
    return result;
}


JNIEXPORT jint JNICALL
JNI_METHOD_NAME(recvMessage)(JNIEnv *env, jclass clazz, jlong ptr, jbyteArray byteArray, jint length) {
    jboolean isCopy;
    jbyte* data = env->GetByteArrayElements(byteArray, &isCopy);
    if (data == nullptr) {
        return -1;
    }

    int result = srt_recvmsg((int) ptr, (char *) data, length);
    if (result <= SRT_ERROR) {
        LOGE("srt_send: %s\n", srt_getlasterror_str());
    }
    env->ReleaseByteArrayElements(byteArray, data, 0);
    return result;
}


JNIEXPORT void JNICALL
JNI_METHOD_NAME(dumpStats)(JNIEnv *env, jclass clazz, jlong ptr) {
    LOGI("Java_org_deviceconnect_android_libsrt_NdkHelper_dumpStats(): ptr=%d", ptr);

    SRT_TRACEBSTATS stats;
    int result = srt_bstats((int) ptr, &stats, 0);
    if (result == SRT_ERROR) {
        return;
    }
    LOGD("dumpStats: pktSentTotal=%ld, pktSndLossTotal=%d, pktSndDropTotal=%d, pktRetransTotal=%d", stats.pktSentTotal, stats.pktSndLossTotal, stats.pktSndDropTotal, stats.pktRetransTotal);
    LOGD("dumpStats: mbpsBandwidth=%f, mbpsMaxBW=%f, byteAvailSndBuf=%d", stats.mbpsBandwidth, stats.mbpsMaxBW, stats.byteAvailSndBuf);
}

#ifdef __cplusplus
}
#endif
