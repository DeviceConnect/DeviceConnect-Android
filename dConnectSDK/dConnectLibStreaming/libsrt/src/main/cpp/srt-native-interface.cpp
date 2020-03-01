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
JNI_METHOD_NAME(createSrtSocket)(JNIEnv *env, jclass clazz) {
    LOGI("Java_org_deviceconnect_android_libsrt_NdkHelper_createSrtSocket()");

    int server_socket = srt_create_socket();
    if (server_socket == SRT_INVALID_SOCK) {
        LOGE("srt_socket: %s", srt_getlasterror_str());
        return -1;
    }
    return server_socket;
}


JNIEXPORT jint JNICALL
JNI_METHOD_NAME(setSockFlag)(JNIEnv *env, jclass clazz, jlong nativePtr, jint opt, jobject value) {
    LOGI("Java_org_deviceconnect_android_libsrt_NdkHelper_setSockFlag(): opt=%d", opt);

    jclass valueClass = env->GetObjectClass(value);

    int result = -1;

    switch (opt) {
        case SRTO_SNDSYN:
        case SRTO_RCVSYN:
        case SRTO_SENDER: // RTO_SENDER	1.0.4	pre	int32_t bool?
        {
            jmethodID boolValueMethodId = env->GetMethodID(valueClass, "booleanValue", "()Z");
            bool data = env->CallBooleanMethod(value, boolValueMethodId);
            result = srt_setsockflag((int) nativePtr, (SRT_SOCKOPT) opt, &data, sizeof data);
        }
            break;
        case SRTO_MAXBW:
        case SRTO_INPUTBW:
        {
            jmethodID longValueMethodId = env->GetMethodID(valueClass, "longValue", "()J");
            int64_t data = env->CallLongMethod(value, longValueMethodId);
            result = srt_setsockflag((int) nativePtr, (SRT_SOCKOPT) opt, &data, sizeof data);
        }
            break;
        case SRTO_LOSSMAXTTL:
        case SRTO_LATENCY:
        case SRTO_RCVLATENCY:
        case SRTO_PEERLATENCY:
        case SRTO_OHEADBW:
        {
            jmethodID intValueMethodId = env->GetMethodID(valueClass, "intValue", "()I");
            int32_t data = env->CallIntMethod(value, intValueMethodId);
            result = srt_setsockflag((int) nativePtr, (SRT_SOCKOPT) opt, &data, sizeof data);
        }
            break;

        default:
            break;
    }

    if (result == SRT_ERROR) {
        LOGE("srt_setsockflag: %s", srt_getlasterror_str());
    }

    env->DeleteLocalRef(valueClass);
    return result;
}


JNIEXPORT jlong JNICALL
JNI_METHOD_NAME(bind)(JNIEnv *env, jclass clazz, jlong ptr, jstring address, jint port) {
    LOGI("Java_org_deviceconnect_android_libsrt_NdkHelper_bind()");

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

    int result = srt_bind((int) ptr, (struct sockaddr*)&sa, sizeof sa);
    if (result == SRT_ERROR) {
        LOGE("srt_bind: %s", srt_getlasterror_str());
    }
    return result;
}


JNIEXPORT jlong JNICALL
JNI_METHOD_NAME(listen)(JNIEnv *env, jclass clazz, jlong ptr, jint backlog) {
    LOGI("Java_org_deviceconnect_android_libsrt_NdkHelper_listen()");

    int result = srt_listen((int) ptr, backlog);
    if (result == SRT_ERROR) {
        LOGE("srt_listen: %s\n", srt_getlasterror_str());
    }
    return result;
}



JNIEXPORT jlong JNICALL
JNI_METHOD_NAME(connect)(JNIEnv *env, jclass clazz, jlong ptr, jstring address, jint port) {
    LOGI("Java_org_deviceconnect_android_libsrt_NdkHelper_connect()");

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

    int result = srt_connect((int) ptr, (struct sockaddr*)&sa, sizeof sa);
    if (result == SRT_ERROR) {
        LOGE("srt_connect: %s", srt_getlasterror_str());
    }
    return result;
}



JNIEXPORT void JNICALL
JNI_METHOD_NAME(closeSrtSocket)(JNIEnv *env, jclass clazz, jlong ptr) {
    LOGI("Java_org_deviceconnect_android_libsrt_NdkHelper_closeSrtSocket()");

    int st = srt_close((int) ptr);
    if (st == SRT_ERROR) {
        LOGE("srt_close: %s\n", srt_getlasterror_str());
    }
}


JNIEXPORT jlong JNICALL
JNI_METHOD_NAME(accept)(JNIEnv *env, jclass clazz, jlong ptr) {
    LOGI("Java_org_deviceconnect_android_libsrt_NdkHelper_accept()");

    int accept_socket = srt_accept((int) ptr, nullptr, nullptr);
    if (accept_socket == SRT_INVALID_SOCK) {
        LOGE("srt_accept: %s\n", srt_getlasterror_str());
        return -1;
    }
    return accept_socket;
}


JNIEXPORT jint JNICALL
JNI_METHOD_NAME(sendMessage)(JNIEnv *env, jclass clazz, jlong ptr, jbyteArray byteArray, jint offset, jint length) {
    jboolean isCopy;
    jbyte* data = env->GetByteArrayElements(byteArray, &isCopy);
    if (data == nullptr) {
        return -1;
    }

    int result = srt_sendmsg((int) ptr, (const char *) &data[offset], length, -1, 0);
    if (result == SRT_ERROR) {
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
    if (result == SRT_ERROR) {
        LOGE("srt_send: %s\n", srt_getlasterror_str());
    }
    env->ReleaseByteArrayElements(byteArray, data, 0);
    return result;
}


JNIEXPORT void JNICALL
JNI_METHOD_NAME(dumpStats)(JNIEnv *env, jclass clazz, jlong ptr) {
    LOGI("Java_org_deviceconnect_android_libsrt_NdkHelper_dumpStats()");

    SRT_TRACEBSTATS stats;
    int result = srt_bstats((int) ptr, &stats, 0);
    if (result == SRT_ERROR) {
        return;
    }
    LOGD("dumpStats: pktSentTotal=%ld, pktRetransTotal=%d, pktSndLossTotal=%d, pktSndDropTotal=%d",
            stats.pktSentTotal, stats.pktRetransTotal, stats.pktSndLossTotal, stats.pktSndDropTotal);
    LOGD("dumpStats: mbpsBandwidth=%f, mbpsMaxBW=%f, byteAvailSndBuf=%d, msRTT=%f",
            stats.mbpsBandwidth, stats.mbpsMaxBW, stats.byteAvailSndBuf, stats.msRTT);
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


#ifdef __cplusplus
}
#endif
