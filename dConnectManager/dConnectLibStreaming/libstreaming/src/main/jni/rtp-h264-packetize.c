#include "common.h"
#include "rtp-socket.h"
#include "rtp-h264-packetize.h"

// RTPとFU headerを足し合わせたサイズ
#define HEADER_LEN (RTP_HEADER_LENGTH + 2)

// パケットの最大サイズ
#define MAX_PACKET_SIZE (MTU - 28)

ssize_t rtp_send_h264_packetize(struct rtp_socket_info *info, struct rtp_message *message) {
    uint8_t buffer[MTU];
    uint8_t header[5];
    uint32_t start_pos = 5;
    uint32_t bytesLeft = message->length - 5;

    memcpy(header, message->data, 5);

    if (header[0] == 0x00 && header[1] == 0x00 && header[2] == 0x00 && header[3] == 0x01) {
        info->header[0] = header[0];
        info->header[1] = header[1];
        info->header[2] = header[2];
        info->header[3] = header[3];
        info->header[4] = header[4];
    } else {
        header[4] = message->data[0];
        bytesLeft = message->length;
        start_pos = 1;
    }

    if (bytesLeft <= MAX_PACKET_SIZE - HEADER_LEN) {
        // Single NAL unit packet
        rtp_socket_set_header(info, buffer);
        rtp_socket_mark_next_packet(buffer);

        memcpy(&buffer[RTP_HEADER_LENGTH], &message->data[4], message->length - 4);

        if (rtp_send_message(info, buffer, RTP_HEADER_LENGTH + message->length - 4) < 0) {
            LOGW("@@@ rtp_send_h264_message: Failed to send message.");
            return 0;
        }

        info->sequence_number++;
    } else {
        // FU-A Fragmentation Unit without interleaving
        uint32_t offset = 0;
        uint32_t length = 0;
        uint8_t *data = &message->data[start_pos];

        while (bytesLeft > 0) {
            rtp_socket_set_header(info, buffer);

            // Set FU-A indicator
            buffer[12] = (uint8_t) (header[4] & 0x60);
            buffer[12] += 28;

            // Set FU-A header
            buffer[13] = (uint8_t) (header[4] & 0x1F);
            if (offset == 0) {
                buffer[13] += 0x80;
            }

            if (bytesLeft > MAX_PACKET_SIZE - HEADER_LEN) {
                length = MAX_PACKET_SIZE - HEADER_LEN;
            } else {
                buffer[13] += 0x40;
                rtp_socket_mark_next_packet(buffer);
                length = bytesLeft;
            }

            memcpy(&buffer[HEADER_LEN], &data[offset], length);

            if (rtp_send_message(info, buffer, HEADER_LEN + length) < 0) {
                LOGW("@@@ rtp_send_h264_message: Failed to send message.");
                return 0;
            }

            offset += length;
            bytesLeft -= length;

            info->sequence_number++;
        }
    }

    return 1;
}
