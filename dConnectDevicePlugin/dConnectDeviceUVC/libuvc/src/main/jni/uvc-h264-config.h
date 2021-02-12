/*
 uvc-h264-config.h
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
#ifndef UVC_H264_CONFIG_H
#define UVC_H264_CONFIG_H

#ifdef __cplusplus
extern "C" {
#endif


#include "uvc-descriptor.h"


/**
 * H.264 用拡張定義.
 */
enum {
    UVCX_VIDEO_UNDEFINED = 0x00,
    UVCX_VIDEO_CONFIG_PROBE = 0x01,
    UVCX_VIDEO_CONFIG_COMMIT = 0x02,
    UVCX_RATE_CONTROL_MODE = 0x03,
    UVCX_TEMPORAL_SCALE_MODE = 0x04,
    UVCX_SPATIAL_SCALE_MODE = 0x05,
    UVCX_SNR_SCALE_MODE = 0x06,
    UVCX_LTR_BUFFER_SIZE_CONTROL = 0x07,
    UVCX_LTR_PICTURE_CONTROL = 0x08,
    UVCX_PICTURE_TYPE_CONTROL = 0x09,
    UVCX_VERSION = 0x0A,
    UVCX_ENCODER_RESET = 0x0B,
    UVCX_FRAMERATE_CONFIG = 0x0C,
    UVCX_VIDEO_ADVANCE_CONFIG = 0x0D,
    UVCX_BITRATE_LAYERS = 0x0E,
    UVCX_QP_STEPS_LAYERS = 0x0F,
};

enum {
    UVC_H264_BMHINTS_RESOLUTION = 0x0001,
    UVC_H264_BMHINTS_PROFILE = 0x0002,
    UVC_H264_BMHINTS_RATECONTROL = 0x0004,
    UVC_H264_BMHINTS_USAGE = 0x0008,
    UVC_H264_BMHINTS_SLICEMODE = 0x0010,
    UVC_H264_BMHINTS_SLICEUNITS = 0x0020,
    UVC_H264_BMHINTS_MVCVIEW = 0x0040,
    UVC_H264_BMHINTS_TEMPORAL = 0x0080,
    UVC_H264_BMHINTS_SNR = 0x0100,
    UVC_H264_BMHINTS_SPATIAL = 0x0200,
    UVC_H264_BMHINTS_SPATIAL_RATIO = 0x0400,
    UVC_H264_BMHINTS_FRAME_INTERVAL = 0x0800,
    UVC_H264_BMHINTS_LEAKY_BKT_SIZE = 0x1000,
    UVC_H264_BMHINTS_BITRATE = 0x2000,
    UVC_H264_BMHINTS_ENTROPY = 0x4000,
    UVC_H264_BMHINTS_IFRAMEPERIOD = 0x8000
};

enum {
    UVC_H264_SLICEMODE_IGNORED = 0x0000,
    UVC_H264_SLICEMODE_BITSPERSLICE = 0x0001,
    UVC_H264_SLICEMODE_MBSPERSLICE = 0x0002,
    UVC_H264_SLICEMODE_SLICEPERFRAME = 0x0003
};

enum {
    UVC_H264_USAGETYPE_REALTIME = 0x01,
    UVC_H264_USAGETYPE_BROADCAST = 0x02,
    UVC_H264_USAGETYPE_STORAGE = 0x03,
    UVC_H264_USAGETYPE_UCCONFIG_0 = 0x04,
    UVC_H264_USAGETYPE_UCCONFIG_1 = 0x05,
    UVC_H264_USAGETYPE_UCCONFIG_2Q = 0x06,
    UVC_H264_USAGETYPE_UCCONFIG_2S = 0x07,
    UVC_H264_USAGETYPE_UCCONFIG_3 = 0x08,
};

enum {
    UVC_H264_RATECONTROL_CBR = 0x01,
    UVC_H264_RATECONTROL_VBR = 0x02,
    UVC_H264_RATECONTROL_CONST_QP = 0x03,
};

enum {
    UVC_H264_STREAMFORMAT_ANNEXB = 0x00,
    UVC_H264_STREAMFORMAT_NAL = 0x01,
};

enum {
    UVC_H264_ENTROPY_CAVLC = 0x00,
    UVC_H264_ENTROPY_CABAC = 0x01,
};

enum {
    UVC_H264_TIMESTAMP_SEI_DISABLE = 0x00,
    UVC_H264_TIMESTAMP_SEI_ENABLE = 0x01
};

enum {
    UVC_H264_PREFLIPPED_DISABLE = 0x00,
    UVC_H264_PREFLIPPED_HORIZONTAL = 0x01
};

enum {
    UVC_H264_PROFILE_BASELINE = 0x4200,
    UVC_H264_PROFILE_MAIN = 0x4D00,
    UVC_H264_PROFILE_HIGH = 0x6400,
    UVC_H264_PROFILE_SCALABLE_BASELINE = 0x5300,
    UVC_H264_PROFILE_SCALABLE_HIGH = 0x5600,
    UVC_H264_PROFILE_MULTIVIEW_HIGH = 0x7600,
    UVC_H264_PROFILE_STEREO_HIGH = 0x8000,
    UVC_H264_PROFILE_CONSTRAINED_BASELINE = 0x4240,
};

enum {
    UVC_H264_PICTYPE_I_FRAME = 0x00,
    UVC_H264_PICTYPE_IDR = 0x01,
    UVC_H264_PICTYPE_IDR_WITH_PPS_SPS = 0x02
};

enum {
    UVC_H264_MUX_OPTION_DISABLE = 0x00,
    UVC_H264_MUX_OPTION_ENABLE = 0x01,
    UVC_H264_MUX_OPTION_H264 = 0x02,
    UVC_H264_MUX_OPTION_YUY2 = 0x04,
    UVC_H264_MUX_OPTION_NV12 = 0x08,
    UVC_H264_MUX_OPTION_MJPEG_CONTAINER = 0x40,
};

/**
 * UVC 1.1 において、H.264 拡張として定義された構造体.
 */
struct uvc_h264_extension_unit {
    uint32_t dwFrameInterval;
    uint32_t dwBitRate;
    uint16_t bmHints;
    uint16_t wConfigurationIndex;
    uint16_t wWidth;
    uint16_t wHeight;
    uint16_t wSliceUnits;
    uint16_t wSliceMode;
    uint16_t wProfile;
    uint16_t wIFramePeriod;
    uint16_t wEstimatedVideoDelay;
    uint16_t wEstimatedMaxConfigDelay;
    uint8_t bUsageType;
    uint8_t bRateControlMode;
    uint8_t bTemporalScaleMode;
    uint8_t bSpatialScaleMode;
    uint8_t bSNRScaleMode;
    uint8_t bStreamMuxOption;
    uint8_t bStreamFormat;
    uint8_t bEntropyCABAC;
    uint8_t bTimestamp;
    uint8_t bNumOfReorderFrames;
    uint8_t bPreviewFlipped;
    uint8_t bView;
    uint8_t bReserved1;
    uint8_t bReserved2;
    uint8_t bStreamID;
    uint8_t bSpatialLayerRatio;
    uint16_t wLeakyBucketSize;
};


/**
 * H264 のエクステンションユニットを持っているか確認します.
 *
 * @param descriptor Descriptorのデータを格納した構造体
 * @param config_id コンフィギュレーションのID
 * @return エクステンションがある場合には UVC_TRUE、それ以外は UVC_FALSE
 */
uint8_t uvc_has_h264_extension(struct uvc_descriptor *descriptor, uint8_t config_id);

/**
 * H264 のエクステンションユニットを取得します.
 * @param descriptor Descriptorのデータを格納した構造体
 * @param config_id コンフィギュレーションのID
 * @return エクステンションユニット、存在しない場合には NULL
 */
struct uvc_vc_extension_unit_descriptor *uvc_find_extension_descriptor(struct uvc_descriptor *descriptor, uint8_t config_id);

/**
 * H264 の設定を出力します.
 *
 * @param config H264 の設定
 */
void uvc_print_h264_extension_unit(struct uvc_h264_extension_unit *config);

#ifdef __cplusplus
}
#endif
#endif //UVC_H264_CONFIG_H
