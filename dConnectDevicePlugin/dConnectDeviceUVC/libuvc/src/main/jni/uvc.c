/*
 uvc.c
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
#include <malloc.h>
#include <sys/stat.h>
#include <sys/mman.h>
#include <sys/ioctl.h>
#include <errno.h>
#include <string.h>
#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <linux/usbdevice_fs.h>
#include <poll.h>
#include <asm/byteorder.h>

#include "uvc.h"

#define SW_TO_SHORT(p) ((p)[0] | ((p)[1] << 8))

#define DW_TO_INT(p) ((p)[0] | ((p)[1] << 8) | ((p)[2] << 16) | ((p)[3] << 24))

#define QW_TO_LONG(p) \
 ((p)[0] | ((p)[1] << 8) | ((p)[2] << 16) | ((p)[3] << 24) \
  | ((uint64_t)(p)[4] << 32) | ((uint64_t)(p)[5] << 40) \
  | ((uint64_t)(p)[6] << 48) | ((uint64_t)(p)[7] << 56))


#define BYTE_TO_BW(s, p) (p)[0] = (s);

#define SHORT_TO_SW(s, p) \
  (p)[0] = ((s) & 0xFF); \
  (p)[1] = (((s) >> 8) & 0xFF);

#define INT_TO_DW(i, p) \
  (p)[0] = (i); \
  (p)[1] = (i) >> 8; \
  (p)[2] = (i) >> 16; \
  (p)[3] = (i) >> 24;

#define LONG_TO_QW(i, p) \
  (p)[0] = (i); \
  (p)[1] = (i) >> 8; \
  (p)[2] = (i) >> 16; \
  (p)[3] = (i) >> 24; \
  (p)[4] = (i) >> 32; \
  (p)[5] = (i) >> 40; \
  (p)[6] = (i) >> 48; \
  (p)[7] = (i) >> 56;


#define UVC_DRIVER_NAME "usbfs"

#define REQ_TYPE_SET 0x21
#define REQ_TYPE_GET 0xA1


static inline int xioctl(int fd, int request, void *arg) {
    int r;

    do {
        r = ioctl(fd, request, arg);
    } while (-1 == r && EINTR == errno);

    return r;
}

static uint16_t uvc_get_active_version(struct uvc_device_handle *handle) {
    return uvc_get_uvc_version(&handle->descriptor, handle->active_config);
}

static uint8_t uvc_get_video_control_interface_number(struct uvc_device_handle *handle) {
    return uvc_get_active_control_interface(handle)->interface->bInterfaceNumber;
}

static uint8_t uvc_get_video_streaming_interface_number(struct uvc_device_handle *handle) {
    return uvc_get_active_streaming_interface(handle)->interface->bInterfaceNumber;
}

static uvc_result uvc_get_configuration(struct uvc_device_handle *handle, uint8_t *active_config) {
    struct usbdevfs_ctrltransfer ctrl = {
            .bRequestType = 0x80,
            .bRequest = GET_CONFIGURATION,
            .wValue = 0,
            .wIndex = 0,
            .wLength = 2,
            .timeout = 1000,
            .data = active_config
    };
    return xioctl(handle->fd, USBDEVFS_CONTROL, &ctrl) == 0 ? UVC_SUCCESS : UVC_ERROR;
}

static uvc_result uvc_get_capabilities(struct uvc_device_handle *handle, uint32_t *caps) {
    return xioctl(handle->fd, USBDEVFS_GET_CAPABILITIES, caps) == 0 ? UVC_SUCCESS : UVC_ERROR;
}

static uvc_result uvc_detach_kernel_driver(struct uvc_device_handle *handle, uint8_t interface) {
    struct usbdevfs_getdriver getdrv;
    getdrv.interface = interface;
    int r = ioctl(handle->fd, USBDEVFS_GETDRIVER, &getdrv);
    if (r == 0 && strcmp(getdrv.driver, UVC_DRIVER_NAME) == 0) {
        return UVC_SUCCESS;
    }

    struct usbdevfs_ioctl command;
    command.ifno = interface;
    command.ioctl_code = USBDEVFS_DISCONNECT;
    command.data = NULL;
    return xioctl(handle->fd, USBDEVFS_IOCTL, &command) == 0 ? UVC_SUCCESS : UVC_ERROR;
}

static uvc_result uvc_attach_kernel_driver(struct uvc_device_handle *handle, uint8_t interface) {
    struct usbdevfs_ioctl command;
    command.ifno = interface;
    command.ioctl_code = USBDEVFS_CONNECT;
    command.data = NULL;
    return xioctl(handle->fd, USBDEVFS_IOCTL, &command) == 0 ? UVC_SUCCESS : UVC_ERROR;
}

static uvc_result uvc_claim_interface(struct uvc_device_handle *handle, uint8_t interface) {
    return xioctl(handle->fd, USBDEVFS_CLAIMINTERFACE, &interface) == 0 ? UVC_SUCCESS : UVC_ERROR;
}

static uvc_result uvc_release_interface(struct uvc_device_handle *handle, uint8_t interface) {
    return xioctl(handle->fd, USBDEVFS_RELEASEINTERFACE, &interface) == 0 ? UVC_SUCCESS : UVC_ERROR;
}

static uvc_result uvc_detach_kernel_driver_and_claim(struct uvc_device_handle *handle, uint8_t interface) {
    struct usbdevfs_disconnect_claim dc;
    int r;

    dc.interface = interface;
    strcpy(dc.driver, UVC_DRIVER_NAME);
    dc.flags = USBDEVFS_DISCONNECT_CLAIM_EXCEPT_DRIVER;
    r = ioctl(handle->fd, USBDEVFS_DISCONNECT_CLAIM, &dc);
    if (r == 0 || errno != ENOTTY) {
        return UVC_SUCCESS;
    }

    r = uvc_detach_kernel_driver(handle, interface);
    if (r != 0) {
        return UVC_ERROR;
    }
    return uvc_claim_interface(handle, interface);
}

static uvc_result uvc_attach_kernel_driver_and_release(struct uvc_device_handle *handle, uint8_t interface) {
    int r = uvc_release_interface(handle, interface);
    if (r < 0) {
        return UVC_ERROR;
    }
    uvc_attach_kernel_driver(handle, interface);
    return UVC_SUCCESS;
}

static uvc_result uvc_set_interface_alt_setting(struct uvc_device_handle *handle, struct uvc_video_streaming_altsetting *altsetting) {
    struct usbdevfs_setinterface setintf;
    setintf.interface = altsetting->interface->bInterfaceNumber;
    setintf.altsetting = altsetting->interface->bAlternateSetting;
    return ioctl(handle->fd, USBDEVFS_SETINTERFACE, &setintf) == 0 ? UVC_SUCCESS : UVC_ERROR;
}

static int uvc_control_transfer(struct uvc_device_handle *handle, uint8_t requestType, uint8_t request, uint16_t wValue, uint16_t wIndex, uint16_t wLength, void *data) {
    struct usbdevfs_ctrltransfer ctrl = {
            .bRequestType = requestType,
            .bRequest = request,
            .wValue = __le16_to_cpu(wValue),
            .wIndex = __le16_to_cpu(wIndex),
            .wLength = __le16_to_cpu(wLength),
            .timeout = 1000,
            .data = data
    };
    return xioctl(handle->fd, USBDEVFS_CONTROL, &ctrl);
}


///////// Video Probe and Commit /////////////


static size_t uvc_video_ctrl_size(struct uvc_device_handle *handle) {
    uint16_t bcdUVC = uvc_get_active_version(handle);
    if (bcdUVC >= 0x0150) {
        return 48;
    } else if (bcdUVC >= 0x0110) {
        return 34;
    } else {
        return 26;
    }
}


static uvc_result uvc_get_video_control(struct uvc_device_handle *handle, struct uvc_video_control *video_control, uint8_t request, uint16_t wValue) {
    uint8_t buf[48];
    uint16_t wIndex = handle->bInterfaceNumber;
    uint16_t wLength = uvc_video_ctrl_size(handle);

    int r = uvc_control_transfer(handle, REQ_TYPE_GET, request, wValue, wIndex, wLength, buf);
    if (r > 0) {
        video_control->bmHint = SW_TO_SHORT(buf);
        video_control->bFormatIndex = buf[2];
        video_control->bFrameIndex = buf[3];
        video_control->dwFrameInterval = DW_TO_INT(buf + 4);
        video_control->wKeyFrameRate = SW_TO_SHORT(buf + 8);
        video_control->wPFrameRate = SW_TO_SHORT(buf + 10);
        video_control->wCompQuality = SW_TO_SHORT(buf + 12);
        video_control->wCompWindowSize = SW_TO_SHORT(buf + 14);
        video_control->wDelay = SW_TO_SHORT(buf + 16);
        video_control->dwMaxVideoFrameSize = DW_TO_INT(buf + 18);
        video_control->dwMaxPayloadTransferSize = DW_TO_INT(buf + 22);
        if (r > 26) {
            video_control->dwClockFrequency = DW_TO_INT(buf + 26);
            video_control->bmFramingInfo = buf[30];
            video_control->bPreferedVersion = buf[31];
            video_control->bMinVersion = buf[32];
            video_control->bMaxVersion = buf[33];
            if (r > 34) {
                video_control->bUsage = buf[34];
                video_control->bBitDepthLuma = buf[35];
                video_control->bmSettings = buf[36];
                video_control->bMaxNumberOfRefFramesPlus1 = buf[37];
                video_control->bmRateControlModes = SW_TO_SHORT(buf + 38);
                video_control->bmLayoutPerStream = QW_TO_LONG(buf + 40);
            }
        }
    }

    return r > 0 ? UVC_SUCCESS : UVC_ERROR;
}


static uvc_result uvc_set_video_control(struct uvc_device_handle *handle, struct uvc_video_control *video_control, uint16_t wValue) {
    uint8_t buf[48];
    uint16_t wIndex = handle->bInterfaceNumber;
    uint16_t wLength = uvc_video_ctrl_size(handle);

    SHORT_TO_SW(video_control->bmHint, buf);
    BYTE_TO_BW(video_control->bFormatIndex, buf + 2);
    BYTE_TO_BW(video_control->bFrameIndex, buf + 3);
    INT_TO_DW(video_control->dwFrameInterval, buf + 4);
    SHORT_TO_SW(video_control->wKeyFrameRate, buf + 8);
    SHORT_TO_SW(video_control->wPFrameRate, buf + 10);
    SHORT_TO_SW(video_control->wCompQuality, buf + 12);
    SHORT_TO_SW(video_control->wCompWindowSize, buf + 14);
    SHORT_TO_SW(video_control->wDelay, buf + 16);
    INT_TO_DW(video_control->dwMaxVideoFrameSize, buf + 18);
    INT_TO_DW(video_control->dwMaxPayloadTransferSize, buf + 22);
    if (wLength > 26) {
        INT_TO_DW(video_control->dwClockFrequency, buf + 26);
        BYTE_TO_BW(video_control->bmFramingInfo, buf + 30);
        BYTE_TO_BW(video_control->bPreferedVersion, buf + 31);
        BYTE_TO_BW(video_control->bMinVersion, buf + 32);
        BYTE_TO_BW(video_control->bMaxVersion, buf + 33);
        if (wLength > 34) {
            BYTE_TO_BW(video_control->bUsage, buf + 34);
            BYTE_TO_BW(video_control->bBitDepthLuma, buf + 35);
            BYTE_TO_BW(video_control->bmSettings, buf + 36);
            BYTE_TO_BW(video_control->bMaxNumberOfRefFramesPlus1, buf + 37);
            SHORT_TO_SW(video_control->bmRateControlModes, buf + 38);
            LONG_TO_QW(video_control->bmLayoutPerStream, buf + 40);
        }
    }

    return uvc_control_transfer(handle, REQ_TYPE_SET, SET_CUR, wValue, wIndex, wLength, buf) > 0 ? UVC_SUCCESS : UVC_ERROR;
}


static size_t uvc_len_video_control(struct uvc_device_handle *handle, uint16_t wValue) {
    uint8_t buf[2];
    uint16_t wIndex = handle->bInterfaceNumber;
    uint16_t wLength = 2;

    int r = uvc_control_transfer(handle, REQ_TYPE_GET, GET_LEN, wValue, wIndex, wLength, buf);
    if (r > 0) {
        return SW_TO_SHORT(buf);
    } else {
        return r;
    }
}

static uvc_result uvc_get_probe_video_control(struct uvc_device_handle *handle, struct uvc_video_control *video_control) {
    return uvc_get_video_control(handle, video_control, GET_CUR, VS_PROBE_CONTROL << 8);
}

static uvc_result uvc_get_max_probe_video_control(struct uvc_device_handle *handle, struct uvc_video_control *video_control) {
    return uvc_get_video_control(handle, video_control, GET_MAX, VS_PROBE_CONTROL << 8);
}

static uvc_result uvc_get_min_probe_video_control(struct uvc_device_handle *handle, struct uvc_video_control *video_control) {
    return uvc_get_video_control(handle, video_control, GET_MIN, VS_PROBE_CONTROL << 8);
}

static uvc_result uvc_set_probe_video_control(struct uvc_device_handle *handle, struct uvc_video_control *video_control) {
    return uvc_set_video_control(handle, video_control, VS_PROBE_CONTROL << 8);
}

static uvc_result uvc_set_commit_video_control(struct uvc_device_handle *handle, struct uvc_video_control *video_control) {
    return uvc_set_video_control(handle, video_control, VS_COMMIT_CONTROL << 8);
}

static size_t uvc_get_len_probe_video_control(struct uvc_device_handle *handle) {
    return uvc_len_video_control(handle, VS_PROBE_CONTROL << 8);
}


static uvc_result uvc_get_h264_extension(struct uvc_device_handle *handle, struct uvc_h264_extension_unit *config, uint8_t request, uint16_t wValue) {
    struct uvc_vc_extension_unit_descriptor *extension = uvc_find_extension_descriptor(&handle->descriptor, handle->active_config);
    uint8_t buf[46];
    uint16_t wIndex = (extension->bUnitID << 8) | handle->bInterfaceNumber;
    uint16_t wLength = 46;

    int r = uvc_control_transfer(handle, REQ_TYPE_GET, request, wValue, wIndex, wLength, buf);
    if (r > 0) {
        config->dwFrameInterval = DW_TO_INT(buf);
        config->dwBitRate = DW_TO_INT(buf + 4);
        config->bmHints = SW_TO_SHORT(buf + 8);
        config->wConfigurationIndex = SW_TO_SHORT(buf + 10);
        config->wWidth = SW_TO_SHORT(buf + 12);
        config->wHeight = SW_TO_SHORT(buf + 14);
        config->wSliceUnits = SW_TO_SHORT(buf + 16);
        config->wSliceMode = SW_TO_SHORT(buf + 18);
        config->wProfile = SW_TO_SHORT(buf + 20);
        config->wIFramePeriod = SW_TO_SHORT(buf + 22);
        config->wEstimatedVideoDelay = SW_TO_SHORT(buf + 24);
        config->wEstimatedMaxConfigDelay = SW_TO_SHORT(buf + 26);
        config->bUsageType = buf[28];
        config->bRateControlMode = buf[29];
        config->bTemporalScaleMode = buf[30];
        config->bSpatialScaleMode = buf[31];
        config->bSNRScaleMode = buf[32];
        config->bStreamMuxOption = buf[33];
        config->bStreamFormat = buf[34];
        config->bEntropyCABAC = buf[35];
        config->bTimestamp = buf[36];
        config->bNumOfReorderFrames = buf[37];
        config->bPreviewFlipped = buf[38];
        config->bView = buf[39];
        config->bReserved1 = buf[40];
        config->bReserved2 = buf[41];
        config->bStreamID = buf[42];
        config->bSpatialLayerRatio = buf[43];
        config->wLeakyBucketSize = SW_TO_SHORT(buf + 44);
    }
    return r > 0 ? UVC_SUCCESS : UVC_ERROR;
}


static uvc_result uvc_set_h264_extension(struct uvc_device_handle *handle, struct uvc_h264_extension_unit *config, uint16_t wValue) {
    struct uvc_vc_extension_unit_descriptor *extension = uvc_find_extension_descriptor(&handle->descriptor, handle->active_config);
    uint8_t buf[46];
    uint16_t wIndex = (extension->bUnitID << 8) | handle->bInterfaceNumber;
    uint16_t wLength = 46;

    INT_TO_DW(config->dwFrameInterval, buf)
    INT_TO_DW(config->dwBitRate, buf + 4)
    SHORT_TO_SW(config->bmHints, buf + 8)
    SHORT_TO_SW(config->wConfigurationIndex, buf + 10)
    SHORT_TO_SW(config->wWidth, buf + 12)
    SHORT_TO_SW(config->wHeight, buf + 14)
    SHORT_TO_SW(config->wSliceUnits, buf + 16)
    SHORT_TO_SW(config->wSliceMode, buf + 18)
    SHORT_TO_SW(config->wProfile, buf + 20)
    SHORT_TO_SW(config->wIFramePeriod, buf + 22)
    SHORT_TO_SW(config->wEstimatedVideoDelay, buf + 24)
    SHORT_TO_SW(config->wEstimatedMaxConfigDelay, buf + 26)
    BYTE_TO_BW(config->bUsageType, buf + 28)
    BYTE_TO_BW(config->bRateControlMode, buf + 29)
    BYTE_TO_BW(config->bTemporalScaleMode, buf + 30)
    BYTE_TO_BW(config->bSpatialScaleMode, buf + 31)
    BYTE_TO_BW(config->bSNRScaleMode, buf + 32)
    BYTE_TO_BW(config->bStreamMuxOption, buf + 33)
    BYTE_TO_BW(config->bStreamFormat, buf + 34)
    BYTE_TO_BW(config->bEntropyCABAC, buf + 35)
    BYTE_TO_BW(config->bTimestamp, buf + 36)
    BYTE_TO_BW(config->bNumOfReorderFrames, buf + 37)
    BYTE_TO_BW(config->bPreviewFlipped, buf + 38)
    BYTE_TO_BW(config->bView, buf + 39)
    BYTE_TO_BW(config->bReserved1, buf + 40)
    BYTE_TO_BW(config->bReserved2, buf + 41)
    BYTE_TO_BW(config->bStreamID, buf + 42)
    BYTE_TO_BW(config->bSpatialLayerRatio, buf + 43)
    SHORT_TO_SW(config->wLeakyBucketSize, buf + 44)

    return uvc_control_transfer(handle, REQ_TYPE_SET, SET_CUR, wValue, wIndex, wLength, buf) > 0 ? UVC_SUCCESS : UVC_ERROR;
}

static uvc_result uvc_get_probe_h264_extension(struct uvc_device_handle *handle, struct uvc_h264_extension_unit *config) {
    return uvc_get_h264_extension(handle, config, GET_CUR, UVCX_VIDEO_CONFIG_PROBE << 8);
}

static uvc_result uvc_get_max_probe_h264_extension(struct uvc_device_handle *handle, struct uvc_h264_extension_unit *config) {
    return uvc_get_h264_extension(handle, config, GET_MAX, UVCX_VIDEO_CONFIG_PROBE << 8);
}

static uvc_result uvc_get_min_probe_h264_extension(struct uvc_device_handle *handle, struct uvc_h264_extension_unit *config) {
    return uvc_get_h264_extension(handle, config, GET_MIN, UVCX_VIDEO_CONFIG_PROBE << 8);
}

static uvc_result uvc_get_def_probe_h264_extension(struct uvc_device_handle *handle, struct uvc_h264_extension_unit *config) {
    return uvc_get_h264_extension(handle, config, GET_DEF, UVCX_VIDEO_CONFIG_PROBE << 8);
}

static uvc_result uvc_set_probe_h264_extension(struct uvc_device_handle *handle, struct uvc_h264_extension_unit *config) {
    return uvc_set_h264_extension(handle, config, UVCX_VIDEO_CONFIG_PROBE << 8);
}

static uvc_result uvc_commit_probe_h264_extension(struct uvc_device_handle *handle, struct uvc_h264_extension_unit *config) {
    return uvc_set_h264_extension(handle, config, UVCX_VIDEO_CONFIG_COMMIT << 8);
}

static size_t uvc_len_probe_h264_extension(struct uvc_device_handle *handle) {
    struct uvc_vc_extension_unit_descriptor *extension = uvc_find_extension_descriptor(&handle->descriptor, handle->active_config);
    uint8_t buf[2];
    uint16_t wIndex = (extension->bUnitID << 8) | handle->bInterfaceNumber;
    uint16_t wValue = UVCX_VIDEO_CONFIG_PROBE << 8;
    uint16_t wLength = 2;

    int r = uvc_control_transfer(handle, REQ_TYPE_GET, GET_LEN, wValue, wIndex, wLength, buf);
    if (r > 0) {
        return SW_TO_SHORT(buf);
    } else {
        return r;
    }
}

///////// Still Probe and Commit /////////////


static uvc_result uvc_get_still_control(struct uvc_device_handle *handle, struct uvc_still_control *still_control, uint8_t request, uint16_t wValue) {
    uint8_t buf[11];
    uint16_t wIndex = still_control->bInterfaceNumber;
    uint16_t wLength = 11;

    int r = uvc_control_transfer(handle, REQ_TYPE_GET, request, wValue, wIndex, wLength, buf);
    if (r > 0) {
        still_control->bFormatIndex = buf[0];
        still_control->bFrameIndex = buf[1];
        still_control->bCompressionIndex = buf[2];
        still_control->dwMaxVideoFrameSize = DW_TO_INT(buf + 3);
        still_control->dwMaxPayloadTransferSize = DW_TO_INT(buf + 7);
    }

    return r > 0 ? UVC_SUCCESS : UVC_ERROR;
}

static uvc_result uvc_set_still_control(struct uvc_device_handle *handle, struct uvc_still_control *still_control, uint16_t wValue) {
    uint8_t buf[11];
    uint16_t wIndex = still_control->bInterfaceNumber;
    uint16_t wLength = 11;

    BYTE_TO_BW(still_control->bFormatIndex, buf);
    BYTE_TO_BW(still_control->bFrameIndex, buf + 1);
    BYTE_TO_BW(still_control->bCompressionIndex, buf + 2);
    INT_TO_DW(still_control->dwMaxVideoFrameSize, buf + 3);
    INT_TO_DW(still_control->dwMaxPayloadTransferSize, buf + 7);

    return uvc_control_transfer(handle, REQ_TYPE_SET, SET_CUR, wValue, wIndex, wLength, buf) > 0 ? UVC_SUCCESS : UVC_ERROR;
}

static uvc_result uvc_get_probe_still_control(struct uvc_device_handle *handle, struct uvc_still_control *still_control) {
    return uvc_get_still_control(handle, still_control, GET_CUR, VS_STILL_PROBE_CONTROL << 8);
}

static uvc_result uvc_get_max_probe_still_control(struct uvc_device_handle *handle, struct uvc_still_control *still_control) {
    return uvc_get_still_control(handle, still_control, GET_MAX, VS_STILL_PROBE_CONTROL << 8);
}

static uvc_result uvc_get_min_probe_still_control(struct uvc_device_handle *handle, struct uvc_still_control *still_control) {
    return uvc_get_still_control(handle, still_control, GET_MIN, VS_STILL_PROBE_CONTROL << 8);
}

static uvc_result uvc_get_def_probe_still_control(struct uvc_device_handle *handle, struct uvc_still_control *still_control) {
    return uvc_get_still_control(handle, still_control, GET_DEF, VS_STILL_PROBE_CONTROL << 8);
}

static uvc_result uvc_set_probe_still_control(struct uvc_device_handle *handle, struct uvc_still_control *still_control) {
    return uvc_set_still_control(handle, still_control, VS_STILL_PROBE_CONTROL << 8);
}

static uvc_result uvc_set_commit_still_control(struct uvc_device_handle *handle, struct uvc_still_control *still_control) {
    return uvc_set_still_control(handle, still_control, VS_STILL_COMMIT_CONTROL << 8);
}


///////// uvc_frame /////////////


/**
 * UVCから転送されてきたフレームを格納するバッファを作成します.
 *
 * @param type フレームバッファのタイプ
 * @param length バッファサイズ
 * @return 構造体へのポインタ
 */
static struct uvc_frame *uvc_create_frame(uint8_t type, uint32_t length) {
    struct uvc_frame *frame = (struct uvc_frame *) calloc(1, sizeof(struct uvc_frame) + length);
    if (frame) {
        frame->type = type;
        frame->length = length;
    }
    return frame;
}

/**
 * UVCから転送されてきたフレームを格納するバッファをリサイズします.
 *
 * @param frame リサイズを行うフレームバッファ
 * @param length バッファサイズ
 * @return 構造体へのポインタ
 */
static struct uvc_frame *uvc_realloc_frame(struct uvc_frame *frame, uint32_t length) {
    struct uvc_frame *newFrame = (struct uvc_frame *) realloc(frame, sizeof(struct uvc_frame) + length);
    if (newFrame) {
        newFrame->length = length;
    }
    return newFrame;
}

///////// uvc_transfer /////////////

/**
 * uvc_transfer のインスタンスを作成します.
 *
 * @param handle UVCデバイスのハンドル
 * @param endpoint 転送先のエンドポイント
 * @param type 送タイプ.
 * @param length サイズ
 * @param num_iso_packet アイソクロナス転送用のパケット数.
 * @param size_iso_packet アイソクロナス転送用のパケットのサイズ.
 * @return uvc_transfer のインスタンス
 */
static struct uvc_transfer *uvc_create_transfer(struct uvc_device_handle *handle, uint8_t endpoint, uint8_t type, uint32_t length, uint8_t num_iso_packet, uint32_t size_iso_packet) {
    struct uvc_transfer *transfer = (struct uvc_transfer *) calloc(1, sizeof(struct uvc_transfer) + length);
    if (transfer) {
        transfer->handle = handle;
        transfer->endpoint = endpoint;
        transfer->type = type;
        transfer->num_iso_packet = num_iso_packet;
        transfer->size_iso_packet = size_iso_packet;
        transfer->length = length;
    }
    return transfer;
}

/**
 * アイソクロナス転送用の USB 要求ブロックをキャンセルします.
 *
 * @param handle UVCデバイスのハンドル
 * @param transfer キャンセルするアイソクロナス転送用の要求
 */
static void uvc_cancel_iso_transfer(struct uvc_device_handle *handle, struct uvc_transfer *transfer) {
    if (transfer->urb) {
        xioctl(handle->fd, USBDEVFS_DISCARDURB, transfer->urb);
        SAFE_FREE(transfer->urb);
    }
}

/**
 * アイソクロナス転送用の USB 要求ブロックを作成します.
 *
 * @param handle UVCデバイスのハンドル
 * @param transfer アイソクロナス転送用の要求
 * @return UVC_SUCCESSの場合はUSB要求の作成に成功、それ以外はUSB要求の作成に失敗
 */
static uvc_result uvc_submit_iso_transfer(struct uvc_device_handle *handle, struct uvc_transfer *transfer) {
    if (transfer->urb == NULL) {
        transfer->urb = (struct usbdevfs_urb *) calloc(1, sizeof(struct usbdevfs_urb) +
                transfer->num_iso_packet * sizeof(struct usbdevfs_iso_packet_desc));
        if (transfer->urb == NULL) {
            LOGE("@@@@ uvc_submit_iso_transfer: Out of memory.");
            return UVC_OUT_OF_MEMORY;
        }
    }

    memset(transfer->urb, 0x0, sizeof(struct usbdevfs_urb));

    transfer->urb->type = USBDEVFS_URB_TYPE_ISO;
    transfer->urb->flags = USBDEVFS_URB_ISO_ASAP;
    transfer->urb->endpoint = transfer->endpoint;
    transfer->urb->number_of_packets = transfer->num_iso_packet;
    transfer->urb->buffer = (void *) transfer->buf;
    transfer->urb->usercontext = transfer;
    for (int i = 0; i < transfer->num_iso_packet; i++) {
        transfer->urb->iso_frame_desc[i].length = transfer->size_iso_packet;
        transfer->urb->iso_frame_desc[i].status = 0;
        transfer->urb->iso_frame_desc[i].actual_length = 0;
    }

    int r = xioctl(handle->fd, USBDEVFS_SUBMITURB, transfer->urb);
    if (r < 0) {
        LOGE("@@@@ uvc_submit_iso_transfer: Failed to ioctl. %d %d", r, errno);
        SAFE_FREE(transfer->urb);
        return UVC_ERROR;
    }
    return UVC_SUCCESS;
}


/**
 * バルク転送用の USB 要求ブロックを作成します.
 *
 * @param handle UVCデバイスのハンドル
 * @param transfer バルク転送用の要求
 * @return UVC_SUCCESSの場合はUSB要求の作成に成功、それ以外はUSB要求の作成に失敗
 */
static uvc_result uvc_submit_bulk_transfer(struct uvc_device_handle *handle, struct uvc_transfer *transfer) {
    if (handle->running == UVC_VIDEO_STOP) {
        return UVC_ERROR;
    }

    if (transfer->urb == NULL) {
        transfer->urb = (struct usbdevfs_urb *) calloc(1, sizeof(struct usbdevfs_urb));
        if (transfer->urb == NULL) {
            return UVC_OUT_OF_MEMORY;
        }
    }

    memset(transfer->urb, 0x0, sizeof(struct usbdevfs_urb));

    transfer->urb->type = USBDEVFS_URB_TYPE_BULK;
    transfer->urb->flags = USBDEVFS_URB_BULK_CONTINUATION;
    transfer->urb->stream_id = 0;
    transfer->urb->endpoint = transfer->endpoint;
    transfer->urb->buffer = (void *) transfer->buf;
    transfer->urb->buffer_length = transfer->length;
    transfer->urb->usercontext = transfer;

    int r = ioctl(handle->fd, USBDEVFS_SUBMITURB, transfer->urb);
    if (r < 0) {
        LOGE("@@@@ uvc_submit_bulk_transfer: Failed to submit urb. r=%d", r);
        SAFE_FREE(transfer->urb);
        return UVC_ERROR;
    }
    return UVC_SUCCESS;
}


/**
 * UVCから転送されてきたフレームバッファを指定されたコールバック関数に通知します.
 *
 * @param handle UVCデバイスのハンドル
 * @param frame 通知するフレームバッファ
 */
static void uvc_notify_frame(struct uvc_device_handle *handle, struct uvc_frame *frame) {
    if (frame->got_bytes > 0) {
//        LOGD("@@@ uvc_notify_frame: frame[%d].got_bytes=%u length=%u pts=%u stc=%u sof=%u",
//             handle->frame_id, frame->got_bytes, frame->length, frame->pts, frame->stc, frame->sof);
        handle->callback(handle->user, frame);
        frame->got_bytes = 0;
    }
}

/**
 * 転送されてきたフレームデータをフレームにコピーします.
 *
 * @param handle UVCデバイスのハンドル
 * @param pktbuf パケットデータ
 * @param actual_length パケットデータサイズ
 */
static void uvc_reap_payload(struct uvc_device_handle *handle, uint8_t *pktbuf, uint32_t actual_length) {
    uint8_t bHeaderLength = pktbuf[0];
    uint8_t bmHeaderInfo = pktbuf[1];

    if (actual_length <= 2 || actual_length <= bHeaderLength) {
        return;
    }

    int frame_id = (bmHeaderInfo & UVC_STREAM_FID);
    if (bmHeaderInfo & UVC_STREAM_ERR) {
        LOGE("@@@ uvc_reap_payload: UVC_STREAM_ERR. frame[%d]", frame_id);
    } else {
        // フレームの切り替え処理
        if (handle->frame_id != frame_id) {
            // TODO: fid が切り替わったタイミングでフレームを通知して良いか確認すること。
            uvc_notify_frame(handle, handle->frame[handle->frame_id]);
            handle->frame_id = frame_id;
        }

        struct uvc_frame *frame = handle->frame[frame_id];
        if (frame) {
            if (bmHeaderInfo & UVC_STREAM_PTS) {
                if (bHeaderLength >= 6) {
                    frame->pts = DW_TO_INT(pktbuf + 2);
                }
            }

            if (bmHeaderInfo & UVC_STREAM_SCR) {
                if (bHeaderLength >= 12) {
                    frame->stc = DW_TO_INT(pktbuf + 6);
                    // D43 - D47 はReservedで0が設定されているので
                    // ここでは、特にマスク処理はしていない。
                    frame->sof = SW_TO_SHORT(pktbuf + 10);
                }
            }

            size_t odd_bytes = actual_length - bHeaderLength;

            if (frame->got_bytes + odd_bytes > frame->length) {
                LOGW("@@@ uvc_reap_payload: Out of bounds. frame-length=%d got_bytes=%d odd_bytes=%d",
                     frame->length, frame->got_bytes, odd_bytes);

                // フレームサイズを超えてしまった時にはリサイズを行う
                frame = uvc_realloc_frame(frame, frame->got_bytes + odd_bytes);
                if (frame == NULL) {
                    LOGE("@@@ uvc_reap_payload: realloc frame[%d]", frame_id);
                    handle->frame[frame_id]->got_bytes = 0;
                    return;
                }
                handle->frame[frame_id] = frame;
            }

            memcpy(frame->buf + frame->got_bytes, pktbuf + bHeaderLength, (size_t) odd_bytes);
            frame->got_bytes += odd_bytes;

            // End of Frame のフラグがある場合にはフレームの通知を行う
            if (bmHeaderInfo & UVC_STREAM_EOF) {
                uvc_notify_frame(handle, frame);
            }
        }
    }
}


/**
 * アイソクロナス転送で送られてきたデータをフレームに設定します.
 *
 * @param handle UVCデバイスのハンドル
 * @param urb USB要求ブロック
 */
static void uvc_reap_iso_transfer(struct uvc_device_handle *handle, struct usbdevfs_urb *urb) {
    for (int i = 0; i < urb->number_of_packets; i++) {
        uint8_t *pktbuf = urb->buffer + urb->iso_frame_desc[i].length * i;
        uint32_t actual_length = urb->iso_frame_desc[i].actual_length;
        uvc_reap_payload(handle, pktbuf, actual_length);
    }
}

/**
 * バルク転送で送られてきたデータをフレームに設定します.
 *
 * @param handle UVCデバイスのハンドル
 * @param urb USB要求ブロック
 */
static void uvc_reap_bulk_transfer(struct uvc_device_handle *handle, struct usbdevfs_urb *urb) {
    uvc_reap_payload(handle, urb->buffer, (uint32_t) urb->actual_length);
}

/**
 * USBからのデータ転送を取得します.
 *
 * @param handle UVCデバイスのハンドル
 */
static uvc_result uvc_reap_transfer(struct uvc_device_handle *handle) {
    struct usbdevfs_urb *urb = NULL;
    int r = xioctl(handle->fd, USBDEVFS_REAPURBNDELAY, &urb);
    if (r < 0 || urb == NULL) {
        LOGE("@@@ uvc_reap_transfer: error %d", r);
        return UVC_ERROR;
    }

    if (handle->running == UVC_VIDEO_STOP) {
        LOGW("@@@ uvc_reap_transfer: uvc camera is already stopped.");
        return UVC_ERROR;
    }

    struct uvc_transfer *transfer = urb->usercontext;
    if (transfer) {
        switch (transfer->type) {
            case UVC_TRANSFER_TYPE_ISO:
                uvc_reap_iso_transfer(handle, urb);
                uvc_submit_iso_transfer(handle, transfer);
                break;

            case UVC_TRANSFER_TYPE_BULK:
                uvc_reap_bulk_transfer(handle, urb);
                uvc_submit_bulk_transfer(handle, transfer);
                break;

            default:
                LOGE("@@@ uvc_reap_transfer: Unknown transfer type.");
                return UVC_ERROR;
        }
    }

    return UVC_SUCCESS;
}

/**
 * ファイルディスクリプタのイベントを待ち受けて、UVCからのデータ転送を取得します.
 *
 * @param handle UVCデバイスのハンドル
 * @return UVC_SUCCESSの場合はポーリングに成功、それ以外は失敗
 */
static uvc_result uvc_poll_event(struct uvc_device_handle *handle) {
    struct pollfd p = {
            .fd = handle->fd,
            .events = POLLOUT,
            .revents = 0
    };

    int res = poll(&p, 1, 1000);
    if (res != 1 || p.revents != POLLOUT) {
        LOGW("@@@ uvc_handle_event_internal: [poll - event %d, res %d, error %d]\n", p.revents, res, errno);
        return UVC_ERROR;
    }

    return uvc_reap_transfer(handle);
}


/**
 * dwFrameIntervalを設定します.
 *
 * @param ctrl dwFrameIntervalを設定する構造体
 * @param fps dwFrameIntervalに設定するFPS
 * @param min インターバルの最小値
 * @param max インターバルの最大値
 * @param step インターバルの間隔
 * @return UVC_SUCCESSの場合には設定成功、それ以外は設定失敗
 */
static uvc_result uvc_set_fps_step(struct uvc_video_control *ctrl, uint32_t fps, uint32_t min, uint32_t max, uint32_t step) {
    uint32_t interval_100ns = 10000000 / fps;
    uint32_t interval_offset = interval_100ns - min;
    if (interval_100ns >= min && interval_100ns <= max && !(interval_offset && (interval_offset % step))) {
        ctrl->dwFrameInterval = interval_100ns;
        return UVC_SUCCESS;
    }
    return UVC_ERROR;
}

/**
 * dwFrameIntervalを設定します.
 *
 * @param ctrl dwFrameIntervalを設定する構造体
 * @param fps dwFrameIntervalに設定するFPS
 * @param interval_size 設定できるインターバルの個数
 * @param intervals インターバルの配列
 * @return UVC_SUCCESSの場合には設定成功、それ以外は設定失敗
 */
static uvc_result uvc_set_fps_fixed(struct uvc_video_control *ctrl, uint32_t fps, uint32_t interval_size, uint32_t *intervals) {
    for (int i = 0; i < interval_size; i++) {
        uint32_t interval = intervals[i];
        if (10000000 / interval == fps) {
            ctrl->dwFrameInterval = interval;
            return UVC_SUCCESS;
        }
    }
    return UVC_ERROR;
}


/**
 * UVCのストリーミング開始のネゴシエーションを行います.
 *
 * @param handle UVCデバイスのハンドル
 * @param format_index フォーマットインデックス
 * @param frame_index フレームインデックス
 * @param fps フレームレート
 * @return UVC_SUCCESSの場合はネゴシエーションに成功、それ以外はネゴシエーションに失敗
 */
static uvc_result uvc_stream_negotiation(struct uvc_device_handle *handle, uint8_t format_index, uint8_t frame_index, uint32_t fps) {
    uvc_result result;
    struct uvc_video_control *ctrl = &handle->video_control;
    struct uvc_vs_format_descriptor *format = uvc_find_format_descriptor(&handle->descriptor, handle->active_config, format_index);
    struct uvc_vs_frame_descriptor *frame = uvc_find_frame_descriptor(&handle->descriptor, handle->active_config, format_index, frame_index);
    if (format == NULL || frame == NULL) {
        LOGE("@@@ uvc_probe_video_control: Not found format or frame descriptor.");
        return UVC_PARAMETER_INVALID;
    }

    uint8_t bInterfaceNumber = format->streaming_interface->interface->bInterfaceNumber;
    handle->bInterfaceNumber = bInterfaceNumber;
    handle->frame_type = frame->bDescriptorSubType;

    int r = uvc_get_max_probe_video_control(handle, &handle->video_control);
    if (r != UVC_SUCCESS) {
        r = uvc_get_min_probe_video_control(handle, &handle->video_control);
        if (r != UVC_SUCCESS) {
            r = uvc_get_probe_video_control(handle, &handle->video_control);
            if (r != UVC_SUCCESS) {
                LOGE("@@@ uvc_probe_video_control: Failed to get probe video controls.");
                return UVC_ERROR;
            }
        }
    }

    ctrl->bmHint = (1 << 0);
    ctrl->bFormatIndex = format_index;
    ctrl->bFrameIndex = frame_index;

    switch (frame->bDescriptorSubType) {
        case VS_FRAME_MJPEG: {
            struct uvc_vs_frame_mjpeg_descriptor *frame_mjpeg = (struct uvc_vs_frame_mjpeg_descriptor *) frame;
            if (frame_mjpeg->bFrameIntervalType == 0) {
                ctrl->dwMaxVideoFrameSize = frame_mjpeg->dwMaxVideoFrameBufferSize;
                uvc_set_fps_step(ctrl, fps,
                                 frame_mjpeg->dwMinFrameInterval,
                                 frame_mjpeg->dwMaxFrameInterval,
                                 frame_mjpeg->dwFrameIntervalStep);
            } else {
                uvc_set_fps_fixed(ctrl, fps, frame_mjpeg->bFrameIntervalType, frame_mjpeg->dwFrameInterval);
            }
        }   break;

        case VS_FRAME_UNCOMPRESSED: {
            struct uvc_vs_frame_uncompressed_descriptor *frame_uncompressed = (struct uvc_vs_frame_uncompressed_descriptor *) frame;
            if (frame_uncompressed->bFrameIntervalType == 0) {
                ctrl->dwMaxVideoFrameSize = frame_uncompressed->dwMaxVideoFrameBufferSize;
                uvc_set_fps_step(ctrl, fps,
                                 frame_uncompressed->dwMinFrameInterval,
                                 frame_uncompressed->dwMaxFrameInterval,
                                 frame_uncompressed->dwFrameIntervalStep);
            } else {
                uvc_set_fps_fixed(ctrl, fps, frame_uncompressed->bFrameIntervalType, frame_uncompressed->dwFrameInterval);
            }
        }   break;

        case VS_FRAME_H264: {
            struct uvc_vs_frame_h264_descriptor *frame_h264 = (struct uvc_vs_frame_h264_descriptor *) frame;
            uvc_set_fps_fixed(ctrl, fps, frame_h264->bNumFrameIntervals, frame_h264->dwFrameInterval);
        }   break;

        default:
            LOGE("@@ uvc_probe_video_control: unknown frame type.");
            break;
    }

    result = uvc_set_probe_video_control(handle, ctrl);
    if (result != UVC_SUCCESS) {
        LOGE("@@ uvc_probe_video_control: Failed to set probe video controls.");
        return UVC_ERROR;
    }

    result = uvc_get_probe_video_control(handle, ctrl);
    if (result != UVC_SUCCESS) {
        LOGE("@@ uvc_probe_video_control: Failed to get probe video controls.");
        return UVC_ERROR;
    }

    if (!(ctrl->bFormatIndex == format_index && ctrl->bFrameIndex == frame_index)) {
        LOGE("@@ uvc_probe_video_control: Failed to set format_index and frame_index.");
        return UVC_ERROR;
    }

    result = uvc_set_commit_video_control(handle, &handle->video_control);
    if (result != UVC_SUCCESS) {
        LOGE("@@ uvc_probe_video_control: Failed to set commit video controls.");
        return UVC_ERROR;
    }

    return result;
}

/**
 * H264 エクステンションユニットのネゴシエーションを行います.
 *
 * @param handle UVCデバイスのハンドル
 * @param format_index フォーマットインデックス
 * @param frame_index フレームインデックス
 * @param fps フレームレート
 * @return UVC_SUCCESS の場合はネゴシエーションに成功、それ以外はネゴシエーションに失敗
 */
static uvc_result uvc_extension_h264_negotiation(struct uvc_device_handle *handle, uint8_t format_index, uint8_t frame_index, uint32_t fps) {
    if (!uvc_has_h264_extension(&handle->descriptor, handle->active_config)) {
        LOGE("@@@ uvc_extension_h264_negotiation: Not support h264 extension unit.");
        return UVC_PARAMETER_INVALID;
    }

    uvc_result result;
    struct uvc_vs_format_descriptor *format = uvc_find_format_descriptor(&handle->descriptor, handle->active_config, format_index);
    struct uvc_vs_frame_descriptor *frame = uvc_find_frame_descriptor(&handle->descriptor, handle->active_config, format_index, frame_index);
    if (format == NULL || frame == NULL) {
        LOGE("@@@ uvc_extension_h264_negotiation: Not found format or frame descriptor.");
        return UVC_PARAMETER_INVALID;
    }

    handle->bInterfaceNumber = format->streaming_interface->interface->bInterfaceNumber;
    handle->frame_type = frame->bDescriptorSubType;
    handle->video_control.bFormatIndex = format_index;
    handle->video_control.bFrameIndex = frame_index;

    size_t s = uvc_len_probe_h264_extension(handle);
    if (s != 46) {
        LOGE("@@@ uvc_extension_h264_negotiation: Failed to get length of h264 configure.");
        return UVC_ERROR;
    }

    result = uvc_get_max_probe_h264_extension(handle, &handle->h264_extension);
    if (result != UVC_SUCCESS) {
        LOGE("uvc_extension_h264_negotiation: Failed to get a h264 configure.");
        return UVC_ERROR;
    }

    if ((handle->h264_extension.bStreamMuxOption & UVC_H264_MUX_OPTION_H264) == 0) {
        LOGE("uvc_extension_h264_negotiation: bStreamMuxOption is not support h264.");
        return UVC_ERROR;
    }

    // TODO: H264 Extension Unit に設定するパラメータの調整

    handle->h264_extension.dwFrameInterval = (uint32_t) (10000000 / fps);
//    handle->h264_extension.dwBitRate = 1000000;
    handle->h264_extension.bmHints = 0x0;
//    handle->h264_extension.wConfigurationIndex = 0;
    handle->h264_extension.wWidth = frame->wWidth;
    handle->h264_extension.wHeight = frame->wHeight;
    handle->h264_extension.wSliceUnits = 0;
    handle->h264_extension.wSliceMode = UVC_H264_SLICEMODE_IGNORED;
    handle->h264_extension.wProfile = UVC_H264_PROFILE_MAIN;
    handle->h264_extension.wIFramePeriod = 2000;
//    handle->h264_extension.wEstimatedVideoDelay = 30;
//    handle->h264_extension.wEstimatedMaxConfigDelay = 250;
//    handle->h264_extension.bUsageType = UVC_H264_USAGETYPE_REALTIME;
    handle->h264_extension.bRateControlMode = UVC_H264_RATECONTROL_VBR;
//    handle->h264_extension.bTemporalScaleMode = 0x0;
//    handle->h264_extension.bSpatialScaleMode = 0x0;
//    handle->h264_extension.bSNRScaleMode = 0x0;
    handle->h264_extension.bStreamMuxOption = UVC_H264_MUX_OPTION_H264 | UVC_H264_MUX_OPTION_ENABLE;
//    handle->h264_extension.bStreamFormat = UVC_H264_STREAMFORMAT_ANNEXB;
//    handle->h264_extension.bEntropyCABAC = UVC_H264_ENTROPY_CAVLC;
//    handle->h264_extension.bTimestamp = UVC_H264_TIMESTAMP_SEI_ENABLE;
//    handle->h264_extension.bNumOfReorderFrames = 0;
//    handle->h264_extension.bPreviewFlipped = UVC_H264_PREFLIPPED_HORIZONTAL;
//    handle->h264_extension.bView = 0x0;
//    handle->h264_extension.bStreamID = 0x0;
//    handle->h264_extension.bSpatialLayerRatio = 0x0;
//    handle->h264_extension.wLeakyBucketSize = 2000;

    result = uvc_set_probe_h264_extension(handle, &handle->h264_extension);
    if (result != UVC_SUCCESS) {
        LOGE("uvc_extension_h264_negotiation: Failed to set a h264 configure.");
        return UVC_ERROR;
    }

    result = uvc_get_probe_h264_extension(handle, &handle->h264_extension);
    if (result != UVC_SUCCESS) {
        LOGE("uvc_extension_h264_negotiation: Failed to get a h264 configure.");
        return UVC_ERROR;
    }

    if (handle->h264_extension.wWidth == 0 && handle->h264_extension.wHeight == 0) {
        LOGE("uvc_extension_h264_negotiation: Failed to get a h264 configure. wWidth=0, wHeight=0");
        return UVC_ERROR;
    }

    result = uvc_commit_probe_h264_extension(handle, &handle->h264_extension);
    if (result != UVC_SUCCESS) {
        LOGE("uvc_extension_h264_negotiation: Failed to commit a h264 configure.");
        return UVC_ERROR;
    }

    return UVC_SUCCESS;
}


/**
 * UVCの静止画開始のネゴシエーションを行います.
 *
 * @param handle UVCデバイスのハンドル
 * @param format_index フォーマットインデックス
 * @param frame_index フレームインデックス
 * @param compressionIndex 圧縮インデックス
 * @return UVC_SUCCESSの場合はネゴシエーションに成功、それ以外はネゴシエーションに失敗
 */
static uvc_result uvc_still_negotiation(struct uvc_device_handle *handle, uint8_t format_index, uint8_t frame_index, uint8_t compressionIndex) {
    uvc_result result;
    struct uvc_still_control *ctrl = &handle->still_control;
    struct uvc_vs_format_descriptor *format = uvc_find_format_descriptor(&handle->descriptor, handle->active_config, format_index);
    struct uvc_vs_frame_descriptor *frame = uvc_find_frame_descriptor(&handle->descriptor, handle->active_config, format_index, frame_index);
    if (format == NULL || frame == NULL) {
        LOGE("@@@ uvc_probe_video_control: Not found format or frame descriptor.");
        return UVC_ERROR;
    }

    uint8_t interfaceNum = format->streaming_interface->interface->bInterfaceNumber;
    handle->still_control.bInterfaceNumber = interfaceNum;
    handle->frame_type = frame->bDescriptorSubType;

    int r = uvc_get_max_probe_still_control(handle, &handle->still_control);
    if (r != UVC_SUCCESS) {
        r = uvc_get_min_probe_still_control(handle, &handle->still_control);
        if (r != UVC_SUCCESS) {
            r = uvc_get_probe_still_control(handle, &handle->still_control);
            if (r != UVC_SUCCESS) {
                LOGE("@@@ uvc_probe_still_control: Failed to get probe still controls.");
                return UVC_ERROR;
            }
        }
    }

    ctrl->bFormatIndex = format_index;
    ctrl->bFrameIndex = frame_index;
    ctrl->bCompressionIndex = compressionIndex;
    ctrl->bInterfaceNumber = interfaceNum;

    result = uvc_set_probe_still_control(handle, ctrl);
    if (result != UVC_SUCCESS) {
        LOGE("@@ uvc_probe_still_control: Failed to set probe still controls.");
        return UVC_ERROR;
    }

    result = uvc_get_probe_still_control(handle, ctrl);
    if (result != UVC_SUCCESS) {
        LOGE("@@ uvc_probe_still_control: Failed to get probe still controls.");
        return UVC_ERROR;
    }

    if (!(ctrl->bFormatIndex == format_index && ctrl->bFrameIndex == frame_index)) {
        LOGE("@@ uvc_probe_still_control: Failed to set format_index and frame_index.");
        return UVC_ERROR;
    }

    result = uvc_set_commit_still_control(handle, &handle->still_control);
    if (result != UVC_SUCCESS) {
        LOGE("@@ uvc_probe_still_control: Failed to set commit still controls.");
        return UVC_ERROR;
    }

    return result;
}

/**
 * フレームの最大サイズを取得します.
 *
 * @param handle UVCデバイスのハンドル
 * @param is_still 静止画の場合にはUVC_TRUE、それ以外はUVC_FALSE
 * @return 最大サイズ、取得に失敗した場合は0
 */
static uint32_t uvc_get_max_frame_size(struct uvc_device_handle *handle, uint8_t is_still) {
    uint8_t bFormatIndex;
    uint8_t bFrameIndex;

    if (is_still) {
        struct uvc_still_control *ctrl = &handle->still_control;
        uint32_t dwMaxVideoFrameSize = ctrl->dwMaxVideoFrameSize;
        if (dwMaxVideoFrameSize > 0) {
            return dwMaxVideoFrameSize;
        }

        bFormatIndex = ctrl->bFormatIndex;
        bFrameIndex = ctrl->bFrameIndex;
    } else {
        struct uvc_video_control *ctrl = &handle->video_control;
        uint32_t dwMaxVideoFrameSize = ctrl->dwMaxVideoFrameSize;
        if (dwMaxVideoFrameSize > 0) {
            return dwMaxVideoFrameSize;
        }

        bFormatIndex = ctrl->bFormatIndex;
        bFrameIndex = ctrl->bFrameIndex;
    }

    struct uvc_vs_frame_descriptor *frame = uvc_find_frame_descriptor(&handle->descriptor, handle->active_config, bFormatIndex, bFrameIndex);
    if (frame) {
        switch (frame->bDescriptorSubType) {
            case VS_FRAME_UNCOMPRESSED: {
                struct uvc_vs_frame_uncompressed_descriptor *m = (struct uvc_vs_frame_uncompressed_descriptor *) frame;
                return m->dwMaxVideoFrameBufferSize;
            }
            case VS_FRAME_MJPEG: {
                struct uvc_vs_frame_mjpeg_descriptor *m = (struct uvc_vs_frame_mjpeg_descriptor *) frame;
                return m->dwMaxVideoFrameBufferSize;
            }
            case VS_FRAME_H264: {
                struct uvc_vs_frame_h264_descriptor *m = (struct uvc_vs_frame_h264_descriptor *) frame;
                return m->dwMaxBitRate / 8;
            }
            default:
                break;
        }
    }
    return 0;
}


/**
 * アイソクロナス転送用の構造体を作成します.
 *
 * @param handle UVCデバイスのハンドル
 * @param is_still 静止画の場合にはUVC_TRUE、それ以外はUVC_FALSE
 * @return 構造体の作成に成功した場合はUVC_SUCCESS
 */
static uvc_result uvc_isochronous_transfer(struct uvc_device_handle *handle, uint8_t is_still) {
    uint32_t config_bytes_per_packet;
    if (is_still) {
        config_bytes_per_packet = handle->still_control.dwMaxPayloadTransferSize;
    } else {
        config_bytes_per_packet = handle->video_control.dwMaxPayloadTransferSize;
    }

    uint32_t endpoint_bytes_per_packet = 0;
    uint32_t packets_per_transfer = 0;
    uint32_t total_transfer_size = 0;
    uint32_t dwMaxVideoFrameSize = uvc_get_max_frame_size(handle, is_still);
    if (dwMaxVideoFrameSize == 0) {
        LOGE("uvc_isochronous_transfer: dwMaxVideoFrame is zero.");
        return UVC_ERROR;
    }

    struct uvc_video_streaming_altsetting *altsetting = uvc_get_active_streaming_altsetting(handle);
    while (altsetting) {
        struct uvc_endpoint_descriptor *endpoint;

        if (is_still) {
            endpoint = altsetting->still_endpoint;
        } else {
            endpoint = altsetting->video_endpoint;
        }

        if (endpoint->bmAttributes & 0x01) {
            endpoint_bytes_per_packet = endpoint->wMaxPacketSize;
            endpoint_bytes_per_packet = (endpoint_bytes_per_packet & 0x07ff) *
                                        (((endpoint_bytes_per_packet >> 11) & 3) + 1);

            // 転送速度が足りない場合がありますが、その場合には最後に見つかった altsetting を使用します。
            if (altsetting->next == NULL || endpoint_bytes_per_packet >= config_bytes_per_packet) {
                packets_per_transfer = (dwMaxVideoFrameSize + endpoint_bytes_per_packet - 1) / endpoint_bytes_per_packet;

                // 使用する uvc_transfer の個数を制限します。
                // 大きいサイズにすると Nexus5X で ENOMEM (メモリ不足) が発生したので、小さくしています。
                if (packets_per_transfer > 4) {
                    packets_per_transfer = 4;
                }

                total_transfer_size = packets_per_transfer * endpoint_bytes_per_packet;
                break;
            }
        }
        altsetting = altsetting->next;
    }

    if (altsetting == NULL) {
        LOGE("@@@ uvc_isochronous_transfer: altsetting not found.");
        return UVC_ERROR;
    }

    if (total_transfer_size == 0 || packets_per_transfer == 0) {
        LOGE("@@@ uvc_isochronous_transfer: Failed to calculate a buffer length.");
        return UVC_ERROR;
    }

    if (uvc_set_interface_alt_setting(handle, altsetting) != UVC_SUCCESS) {
        LOGE("@@@ uvc_isochronous_transfer: Failed to set altsetting.");
        return UVC_ERROR;
    }

    for (int i = 0; i < TRANSFER_SIZE; i++) {
        struct uvc_endpoint_descriptor *endpoint;
        if (is_still) {
            endpoint = altsetting->still_endpoint;
        } else {
            endpoint = altsetting->video_endpoint;
        }

        handle->transfers[i] = uvc_create_transfer(handle, endpoint->bEndpointAddress, UVC_TRANSFER_TYPE_ISO,
                                                   total_transfer_size, (uint8_t) packets_per_transfer, endpoint_bytes_per_packet);
        if (handle->transfers[i] == NULL) {
            return UVC_OUT_OF_MEMORY;
        }
    }

    return UVC_SUCCESS;
}

/**
 * バルク転送用の構造体を作成します.
 *
 * @param handle UVCデバイスのハンドル
 * @return 構造体の作成に成功した場合はUVC_SUCCESS
 */
static uvc_result uvc_bulk_transfer(struct uvc_device_handle *handle) {
    uint32_t length = handle->video_control.dwMaxPayloadTransferSize;
    uint8_t endpoint = uvc_get_active_streaming_interface(handle)->header->bEndpointAddress;

    for (int i = 0; i < TRANSFER_SIZE; i++) {
        handle->transfers[i] = uvc_create_transfer(handle, endpoint, UVC_TRANSFER_TYPE_BULK, length, 0, 0);
        if (handle->transfers[i] == NULL) {
            return UVC_OUT_OF_MEMORY;
        }
    }
    return UVC_SUCCESS;
}

/**
 * UVC からのデータ転送を行う構造体とフレームバッファの構造体のメモリ解放を行います.
 *
 * @param handle UVCを操作するハンドラへのポインタ
 */
static void uvc_dispose_transfer_and_frame(struct uvc_device_handle *handle) {
    for (int i = 0; i < TRANSFER_SIZE; i++) {
        if (handle->transfers[i]) {
            if (handle->transfers[i]->urb) {
                uvc_cancel_iso_transfer(handle, handle->transfers[i]);
            }
            SAFE_FREE(handle->transfers[i]);
        }
    }

    for (int i = 0; i < UVC_FRAME_SIZE; i++) {
        SAFE_FREE(handle->frame[i]);
    }

    uvc_attach_kernel_driver_and_release(handle, uvc_get_video_streaming_interface_number(handle));
}

/**
 * UVC デバイスが isochronous が使用できるか確認します.
 *
 * @param handle UVCを操作するハンドラへのポインタ
 * @return isochronousの場合は UVC_TRUE 、それ以外の場合は UVC_FALSE
 */
static int uvc_is_isochronous(struct uvc_device_handle *handle) {
    struct uvc_video_streaming_altsetting *altsetting = uvc_get_active_streaming_altsetting(handle);
    while (altsetting) {
        // isochronousのフラグがONになっている endpoint かチェック
        if (altsetting->video_endpoint->bmAttributes & 0x01) {
            return UVC_TRUE;
        }
        altsetting = altsetting->next;
    }
    return UVC_FALSE;
}


static uvc_result uvc_create_fps_step(uint32_t **fps, uint32_t *length, uint32_t min, uint32_t max, uint32_t step) {
    uint32_t cnt = (max - min) / step;
    uint32_t *temp = calloc(cnt, sizeof(uint32_t));
    if (temp == NULL) {
        return UVC_OUT_OF_MEMORY;
    }

    for (int i = 0; i < cnt; i++) {
        temp[i] = min + step * i;
    }

    *fps = temp;
    *length = cnt;

    return UVC_SUCCESS;
}


static uvc_result uvc_create_fps_fixed(uint32_t **fps, uint32_t *length, uint32_t interval_size, uint32_t *intervals) {
    uint32_t *temp = calloc(interval_size, sizeof(uint32_t));
    if (temp == NULL) {
        return UVC_OUT_OF_MEMORY;
    }

    for (int i = 0; i < interval_size; i++) {
        temp[i] = (uint32_t) (10000000 / intervals[i]);
    }

    *fps = temp;
    *length = interval_size;

    return UVC_SUCCESS;
}


////////////////////// public /////////////////////////


/**
 * UVCデバイスのハンドルを作成します.
 *
 * @param fd UVCデバイスへのファイルディスクリプタ
 * @return UVCデバイスのハンドルへのポインタ
 */
struct uvc_device_handle *uvc_open_device(int fd) {
    struct uvc_device_handle *handle = (struct uvc_device_handle *) calloc(1, sizeof(struct uvc_device_handle));
    if (handle) {
        handle->fd = fd;

        if (!lseek(fd, 0, SEEK_SET)) {
            uint8_t desc[4096];
            ssize_t length = read(fd, desc, sizeof(desc));
            if (uvc_parse_descriptor(&handle->descriptor, desc, (uint32_t) length) != UVC_SUCCESS) {
                uvc_dispose_descriptor(&handle->descriptor);
                SAFE_FREE(handle);
                return NULL;
            }
        } else {
            SAFE_FREE(handle);
            return NULL;
        }

        uvc_get_configuration(handle, &handle->active_config);
        uvc_get_capabilities(handle, &handle->caps);
        uvc_detach_kernel_driver_and_claim(handle, uvc_get_video_control_interface_number(handle));

        LOGD("uvc version: %02X", uvc_get_uvc_version(&handle->descriptor, handle->active_config));
        LOGD("uvc_get_configuration: %02X", handle->active_config);
        LOGD("uvc_get_capabilities: %02X", handle->caps);
        LOGD("uvc_has_h264_extension: %02X", uvc_has_h264_extension(&handle->descriptor, handle->active_config));
    }
    return handle;
}

/**
 * UVCデバイスのプレビューを開始します.
 *
 * @param handle UVCデバイスのハンドル
 * @param formatIndex フォーマットインデックス
 * @param frameIndex フレームインデックス
 * @return カメラの開始に成功した場合はUVC_SUCCESSを返却、開始に失敗した場合にはUVC_ERRORなどを返却します。
 */
uvc_result uvc_start_video(struct uvc_device_handle *handle, uint8_t formatIndex, uint8_t frameIndex, uint32_t fps, int32_t use_h264) {
    uvc_result result;

    if (handle->running == UVC_VIDEO_RUNNING) {
        LOGW("@@@ uvc_start_video: uvv camera is already running.");
        return UVC_ALREADY_RUNNING;
    }

    uvc_detach_kernel_driver_and_claim(handle, uvc_get_video_streaming_interface_number(handle));

    if (use_h264) {
        result = uvc_extension_h264_negotiation(handle, formatIndex, frameIndex, fps);
        if (result != UVC_SUCCESS) {
            LOGE("@@@ uvc_start_video: Failed to negotiation a extension unit.");
            return result;
        }
    }

    result = uvc_stream_negotiation(handle, formatIndex, frameIndex, fps);
    if (result != UVC_SUCCESS) {
        LOGE("@@@ uvc_start_video: Failed to negotiation a video control.");
        return result;
    }

    if (uvc_is_isochronous(handle)) {
        result = uvc_isochronous_transfer(handle, UVC_FALSE);
    } else {
        result = uvc_bulk_transfer(handle);
    }

    if (result != UVC_SUCCESS) {
        uvc_dispose_transfer_and_frame(handle);
        LOGE("@@ uvc_start_video: Failed to create a transfer.");
        return result;
    }

    uint32_t dwMaxVideoFrameSize = uvc_get_max_frame_size(handle, UVC_FALSE);
    if (dwMaxVideoFrameSize == 0) {
        uvc_dispose_transfer_and_frame(handle);
        LOGE("@@@ uvc_start_video: Not found a dwMaxVideoFrameSize.");
        return UVC_ERROR;
    }

    for (int i = 0; i < UVC_FRAME_SIZE; i++) {
        handle->frame[i] = uvc_create_frame(handle->frame_type, dwMaxVideoFrameSize);
        if (handle->frame[i] == NULL) {
            uvc_dispose_transfer_and_frame(handle);
            LOGE("@@@ uvc_start_video: Failed to create a uvc_frame.");
            return UVC_OUT_OF_MEMORY;
        }
    }

    for (int i = 0; i < TRANSFER_SIZE; i++) {
        switch (handle->transfers[i]->type) {
            case UVC_TRANSFER_TYPE_ISO: {
                result = uvc_submit_iso_transfer(handle, handle->transfers[i]);
                if (result != UVC_SUCCESS) {
                    uvc_dispose_transfer_and_frame(handle);
                    return result;
                }
            }   break;

            case UVC_TRANSFER_TYPE_BULK: {
                result = uvc_submit_bulk_transfer(handle, handle->transfers[i]);
                if (result != UVC_SUCCESS) {
                    uvc_dispose_transfer_and_frame(handle);
                    return result;
                }
            }   break;

            default:
                LOGE("@@@ uvc_start_video: Unknown transfer type.");
                return UVC_ERROR;
        }
    }

    handle->running = UVC_VIDEO_RUNNING;

    return UVC_SUCCESS;
}

/**
 * UVCデバイスのプレビューを停止します.
 *
 * @param handle UVCデバイスのハンドル
 * @return
 */
uvc_result uvc_stop_video(struct uvc_device_handle *handle) {
    // ここではフラグを STOP に変更だけしておく
    // uvc_handle_event の中で後始末を行う。
    handle->running = UVC_VIDEO_STOP;
    return UVC_SUCCESS;
}

/**
 * UVCデバイスのハンドルを削除します.
 *
 * @param handle UVCデバイスのハンドルへのポインタ
 */
uvc_result uvc_close_device(struct uvc_device_handle *handle) {
    uvc_attach_kernel_driver_and_release(handle, uvc_get_video_control_interface_number(handle));
    uvc_dispose_transfer_and_frame(handle);
    uvc_dispose_descriptor(&handle->descriptor);
    SAFE_FREE(handle);
    return UVC_SUCCESS;
}

// uvc_handle_event の中でカウントするエラーの上限
#define UVC_HANDLE_EVENT_ERROR_COUNT 10

/**
 * UVCデバイスからのイベント処置を行います.
 *
 * uvc_stop_camera が呼び出されるまで、この関数は終了しません。
 * 必ず別のスレッドを作成して呼び出すことが必要です。
 *
 * @param handle UVCデバイスのハンドルへのポインタ
 */
uvc_result uvc_handle_event(struct uvc_device_handle *handle) {
    // アプリが強制終了したときにスレッドが生き残ることがある
    // この while を抜けることができなくなる可能性があるので、
    // error_count で処理を抜けるようにしておく。
    uint32_t error_count = 0;
    while (handle->running == UVC_VIDEO_RUNNING && error_count < UVC_HANDLE_EVENT_ERROR_COUNT) {
        if (uvc_poll_event(handle) != UVC_SUCCESS) {
            error_count++;
        } else {
            error_count = 0;
        }
    }

    if (error_count >= UVC_HANDLE_EVENT_ERROR_COUNT) {
        LOGE("@@@@ uvc_handle_event: UVC camera is stopped because an error occurred.");
    }

    // エラー終了の場合を考慮して、状態をSTOPにしておく
    handle->running = UVC_VIDEO_STOP;

    // frame と transfer を解放
    uvc_dispose_transfer_and_frame(handle);

    // エラーカウントが閾値を超えていた場合にはエラーを返却する
    return error_count < UVC_HANDLE_EVENT_ERROR_COUNT ? UVC_SUCCESS : UVC_ERROR;
}


uvc_result uvc_get_fps_list(struct uvc_vs_frame_descriptor *frame, uint32_t **fps, uint32_t *length, uint32_t *default_fps) {
    switch (frame->bDescriptorSubType) {
        case VS_FRAME_MJPEG: {
            struct uvc_vs_frame_mjpeg_descriptor *frame_mjpeg = (struct uvc_vs_frame_mjpeg_descriptor *) frame;
            *default_fps = 10000000 / frame_mjpeg->dwDefaultFrameInterval;
            if (frame_mjpeg->bFrameIntervalType == 0) {
                return uvc_create_fps_step(fps, length,
                                           frame_mjpeg->dwMinFrameInterval,
                                           frame_mjpeg->dwMaxFrameInterval,
                                           frame_mjpeg->dwFrameIntervalStep);
            } else {
                return uvc_create_fps_fixed(fps, length,
                                            frame_mjpeg->bFrameIntervalType,
                                            frame_mjpeg->dwFrameInterval);
            }
        }

        case VS_FRAME_UNCOMPRESSED: {
            struct uvc_vs_frame_uncompressed_descriptor *frame_uncompressed = (struct uvc_vs_frame_uncompressed_descriptor *) frame;
            *default_fps = 10000000 / frame_uncompressed->dwDefaultFrameInterval;
            if (frame_uncompressed->bFrameIntervalType == 0) {
                return uvc_create_fps_step(fps, length,
                                           frame_uncompressed->dwMinFrameInterval,
                                           frame_uncompressed->dwMaxFrameInterval,
                                           frame_uncompressed->dwFrameIntervalStep);
            } else {
                return uvc_create_fps_fixed(fps, length,
                                            frame_uncompressed->bFrameIntervalType,
                                            frame_uncompressed->dwFrameInterval);
            }
        }

        case VS_FRAME_H264 : {
            struct uvc_vs_frame_h264_descriptor *frame_h264 = (struct uvc_vs_frame_h264_descriptor *) frame;
            *default_fps = 10000000 / frame_h264->dwDefaultFrameInterval;
            return uvc_create_fps_fixed(fps, length,
                                        frame_h264->bNumFrameIntervals,
                                        frame_h264->dwFrameInterval);
        }

        default:
            LOGE("@@ uvc_get_fps: unknown frame type.");
            return UVC_ERROR;
    }
}


struct uvc_video_streaming_interface *uvc_get_active_streaming_interface(struct uvc_device_handle *handle) {
    return uvc_get_video_streaming_interface(&handle->descriptor, handle->active_config);
}


struct uvc_video_control_interface *uvc_get_active_control_interface(struct uvc_device_handle *handle) {
    return uvc_get_video_control_interface(&handle->descriptor, handle->active_config);
}


struct uvc_video_streaming_altsetting *uvc_get_active_streaming_altsetting(struct uvc_device_handle *handle) {
    return uvc_get_video_streaming_altsetting(&handle->descriptor, handle->active_config);
}


struct uvc_vc_extension_unit_descriptor *uvc_get_active_extension_descriptor(struct uvc_device_handle *handle) {
    return uvc_find_extension_descriptor(&handle->descriptor, handle->active_config);
}


uvc_result uvc_disconnect_interface(struct uvc_device_handle *handle, unsigned int interface) {
    struct usbdevfs_disconnect_claim dc;
    dc.interface = interface;
    strcpy(dc.driver, UVC_DRIVER_NAME);
    dc.flags = USBDEVFS_DISCONNECT_CLAIM_EXCEPT_DRIVER;

    int r = xioctl(handle->fd, USBDEVFS_DISCONNECT_CLAIM, &dc);
    if (r == 0) {
        struct usbdevfs_ioctl command;
        command.ifno = interface;
        command.ioctl_code = USBDEVFS_DISCONNECT;
        command.data = NULL;
        r = xioctl(handle->fd, USBDEVFS_IOCTL, &command) == 0 ? UVC_SUCCESS : UVC_ERROR;
        if (r == 0) {
            return UVC_SUCCESS;
        }
    }
    return UVC_ERROR;
}


uvc_result uvc_set_configuration(struct uvc_device_handle *handle, unsigned int config_id) {
    int r = xioctl(handle->fd, USBDEVFS_SETCONFIGURATION, &config_id);
    if (r == 0) {
        handle->active_config = (uint8_t) config_id;
        return UVC_SUCCESS;
    }
    return UVC_ERROR;
}


uint8_t uvc_get_still_capture_method(struct uvc_device_handle *handle) {
    struct uvc_video_streaming_interface *streaming_interface = uvc_get_active_streaming_interface(handle);
    if (streaming_interface == NULL || streaming_interface->header == NULL) {
        return 0;
    }
    return streaming_interface->header->bStillCaptureMethod;
}


uvc_result uvc_capture_still_image(struct uvc_device_handle *handle, uint8_t formatIndex, uint8_t frameIndex, uint8_t compressionIndex) {
    uvc_result result;

    if (handle->running == UVC_VIDEO_RUNNING) {
        LOGW("@@@ uvc_capture_still_image: uvv camera is already running.");
        return UVC_ALREADY_RUNNING;
    }

    uint8_t method = uvc_get_still_capture_method(handle);
    if (method != METHOD_2 && method != METHOD_3) {
        LOGW("@@@ uvc_capture_still_image: Not supported a still capture.");
        return UVC_ERROR;
    }

    uvc_detach_kernel_driver_and_claim(handle, uvc_get_video_streaming_interface_number(handle));

    result = uvc_still_negotiation(handle, formatIndex, frameIndex, compressionIndex);
    if (result != UVC_SUCCESS) {
        LOGE("@@@ uvc_capture_still_image: Failed to negotiation a still control.");
        return result;
    }

    if (uvc_is_isochronous(handle)) {
        result = uvc_isochronous_transfer(handle, UVC_TRUE);
    } else {
        result = uvc_bulk_transfer(handle);
    }

    if (result != UVC_SUCCESS) {
        uvc_dispose_transfer_and_frame(handle);
        return result;
    }

    uint32_t dwMaxVideoFrameSize = uvc_get_max_frame_size(handle, UVC_TRUE);
    if (dwMaxVideoFrameSize == 0) {
        LOGE("@@@ uvc_capture_still_image: Not found a dwMaxVideoFrameSize.");
        return UVC_ERROR;
    }

    for (int i = 0; i < UVC_FRAME_SIZE; i++) {
        handle->frame[i] = uvc_create_frame(handle->frame_type, dwMaxVideoFrameSize);
        if (handle->frame[i] == NULL) {
            uvc_dispose_transfer_and_frame(handle);
            return UVC_OUT_OF_MEMORY;
        }
    }

    for (int i = 0; i < TRANSFER_SIZE; i++) {
        switch (handle->transfers[i]->type) {
            case UVC_TRANSFER_TYPE_ISO: {
                result = uvc_submit_iso_transfer(handle, handle->transfers[i]);
                if (result != UVC_SUCCESS) {
                    uvc_dispose_transfer_and_frame(handle);
                    return result;
                }
            }   break;

            case UVC_TRANSFER_TYPE_BULK: {
                result = uvc_submit_bulk_transfer(handle, handle->transfers[i]);
                if (result != UVC_SUCCESS) {
                    uvc_dispose_transfer_and_frame(handle);
                    return result;
                }
            }   break;

            default:
                LOGE("@@@ uvc_capture_still_image: Unknown transfer type.");
                return UVC_ERROR;
        }
    }

    handle->running = UVC_VIDEO_RUNNING;

    return UVC_SUCCESS;
}


uvc_result uvc_set_camera_terminal_control(struct uvc_device_handle *handle, int control, void* value, int length) {
    struct uvc_video_control_interface *control_interface = uvc_get_active_control_interface(handle);
    if (control_interface == NULL) {
        return UVC_ERROR;
    }

    struct uvc_vc_input_terminal_descriptor *input = control_interface->input_terminal;
    if (input == NULL) {
        return UVC_ERROR;
    }

    uint16_t wValue = (uint16_t) (control << 8);
    uint16_t wIndex = (input->bTerminalID << 8) | control_interface->interface->bInterfaceNumber;
    uint16_t wLength = (uint16_t) length;
    return uvc_control_transfer(handle, REQ_TYPE_SET, SET_CUR, wValue, wIndex, wLength, value) > 0 ? UVC_SUCCESS : UVC_ERROR;
}


uvc_result uvc_get_camera_terminal_control(struct uvc_device_handle *handle, int control, int request, void *value, int length) {
    struct uvc_video_control_interface *control_interface = uvc_get_active_control_interface(handle);
    if (control_interface == NULL) {
        return UVC_ERROR;
    }

    struct uvc_vc_input_terminal_descriptor *input = control_interface->input_terminal;
    if (input == NULL) {
        return UVC_ERROR;
    }

    uint16_t wValue = (uint16_t) (control << 8);
    uint16_t wIndex = (input->bTerminalID << 8) | control_interface->interface->bInterfaceNumber;
    uint16_t wLength = (uint16_t) length;
    return uvc_control_transfer(handle, REQ_TYPE_GET, (uint8_t) request, wValue, wIndex, wLength, value) > 0 ? UVC_SUCCESS : UVC_ERROR;
}


uvc_result uvc_set_processing_unit_control(struct uvc_device_handle *handle, int control, void* value, int length) {
    struct uvc_video_control_interface *control_interface = uvc_get_active_control_interface(handle);
    if (control_interface == NULL) {
        return UVC_ERROR;
    }

    struct uvc_vc_processing_unit_descriptor *processing = control_interface->processing;
    if (processing == NULL) {
        return UVC_ERROR;
    }

    uint16_t wValue = (uint16_t) (control << 8);
    uint16_t wIndex = (processing->bUnitID << 8) | control_interface->interface->bInterfaceNumber;
    uint16_t wLength = (uint16_t) length;
    return uvc_control_transfer(handle, REQ_TYPE_SET, SET_CUR, wValue, wIndex, wLength, value) > 0 ? UVC_SUCCESS : UVC_ERROR;
}


uvc_result uvc_get_processing_unit_control(struct uvc_device_handle *handle, int control, int request, void *value, int length) {
    struct uvc_video_control_interface *control_interface = uvc_get_active_control_interface(handle);
    if (control_interface == NULL) {
        return UVC_ERROR;
    }

    struct uvc_vc_processing_unit_descriptor *processing = control_interface->processing;
    if (processing == NULL) {
        return UVC_ERROR;
    }

    uint16_t wValue = (uint16_t) (control << 8);
    uint16_t wIndex = (processing->bUnitID << 8) | control_interface->interface->bInterfaceNumber;
    uint16_t wLength = (uint16_t) length;
    return uvc_control_transfer(handle, REQ_TYPE_GET, (uint8_t) request, wValue, wIndex, wLength, value) > 0 ? UVC_SUCCESS : UVC_ERROR;
}


uvc_result uvc_set_encoding_unit_control(struct uvc_device_handle *handle, int control, void* value, int length) {
    struct uvc_video_control_interface *control_interface = uvc_get_active_control_interface(handle);
    if (control_interface == NULL) {
        return UVC_ERROR;
    }

    struct uvc_vc_encoding_unit_descriptor *encoding = control_interface->encoding;
    if (encoding == NULL) {
        return UVC_ERROR;
    }

    uint16_t wValue = (uint16_t) (control << 8);
    uint16_t wIndex = (encoding->bUnitID << 8) | control_interface->interface->bInterfaceNumber;
    uint16_t wLength = (uint16_t) length;
    return uvc_control_transfer(handle, REQ_TYPE_SET, SET_CUR, wValue, wIndex, wLength, value) > 0 ? UVC_SUCCESS : UVC_ERROR;
}


uvc_result uvc_get_encoding_unit_control(struct uvc_device_handle *handle, int control, int request, void *value, int length) {
    struct uvc_video_control_interface *control_interface = uvc_get_active_control_interface(handle);
    if (control_interface == NULL) {
        return UVC_ERROR;
    }

    struct uvc_vc_encoding_unit_descriptor *encoding = control_interface->encoding;
    if (encoding == NULL) {
        return UVC_ERROR;
    }

    uint16_t wValue = (uint16_t) (control << 8);
    uint16_t wIndex = (encoding->bUnitID << 8) | control_interface->interface->bInterfaceNumber;
    uint16_t wLength = (uint16_t) length;
    return uvc_control_transfer(handle, REQ_TYPE_GET, (uint8_t) request, wValue, wIndex, wLength, value) > 0 ? UVC_SUCCESS : UVC_ERROR;
}
