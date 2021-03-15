/*
 uvc.h
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
#ifndef UVC_H
#define UVC_H

#ifdef __cplusplus
extern "C" {
#endif


#include <stdint.h>
#include "common.h"
#include "uvc-descriptor.h"
#include "uvc-h264-config.h"

/** VideoStreaming interface control selector (A.9.7) */
enum {
    VS_CONTROL_UNDEFINED = 0x00,
    VS_PROBE_CONTROL = 0x01,
    VS_COMMIT_CONTROL = 0x02,
    VS_STILL_PROBE_CONTROL = 0x03,
    VS_STILL_COMMIT_CONTROL = 0x04,
    VS_STILL_IMAGE_TRIGGER_CONTROL = 0x05,
    VS_STREAM_ERROR_CODE_CONTROL = 0x06,
    VS_GENERATE_KEY_FRAME_CONTROL = 0x07,
    VS_UPDATE_FRAME_SEGMENT_CONTROL = 0x08,
    VS_SYNCH_DELAY_CONTROL = 0x09
};


/**
 * Video Class-Specific Request Codes.
 */
enum uvc_request_code {
    GET_STATUS = 0x00,
    CLEAR_FEATURE = 0x01,
    SET_FEATURE = 0x03,
    SET_ADDRESS = 0x05,
    GET_DESCRIPTOR = 0x06,
    SET_DESCRIPTOR = 0x07,
    GET_CONFIGURATION = 0x08,
    SET_CONFIGURATION = 0x09,
    GET_INTERFACE = 0x0a,
    SET_INTERFACE = 0x0b,
    SYNCH_FRAME = 0x0c,

    // UVC
    RC_UNDEFINED = 0x00,
    SET_CUR = 0x01,
    SET_CUR_ALL = 0x11,
    GET_CUR = 0x81,
    GET_MIN = 0x82,
    GET_MAX = 0x83,
    GET_RES = 0x84,
    GET_LEN = 0x85,
    GET_INFO = 0x86,
    GET_DEF = 0x87,
    GET_CUR_ALL = 0x91,
    GET_MIN_ALL = 0x92,
    GET_MAX_ALL = 0x93,
    GET_RES_ALL = 0x94,
    GET_DEF_ALL = 0x97
};


enum {
    TYPE_CT = 0,
    TYPE_PU = 1,
    TYPE_EU = 2
};

enum {
    CT_CONTROL_UNDEFINED = 0x00,
    CT_SCANNING_MODE_CONTROL = 0x01,
    CT_AE_MODE_CONTROL = 0x02,
    CT_AE_PRIORITY_CONTROL = 0x03,
    CT_EXPOSURE_TIME_ABSOLUTE_CONTROL = 0x04,
    CT_EXPOSURE_TIME_RELATIVE_CONTROL = 0x05,
    CT_FOCUS_ABSOLUTE_CONTROL= 0x06,
    CT_FOCUS_RELATIVE_CONTROL = 0x07,
    CT_FOCUS_AUTO_CONTROL = 0x08,
    CT_IRIS_ABSOLUTE_CONTROL = 0x09,
    CT_IRIS_RELATIVE_CONTROL = 0x0A,
    CT_ZOOM_ABSOLUTE_CONTROL = 0x0B,
    CT_ZOOM_RELATIVE_CONTROL = 0x0C,
    CT_PANTILT_ABSOLUTE_CONTROL = 0x0D,
    CT_PANTILT_RELATIVE_CONTROL = 0x0E,
    CT_ROLL_ABSOLUTE_CONTROL = 0x0F,
    CT_ROLL_RELATIVE_CONTROL = 0x10,
    CT_PRIVACY_CONTROL = 0x11,
    CT_FOCUS_SIMPLE_CONTROL = 0x12,
    CT_WINDOW_CONTROL = 0x13,
    CT_REGION_OF_INTEREST_CONTROL = 0x14
};


enum {
    PU_CONTROL_UNDEFINED = 0x00,
    PU_BACKLIGHT_COMPENSATION_CONTROL = 0x01,
    PU_BRIGHTNESS_CONTROL = 0x02,
    PU_CONTRAST_CONTROL = 0x03,
    PU_GAIN_CONTROL = 0x04,
    PU_POWER_LINE_FREQUENCY_CONTROL = 0x05,
    PU_HUE_CONTROL = 0x06,
    PU_SATURATION_CONTROL = 0x07,
    PU_SHARPNESS_CONTROL = 0x08,
    PU_GAMMA_CONTROL = 0x09,
    PU_WHITE_BALANCE_TEMPERATURE_CONTROL = 0x0A,
    PU_WHITE_BALANCE_TEMPERATURE_AUTO_CONTROL = 0x0B,
    PU_WHITE_BALANCE_COMPONENT_CONTROL = 0x0C,
    PU_WHITE_BALANCE_COMPONENT_AUTO_CONTROL = 0x0D,
    PU_DIGITAL_MULTIPLIER_CONTROL = 0x0E,
    PU_DIGITAL_MULTIPLIER_LIMIT_CONTROL = 0x0F,
    PU_HUE_AUTO_CONTROL = 0x10,
    PU_ANALOG_VIDEO_STANDARD_CONTROL = 0x11,
    PU_ANALOG_LOCK_STATUS_CONTROL = 0x12,
    PU_CONTRAST_AUTO_CONTROL = 0x13
};


enum {
    EU_CONTROL_UNDEFINED = 0x00,
    EU_SELECT_LAYER_CONTROL = 0x01,
    EU_PROFILE_TOOLSET_CONTROL = 0x02,
    EU_VIDEO_RESOLUTION_CONTROL = 0x03,
    EU_MIN_FRAME_INTERVAL_CONTROL = 0x04,
    EU_SLICE_MODE_CONTROL = 0x05,
    EU_RATE_CONTROL_MODE_CONTROL = 0x06,
    EU_AVERAGE_BITRATE_CONTROL = 0x07,
    EU_CPB_SIZE_CONTROL = 0x08,
    EU_PEAK_BIT_RATE_CONTROL = 0x09,
    EU_QUANTIZATION_PARAMS_CONTROL = 0x0A,
    EU_SYNC_REF_FRAME_CONTROL = 0x0B,
    EU_LTR_BUFFER_CONTROL = 0x0C,
    EU_LTR_PICTURE_CONTROL = 0x0D,
    EU_LTR_VALIDATION_CONTROL = 0x0E,
    EU_LEVEL_IDC_LIMIT_CONTROL = 0x0F,
    EU_SEI_PAYLOADTYPE_CONTROL = 0x10,
    EU_QP_RANGE_CONTROL = 0x11,
    EU_PRIORITY_CONTROL = 0x12,
    EU_START_OR_STOP_LAYER_CONTROL = 0x13,
    EU_ERROR_RESILIENCY_CONTROL = 0x14
};


/** Payload header flags (2.4.3.3) */
#define UVC_STREAM_EOH (1 << 7)
#define UVC_STREAM_ERR (1 << 6)
#define UVC_STREAM_STI (1 << 5)
#define UVC_STREAM_RES (1 << 4)
#define UVC_STREAM_SCR (1 << 3)
#define UVC_STREAM_PTS (1 << 2)
#define UVC_STREAM_EOF (1 << 1)
#define UVC_STREAM_FID (1 << 0)


struct uvc_device_handle;
struct uvc_frame;


/**
 * フレームバッファを通知するコールバック関数の定義.
 */
typedef void (*UVC_FRAME_CALLBACK)(void *user, struct uvc_frame *frame);


/**
 * UVC デバイスの動作状態を定義.
 */
typedef enum _uvc_status {
    /**
     * UVC デバイスの動画撮影が停止中.
     */
    UVC_VIDEO_STOP = 0,

    /**
     * UVC デバイスの動画撮影が動作中.
     */
    UVC_VIDEO_RUNNING = 1
} uvc_status;


/**
 * Video Probe and Commit Controls.
 */
struct uvc_video_control {
    uint16_t bmHint;
    uint8_t bFormatIndex;
    uint8_t bFrameIndex;
    uint32_t dwFrameInterval;
    uint16_t wKeyFrameRate;
    uint16_t wPFrameRate;
    uint16_t wCompQuality;
    uint16_t wCompWindowSize;
    uint16_t wDelay;
    uint32_t dwMaxVideoFrameSize;
    uint32_t dwMaxPayloadTransferSize;

    // UVC 1.1
    uint32_t dwClockFrequency;
    uint8_t bmFramingInfo;
    uint8_t bPreferedVersion;
    uint8_t bMinVersion;
    uint8_t bMaxVersion;

    // UVC 1.5
    uint8_t bUsage;
    uint8_t bBitDepthLuma;
    uint8_t bmSettings;
    uint8_t bMaxNumberOfRefFramesPlus1;
    uint16_t bmRateControlModes;
    uint64_t bmLayoutPerStream;
};

/**
 * Still Probe and Commit Controls.
 */
struct uvc_still_control {
    uint8_t bFormatIndex;
    uint8_t bFrameIndex;
    uint8_t bCompressionIndex;
    uint32_t dwMaxVideoFrameSize;
    uint32_t dwMaxPayloadTransferSize;

    // interfaceNumberを保持
    uint8_t bInterfaceNumber;
};


/**
 * フレームバッファのサイズを定義.
 */
#define UVC_FRAME_SIZE 2

/**
 * UVC デバイスから送られてくるフレームデータ.
 */
struct uvc_frame {
    /**
     * フレームタイプ.
     */
    uint8_t type;

    /**
     * Presentation Time Stamp.
     */
    uint32_t pts;

    /**
     * System Time Clock.
     */
    uint32_t stc;

    /**
     * 1KHz SOF token counter.
     */
    uint16_t sof;

    /**
     * UVC デバイスから取得したデータサイズ.
     */
    uint32_t got_bytes;

    /**
     * フレームのバッファサイズ.
     */
    uint32_t length;

    /**
     * フレームバッファ.
     * <p>
     * 配列が0で定義してあるが、length のサイズ分だけメモリが確保されています。
     * </p>
     */
    uint8_t buf[0];
};

/**
 * リクエストの個数.
 */
#define TRANSFER_SIZE 10


enum {
    UVC_TRANSFER_TYPE_ISO = 1,
    UVC_TRANSFER_TYPE_BULK = 2
};


/**
 * UVC デバイスへのリクエストとレスポンスを格納する構造体.
 */
struct uvc_transfer {
    /**
     * UVC デバイスのハンドル.
     */
    struct uvc_device_handle *handle;

    /**
     * linux の USB 要求ブロック.
     */
    struct usbdevfs_urb *urb;

    /**
     * 転送タイプ.
     */
    uint8_t type;

    /**
     * 転送を行うエンドポイント.
     */
    uint8_t endpoint;

    /**
     * 転送用バッファのサイズ.
     */
    uint32_t length;

    /**
     * アイソクロナス転送用のパケット数.
     */
    uint8_t num_iso_packet;

    /**
     * アイソクロナス転送用のパケットのサイズ.
     */
    uint32_t size_iso_packet;

    /**
     * 転送用バッファ.
     */
    uint8_t buf[0];
};


/**
 * UVC デバイスを管理する構造体.
 */
struct uvc_device_handle {
    /**
     * UVC デバイスのディスクリプタ.
     */
    struct uvc_descriptor descriptor;

    /**
     * UVC デバイスのビデオをコントロールするための構造体.
     *
     * 「4.3.1.1 Video Probe and Commit Controls」で定義されている。
     */
    struct uvc_video_control video_control;

    /**
     * UVC デバイスの静止画をコントロールするための構造値.
     *
     * 「4.3.1.2 Video Still Probe Control and Still Commit Control」で定義されている。
     */
    struct uvc_still_control still_control;

    /**
     * H.264 用 Extension Unit.
     */
    struct uvc_h264_extension_unit h264_extension;

    /**
     * UVC デバイスへのファイルディスクリプタ.
     */
    int fd;

    /**
     * UVC デバイスの動作状態.
     */
    uvc_status running;

    /**
     * UVC デバイスのコンフィグ.
     */
    uint8_t active_config;

    /**
     * UVC デバイスの機能.
     */
    uint32_t caps;

    /**
     * bInterfaceNumber を保持.
     */
    uint8_t bInterfaceNumber;

    /**
     * 選択されたフレームタイプ.
     */
    uint8_t frame_type;

    /**
     * H264 多重化フラグ.
     *
     * H264 を MJPEG や YUV に多重化する場合は UVC_TRUE を設定します。
     */
    int use_ext_h264;

    /**
     * 現在処理を行なっているフレームID.
     * <p>
     * UVC では、0, 1 が交互に送られてくる。
     * </p>
     */
    int frame_id;

    /**
     * UVC デバイスから送られてくるフレームデータを格納する変数.
     * <p>
     * UVC では、フレームは0,1なので、2個用意しておく。
     * </p>
     */
    struct uvc_frame *frame[UVC_FRAME_SIZE];

    /**
     * UVC デバイスへ送るリクエストを格納する変数.
     */
    struct uvc_transfer *transfers[TRANSFER_SIZE];

    /**
     * フレームを通知する関数.
     */
    UVC_FRAME_CALLBACK callback;

    /**
     * フレームに通知するときのユーザが設定するポインタ.
     */
    void *user;
};


/**
 * uvc_device_handle を作成して、デバイスから DescriptorParser を読み込む。
 *
 * @param fd UVCデバイスのファイルディスクリプタ
 * @return uvc_device_handle のポインタ
 */
struct uvc_device_handle *uvc_open_device(int fd);

/**
 * 指定された UVC デバイスの動画撮影を開始します.
 *
 * @param handle UVCデバイス
 * @param formatIndex フォーマットインデックス
 * @param frameIndex フレームインデックス
 * @param fps フレームレート
 * @param use_h264 H264 Extension 使用フラグ
 * @return
 */
uvc_result uvc_start_video(struct uvc_device_handle *handle, uint8_t formatIndex, uint8_t frameIndex, uint32_t fps, int32_t use_h264);

/**
 * 指定された UVC デバイスの動画撮影を停止します.
 *
 * @param handle UVCデバイス
 * @return
 */
uvc_result uvc_stop_video(struct uvc_device_handle *handle);

/**
 * UVC デバイスをクローズします.
 * <p>
 * 一旦、クローズすると動かなくなりますので、ご注意ください。
 * </p>
 * @param handle UVCデバイス
 * @return
 */
uvc_result uvc_close_device(struct uvc_device_handle *handle);

/**
 * UVC デバイスのイベント処理を行う関数.
 * <p>
 * iso や bulk などの転送処理を行うのでメインスレッドではなく、別のスレッドを作成して呼び出してください。
 * </p>
 * @param handle UVCデバイス
 * @return
 */
uvc_result uvc_handle_event(struct uvc_device_handle *handle);

/**
 * UVCデバイスが持つ fps のリストを取得します.
 * @param frame フレーム
 * @param fps FPSを格納するポインタ
 * @param length FPSのリスト数を格納する変数
 * @param default_fps デフォルトのFPSを格納する変数
 * @return UVC_SUCCESSの場合はFPSリストの取得に成功、それ以外はリストの取得に失敗
 */
uvc_result uvc_get_fps_list(struct uvc_vs_frame_descriptor *frame, uint32_t **fps, uint32_t *length, uint32_t *default_fps);

/**
 * アクティブに設定されている uvc_video_streaming_interface の構造体を取得します.
 *
 * @param handle UVCデバイス
 * @return uvc_video_streaming_interfaceのポインタ、取得できない場合には NULL を返却します。
 */
struct uvc_video_streaming_interface *uvc_get_active_streaming_interface(struct uvc_device_handle *handle);

/**
 * アクティブに設定されている uvc_video_control_interface の構造体を取得します.
 *
 * @param handle UVCデバイス
 * @return uvc_video_control_interface、取得できない場合には NULL を返却します。
 */
struct uvc_video_control_interface *uvc_get_active_control_interface(struct uvc_device_handle *handle);

/**
 * アクティブに設定されている uvc_video_streaming_altsetting の構造体を取得します.
 *
 * @param handle UVCデバイス
 * @return uvc_video_streaming_altsetting、取得できない場合には NULL を返却します。
 */
struct uvc_video_streaming_altsetting *uvc_get_active_streaming_altsetting(struct uvc_device_handle *handle);

/**
 * アクティブに設定されている uvc_vc_extension_unit_descriptor の構造体を取得します.
 *
 * @param handle UVCデバイス
 * @return uvc_vc_extension_unit_descriptor、取得できない場合には NULL を返却します。
 */
struct uvc_vc_extension_unit_descriptor *uvc_get_active_extension_descriptor(struct uvc_device_handle *handle);

/**
 * アクティブにするコンフィギュレーションを設定します.
 *
 * @param handle UVCデバイス
 * @param config_id アクティブにするコンフィギュレーションID
 * @return UVC_SUCCESSの場合は設定に成功、それ以外は設定に失敗
 */
uvc_result uvc_set_configuration(struct uvc_device_handle *handle, unsigned int config_id);

/**
 * 指定されたインターフェースを解除します.
 * <p>
 * Android OS側でclaimされたインターフェースを解除します。
 * </p>
 * @param handle UVCデバイス
 * @param interface 解除するインターフェース
 * @return UVC_SUCCESSの場合は解除に成功、それ以外は解除に失敗
 */
uvc_result uvc_disconnect_interface(struct uvc_device_handle *handle, unsigned int interface);

/**
 * 静止画のモードを取得します.
 *
 * @param handle UVCデバイス
 * @return METHOD_0の場合は未サポート
 *         METHOD_1の場合はハードキーのみ対応
 *         METHOD_2の場合はプレビューと排他的撮影可能
 *         METHOD_3の場合にはプレビューと同時撮影可能
 */
uint8_t uvc_get_still_capture_method(struct uvc_device_handle *handle);

/**
 * 静止画の撮影を行います.
 *
 * @param handle UVCデバイス
 * @param formatIndex フォーマットインデックス
 * @param frameIndex フレームインデックス
 * @param compressionIndex 圧縮インデックス
 * @return UVC_SUCCESSの場合は撮影に成功、それ以外は撮影に失敗
 */
uvc_result uvc_capture_still_image(struct uvc_device_handle *handle, uint8_t formatIndex, uint8_t frameIndex, uint8_t compressionIndex);

/**
 * カメラターミナルコントロールの設定を行います.
 *
 * @param handle UVCデバイス
 * @param control コントロールID
 * @param value 設定する値
 * @param length 設定する値のサイズ
 * @return UVC_SUCCESSの場合は撮影に成功、それ以外は撮影に失敗
 */
uvc_result uvc_set_camera_terminal_control(struct uvc_device_handle *handle, int control, void* value, int length);

/**
 * カメラターミナルコントロールの値を取得します.
 *
 * @param handle UVCデバイス
 * @param control コントロールID
 * @param request リクエストタイプ
 * @param value 設定する値
 * @param length 設定する値のサイズ
 * @return UVC_SUCCESSの場合は撮影に成功、それ以外は撮影に失敗
 */
uvc_result uvc_get_camera_terminal_control(struct uvc_device_handle *handle, int control, int request, void *value, int length);

/**
 * プロセシングユニットコントロールの設定を行います.
 *
 * @param handle UVCデバイス
 * @param control コントロールID
 * @param value 設定する値
 * @param length 設定する値のサイズ
 * @return UVC_SUCCESSの場合は撮影に成功、それ以外は撮影に失敗
 */
uvc_result uvc_set_processing_unit_control(struct uvc_device_handle *handle, int control, void* value, int length);

/**
 * プロセシングユニットコントロールの値を取得します.
 *
 * @param handle UVCデバイス
 * @param control コントロールID
 * @param request リクエストタイプ
 * @param value 設定する値
 * @param length 設定する値のサイズ
 * @return UVC_SUCCESSの場合は撮影に成功、それ以外は撮影に失敗
 */
uvc_result uvc_get_processing_unit_control(struct uvc_device_handle *handle, int control, int request, void *value, int length);

/**
 * エンコーディングユニットコントロールの設定を行います.
 *
 * @param handle UVCデバイス
 * @param control コントロールID
 * @param value 設定する値
 * @param length 設定する値のサイズ
 * @return UVC_SUCCESSの場合は撮影に成功、それ以外は撮影に失敗
 */
uvc_result uvc_set_encoding_unit_control(struct uvc_device_handle *handle, int control, void* value, int length);

/**
 * エンコーディングユニットコントロールの値を取得します.
 *
 * @param handle UVCデバイス
 * @param control コントロールID
 * @param request リクエストタイプ
 * @param value 設定する値
 * @param length 設定する値のサイズ
 * @return UVC_SUCCESSの場合は撮影に成功、それ以外は撮影に失敗
 */
uvc_result uvc_get_encoding_unit_control(struct uvc_device_handle *handle, int control, int request, void *value, int length);


#ifdef __cplusplus
}
#endif
#endif //UVC_H
