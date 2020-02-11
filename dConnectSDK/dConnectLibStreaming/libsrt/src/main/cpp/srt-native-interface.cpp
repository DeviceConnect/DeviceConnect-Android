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

    int yes = 1;
    int st;
    int ss = srt_create_socket();
    if (ss == SRT_ERROR) {
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
        return -1;
    }
    env->ReleaseStringUTFChars(address, addressString);

    srt_setsockflag(ss, SRTO_RCVSYN, &yes, sizeof yes);

    st = srt_bind(ss, (struct sockaddr*)&sa, sizeof sa);
    if (st == SRT_ERROR) {
        LOGE("srt_bind: %s", srt_getlasterror_str());
        return -1;
    }

    st = srt_listen(ss, backlog);
    if (st == SRT_ERROR) {
        LOGE("srt_listen: %s\n", srt_getlasterror_str());
        return -1;
    }

    return ss;
}


JNIEXPORT void JNICALL
JNI_METHOD_NAME(closeSrtSocket)(JNIEnv *env, jclass clazz, jlong ptr) {
    LOGI("Java_org_deviceconnect_android_libsrt_NdkHelper_closeSrtSocket()");

    int st = srt_close((int) ptr);
    if (st == SRT_ERROR) {
        LOGE("srt_close: %s\n", srt_getlasterror_str());
    }
}

JNIEXPORT long JNICALL
JNI_METHOD_NAME(accept)(JNIEnv *env, jclass clazz, jlong ptr) {
    LOGI("Java_org_deviceconnect_android_libsrt_NdkHelper_accept()");

    int st = srt_accept((int) ptr, nullptr, nullptr);
    if (st == SRT_ERROR) {
        LOGE("srt_accept: %s\n", srt_getlasterror_str());
        return -1;
    }

    return st;
}

JNIEXPORT int JNICALL
JNI_METHOD_NAME(sendMessage)(JNIEnv *env, jclass clazz, jlong ptr, jbyteArray byteArray, jint offset, jint length) {
    jboolean isCopy;
    jbyte* data = env->GetByteArrayElements(byteArray, &isCopy);
    if (data == nullptr) {
        return -1;
    }

    int result = srt_sendmsg((int) ptr, (const char *) &data[offset], length, -1, 0);
    if (result < SRT_ERROR) {
        LOGE("srt_send: %s\n", srt_getlasterror_str());
    }
    env->ReleaseByteArrayElements(byteArray, data, 0);
    return result;
}


JNIEXPORT int JNICALL
JNI_METHOD_NAME(recvMessage)(JNIEnv *env, jclass clazz, jlong ptr, jbyteArray byteArray, jint length) {
    jboolean isCopy;
    jbyte* data = env->GetByteArrayElements(byteArray, &isCopy);
    if (data == nullptr) {
        return -1;
    }

    int result = srt_recvmsg((int) ptr, (char *) data, length);
    if (result < SRT_ERROR) {
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
    LOGD("dumpStats: pktSentTotal=%ld, pktRetransTotal=%d, pktSndLossTotal=%d", stats.pktSentTotal, stats.pktRetransTotal, stats.pktSndLossTotal);
    LOGD("dumpStats: mbpsBandwidth=%f, mbpsMaxBW=%f, byteAvailSndBuf=%d", stats.mbpsBandwidth, stats.mbpsMaxBW, stats.byteAvailSndBuf);
}

JNIEXPORT jobject JNICALL
JNI_METHOD_NAME(getPeerName)(JNIEnv *env, jclass clazz, jlong nativeSocket) {
    LOGI("Java_org_deviceconnect_android_libsrt_NdkHelper_getPeerName()");
    struct sockaddr addr;
    int addrlen;
    int ret;

    ret = srt_getpeername((SRTSOCKET) nativeSocket, &addr, &addrlen);
    if (ret == SRT_ERROR) {
        LOGE("getPeerName: srt_getpeername: %s\n", srt_getlasterror_str());
        return nullptr;
    }

    // クライアントのIPアドレス
    char format[] = "%d.%d.%d.%d";
    char buf[15];
    sprintf(buf, format, addr.sa_data[2], addr.sa_data[3], addr.sa_data[4], addr.sa_data[5]);
    jstring address = env->NewStringUTF(buf);

    return address;
}

#ifdef __cplusplus
}
#endif
