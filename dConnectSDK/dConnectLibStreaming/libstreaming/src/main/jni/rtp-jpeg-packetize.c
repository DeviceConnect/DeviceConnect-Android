#include "common.h"
#include "rtp-socket.h"
#include "rtp-jpeg-packetize.h"

#define SOI 0xD8
#define EOI 0xD9
#define DQT 0xDB
#define SOF 0xC0
#define DHT 0xC4
#define SOS 0xDA
#define DRI 0xDD

#define RTP_JPEG_RESTART 0x40

static void rtp_jpeg_parse_DQT(struct rtp_jpeg_data *jpeg, uint8_t *data, uint32_t length) {
    for (int i = 0; i < length; i += 65) {
        if ((data[i] & 0xF0) != 0) {
            LOGE("@@@@ rtp_jpeg_parse_DQT: invalid format.");
            return;
        }
        memcpy(jpeg->quantization[jpeg->quantization_num], &data[i + 1], 64);
        jpeg->quantization_num++;
    }
}


static void rtp_jpeg_parse(struct rtp_jpeg_data *jpeg, uint8_t *data, uint32_t length) {
    uint32_t len = 0;
    for (uint32_t i = 0; i < length; i += (2 + len)) {
        uint8_t d = data[i];
        if (d == 0xFF) {
            d = data[i + 1];
            if (d != SOI) {
                len = (data[i + 2] << 8) | data[i + 3];
                switch (d) {
                    case DQT: // Quantization Table
                        rtp_jpeg_parse_DQT(jpeg, &data[i + 4], len - 2);
                        break;

                    case DRI:
                        jpeg->dri = (uint16_t) (((data[i + 4] & 0xFF) << 8) | (data[i + 5] & 0xFF));
                        break;

                    case SOF: // Start of Frame
                        jpeg->height = (uint16_t) (((data[i + 5] & 0xFF) << 8) | (data[i + 6] & 0xFF));
                        jpeg->width = (uint16_t) (((data[i + 7] & 0xFF) << 8) | (data[i + 8] & 0xFF));
                        break;

                    case EOI: // End of Image
                    case DHT: // Huffman Table
                    default:  // Other
                        break;

                    case SOS: // Start of Scan
                        jpeg->sos_pos = i + len + 2;
                        return;
                }
            } else {
                len = 0;
            }
        }
    }
}


ssize_t rtp_send_mjpeg_packetize(struct rtp_socket_info *info, struct rtp_message *message) {
    uint8_t buffer[MTU];

    struct rtp_jpeg_data jpeg;
    memset(&jpeg, 0, sizeof(struct rtp_jpeg_data));

    rtp_jpeg_parse(&jpeg, message->data, message->length);

    uint8_t q = 255;
    uint8_t width = (uint8_t) (jpeg.width >> 3);
    uint8_t height = (uint8_t) (jpeg.height >> 3);
    uint8_t type = 0;
    if (jpeg.dri != 0) {
        type |= RTP_JPEG_RESTART;
    }

    uint32_t offset = 0;
    uint32_t length = 0;
    uint8_t *data = &message->data[jpeg.sos_pos];
    uint32_t bytesLeft = message->length - jpeg.sos_pos;
    while (bytesLeft > 0) {
        rtp_socket_set_header(info, buffer);

        uint32_t headerLen = RTP_HEADER_LENGTH;

        buffer[headerLen++] = 0; // typespec
        buffer[headerLen++] = (uint8_t) ((offset >> 16) & 0xFF);
        buffer[headerLen++] = (uint8_t) ((offset >> 8) & 0xFF);
        buffer[headerLen++] = (uint8_t) ((offset) & 0xFF);
        buffer[headerLen++] = (uint8_t) (type & 0xFF);
        buffer[headerLen++] = (uint8_t) (q & 0xFF);
        buffer[headerLen++] = (uint8_t) (width & 0xFF);
        buffer[headerLen++] = (uint8_t) (height & 0xFF);

        if (jpeg.dri != 0) {
            buffer[headerLen++] = (uint8_t) ((jpeg.dri >> 8) & 0xFF);
            buffer[headerLen++] = (uint8_t) ((jpeg.dri) & 0xFF);
            buffer[headerLen++] = (uint8_t) 0xFF;
            buffer[headerLen++] = (uint8_t) 0xFF;
        }

        if (offset == 0 && jpeg.quantization_num > 0) {
            uint32_t len = (uint32_t) 64 * jpeg.quantization_num;
            buffer[headerLen++] = 0; // mbz
            buffer[headerLen++] = 0; // precision
            buffer[headerLen++] = (uint8_t) ((len >> 8) & 0xFF);
            buffer[headerLen++] = (uint8_t) ((len) & 0xFF);
            for (int i = 0; i < jpeg.quantization_num; i++) {
                memcpy(&buffer[headerLen], jpeg.quantization[i], 64);
                headerLen += 64;
            }
        }

        if (bytesLeft > MAX_PACKET_SIZE - headerLen) {
            length = MAX_PACKET_SIZE - headerLen;
        } else {
            rtp_socket_mark_next_packet(buffer);
            length = bytesLeft;
        }

        memcpy(&buffer[headerLen], &data[offset], length);

        if (rtp_send_message(info, buffer, headerLen + length) < 0) {
            LOGW("@@@ rtp_send_h264_message: Failed to send message.");
            return 0;
        }

        offset += length;
        bytesLeft -= length;

        info->sequence_number++;
    }
    return 1;
}

