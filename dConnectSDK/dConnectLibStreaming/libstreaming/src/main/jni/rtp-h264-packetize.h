#ifndef RTP_H264_PACKETIZE_H
#define RTP_H264_PACKETIZE_H

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

ssize_t rtp_send_h264_packetize(struct rtp_socket_info *info, struct rtp_message *message);

#ifdef __cplusplus
}
#endif
#endif //RTP_H264_PACKETIZE_H

