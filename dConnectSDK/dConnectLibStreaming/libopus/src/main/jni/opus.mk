ROOT := $(call my-dir)

# Build libopus
LOCAL_PATH := $(ROOT)/opus
include $(CLEAR_VARS)

#include the .mk files
include $(LOCAL_PATH)/celt_sources.mk
include $(LOCAL_PATH)/silk_sources.mk
include $(LOCAL_PATH)/opus_sources.mk

#fixed point sources
SILK_SOURCES += $(SILK_SOURCES_FIXED)

#ARM build
CELT_SOURCES += $(CELT_SOURCES_ARM)
SILK_SOURCES += $(SILK_SOURCES_ARM)
LOCAL_SRC_FILES := \
    $(CELT_SOURCES) \
    $(SILK_SOURCES) \
    $(OPUS_SOURCES)

LOCAL_C_INCLUDES := \
    $(LOCAL_PATH)/include \
    $(LOCAL_PATH)/silk \
    $(LOCAL_PATH)/silk/fixed \
    $(LOCAL_PATH)/celt

LOCAL_CFLAGS        := -DNULL=0 -DSOCKLEN_T=socklen_t -DLOCALE_NOT_USED -D_LARGEFILE_SOURCE=1 -D_FILE_OFFSET_BITS=64
LOCAL_CFLAGS        += -Drestrict='' -D__EMX__ -DOPUS_BUILD -DFIXED_POINT=1 -DDISABLE_FLOAT_API -DUSE_ALLOCA -DHAVE_LRINT -DHAVE_LRINTF -O3 -fno-math-errno
LOCAL_CPPFLAGS      := -DBSD=1
LOCAL_CPPFLAGS      += -ffast-math -O3 -funroll-loops

LOCAL_MODULE := opus_static
include $(BUILD_STATIC_LIBRARY)

