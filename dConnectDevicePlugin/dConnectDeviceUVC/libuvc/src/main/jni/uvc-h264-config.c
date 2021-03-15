/*
 uvc-h264-config.c
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
#include <string.h>

#include "uvc-h264-config.h"

static const uint8_t GUID_UVCX_H264_XU[] = {
        0x41, 0x76, 0x9e, 0xa2, 0x04, 0xde, 0xe3, 0x47, 0x8b, 0x2b, 0xf4, 0x34, 0x1a, 0xff, 0x00, 0x3b
};


uint8_t uvc_has_h264_extension(struct uvc_descriptor *descriptor, uint8_t config_id) {
    if (descriptor == NULL) {
        return UVC_FALSE;
    }

    struct uvc_configuration_descriptor *config = uvc_get_configuration_descriptor(descriptor, config_id);
    if (config) {
        struct uvc_vc_extension_unit_descriptor *extension = config->control_interface->extension;
        while (extension) {
            if (memcmp(extension->guidExtensionCode, GUID_UVCX_H264_XU, 16) == 0) {
                return UVC_TRUE;
            }
            extension = extension->next;
        }
    }

    return UVC_FALSE;
}


struct uvc_vc_extension_unit_descriptor *uvc_find_extension_descriptor(struct uvc_descriptor *descriptor, uint8_t config_id) {
    if (descriptor == NULL) {
        return NULL;
    }

    struct uvc_configuration_descriptor *config = uvc_get_configuration_descriptor(descriptor, config_id);
    if (config) {
        struct uvc_vc_extension_unit_descriptor *extension = config->control_interface->extension;
        while (extension) {
            if (memcmp(extension->guidExtensionCode, GUID_UVCX_H264_XU, 16) == 0) {
                return extension;
            }
            extension = extension->next;
        }
    }

    return NULL;
}


void uvc_print_h264_extension_unit(struct uvc_h264_extension_unit *config) {
    LOGI("h264_extension");
    LOGI("    h264_extension.dwFrameInterval: %d", config->dwFrameInterval);
    LOGI("    h264_extension.dwBitRate: %d", config->dwBitRate);
    LOGI("    h264_extension.bmHints: 0x%04X", config->bmHints);
    LOGI("    h264_extension.wConfigurationIndex: %d", config->wConfigurationIndex);
    LOGI("    h264_extension.wWidth: %d", config->wWidth);
    LOGI("    h264_extension.wHeight: %d", config->wHeight);
    LOGI("    h264_extension.wSliceUnits: %d", config->wSliceUnits);
    LOGI("    h264_extension.wSliceMode: %d", config->wSliceMode);
    LOGI("    h264_extension.wProfile: 0x%04X", config->wProfile);
    LOGI("    h264_extension.wIFramePeriod: %d", config->wIFramePeriod);
    LOGI("    h264_extension.wEstimatedVideoDelay: %d", config->wEstimatedVideoDelay);
    LOGI("    h264_extension.wEstimatedMaxConfigDelay: %d", config->wEstimatedMaxConfigDelay);
    LOGI("    h264_extension.bUsageType: 0x%02X", config->bUsageType);
    LOGI("    h264_extension.bRateControlMode: 0x%02X", config->bRateControlMode);
    LOGI("    h264_extension.bTemporalScaleMode: 0x%02X", config->bTemporalScaleMode);
    LOGI("    h264_extension.bSpatialScaleMode: 0x%02X", config->bSpatialScaleMode);
    LOGI("    h264_extension.bSNRScaleMode: 0x%02X", config->bSNRScaleMode);
    LOGI("    h264_extension.bStreamMuxOption: 0x%02X", config->bStreamMuxOption);
    LOGI("    h264_extension.bStreamFormat: 0x%02X", config->bStreamFormat);
    LOGI("    h264_extension.bEntropyCABAC: 0x%02X", config->bEntropyCABAC);
    LOGI("    h264_extension.bTimestamp: 0x%02X", config->bTimestamp);
    LOGI("    h264_extension.bNumOfReorderFrames: 0x%02X", config->bNumOfReorderFrames);
    LOGI("    h264_extension.bPreviewFlipped: 0x%02X", config->bPreviewFlipped);
    LOGI("    h264_extension.bView: 0x%02X", config->bView);
    LOGI("    h264_extension.bReserved1: 0x%02X", config->bReserved1);
    LOGI("    h264_extension.bReserved2: 0x%02X", config->bReserved2);
    LOGI("    h264_extension.bStreamID: 0x%02X", config->bStreamID);
    LOGI("    h264_extension.bSpatialLayerRatio: 0x%02X", config->bSpatialLayerRatio);
    LOGI("    h264_extension.wLeakyBucketSize: %d", config->wLeakyBucketSize);
}
