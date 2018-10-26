#include "common.h"
#include "rtp-socket.h"
#include "rtcp-socket.h"
#include "rtp-jpeg-packetize.h"
#include "rtp-h264-packetize.h"


/////////////// public ///////////////


ssize_t rtp_send_message(struct rtp_socket_info *info, uint8_t *message, uint32_t length) {
    rtcp_socket_update(info->rtcp_info, length);
    return sendto(info->sd, message, length, 0, (struct sockaddr *)&info->addr, sizeof(info->addr));
}


struct rtp_socket_info *rtp_socket_open(const uint8_t *host, uint16_t port) {
    struct rtp_socket_info *info = (struct rtp_socket_info *) calloc(1, sizeof(struct rtp_socket_info));
    if (info == NULL) {
        LOGE("@@@ rtp_socket_open: Failed to calloc a struct.");
        return NULL;
    }

    info->sd = socket(AF_INET, SOCK_DGRAM, 0);
    info->port = port;
    info->addr.sin_family = AF_INET;
    info->addr.sin_port = htons(port);
    info->addr.sin_addr.s_addr = inet_addr((const char *) host);

    in_addr_t in_addr = inet_addr("127.0.0.1");

    if (setsockopt(info->sd, IPPROTO_IP, IP_MULTICAST_IF, (char *)&in_addr, sizeof(in_addr)) != 0) {
        LOGE("@@@ rtp_socket_open: Failed to setsockopt IP_MULTICAST_IF.");
        rtp_socket_close(info);
        return NULL;
    }

    info->payload_type = 96;
    info->clock = 90000;

    return info;
}


void rtp_socket_set_ttl(struct rtp_socket_info *info, uint8_t ttl) {
    info->ttl = ttl;

    if (setsockopt(info->sd, IPPROTO_IP, IP_MULTICAST_TTL, (char *)&ttl, sizeof(ttl)) != 0) {
        LOGE("@@@ rtp_socket_open: Failed to setsockopt IP_MULTICAST_TTL.");
        rtp_socket_close(info);
        return;
    }
}


void rtp_socket_set_ssrc(struct rtp_socket_info *info, uint32_t ssrc) {
    info->ssrc = ssrc;
}


void rtp_socket_send_message(struct rtp_socket_info *info, struct rtp_message *message) {
    info->timestamp = message->timestamp;

    switch (message->packet_type) {
        case RTP_PACKET_H264:
            rtp_send_h264_packetize(info, message);
            break;

        case RTP_PACKET_MJPEG:
            rtp_send_mjpeg_packetize(info, message);
            break;

        default:
            LOGE("@@@ rtp_socket_send_message: Unknown packet type. %d", message->packet_type);
            break;
    }
}


void rtp_socket_close(struct rtp_socket_info *info) {
    if (info && info->sd) {
        close(info->sd);
        info->sd = 0;
    }
    SAFE_FREE(info);
}


void rtp_socket_set_header(struct rtp_socket_info *info, uint8_t *buffer) {
    buffer[0] = 2 << 6;
    buffer[1] = info->payload_type;
    buffer[2] = (uint8_t) ((info->sequence_number >> 8) & 0xFF);
    buffer[3] = (uint8_t) ((info->sequence_number) & 0xFF);
    buffer[4] = (uint8_t) ((info->timestamp >> 24) & 0xFF);
    buffer[5] = (uint8_t) ((info->timestamp >> 16) & 0xFF);
    buffer[6] = (uint8_t) ((info->timestamp >> 8) & 0xFF);
    buffer[7] = (uint8_t) ((info->timestamp) & 0xFF);
    buffer[8] = (uint8_t) ((info->ssrc >> 24) & 0xFF);
    buffer[9] = (uint8_t) ((info->ssrc >> 16) & 0xFF);
    buffer[10] = (uint8_t) ((info->ssrc >> 8) & 0xFF);
    buffer[11] = (uint8_t) ((info->ssrc) & 0xFF);
}


void rtp_socket_mark_next_packet(uint8_t *buffer) {
    buffer[1] |= 0x80;
}


uint32_t rtp_compute_ts(struct rtp_socket_info *info, uint64_t pts) {
    return (uint32_t) ((pts / 100L) * (info->clock / 1000L) / 10000L);
}