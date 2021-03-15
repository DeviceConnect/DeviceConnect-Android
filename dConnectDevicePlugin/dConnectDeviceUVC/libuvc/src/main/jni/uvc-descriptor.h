/*
 uvc-descriptor.h
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
#ifndef UVC_DESCRIPTOR_H
#define UVC_DESCRIPTOR_H

#ifdef __cplusplus
extern "C" {
#endif

#include <stdint.h>
#include "common.h"

#include <linux/version.h>
#if LINUX_VERSION_CODE > KERNEL_VERSION(2, 6, 20)
#include <linux/usb/ch9.h>
#else
#include <linux/usb_ch9.h>
#endif


/**
 * Video Interface Class Code.
 */
enum uvc_interface_class {
    CC_VIDEO = 0x0E
};

/**
 * Video Interface Subclass Codes.
 */
enum uvc_interface_subclass {
    SC_UNDEFINED = 0x00,
    SC_VIDEOCONTROL = 0x01,
    SC_VIDEOSTREAMING = 0x02,
    SC_VIDEO_INTERFACE_COLLECTION = 0x03
};

/**
 * Descriptor Types.
 */
enum uvc_descriptor_type {
    DEVICE = 0x01,
    CONFIGURATION = 0x02,
    STRING = 0x03,
    INTERFACE = 0x04,
    ENDPOINT = 0x05,
    DEVICE_QUALIFIER = 0x06,
    OTHER_SPEED_CONFIGURATION = 0x07,
    INTERFACE_POWER = 0x08,

    // USB 2.0
    OTG = 0x09,
    INTERFACE_ASSOCIATION = 0x0B,

    // UVC
    CS_UNDEFINED = 0x20,
    CS_DEVICE = 0x21,
    CS_CONFIGURATION = 0x22,
    CS_STRING = 0x23,
    CS_INTERFACE = 0x24,
    CS_ENDPOINT = 0x25
};

/**
 * Video Class-Specific VC Interface Descriptor Subtypes.
 */
enum uvc_vc_descriptor_subtype {
    VC_DESCRIPTOR_UNDEFINED = 0x00,
    VC_HEADER = 0x01,
    VC_INPUT_TERMINAL = 0x02,
    VC_OUTPUT_TERMINAL = 0x03,
    VC_SELECTOR_UNIT = 0x04,
    VC_PROCESSING_UNIT = 0x05,
    VC_EXTENSION_UNIT = 0x06,
    VC_ENCODING_UNIT = 0x07
};


/**
 * Video Class-Specific VS Interface Descriptor Subtypes.
 */
enum uvc_vs_descriptor_subtype {
    VS_UNDEFINED = 0x00,
    VS_INPUT_HEADER = 0x01,
    VS_OUTPUT_HEADER = 0x02,
    VS_STILL_IMAGE_FRAME = 0x03,
    VS_FORMAT_UNCOMPRESSED = 0x04,
    VS_FRAME_UNCOMPRESSED = 0x05,
    VS_FORMAT_MJPEG = 0x06,
    VS_FRAME_MJPEG = 0x07,
    VS_FORMAT_MPEG2TS = 0x0A,
    VS_FORMAT_DV = 0x0C,
    VS_COLORFORMAT = 0x0D,
    VS_FORMAT_FRAME_BASED = 0x10,
    VS_FRAME_FRAME_BASED = 0x11,
    VS_FORMAT_STREAM_BASED = 0x12,
    VS_FORMAT_H264 = 0x13,
    VS_FRAME_H264 = 0x14,
    VS_FORMAT_H264_SIMULCAST = 0x15,
    VS_FORMAT_VP8 = 0x16,
    VS_FRAME_VP8 = 0x17,
    VS_FORMAT_VP8_SIMULCAST = 0x18
};

/**
 * Video Class-Specific Endpoint Descriptor Subtypes.
 */
enum uvc_ep_descriptor_subtype {
    EP_UNDEFINED = 0x00,
    EP_GENERAL = 0x01,
    EP_ENDPOINT = 0x02,
    EP_INTERRUPT = 0x03
};


/**
 * Input Terminal Types.
 */
enum uvc_input_terminal_types {
    ITT_VENDOR_SPECIFIC = 0x0200,
    ITT_CAMERA = 0x0201,
    ITT_MEDIA_TRANSPORT_INPUT = 0x0202
};


/**
 * Camera Terminal Controls.
 */
enum uvc_camera_terminal_control {
    SCANNING_MODE = 1,
    AUTO_EXPOSURE_MODE = (1 << 1),
    AUTO_EXPOSURE_PRIORITY = (1 << 2),
    EXPOSURE_TIME_ABSOLUTE = (1 << 3),
    EXPOSURE_TIME_RELATIVE = (1 << 4),
    FOCUS_ABSOLUTE = (1 << 5),
    FOCUS_RELATIVE = (1 << 6),
    IRIS_ABSOLUTE = (1 << 7),
    IRIS_RELATIVE = (1 << 8),
    ZOOM_ABSOLUTE = (1 << 9),
    ZOOM_RELATIVE = (1 << 10),
    PANTILT_ABSOLUTE = (1 << 11),
    PANTILT_RELATIVE = (1 << 12),
    ROLL_ABSOLUTE = (1 << 13),
    ROLL_RELATIVE = (1 << 14),
    RESERVED_1 = (1 << 15),
    RESERVED_2 = (1 << 16),
    FOCUS_AUTO = (1 << 17),
    PRIVACY = (1 << 18)
};


/**
 * Processing Unit Controls.
 */
enum uvc_processing_control {
    BRIGHTNESS = 1,
    CONTRAST = (1 << 1),
    HUE = (1 << 2),
    SATURATION = (1 << 3),
    SHARPNESS = (1 << 4),
    GAMMA = (1 << 5),
    WHITE_BALANCE_TEMPERATURE = (1 << 6),
    WHITE_BALANCE_COMPONENT = (1 << 7),
    BACKLIGHT_COMPENSATION = (1 << 8),
    GAIN = (1 << 9),
    POWER_LINE_FREQUENCY = (1 << 10),
    HUE_AUTO = (1 << 11),
    WHITE_BALANCE_TEMPERATURE_AUTO = (1 << 12),
    WHITE_BALANCE_COMPONENT_AUTO = (1 << 13),
    DIGITAL_MULTIPLIER = (1 << 14),
    DIGITAL_MULTIPLIER_LIMIT = (1 << 15),
    ANALOG_VIDEO_STANDARD = (1 << 16),
    ANALOG_VIDEO_LOCK_STATUS = (1 << 17)
};


/**
 * 静止画のメソッドを定義します.
 */
enum uvc_vs_still_capture_method {
    METHOD_0 = 0,
    METHOD_1 = 1,
    METHOD_2 = 2,
    METHOD_3 = 3
};


/**
 * 無圧縮時のフォーマットを定義します.
 */
enum uvc_uncompressed_format {
    UNKNOWN = -1,
    YUY2 = 0,
    NV12 = 1,
    M420 = 2,
    I420 = 3
};


/**
 * Device Descriptor.
 */
struct uvc_device_descriptor {
    uint8_t bLength;
    uint8_t bDescriptorType;
    uint16_t bcdUSB;
    uint8_t bDeviceClass;
    uint8_t bDeviceSubClass;
    uint8_t bDeviceProtocol;
    uint8_t bMaxPacketSize0;
    uint16_t idVendor;
    uint16_t idProduct;
    uint16_t bcdDevice;
    uint8_t iManufacturer;
    uint8_t iProduct;
    uint8_t iSerialNumber;
    uint8_t bNumConfigurations;
};

/**
 * Configuration Descriptor.
 */
struct uvc_configuration_descriptor {
    struct uvc_configuration_descriptor *next;
    struct uvc_video_control_interface *control_interface;
    struct uvc_video_streaming_interface *streaming_interface;
    struct uvc_video_streaming_altsetting *altsetting;

    uint8_t bLength;
    uint8_t bDescriptorType;
    uint16_t wTotalLength;
    uint8_t bNumInterfaces;
    uint8_t bConfigurationValue;
    uint8_t iConfiguration;
    uint8_t bmAttributes;
    uint8_t bMaxPower;
};

/**
 * String Descriptor.
 */
struct uvc_string_descriptor {
    uint8_t bLength;
    uint8_t bDescriptorType;
    uint8_t *wLANGID;
};

/**
 * Interface Descriptor.
 */
struct uvc_interface_descriptor {
    uint8_t bLength;
    uint8_t bDescriptorType;
    uint8_t bInterfaceNumber;
    uint8_t bAlternateSetting;
    uint8_t bNumEndpoints;
    uint8_t bInterfaceClass;
    uint8_t bInterfaceSubClass;
    uint8_t bInterfaceProtocol;
    uint8_t iInterface;
};

/**
 * EndPoint Descriptor.
 */
struct uvc_endpoint_descriptor {
    struct uvc_endpoint_descriptor *next;

    uint8_t bLength;
    uint8_t bDescriptorType;
    uint8_t bEndpointAddress;
    uint8_t bmAttributes;
    uint16_t wMaxPacketSize;
    uint8_t bInterval;
};

/**
 * Interrupt Endpoint Descriptor.
 */
struct uvc_interrupt_endpoint_descriptor {
    struct uvc_interrupt_endpoint_descriptor *next;

    uint8_t bLength;
    uint8_t bDescriptorType;
    uint8_t bDescriptorSubType;
    uint16_t wMaxTransferSize;
};

/**
 * Interface Association Descriptor.
 */
struct uvc_interface_association_descriptor {
    uint8_t bLength;
    uint8_t bDescriptorType;
    uint8_t bFirstInterface;
    uint8_t bInterfaceCount;
    uint8_t bFunctionClass;
    uint8_t bFunctionSubClass;
    uint8_t bFunctionProtocol;
    uint8_t iFunction;
};

//// Video Control Descriptor

/**
 * Class-specific VC Interface Descriptor.
 */
struct uvc_vc_header_descriptor {
    uint8_t bLength;
    uint8_t bDescriptorType;
    uint8_t bDescriptorSubType;
    uint16_t bcdUVC;
    uint16_t wTotalLength;
    uint32_t dwClockFrequency;
    uint8_t bInCollection;
    uint8_t *baInterfaceNr;
};

/**
 * Input Terminal Descriptor.
 */
struct uvc_vc_input_terminal_descriptor {
    struct uvc_vc_input_terminal_descriptor *next;

    uint8_t bLength;
    uint8_t bDescriptorType;
    uint8_t bDescriptorSubType;
    uint8_t bTerminalID;
    uint16_t wTerminalType;
    uint8_t bAssocTerminal;
    uint8_t iTerminal;

    // Camera
    uint16_t wObjectiveFocalLengthMin;
    uint16_t wObjectiveFocalLengthMax;
    uint16_t wOcularFocalLength;

    uint8_t bControlSize;
    uint8_t *bmControls;

    // Media Transport
    uint8_t bTransportModeSize;
    uint8_t *bmTransportModes;
};

/**
 * Output Terminal Descriptor.
 */
struct uvc_vc_output_terminal_descriptor {
    struct uvc_vc_output_terminal_descriptor *next;

    uint8_t bLength;
    uint8_t bDescriptorType;
    uint8_t bDescriptorSubType;
    uint8_t bTerminalID;
    uint16_t wTerminalType;
    uint8_t bAssocTerminal;
    uint8_t bSourceID;
    uint8_t iTerminal;
};

/**
 * Selector Unit Descriptor.
 */
struct uvc_vc_selector_unit_descriptor {
    struct uvc_vc_selector_unit_descriptor *next;

    uint8_t bLength;
    uint8_t bDescriptorType;
    uint8_t bDescriptorSubType;
    uint8_t bUnitID;
    uint8_t bNrInPins;
    uint8_t *baSourceID;
    uint8_t iSelector;
};

/**
 * Processing Unit Descriptor.
 */
struct uvc_vc_processing_unit_descriptor {
    struct uvc_vc_processing_unit_descriptor *next;

    uint8_t bLength;
    uint8_t bDescriptorType;
    uint8_t bDescriptorSubType;
    uint8_t bUnitID;
    uint8_t bSourceID;
    uint16_t wMaxMultiplier;
    uint8_t bControlSize;
    uint8_t *bmControls;
    uint8_t iProcessing;
    uint8_t bmVideoStandards;
};

/**
 * Extension Unit Descriptor
 */
struct uvc_vc_extension_unit_descriptor {
    struct uvc_vc_extension_unit_descriptor *next;

    uint8_t bLength;
    uint8_t bDescriptorType;
    uint8_t bDescriptorSubType;
    uint8_t bUnitID;
    uint8_t guidExtensionCode[16];
    uint8_t bNumControls;
    uint8_t bNrInPins;
    uint8_t *baSourceID;
    uint8_t bControlSize;
    uint8_t *bmControls;
    uint8_t iExtension;
};

/**
 * Encoding Unit Descriptor.
 */
struct uvc_vc_encoding_unit_descriptor {
    struct uvc_vc_encoding_unit_descriptor *next;

    uint8_t bLength;
    uint8_t bDescriptorType;
    uint8_t bDescriptorSubType;
    uint8_t bUnitID;
    uint8_t bSourceID;
    uint8_t iEncoding;
    uint8_t bControlSize;  // The value must be 3.
    uint8_t *bmControls;
    uint8_t *bmControlsRuntime;
};

//// Video Streaming Descriptor

/**
 * Class-specific VS Header Descriptor (Input).
 */
struct uvc_vs_header_descriptor {
    uint8_t bLength;
    uint8_t bDescriptorType;
    uint8_t bDescriptorSubType;
    uint8_t bNumFormats;
    uint16_t wTotalLength;
    uint8_t bEndpointAddress;
    uint8_t bmInfo;
    uint8_t bTerminalLink;
    uint8_t bStillCaptureMethod;
    uint8_t bTriggerSupport;
    uint8_t bTriggerUsage;
    uint8_t bControlSize;
    uint8_t *bmaControls;
};


struct uvc_video_streaming_interface;


/**
 * Video Stream Format Descriptor で共通部分を定義した構造体.
 * <p>
 * 基本的に Format Descriptor が共通でもつデータを先頭に定義しておき
 * キャストすることで簡単にアクセスできるようにする。
 *
 * ただし、各 format で共通部分の配置を変えてしまうとキャストした時に
 * データが壊れてしまうので、十分に注意すること。
 * </p>
 */
struct uvc_vs_format_descriptor {
    struct uvc_vs_format_descriptor *next;
    struct uvc_vs_frame_descriptor *frame;
    struct uvc_video_streaming_interface *streaming_interface;

    uint8_t bLength;
    uint8_t bDescriptorType;
    uint8_t bDescriptorSubType;
    uint8_t bFormatIndex;
    uint8_t bNumFrameDescriptors;
};

/**
 * Video Stream Frame Descriptor で共通部分を定義した構造体.
 * <p>
 * 基本的に Frame Descriptor が共通でもつデータを先頭に定義しておき
 * キャストすることで簡単にアクセスできるようにする。
 *
 * ただし、各 frame で共通部分の配置を変えてしまうとキャストした時に
 * データが壊れてしまうので、十分に注意すること。
 * </p>
 */
struct uvc_vs_frame_descriptor {
    struct uvc_vs_frame_descriptor *next;

    uint8_t bLength;
    uint8_t bDescriptorType;
    uint8_t bDescriptorSubType;
    uint8_t bFrameIndex;
    uint8_t bmCapabilities;
    uint16_t wWidth;
    uint16_t wHeight;
};

/**
 * Class-specific VS Format Descriptor (MJPEG).
 *
 * struct uvc_vs_format_descriptor と先頭の構造を同じにしています。
 * 構造の配置が変わってしまうとデータが壊れてしまうので注意すること。
 */
struct uvc_vs_format_mjpeg_descriptor {
    struct uvc_vs_format_mjpeg_descriptor *next;
    struct uvc_vs_frame_mjpeg_descriptor *frame;
    struct uvc_video_streaming_interface *streaming_interface;

    uint8_t bLength;
    uint8_t bDescriptorType;
    uint8_t bDescriptorSubType;
    uint8_t bFormatIndex;
    uint8_t bNumFrameDescriptors;
    uint8_t bmFlags;
    uint8_t bDefaultFrameIndex;
    uint8_t bAspectRatioX;
    uint8_t bAspectRatioY;
    uint8_t bmInterlaceFlags;
    uint8_t bCopyProtect;
};

/**
 * Class-specific VS Frame Descriptor (MJPEG).
 *
 * struct uvc_vs_frame_descriptor と先頭の構造を同じにしています。
 * 構造の配置が変わってしまうとデータが壊れてしまうので注意すること。
 */
struct uvc_vs_frame_mjpeg_descriptor {
    struct uvc_vs_frame_mjpeg_descriptor *next;

    uint8_t bLength;
    uint8_t bDescriptorType;
    uint8_t bDescriptorSubType;
    uint8_t bFrameIndex;
    uint8_t bmCapabilities;
    uint16_t wWidth;
    uint16_t wHeight;
    uint32_t dwMinBitRate;
    uint32_t dwMaxBitRate;
    uint32_t dwMaxVideoFrameBufferSize;
    uint32_t dwDefaultFrameInterval;
    uint8_t bFrameIntervalType;
    uint32_t dwMinFrameInterval;
    uint32_t dwMaxFrameInterval;
    uint32_t dwFrameIntervalStep;
    uint32_t *dwFrameInterval;
};

/**
 * Class-specific VS Format Descriptor (Uncompressed).
 *
 * struct uvc_vs_format_descriptor と先頭の構造を同じにしています。
 * 構造の配置が変わってしまうとデータが壊れてしまうので注意すること。
 */
struct uvc_vs_format_uncompressed_descriptor {
    struct uvc_vs_frame_uncompressed_descriptor *frame;
    struct uvc_vs_format_uncompressed_descriptor *next;
    struct uvc_video_streaming_interface *streaming_interface;

    uint8_t bLength;
    uint8_t bDescriptorType;
    uint8_t bDescriptorSubType;
    uint8_t bFormatIndex;
    uint8_t bNumFrameDescriptors;
    uint8_t guidFormat[16];
    uint8_t bBitsPerPixel;
    uint8_t bDefaultFrameIndex;
    uint8_t bAspectRatioX;
    uint8_t bAspectRatioY;
    uint8_t bmInterlaceFlags;
    uint8_t bCopyProtect;
};

/**
 * Class-specific VS Frame Descriptor (Uncompressed).
 *
 * struct uvc_vs_frame_descriptor と先頭の構造を同じにしています。
 * 構造の配置が変わってしまうとデータが壊れてしまうので注意すること。
 */
struct uvc_vs_frame_uncompressed_descriptor {
    struct uvc_vs_frame_uncompressed_descriptor *next;

    uint8_t bLength;
    uint8_t bDescriptorType;
    uint8_t bDescriptorSubType;
    uint8_t bFrameIndex;
    uint8_t bmCapabilities;
    uint16_t wWidth;
    uint16_t wHeight;
    uint32_t dwMinBitRate;
    uint32_t dwMaxBitRate;
    uint32_t dwMaxVideoFrameBufferSize;
    uint32_t dwDefaultFrameInterval;
    uint8_t bFrameIntervalType;
    uint32_t dwMinFrameInterval;
    uint32_t dwMaxFrameInterval;
    uint32_t dwFrameIntervalStep;
    uint32_t *dwFrameInterval;
};

/**
 * H.264 Payload Video Format Descriptor.
 *
 * struct uvc_vs_format_descriptor と先頭の構造を同じにしています。
 * 構造の配置が変わってしまうとデータが壊れてしまうので注意すること。
 */
struct uvc_vs_format_h264_descriptor {
    struct uvc_vs_format_h264_descriptor *next;
    struct uvc_vs_frame_h264_descriptor *frame;
    struct uvc_video_streaming_interface *streaming_interface;

    uint8_t bLength;
    uint8_t bDescriptorType;
    uint8_t bDescriptorSubType;
    uint8_t bFormatIndex;
    uint8_t bNumFrameDescriptors;
    uint8_t bDefaultFrameIndex;
    uint8_t bMaxCodecConfigDelay;
    uint8_t bmSupportedSliceModes;
    uint8_t bmSupportedSyncFrameTypes;
    uint8_t bResuolutionScaling;
    uint8_t Reserved1;
    uint8_t bmSupportedRateControlModes;
    uint16_t wMaxMBperSecOneResolutionNoScalability;
    uint16_t wMaxMBperSecTwoResolutionsNoScalability;
    uint16_t wMaxMBperSecThreeResolutionsNoScalability;
    uint16_t wMaxMBperSecFourResolutionsNoScalability;
    uint16_t wMaxMBperSecOneResolutionTemporalScalability;
    uint16_t wMaxMBperSecTwoResolutionsTemporalScalablility;
    uint16_t wMaxMBperSecThreeResolutionsTemporalScalability;
    uint16_t wMaxMBperSecFourResolutionsTemporalScalability;
    uint16_t wMaxMBperSecOneResolutionTemporalQualityScalability;
    uint16_t wMaxMBperSecTwoResolutionsTemporalQualityScalability;
    uint16_t wMaxMBperSecThreeResolutionsTemporalQualityScalability;
    uint16_t wMaxMBperSecFourResolutionsTemporalQualityScalability;
    uint16_t wMaxMBperSecOneResolutionsTemporalSpatialScalability;
    uint16_t wMaxMBperSecTwoResolutionsTemporalSpatialScalability;
    uint16_t wMaxMBperSecThreeResolutionsTemporalSpatialScalability;
    uint16_t wMaxMBperSecFourResolutionsTemporalSpatialScalability;
    uint16_t wMaxMBperSecOneResolutionFullScalability;
    uint16_t wMaxMBperSecTwoResolutionsFullScalability;
    uint16_t wMaxMBperSecThreeResolutionsFullScalability;
    uint16_t wMaxMBperSecFourResolutionsFullScalability;
};


/**
 * H.264 Payload Video Frame Descriptor.
 *
 * struct uvc_vs_frame_descriptor と先頭の構造を同じにしています。
 * 構造の配置が変わってしまうとデータが壊れてしまうので注意すること。
 */
struct uvc_vs_frame_h264_descriptor {
    struct uvc_vs_frame_h264_descriptor *next;

    uint8_t bLength;
    uint8_t bDescriptorType;
    uint8_t bDescriptorSubType;
    uint8_t bFrameIndex;
    uint8_t placeHolder;          // h264 には bmCapabilities がないので、代用として uint8_t を入れる
    uint16_t wWidth;
    uint16_t wHeight;
    uint16_t wSARwidth;
    uint16_t wSARheight;
    uint16_t wProfie;
    uint8_t bLevelIDC;
    uint16_t wConstrainedToolset;
    uint32_t bmSupportedUsages;
    uint16_t bmCapabilities;
    uint32_t bmSVCCapabilities;
    uint32_t bmMVCCapabilities;
    uint32_t dwMinBitRate;
    uint32_t dwMaxBitRate;
    uint32_t dwDefaultFrameInterval;
    uint8_t bNumFrameIntervals;
    uint32_t *dwFrameInterval;
};

/**
 * Class-specific Still Image Frame Descriptor.
 */
struct uvc_vs_still_image_frame_descriptor {
    uint8_t bLength;
    uint8_t bDescriptorType;
    uint8_t bDescriptorSubType;
    uint8_t bEndpointAddress;
    uint8_t bNumImageSizePatterns;
    uint16_t *wWidth;
    uint16_t *wHeight;
    uint8_t bNumCompressionPtn;
    uint8_t *bCompression;
};

/**
 * Class-specific Color Matching Descriptor.
 */
struct uvc_vs_color_matching_descriptor {
    struct uvc_vs_color_matching_descriptor *next;

    uint8_t bLength;
    uint8_t bDescriptorType;
    uint8_t bDescriptorSubType;
    uint8_t bColorPrimaries;
    uint8_t bTransferCharacteristics;
    uint8_t bMatrixCoefficients;
};

/**
 * Video Control I/F.
 */
struct uvc_video_control_interface {
    struct uvc_interface_descriptor *interface;
    struct uvc_vc_header_descriptor *header;
    struct uvc_vc_input_terminal_descriptor *input_terminal;
    struct uvc_vc_output_terminal_descriptor *output_terminal;
    struct uvc_vc_selector_unit_descriptor *selector;
    struct uvc_vc_processing_unit_descriptor *processing;
    struct uvc_vc_extension_unit_descriptor *extension;
    struct uvc_vc_encoding_unit_descriptor *encoding;
    struct uvc_endpoint_descriptor *endpoint;
    struct uvc_interrupt_endpoint_descriptor *interrupt_endpoint;
};

/**
 * Video Streaming I/F.
 *
 * Alt. Setting 0
 */
struct uvc_video_streaming_interface {
    struct uvc_video_streaming_interface *next;

    struct uvc_interface_descriptor *interface;
    struct uvc_vs_header_descriptor *header;
    struct uvc_vs_format_descriptor *format;
    struct uvc_vs_still_image_frame_descriptor *frame_still;
    struct uvc_vs_color_matching_descriptor *color_matching;
    struct uvc_endpoint_descriptor *still_endpoint;
};

/**
 * Video Streaming I/F.
 *
 * Alt. Setting 1 - n
 */
struct uvc_video_streaming_altsetting {
    struct uvc_video_streaming_altsetting *next;

    struct uvc_interface_descriptor *interface;
    struct uvc_endpoint_descriptor *video_endpoint;
    struct uvc_endpoint_descriptor *still_endpoint;
};


/**
 * UVC Descriptor.
 */
struct uvc_descriptor {
    struct uvc_device_descriptor *device;
    struct uvc_configuration_descriptor *configuration;
    struct uvc_interface_association_descriptor *interface_association;
};

/**
 * Descriptor のコンフィグを取得します。
 *
 * @param descriptor データを格納する構造体
 * @param config_id コンフィグID
 *
 * @return コンフィグレーションのディスクリプタ
 */
struct uvc_configuration_descriptor *uvc_get_configuration_descriptor(struct uvc_descriptor *descriptor, uint8_t config_id);

/**
 * UVC に必要な Descriptor を解釈して構造体にデータを格納します.
 *
 * @param descriptor データを格納する構造体
 * @param buffer Descriptorが格納されているバッファ
 * @param length バッファサイズ
 * @return UVC_SUCCESSの場合には解析に成功、それ以外の場合は解析に失敗
 */
uvc_result uvc_parse_descriptor(struct uvc_descriptor *descriptor, uint8_t *buffer, int32_t length);

/**
 * Descriptor で確保したメモリを解放します.
 *
 * @param descriptor Descriptorのデータを格納した構造体
 */
void uvc_dispose_descriptor(struct uvc_descriptor *descriptor);

/**
 * bcdUVCの値を取得します.
 *
 * @param descriptor Descriptorのデータを格納した構造体
 * @param config_id bcdUVCが属しているコンフィギュレーションのID
 * @return bcdUVCの値
 */
uint16_t uvc_get_uvc_version(struct uvc_descriptor *descriptor, uint8_t config_id);

/**
 * 指定されたコンフィギュレーションのIDに対応する uvc_video_control_interface を取得します.
 *
 * @param descriptor Descriptorのデータを格納した構造体
 * @param config_id コンフィギュレーションのID
 * @return uvc_video_control_interface のポインタ、対応した uvc_video_control_interface が無い場合にはNULLを返却します。
 */
struct uvc_video_control_interface *uvc_get_video_control_interface(struct uvc_descriptor *descriptor, uint8_t config_id);

/**
 * 指定されたコンフィギュレーションのIDに対応する uvc_video_streaming_interface を取得します.
 *
 * @param descriptor Descriptorのデータを格納した構造体
 * @param config_id コンフィギュレーションのID
 * @return uvc_video_streaming_interfaceのポインタ、対応した uvc_video_streaming_interface のポインタが無い場合にはNULLを返却します。
 */
struct uvc_video_streaming_interface *uvc_get_video_streaming_interface(struct uvc_descriptor *descriptor, uint8_t config_id);

/**
 * 指定されたコンフィギュレーションのIDに対応する uvc_video_streaming_altsetting を取得します.
 *
 * @param descriptor Descriptorのデータを格納した構造体
 * @param config_id コンフィギュレーションのID
 * @return uvc_video_streaming_altsetting のポインタ、対応した uvc_video_streaming_altsetting のポインタが無い場合にはNULLを返却します。
 */
struct uvc_video_streaming_altsetting *uvc_get_video_streaming_altsetting(struct uvc_descriptor *descriptor, uint8_t config_id);

/**
 * 指定されたインデックスのフォーマットを取得します.
 *
 * @param descriptor Descriptorのデータを格納した構造体
 * @param config_id コンフィギュレーションのID
 * @param format_index フォーマットインデックス
 * @return 対応したフォーマットディスクリプタ、対応したフォーマットディスクリプタが見つからない場合にはNULL
 */
struct uvc_vs_format_descriptor *uvc_find_format_descriptor(struct uvc_descriptor *descriptor, uint8_t config_id, uint8_t format_index);

/**
 * 指定されたインデックスのフレームディスクリプタを取得します.
 *
 * @param descriptor Descriptorのデータを格納した構造体
 * @param config_id コンフィギュレーションのID
 * @param format_index フォーマットインデックス
 * @param frame_index フレームインデックス
 * @return 対応したフレームディスクリプタ、対応したフレームディスクリプタが見つからない場合にはNULL
 */
struct uvc_vs_frame_descriptor *uvc_find_frame_descriptor(struct uvc_descriptor *descriptor, uint8_t config_id, uint8_t format_index, uint8_t frame_index);

/**
 * 無圧縮のフォーマットを取得します.
 *
 * @param format フォーマットのデゥスクリプタ
 * @return 無圧縮のフォーマット
 */
enum uvc_uncompressed_format uvc_get_uncompressed_format(struct uvc_vs_format_descriptor *format);


#ifdef __cplusplus
}
#endif
#endif //UVC_DESCRIPTOR_H
