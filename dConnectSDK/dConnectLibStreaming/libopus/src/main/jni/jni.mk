LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_C_INCLUDES := \
    $(LOCAL_PATH)/opus/include \
    $(LOCAL_PATH)/opus/silk \
    $(LOCAL_PATH)/opus/silk/fixed \
    $(LOCAL_PATH)/opus/celt

LOCAL_CFLAGS := -DDEBUG

LOCAL_SRC_FILES := opus_jni_encoder.c opus_jni_decoder.c

LOCAL_LDLIBS := -lm -llog

LOCAL_STATIC_LIBRARIES += opus_static

LOCAL_MODULE := opus-share
include $(BUILD_SHARED_LIBRARY)