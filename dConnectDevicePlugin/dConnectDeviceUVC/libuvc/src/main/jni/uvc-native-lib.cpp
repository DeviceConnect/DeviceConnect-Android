/*
 uvc-native-lib.cpp
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
#include <jni.h>
#include <unistd.h>
#include <malloc.h>
#include <string.h>

#include "uvc.h"

/**
 * JNI のパッケージ名、クラス名を定義.
 */
#define JNI_METHOD_NAME(name) \
    Java_org_deviceconnect_android_libuvc_UVCCameraNative_##name


#ifdef __cplusplus
extern "C" {
#endif

/**
 * JPEGヘッダーで使用されるハフマンテーブルのサイズ.
 */
static const size_t huff_tbl_size = 432;

/**
 * JPEGヘッダーで使用されるハフマンテーブル.
 */
static const uint8_t huff_tbl[huff_tbl_size] = {
        // dc_huff_tbl_0
        0xff, 0xc4, 0x00, 0x1f, 0x00, 0x00, 0x01, 0x05, 0x01, 0x01,
        0x01, 0x01, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
        0x09, 0x0a, 0x0b,

        // ac_huff_tbl_0
        0xff, 0xc4, 0x00, 0xb5, 0x10, 0x00, 0x02, 0x01, 0x03, 0x03,
        0x02, 0x04, 0x03, 0x05, 0x05, 0x04, 0x04, 0x00, 0x00, 0x01,
        0x7d, 0x01, 0x02, 0x03, 0x00, 0x04, 0x11, 0x05, 0x12, 0x21,
        0x31, 0x41, 0x06, 0x13, 0x51, 0x61, 0x07, 0x22, 0x71, 0x14,
        0x32, 0x81, 0x91, 0xa1, 0x08, 0x23, 0x42, 0xb1, 0xc1, 0x15,
        0x52, 0xd1, 0xf0, 0x24, 0x33, 0x62, 0x72, 0x82, 0x09, 0x0a,
        0x16, 0x17, 0x18, 0x19, 0x1a, 0x25, 0x26, 0x27, 0x28, 0x29,
        0x2a, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3a, 0x43, 0x44,
        0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x53, 0x54, 0x55, 0x56,
        0x57, 0x58, 0x59, 0x5a, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68,
        0x69, 0x6a, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7a,
        0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89, 0x8a, 0x92, 0x93,
        0x94, 0x95, 0x96, 0x97, 0x98, 0x99, 0x9a, 0xa2, 0xa3, 0xa4,
        0xa5, 0xa6, 0xa7, 0xa8, 0xa9, 0xaa, 0xb2, 0xb3, 0xb4, 0xb5,
        0xb6, 0xb7, 0xb8, 0xb9, 0xba, 0xc2, 0xc3, 0xc4, 0xc5, 0xc6,
        0xc7, 0xc8, 0xc9, 0xca, 0xd2, 0xd3, 0xd4, 0xd5, 0xd6, 0xd7,
        0xd8, 0xd9, 0xda, 0xe1, 0xe2, 0xe3, 0xe4, 0xe5, 0xe6, 0xe7,
        0xe8, 0xe9, 0xea, 0xf1, 0xf2, 0xf3, 0xf4, 0xf5, 0xf6, 0xf7,
        0xf8, 0xf9, 0xfa,

        // dc_huff_tbl_1
        0xff, 0xc4, 0x00, 0x1f, 0x01, 0x00, 0x03, 0x01, 0x01, 0x01,
        0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
        0x09, 0x0a, 0x0b,

        // ac_huff_tbl_1
        0xff, 0xc4, 0x00, 0xb5, 0x11, 0x00, 0x02, 0x01, 0x02, 0x04,
        0x04, 0x03, 0x04, 0x07, 0x05, 0x04, 0x04, 0x00, 0x01, 0x02,
        0x77, 0x00, 0x01, 0x02, 0x03, 0x11, 0x04, 0x05, 0x21, 0x31,
        0x06, 0x12, 0x41, 0x51, 0x07, 0x61, 0x71, 0x13, 0x22, 0x32,
        0x81, 0x08, 0x14, 0x42, 0x91, 0xa1, 0xb1, 0xc1, 0x09, 0x23,
        0x33, 0x52, 0xf0, 0x15, 0x62, 0x72, 0xd1, 0x0a, 0x16, 0x24,
        0x34, 0xe1, 0x25, 0xf1, 0x17, 0x18, 0x19, 0x1a, 0x26, 0x27,
        0x28, 0x29, 0x2a, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3a, 0x43,
        0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x53, 0x54, 0x55,
        0x56, 0x57, 0x58, 0x59, 0x5a, 0x63, 0x64, 0x65, 0x66, 0x67,
        0x68, 0x69, 0x6a, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79,
        0x7a, 0x82, 0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89, 0x8a,
        0x92, 0x93, 0x94, 0x95, 0x96, 0x97, 0x98, 0x99, 0x9a, 0xa2,
        0xa3, 0xa4, 0xa5, 0xa6, 0xa7, 0xa8, 0xa9, 0xaa, 0xb2, 0xb3,
        0xb4, 0xb5, 0xb6, 0xb7, 0xb8, 0xb9, 0xba, 0xc2, 0xc3, 0xc4,
        0xc5, 0xc6, 0xc7, 0xc8, 0xc9, 0xca, 0xd2, 0xd3, 0xd4, 0xd5,
        0xd6, 0xd7, 0xd8, 0xd9, 0xda, 0xe2, 0xe3, 0xe4, 0xe5, 0xe6,
        0xe7, 0xe8, 0xe9, 0xea, 0xf2, 0xf3, 0xf4, 0xf5, 0xf6, 0xf7,
        0xf8, 0xf9, 0xfa
};


/**
 * Java側の UVCCamera クラスにアクセスするためのIDを保持する構造体.
 */
struct java_uvc_camera {
    jmethodID onFrame_method_id;
    jmethodID getFrame_method_id;
};


/**
 * Java側の Frame クラスにアクセスするためのIDを保持する構造体.
 */
struct java_frame {
    jmethodID getId_method_id;
    jmethodID getBuffer_method_id;
    jmethodID resizeBuffer_method_id;
    jmethodID setLength_method_id;
    jfieldID parameter_field_id;
};


/**
 * Java側の Parameter クラスにアクセスするためのIDを保持する構造体.
 */
struct java_parameter {
    jfieldID formatIndexId_field_id;
    jfieldID frameIndexId_field_id;
    jfieldID fpsId_field_id;
};


/**
 * Extension Unit で MJPEG に多重化された H264 のデータを格納する構造体.
 */
struct ext_h264_buffer {
    uint32_t length;
    uint32_t got_bytes;
    uint32_t payload_size;
    uint8_t bytes[0];
};

/**
 * uvc_device_handle から受け取るコールバック関数に渡す構造体.
 */
struct user_context {
    JNIEnv *env;
    jobject obj;
    uint8_t count;
    uint8_t use_ext_h264;
    struct java_uvc_camera uvc;
    struct java_frame frame;
    struct java_parameter parameter;
    struct uvc_device_handle *handle;
    struct ext_h264_buffer *buffer;
};

/**
 * スタートマーカ(Start Of Image).
 */
#define SOI 0xD8

/**
 * エンドマーカ(End Of Image).
 */
#define EOI 0xD9

/**
 * スキャンヘッダー(Start of Scan).
 */
#define SOS 0xDA
#define DHT 0xC4

/**
 * フレームヘッダー(Start of Frame).
 */
#define SOF 0xC0

#define APP_4_MAKER 0xE4

/**
 * JPEGのセグメントを解釈して、SOIの位置とDHTの有無を確認します.
 *
 * @param data JPEGデータ
 * @param length JPEGデータサイズ
 * @param soi SOIの位置を格納するポインタ
 * @param dht  DHTの個数を格納するポインタ
 */
static void jpeg_parse(uint8_t *data, uint32_t length, uint32_t *soi, uint32_t *dht) {
    uint32_t len = 0;
    for (uint32_t i = 0; i < length; i += (2 + len)) {
        uint8_t d = data[i];
        if (d == 0xFF) {
            d = data[i + 1];
            if (d == SOI) {
                *soi = i;
                len = 0;
            } else {
                len = (data[i + 2] << 8) | data[i + 3];
                switch (d) {
                    case DHT: // Huffman Table
                        *dht = (*dht) + 1;
                        break;
                    case SOS: // Start of scan segment
                        i = length;
                        break;
                    default:  // Other
                        break;
                }
            }
        }
    }
}


/**
 * uvc からの静止画用のフレームバッファ通知を受け取るコールバック関数.
 *
 * @param handle uvc を操作するハンドル
 * @param frame フレームバッファ
 */
static void callback_still_frame(void *user, struct uvc_frame *frame) {
    struct user_context *context = (struct user_context *) user;
    if (context == NULL) {
        return;
    }

    // 最初の15フレームは画面が暗くなっているので無視します。
    if (context->count < 15) {
        context->count++;
        return;
    }

    // uvc_device_handleの状態を停止状態にする
    context->handle->running = UVC_VIDEO_STOP;

    jobject frameObj = context->obj;
    if (frameObj == NULL) {
        return;
    }

    uint8_t *data = frame->buf;
    uint32_t data_size = frame->got_bytes;
    int insert_dht = UVC_FALSE;

    switch (frame->type) {
        default:
        case VS_FRAME_UNCOMPRESSED:
        case VS_FRAME_H264:
            break;
        case VS_FRAME_MJPEG: {
            uint32_t soi = frame->got_bytes;
            uint32_t dht = 0;
            jpeg_parse(frame->buf, frame->got_bytes, &soi, &dht);
            if (soi == frame->got_bytes) {
                // SOIが見つからない場合は無視
                return;
            }

            if (soi > 0) {
                data = frame->buf + soi;
                data_size = frame->got_bytes - soi;
            }

            // ハフマンテーブルをコピーするので、その分をデータサイズに追加
            if (dht == 0) {
                insert_dht = UVC_TRUE;
                data_size += huff_tbl_size;
            }
        }   break;
    }


    // Frame#resizeBuffer() と Frame#setLength() でバッファの初期化
    context->env->CallVoidMethod(frameObj, context->frame.resizeBuffer_method_id, data_size);
    context->env->CallVoidMethod(frameObj, context->frame.setLength_method_id, data_size);

    // Frame#getBuffer() からバッファを取得
    jbyteArray byteArray = (jbyteArray) context->env->CallObjectMethod(frameObj, context->frame.getBuffer_method_id);
    if (byteArray == NULL) {
        context->env->DeleteLocalRef(frameObj);
        return;
    }

    jlong capacity = context->env->GetArrayLength(byteArray);
    if (data_size > capacity) {
        context->env->DeleteLocalRef(byteArray);
        return;
    }

    jboolean isCopy;
    uint8_t *dst = (uint8_t *) context->env->GetByteArrayElements(byteArray, &isCopy);
    if (dst == NULL) {
        context->env->DeleteLocalRef(byteArray);
        return;
    }

    if (insert_dht == UVC_TRUE) {
        // MotionJPEG の場合に、ハフマンテーブルがない場合には、ここでコピーしておく。
        // mjpeg_processing_frame でコピーを行うと memcpy が発生してしまうので。
        memcpy(dst, data, 2);
        memcpy((dst + 2), huff_tbl, huff_tbl_size);
        memcpy((dst + 2 + huff_tbl_size), (data + 2), data_size - huff_tbl_size - 2);
    } else {
        memcpy(dst, data, data_size);
    }

    context->env->ReleaseByteArrayElements(byteArray, (jbyte *) dst, 0);
    context->env->DeleteLocalRef(byteArray);
}


/**
 * フレームデータの値を Java 側に通知します.
 *
 * @param context コンテキスト
 * @param data フレームデータ
 * @param data_size データサイズ
 * @param pts Presentation timestamp
 * @param insert_dht ハフマンテーブルをコピーする場合はUVC_TRUE、それ以外はUVC_FALSE
 */
static void send_frame(struct user_context *context, uint8_t *data, uint32_t data_size, uint32_t pts, int insert_dht) {
    if (data_size <= 0) {
        LOGE("@@@ send_frame: data_size is negative.");
        return;
    }

    // ハフマンテーブルをコピーするので、その分をデータサイズに追加
    if (insert_dht == UVC_TRUE) {
        data_size += huff_tbl_size;
    }

    jobject frameObj = context->env->CallObjectMethod(context->obj, context->uvc.getFrame_method_id, data_size);
    if (frameObj == NULL) {
        LOGE("@@@ send_frame: Failed to get a frame object from java vm.");
        return;
    }

    jint id = context->env->CallIntMethod(frameObj, context->frame.getId_method_id);
    jbyteArray byteArray = (jbyteArray) context->env->CallObjectMethod(frameObj, context->frame.getBuffer_method_id);
    if (byteArray == NULL) {
        context->env->DeleteLocalRef(frameObj);
        return;
    }

    jlong capacity = context->env->GetArrayLength(byteArray);
    if (data_size > capacity) {
        context->env->DeleteLocalRef(byteArray);
        context->env->DeleteLocalRef(frameObj);
        return;
    }

    jboolean isCopy;
    uint8_t *dst = (uint8_t *) context->env->GetByteArrayElements(byteArray, &isCopy);
    if (dst == NULL) {
        context->env->DeleteLocalRef(byteArray);
        context->env->DeleteLocalRef(frameObj);
        return;
    }

    if (insert_dht == UVC_TRUE) {
        // MotionJPEG の場合に、ハフマンテーブルがない場合には、ここでコピーしておく。
        // mjpeg_processing_frame でコピーを行うとmemcpyが発生してしまうので。
        memcpy(dst, data, 2);
        memcpy((dst + 2), huff_tbl, huff_tbl_size);
        memcpy((dst + 2 + huff_tbl_size), (data + 2), data_size - huff_tbl_size - 2);
    } else {
        memcpy(dst, data, data_size);
    }

    context->env->CallVoidMethod(context->obj, context->uvc.onFrame_method_id, id, data_size, pts);

    context->env->ReleaseByteArrayElements(byteArray, (jbyte *) dst, 0);
    context->env->DeleteLocalRef(byteArray);
    context->env->DeleteLocalRef(frameObj);
}


/**
 * H264 のフレームデータを処理して Java 側に通知します.
 * <p>
 * NALU では、データの開始が 0x000001 または 0x00000001 になります。<br>
 * Java 側で処理しやすいように、配列の先頭が 0x000001 または 0x00000001 になるように処理を行なってから通知します。
 * </p>
 * @param context コンテキスト
 * @param frame 通知するフレームデータ
 */
static void h264_processing_frame(struct user_context *context, struct uvc_frame *frame) {
    uint8_t *data = frame->buf;
    uint32_t length = frame->got_bytes;
    uint32_t start_pos = 0;
    uint32_t count = 0;

    for (uint32_t i = 0; i < length - 4; i++) {
        if (data[i] == 0 && data[i + 1] == 0 && data[i + 2] == 0x00 && data[i + 3] == 0x01) {
            if (count > 0) {
                send_frame(context, &data[start_pos], i - start_pos, frame->pts, UVC_FALSE);
            }
            start_pos = i;
            count++;
        }
    }

    if (length - start_pos > 0) {
        send_frame(context, &data[start_pos], length - start_pos, frame->pts, UVC_FALSE);
    }
}


/**
 * Uncompressed のフレームデータを処理して Java 側に通知します.
 *
 * @param context コンテキスト
 * @param frame 通知するフレームデータ
 */
static void uncompressed_processing_frame(struct user_context *context, struct uvc_frame *frame) {
    send_frame(context, frame->buf, frame->got_bytes, frame->pts, UVC_FALSE);
}


/**
 * Motion JPEG のフレームデータを処理して Java 側に通知します.
 *
 * @param context コンテキスト
 * @param frame 通知するフレームデータ
 */
static void mjpeg_processing_frame(struct user_context *context, struct uvc_frame *frame) {
    uint32_t soi = frame->got_bytes;
    uint32_t dht = 0;
    jpeg_parse(frame->buf, frame->got_bytes, &soi, &dht);
    if (soi == frame->got_bytes) {
        // SOIが見つからない場合は無視
        return;
    }
    send_frame(context, frame->buf + soi, frame->got_bytes - soi, frame->pts, dht == 0 ? UVC_TRUE : UVC_FALSE);
}

/**
 * MJPEG に格納された H264 のデータを処理して Java 側に通知します.
 *
 * USB_Video_Payload_H 264_1 0.pdf 「3.4 Packetization」を参照すること。
 *
 * @param context コンテキスト
 * @param frame 通知するフレームデータ
 */
static void ext_h264_processing_frame(struct user_context *context, struct uvc_frame *frame) {
    uint8_t *data = frame->buf;
    uint32_t length = frame->length;
    uint32_t len = 0;
    for (uint32_t i = 0; i < length; i += (2 + len)) {
        uint8_t d = data[i];
        if (d == 0xFF) {
            d = data[i + 1];
            if (d == SOI) {
                len = 0;
            } else {
                len = (data[i + 2] << 8) | data[i + 3];
                switch (d) {
                    case APP_4_MAKER: {
                        if (context->buffer->got_bytes == 0) {
                            uint16_t version = (data[i + 4]) | (data[i + 5]  << 8);
                            uint16_t headerLength = (data[i + 6]) | (data[i + 7]  << 8);
                            uint32_t streamType = (data[i + 8]) | (data[i + 9] << 8)
                                                  | (data[i + 10] << 16) | (data[i + 11] << 24);
                            uint16_t width = (data[i + 12] ) | (data[i + 13] << 8);
                            uint16_t height = (data[i + 14]) | (data[i + 15] << 8);
                            uint32_t interval = (data[i + 16]) | (data[i + 17] << 8)
                                                | (data[i + 18] << 16) | (data[i + 19] << 24);
                            uint16_t delay = (data[i + 20]) | (data[i + 21] << 8);
                            uint32_t presentationTime = (data[i + 22]) | (data[i + 23] << 8)
                                                        | (data[i + 24] << 16) | (data[i + 25] << 24);
                            uint32_t payloadSize = (data[i + 26]) | (data[i + 27] << 8)
                                                   | (data[i + 28] << 16) | (data[i + 29] << 24);

                            if (version != 0x0100) {
                                // バージョンが不正
                                LOGE("ext_h264_processing_frame: Not support version: 0x%02X", version);
                                return;
                            }

                            // セグメントサイズを計算
                            // Segment Length = Length (2byte) + Header (22byte) + Payload Size (4byte)
                            uint32_t segment_length = len - (2 + headerLength + 4);

                            // バッファサイズがペイロードよりも小さい場合には、リサイズします。
                            if (context->buffer->length < payloadSize) {
                                context->buffer = (struct ext_h264_buffer * ) calloc(1,
                                        sizeof(struct ext_h264_buffer) + payloadSize);
                                context->buffer->length = payloadSize;
                            }

                            context->buffer->payload_size = payloadSize;
                            context->buffer->got_bytes = 0;

                            memcpy(&context->buffer->bytes[context->buffer->got_bytes], &data[i + 30], segment_length);
                            context->buffer->got_bytes += segment_length;
                        } else {
                            // セグメントサイズを計算
                            // Segment Length = Length (2byte)
                            uint32_t segmentLength = len - 2;

                            memcpy(&context->buffer->bytes[context->buffer->got_bytes], &data[i + 4], segmentLength);
                            context->buffer->got_bytes += segmentLength;
                        }

                        if (context->buffer->got_bytes >= context->buffer->payload_size) {
                            context->buffer->got_bytes = 0;
                            send_frame(context, context->buffer->bytes, context->buffer->payload_size, frame->pts, UVC_FALSE);
                        }
                    }   break;
                    case SOS: // Start Of Scan
                        // SOS は、イメージデータの先頭に入っています。
                        // これより後ろには、APP 4 Maker は存在しないので、終了します。
                        return;
                    default:  // Other
                        break;
                }
            }
        }
    }
}

/**
 * uvc からのフレームバッファの通知を受け取るコールバック関数.
 *
 * @param handle uvc を操作するハンドル
 * @param frame フレームバッファ
 */
static void callback_stream_frame(void *user, struct uvc_frame *frame) {
    struct user_context *context = (struct user_context *) user;
    if (context) {
        switch (frame->type) {
            case VS_FRAME_UNCOMPRESSED:
                uncompressed_processing_frame(context, frame);
                break;

            case VS_FRAME_MJPEG:
                if (context->use_ext_h264) {
                    ext_h264_processing_frame(context, frame);
                } else {
                    mjpeg_processing_frame(context, frame);
                }
                break;

            case VS_FRAME_H264:
                h264_processing_frame(context, frame);
                break;

            default:
                LOGE("@@@ callback_stream_frame: Unknown frame type.");
                break;
        }
    }
}


/**
 * Frame Descriptor のデータを Java の Parameter クラスに変換します.
 *
 * @param env Javaの環境
 * @param format フォーマット
 * @param frame フレーム
 * @param has_ext_h264 Extension Unit が存在する場合はtrue、それ以外はfalse
 * @return Parameterクラスのインスタンス
 */
static jobject create_uvc_parameter(JNIEnv *env, struct uvc_vs_format_descriptor *format, struct uvc_vs_frame_descriptor *frame, jboolean has_ext_h264) {
    jclass clazz = env->FindClass("org/deviceconnect/android/libuvc/Parameter");
    jmethodID mid = env->GetMethodID(clazz, "<init>","()V");
    jmethodID putMethodID = env->GetMethodID(clazz, "putExtra", "(Ljava/lang/String;J)V");
    jfieldID formatIndexId = env->GetFieldID(clazz, "mFormatIndex", "I");
    jfieldID frameTypeId = env->GetFieldID(clazz, "mFrameType", "I");
    jfieldID frameIndexId = env->GetFieldID(clazz, "mFrameIndex", "I");
    jfieldID widthId = env->GetFieldID(clazz, "mWidth", "I");
    jfieldID heightId = env->GetFieldID(clazz, "mHeight", "I");
    jfieldID fpsId = env->GetFieldID(clazz, "mFps", "I");
    jfieldID fpsListId = env->GetFieldID(clazz, "mFpsList", "[I");

    jobject obj = env->NewObject(clazz, mid);
    env->SetIntField(obj, formatIndexId, (jint) format->bFormatIndex);
    env->SetIntField(obj, frameTypeId, (jint) frame->bDescriptorSubType);
    env->SetIntField(obj, frameIndexId, (jint) frame->bFrameIndex);
    env->SetIntField(obj, widthId, (jint) frame->wWidth);
    env->SetIntField(obj, heightId, (jint) frame->wHeight);

    uint32_t defaultFps = 0;
    uint32_t *fps = NULL;
    uint32_t length = 0;
    if (uvc_get_fps_list(frame, &fps, &length, &defaultFps) == UVC_SUCCESS) {
        jintArray fpsArray = env->NewIntArray(length);
        env->SetObjectField(obj, fpsListId, fpsArray);
        env->DeleteLocalRef(fpsArray);
        SAFE_FREE(fps);

        env->SetIntField(obj, fpsId, (jint) defaultFps);
    }

    // H264 の場合は wProfile と bLevelIDC の値は Java 側に通知しておく。
    switch (frame->bDescriptorSubType) {
        case VS_FRAME_H264: {
            struct uvc_vs_frame_h264_descriptor *f = (struct uvc_vs_frame_h264_descriptor *) frame;

            const char *wProfileStr = "wProfile";
            const char *bLevelIDCStr = "bLevelIDC";
            const char *dwMaxBitRateStr = "dwMaxBitRate";

            jstring wProfile = env->NewStringUTF(wProfileStr);
            env->CallVoidMethod(obj, putMethodID, wProfile, (jlong) f->wProfie);
            env->DeleteLocalRef(wProfile);

            jstring bLevelIDC = env->NewStringUTF(bLevelIDCStr);
            env->CallVoidMethod(obj, putMethodID, bLevelIDC, (jlong) f->bLevelIDC);
            env->DeleteLocalRef(bLevelIDC);

            jstring dwMaxBitRate = env->NewStringUTF(dwMaxBitRateStr);
            env->CallVoidMethod(obj, putMethodID, dwMaxBitRate, (jlong) f->dwMaxBitRate);
            env->DeleteLocalRef(dwMaxBitRate);
        }   break;

        case VS_FRAME_UNCOMPRESSED: {
            const char *guidStr = "guid";
            jstring guid = env->NewStringUTF(guidStr);
            env->CallVoidMethod(obj, putMethodID, guid, (jlong) uvc_get_uncompressed_format(format));
            env->DeleteLocalRef(guid);
        }   break;

        case VS_FRAME_MJPEG: {
            if (has_ext_h264) {
                const char *extensionStr = "h264";
                jstring extension = env->NewStringUTF(extensionStr);
                env->CallVoidMethod(obj, putMethodID, extension, (jlong) 1);
                env->DeleteLocalRef(extension);
            }
        }   break;

        default:
            // TODO 他のタイプで必要なパラメータがあれば、ここで通知すること。
            break;
    }

    env->DeleteLocalRef(clazz);
    return obj;
}


/**
 * bmControls のcontrol目のビットを確認してサポート状況を確認します.
 *
 * @param bmControls ディスクリプタから取得したbmControls
 * @param control 何ビットめか
 * @return サポートしている場合はJNI_TRUE、それ以外はJNI_FALSE
 */
static jboolean is_supported_control(uint8_t bmControls, int control) {
    return (bmControls & (1 << control)) != 0 ? (jboolean) JNI_TRUE : (jboolean) JNI_FALSE;
}


/**
 * Parameter クラスのメソッドIDなどの初期化を行います.
 *
 * @param env Java環境クラス
 * @param context 値を格納するコンテキスト
 * @return 成功した場合にはUVC_SUCCESS、失敗した場合にはUVC_ERROR
 */
static uvc_result java_parameter_init(JNIEnv *env, struct user_context *context) {
    jclass paramClass = env->FindClass("org/deviceconnect/android/libuvc/Parameter");
    if (paramClass == NULL) {
        return UVC_ERROR;
    }

    context->parameter.formatIndexId_field_id = env->GetFieldID(paramClass, "mFormatIndex", "I");
    context->parameter.frameIndexId_field_id = env->GetFieldID(paramClass, "mFrameIndex", "I");
    context->parameter.fpsId_field_id = env->GetFieldID(paramClass, "mFps", "I");

    env->DeleteLocalRef(paramClass);

    return (context->parameter.formatIndexId_field_id &&
            context->parameter.frameIndexId_field_id &&
            context->parameter.fpsId_field_id) ? UVC_SUCCESS : UVC_ERROR;
}


/**
 * UVCCamera クラスのメソッドIDなどの初期化を行います.
 *
 * @param env Java環境クラス
 * @param context 値を格納するコンテキスト
 * @return 成功した場合にはUVC_SUCCESS、失敗した場合にはUVC_ERROR
 */
static uvc_result java_uvc_camera_init(JNIEnv *env, struct user_context *context) {
    jclass uvcCamClass = env->FindClass("org/deviceconnect/android/libuvc/UVCCamera");
    if (uvcCamClass == NULL) {
        return UVC_ERROR;
    }

    context->uvc.onFrame_method_id = env->GetMethodID(uvcCamClass, "onFrame", "(IIJ)V");
    context->uvc.getFrame_method_id = env->GetMethodID(uvcCamClass, "getFrame", "(I)Lorg/deviceconnect/android/libuvc/Frame;");

    env->DeleteLocalRef(uvcCamClass);

    return (context->uvc.onFrame_method_id && context->uvc.getFrame_method_id) ? UVC_SUCCESS : UVC_ERROR;
}


/**
 * Frame クラスのメソッドIDなどの初期化を行います.
 *
 * @param env Java環境クラス
 * @param context 値を格納するコンテキスト
 * @return 成功した場合にはUVC_SUCCESS、失敗した場合にはUVC_ERROR
 */
static uvc_result java_frame_init(JNIEnv *env, struct user_context *context) {
    jclass frameClass = env->FindClass("org/deviceconnect/android/libuvc/Frame");
    if (frameClass == NULL) {
        return UVC_ERROR;
    }

    context->frame.getId_method_id = env->GetMethodID(frameClass, "getId", "()I");
    context->frame.getBuffer_method_id = env->GetMethodID(frameClass, "getBuffer", "()[B");
    context->frame.resizeBuffer_method_id = env->GetMethodID(frameClass, "resizeBuffer", "(I)V");
    context->frame.setLength_method_id = env->GetMethodID(frameClass, "setLength", "(I)V");
    context->frame.parameter_field_id = env->GetFieldID(frameClass, "mParameter", "Lorg/deviceconnect/android/libuvc/Parameter;");

    env->DeleteLocalRef(frameClass);

    return (context->frame.getId_method_id &&
            context->frame.getBuffer_method_id &&
            context->frame.resizeBuffer_method_id &&
            context->frame.setLength_method_id) ? UVC_SUCCESS : UVC_ERROR;
}


///////////////////


JNIEXPORT jlong JNI_METHOD_NAME(open)(JNIEnv *env, jclass clazz, jint fd) {
    LOGI("Java_org_deviceconnect_android_libuvc_UVCCameraNative_open(%d)", fd);

    struct uvc_device_handle *handle = uvc_open_device(fd);
    return (jlong) handle;
}


JNIEXPORT jint JNI_METHOD_NAME(getParameter)(JNIEnv *env, jclass clazz, jlong nativePtr, jobject array) {
    LOGI("Java_org_deviceconnect_android_libuvc_UVCCameraNative_getParameter(%ld)", nativePtr);

    jclass arrayListClazz = env->FindClass("java/util/ArrayList");
    if (arrayListClazz == NULL) {
        return UVC_ERROR;
    }

    jmethodID addMethodID = env->GetMethodID(arrayListClazz, "add", "(Ljava/lang/Object;)Z");
    if (addMethodID == NULL) {
        return UVC_ERROR;
    }

    struct uvc_device_handle *handle = (struct uvc_device_handle *) nativePtr;
    struct uvc_video_streaming_interface *streaming_interface = uvc_get_active_streaming_interface(handle);
    struct uvc_vc_extension_unit_descriptor *h264_extension = uvc_get_active_extension_descriptor(handle);

    while (streaming_interface) {
        struct uvc_vs_format_descriptor *format = streaming_interface->format;
        while (format) {
            struct uvc_vs_frame_descriptor *frame = format->frame;
            while (frame) {
                jobject element = create_uvc_parameter(env, format, frame, h264_extension != NULL);
                if (element) {
                    env->CallBooleanMethod(array, addMethodID, element);
                    env->DeleteLocalRef(element);
                }
                frame = frame->next;
            }
            format = format->next;
        }
        streaming_interface = streaming_interface->next;
    }

    env->DeleteLocalRef(arrayListClazz);

    return UVC_SUCCESS;
}


JNIEXPORT jint JNI_METHOD_NAME(getOption)(JNIEnv *env, jclass clazz, jlong nativePtr, jobject option) {
    LOGI("Java_org_deviceconnect_android_libuvc_UVCCameraNative_getOption(%ld)", nativePtr);

    jclass optionClazz = env->FindClass("org/deviceconnect/android/libuvc/Option");
    if (optionClazz == NULL) {
        return UVC_ERROR;
    }

    jmethodID putCameraMethodID = env->GetMethodID(optionClazz, "putCameraTerminalControls", "(IZ)V");
    if (putCameraMethodID == NULL) {
        env->DeleteLocalRef(optionClazz);
        return UVC_ERROR;
    }

    jmethodID putProcessingMethodID = env->GetMethodID(optionClazz, "putProcessingUnitControls", "(IZ)V");
    if (putProcessingMethodID == NULL) {
        env->DeleteLocalRef(optionClazz);
        return UVC_ERROR;
    }

    // サポート状況を Java 側に通知します。
    struct uvc_device_handle *handle = (struct uvc_device_handle *) nativePtr;
    struct uvc_video_control_interface *control = uvc_get_active_control_interface(handle);
    {
        // p[x][0]: コントロールを示すID
        // p[x][1]: バイト数 (bmControlsの何バイト目か)
        // p[x][2]: ビット数 (何ビット目がサポートフラグになっているか)
        const static int p[20][3] = {
                {CT_SCANNING_MODE_CONTROL, 0, 0},
                {CT_AE_MODE_CONTROL, 0, 1},
                {CT_AE_PRIORITY_CONTROL, 0, 2},
                {CT_EXPOSURE_TIME_ABSOLUTE_CONTROL, 0, 3},
                {CT_EXPOSURE_TIME_RELATIVE_CONTROL, 0, 4},
                {CT_FOCUS_ABSOLUTE_CONTROL, 0, 5},
                {CT_FOCUS_RELATIVE_CONTROL, 0, 6},
                {CT_FOCUS_AUTO_CONTROL, 2, 1},
                {CT_IRIS_ABSOLUTE_CONTROL, 0, 7},
                {CT_IRIS_RELATIVE_CONTROL, 1, 0},
                {CT_ZOOM_ABSOLUTE_CONTROL, 1, 1},
                {CT_ZOOM_RELATIVE_CONTROL, 1, 2},
                {CT_PANTILT_ABSOLUTE_CONTROL, 1, 3},
                {CT_PANTILT_RELATIVE_CONTROL, 1, 4},
                {CT_ROLL_ABSOLUTE_CONTROL, 1, 5},
                {CT_ROLL_RELATIVE_CONTROL, 1, 6},
                {CT_PRIVACY_CONTROL, 2, 2},
                {CT_FOCUS_SIMPLE_CONTROL, 2, 3},
                {CT_WINDOW_CONTROL, 2, 4},
                {CT_REGION_OF_INTEREST_CONTROL, 2, 5},
        };
        struct uvc_vc_input_terminal_descriptor *input = control->input_terminal;
        if (input) {
            for (int i = 0; i < 20; i++) {
                jint ctrl = p[i][0];
                jboolean support = is_supported_control(input->bmControls[p[i][1]], p[i][2]);
                env->CallVoidMethod(option, putCameraMethodID, ctrl, support);
            }
        } else {
            for (int i = 0; i < 20; i++) {
                env->CallVoidMethod(option, putCameraMethodID, p[i][0], JNI_FALSE);
            }
        }
    }

    {
        const static int p[19][3] = {
                {PU_BACKLIGHT_COMPENSATION_CONTROL, 1, 0},
                {PU_BRIGHTNESS_CONTROL, 0, 0},
                {PU_CONTRAST_CONTROL, 0, 1},
                {PU_GAIN_CONTROL, 1, 1},
                {PU_POWER_LINE_FREQUENCY_CONTROL, 1, 2},
                {PU_HUE_CONTROL, 0, 2},
                {PU_SATURATION_CONTROL, 0, 3},
                {PU_SHARPNESS_CONTROL, 0, 4},
                {PU_GAMMA_CONTROL, 0, 5},
                {PU_WHITE_BALANCE_TEMPERATURE_CONTROL, 0, 6},
                {PU_WHITE_BALANCE_TEMPERATURE_AUTO_CONTROL, 1, 4},
                {PU_WHITE_BALANCE_COMPONENT_CONTROL, 0, 7},
                {PU_WHITE_BALANCE_COMPONENT_AUTO_CONTROL, 1, 5},
                {PU_DIGITAL_MULTIPLIER_CONTROL, 1, 6},
                {PU_DIGITAL_MULTIPLIER_LIMIT_CONTROL, 1, 7},
                {PU_HUE_AUTO_CONTROL, 1, 3},
                {PU_ANALOG_VIDEO_STANDARD_CONTROL, 2, 0},
                {PU_ANALOG_LOCK_STATUS_CONTROL, 2, 1},
                {PU_CONTRAST_AUTO_CONTROL, 2, 2},
        };
        struct uvc_vc_processing_unit_descriptor *processing = control->processing;
        if (processing) {
            for (int i = 0; i < 19; i++) {
                jint ctrl = p[i][0];
                jboolean support = is_supported_control(processing->bmControls[p[i][1]], p[i][2]);
                env->CallVoidMethod(option, putProcessingMethodID, ctrl, support);
            }
        } else {
            for (int i = 0; i < 19; i++) {
                env->CallVoidMethod(option, putProcessingMethodID, p[i][0], JNI_FALSE);
            }
        }
    }

    env->DeleteLocalRef(optionClazz);

    return UVC_SUCCESS;
}


JNIEXPORT jint JNI_METHOD_NAME(startVideo)(JNIEnv *env, jclass clazz, jlong nativePtr, jint formatIndex, jint frameIndex, jint fps, jboolean useH264) {
    LOGI("Java_org_deviceconnect_android_libuvc_UVCCameraNative_startVideo(%ld, %d, %d, %d)", nativePtr, formatIndex, frameIndex, useH264);

    struct uvc_device_handle *handle = (struct uvc_device_handle *) nativePtr;
    handle->use_ext_h264 = useH264;
    return uvc_start_video(handle, (uint8_t) formatIndex, (uint8_t) frameIndex, (uint32_t) fps,
                           useH264);
}


JNIEXPORT jint JNI_METHOD_NAME(stopVideo)(JNIEnv *env, jclass clazz, jlong nativePtr) {
    LOGI("Java_org_deviceconnect_android_libuvc_UVCCameraNative_stopVideo(%ld)", nativePtr);

    struct uvc_device_handle *handle = (struct uvc_device_handle *) nativePtr;
    return uvc_stop_video(handle);
}


JNIEXPORT jint JNI_METHOD_NAME(captureStillImage)(JNIEnv *env, jclass clazz, jlong nativePtr, jobject frame) {
    LOGI("Java_org_deviceconnect_android_libuvc_UVCCameraNative_captureStillImage(%ld)", nativePtr);

    uvc_result result = UVC_ERROR;
    uint8_t formatIndex;
    uint8_t frameIndex;
    uint32_t fps;
    jobject paramObj;

    struct uvc_device_handle *handle = (struct uvc_device_handle *) nativePtr;
    if (handle->running == UVC_VIDEO_RUNNING) {
        return UVC_ERROR;
    }

    struct user_context *context = (struct user_context *) calloc(1, sizeof(struct user_context));
    if (context == NULL) {
        return UVC_ERROR;
    }

    context->handle = handle;
    context->count = 0;
    context->env = env;
    context->obj = env->NewGlobalRef(frame);
    if (context->obj == NULL) {
        goto END;
    }

    if (java_parameter_init(env, context) != UVC_SUCCESS) {
        goto END;
    }

    if (java_frame_init(env, context) != UVC_SUCCESS) {
        goto END;
    }

    handle->user = context;
    handle->callback = callback_still_frame;

    paramObj = env->GetObjectField(frame, context->frame.parameter_field_id);

    formatIndex = (uint8_t) env->GetIntField(paramObj, context->parameter.formatIndexId_field_id);
    frameIndex =  (uint8_t) env->GetIntField(paramObj, context->parameter.frameIndexId_field_id);
    fps = (uint32_t) env->GetIntField(paramObj, context->parameter.fpsId_field_id);

    switch (uvc_get_still_capture_method(handle)) {
        default:
        case METHOD_0:
        case METHOD_1:
            // method が 0,1 の場合には静止画に対応していない。
            // ここでは、プレビューを1枚だけ取得して、静止画として処理を行う
            result = uvc_start_video(handle, formatIndex, frameIndex, fps, 0);
            if (result == UVC_SUCCESS) {
                result = uvc_handle_event(handle);
            }
            break;

        case METHOD_2:
        case METHOD_3:
            result = uvc_capture_still_image(handle, formatIndex, frameIndex, 0);
            if (result == UVC_SUCCESS) {
                result = uvc_handle_event(handle);
            }
            break;
    }

END:
    if (context->obj) {
        env->DeleteGlobalRef(context->obj);
    }
    SAFE_FREE(context);
    handle->user = NULL;

    return result;
}


JNIEXPORT jint JNI_METHOD_NAME(close)(JNIEnv *env, jclass clazz, jlong nativePtr) {
    LOGI("Java_org_deviceconnect_android_libuvc_UVCCameraNative_close(%ld)", nativePtr);

    struct uvc_device_handle *handle = (struct uvc_device_handle *) nativePtr;
    uvc_close_device(handle);
    return UVC_SUCCESS;
}

JNIEXPORT jint JNI_METHOD_NAME(handleEvent)(JNIEnv *env, jclass clazz, jlong nativePtr, jobject obj) {
    LOGI("Java_org_deviceconnect_android_libuvc_UVCCameraNative_handleEvent(%ld)", nativePtr);

    uvc_result result = UVC_ERROR;
    struct uvc_device_handle *handle = (struct uvc_device_handle *) nativePtr;

    if (handle->running == UVC_VIDEO_STOP) {
        return UVC_ERROR;
    }

    struct user_context *context = (struct user_context *) calloc(1, sizeof(struct user_context));
    if (context == NULL) {
        return UVC_OUT_OF_MEMORY;
    }

    if (handle->use_ext_h264) {
        context->buffer = (struct ext_h264_buffer *) calloc(1, sizeof(struct ext_h264_buffer) + 4096);
        if (context->buffer == NULL) {
            SAFE_FREE(context)
            return UVC_OUT_OF_MEMORY;
        }
        context->buffer->got_bytes = 0;
        context->buffer->payload_size = 0;
        context->buffer->length = 4096;
        context->use_ext_h264 = UVC_TRUE;
    }

    context->env = env;
    context->obj = env->NewGlobalRef(obj);
    if (context->obj == NULL) {
        goto END;
    }

    if (java_uvc_camera_init(env, context) != UVC_SUCCESS) {
        goto END;
    }

    if (java_frame_init(env, context) != UVC_SUCCESS) {
        goto END;
    }

    handle->user = context;
    handle->callback = callback_stream_frame;

    result = uvc_handle_event(handle);

END:
    if (context->obj) {
        env->DeleteGlobalRef(context->obj);
    }
    if (context->buffer) {
        SAFE_FREE(context->buffer);
    }
    SAFE_FREE(context);
    handle->user = NULL;

    return result;
}


JNIEXPORT jint JNI_METHOD_NAME(isRunning)(JNIEnv *env, jclass clazz, jlong nativePtr) {
    LOGI("Java_org_deviceconnect_android_libuvc_UVCCameraNative_isRunning(%ld)", nativePtr);

    struct uvc_device_handle *handle = (struct uvc_device_handle *) nativePtr;
    return handle->running;
}


JNIEXPORT jint JNI_METHOD_NAME(detachInterface)(JNIEnv *env, jclass clazz, jlong nativePtr, jint interface) {
    LOGI("Java_org_deviceconnect_android_libuvc_UVCCameraNative_detachInterface(%ld, %d)", nativePtr, interface);

    struct uvc_device_handle *handle = (struct uvc_device_handle *) nativePtr;
    return uvc_disconnect_interface(handle, (unsigned int) interface);
}


JNIEXPORT jint JNI_METHOD_NAME(setConfig)(JNIEnv *env, jclass clazz, jlong nativePtr, jint configId) {
    LOGI("Java_org_deviceconnect_android_libuvc_UVCCameraNative_setConfig(%ld)", nativePtr);

    struct uvc_device_handle *handle = (struct uvc_device_handle *) nativePtr;
    return uvc_set_configuration(handle, (unsigned int) configId);
}


JNIEXPORT jint JNI_METHOD_NAME(getStillCaptureMethod)(JNIEnv *env, jclass clazz, jlong nativePtr) {
    LOGI("Java_org_deviceconnect_android_libuvc_UVCCameraNative_getStillCaptureMethod(%ld)", nativePtr);

    struct uvc_device_handle *handle = (struct uvc_device_handle *) nativePtr;
    return uvc_get_still_capture_method(handle);
}


JNIEXPORT jint JNI_METHOD_NAME(applyControl)(JNIEnv *env, jclass clazz, jlong nativePtr, jint type, jint control, jint request, jbyteArray valueArray) {
    LOGI("Java_org_deviceconnect_android_libuvc_UVCCameraNative_applyControl(%ld, %d, %d)", nativePtr, control, request);

    struct uvc_device_handle *handle = (struct uvc_device_handle *) nativePtr;
    if (handle == NULL) {
        return UVC_PARAMETER_INVALID;
    }

    jboolean isCopy;
    uint8_t *buf = (uint8_t *) env->GetByteArrayElements(valueArray, &isCopy);
    if (buf == NULL) {
        return UVC_PARAMETER_INVALID;
    }

    int capacity = env->GetArrayLength(valueArray);
    if (capacity <= 0) {
        env->ReleaseByteArrayElements(valueArray, (jbyte *) buf, 0);
        return UVC_PARAMETER_INVALID;
    }

    uvc_result result = UVC_ERROR;
    switch (request) {
        case SET_CUR: {
            switch (type) {
                case TYPE_CT:
                    result = uvc_set_camera_terminal_control(handle, control, buf, capacity);
                    break;
                case TYPE_PU:
                    result = uvc_set_processing_unit_control(handle, control, buf, capacity);
                    break;
                case TYPE_EU:
                    result = uvc_set_encoding_unit_control(handle, control, buf, capacity);
                    break;
                default:
                    break;
            }
        }   break;

        case GET_MIN:
        case GET_MAX:
        case GET_DEF:
        case GET_CUR:
        default: {
            switch (type) {
                case TYPE_CT:
                    result = uvc_get_camera_terminal_control(handle, control, request, buf, capacity);
                    break;
                case TYPE_PU:
                    result = uvc_get_processing_unit_control(handle, control, request, buf, capacity);
                    break;
                case TYPE_EU:
                    result = uvc_get_encoding_unit_control(handle, control, request, buf, capacity);
                    break;
                default:
                    break;
            }
        }   break;
    }

    env->ReleaseByteArrayElements(valueArray, (jbyte *) buf, 0);

    return result;
}


#ifdef __cplusplus
}
#endif