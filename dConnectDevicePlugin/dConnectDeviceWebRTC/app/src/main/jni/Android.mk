LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := ImageUtils
LOCAL_SRC_FILES := ImageUtils.cpp
LOCAL_LDLIBS    := -llog

include $(BUILD_SHARED_LIBRARY)
