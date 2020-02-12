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

    int st;
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

    int32_t yes = 1;
    srt_setsockflag(server_socket, SRTO_SENDER, &yes, sizeof yes);

    int64_t maxBW = 0;
    srt_setsockflag(server_socket, SRTO_MAXBW, &maxBW, sizeof maxBW);

    st = srt_bind(server_socket, (struct sockaddr*)&sa, sizeof sa);
    if (st == SRT_ERROR) {
        LOGE("srt_bind: %s", srt_getlasterror_str());
        srt_close(server_socket);
        return -1;
    }

    st = srt_listen(server_socket, backlog);
    if (st == SRT_ERROR) {
        LOGE("srt_listen: %s\n", srt_getlasterror_str());
        srt_close(server_socket);
        return -1;
    }

    return server_socket;
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

    int accept_socket = srt_accept((int) ptr, nullptr, nullptr);
    if (accept_socket == SRT_INVALID_SOCK) {
        LOGE("srt_accept: %s\n", srt_getlasterror_str());
        return -1;
    }

    bool no = false;
    srt_setsockflag(accept_socket, SRTO_RCVSYN, &no, sizeof no);
    srt_setsockflag(accept_socket, SRTO_SNDSYN, &no, sizeof no);

    int64_t srtInputBW = 1024 * 1024;
    int srtOheaBW = 25;
    srt_setsockflag(accept_socket, SRTO_INPUTBW, &srtInputBW, sizeof srtInputBW);
    srt_setsockflag(accept_socket, SRTO_OHEADBW, &srtOheaBW, sizeof srtOheaBW);

    return accept_socket;
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
    int ret = srt_getpeername((SRTSOCKET) nativeSocket, &addr, &addrlen);
    if (ret == SRT_ERROR) {
        LOGE("getPeerName: srt_getpeername: %s\n", srt_getlasterror_str());
        return nullptr;
    }

    // クライアントのIPアドレス
    char buf[15];
    sprintf(buf, "%d.%d.%d.%d", addr.sa_data[2], addr.sa_data[3], addr.sa_data[4], addr.sa_data[5]);
    jstring address = env->NewStringUTF(buf);
    return address;
}


JNIEXPORT void JNICALL
JNI_METHOD_NAME(setSrtOptions)(JNIEnv *env, jclass clazz, jlong nativeSocket, jlong inputBW, jint oheaBW) {
    LOGI("Java_org_deviceconnect_android_libsrt_NdkHelper_getPeerName()");

    //  It controls the maximum bandwidth together with SRTO_OHEADBW option according to the formula:
    //  MAXBW = INPUTBW * (100 + OHEADBW) / 100.
    int64_t srtInputBW = inputBW;
    int srtOheaBW = oheaBW;
    srt_setsockflag((SRTSOCKET) nativeSocket, SRTO_INPUTBW, &srtInputBW, sizeof srtInputBW);
    srt_setsockflag((SRTSOCKET) nativeSocket, SRTO_OHEADBW, &srtOheaBW, sizeof srtOheaBW);
}


#ifdef __cplusplus
}
#endif
