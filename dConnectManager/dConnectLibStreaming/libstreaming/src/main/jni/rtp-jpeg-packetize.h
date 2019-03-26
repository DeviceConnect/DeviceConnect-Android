#ifndef RTP_JPEG_PACKETIZE_H
#define RTP_JPEG_PACKETIZE_H

#include <sys/socket.h>
#include <arpa/inet.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <errno.h>

#ifdef __cplusplus
extern "C" {
#endif

struct rtp_socket_info;
struct rtp_message;

struct rtp_jpeg_data {
    uint32_t sos_pos;
    uint16_t dri;
    uint16_t width;
    uint16_t height;
    uint8_t quantization_num;
    uint8_t quantization[4][64];
};

ssize_t rtp_send_mjpeg_packetize(struct rtp_socket_info *info, struct rtp_message *message);

#ifdef __cplusplus
}
#endif
#endif //RTP_JPEG_PACKETIZE_H
