#include "common.h"
#include "rtcp-socket.h"

// RTCPパケットサイズ
#define PACKET_LENGTH 28

static ssize_t rtcp_send_message(struct rtcp_socket_info *info, uint8_t *message, uint32_t length) {
    return sendto(info->sd, message, length, 0, (struct sockaddr *)&info->addr, sizeof(info->addr));
}

static void rtcp_socket_send_report(struct rtcp_socket_info *info) {
    // Byte  0
    // Byte  1           ->  Packet Type PT
    // Byte  2, 3        ->  Length
    // Byte  4, 5, 6, 7  ->  SSRC
    // Byte  8, 9,10,11  ->  NTP timestamp hb
    // Byte 12,13,14,15  ->  NTP timestamp lb
    // Byte 16,17,18,19  ->  RTP timestamp
    // Byte 20,21,22,23  ->  packet count
    // Byte 24,25,26,27  ->  octet count

    uint32_t packetLen = PACKET_LENGTH / 4 - 1;
    uint32_t hb = (uint32_t) (info->ntpts / 1000000000);
    uint32_t lb = (uint32_t) (((info->ntpts - hb * 1000000000) * 4294967296L) / 1000000000);

    uint8_t buffer[PACKET_LENGTH];
    buffer[0] = 2 << 6;
    buffer[1] = 200;
    buffer[2] = (uint8_t) ((packetLen >> 8) & 0xFF);
    buffer[3] = (uint8_t) ((packetLen) & 0xFF);
    buffer[4] = (uint8_t) ((info->ssrc >> 24) & 0xFF);
    buffer[5] = (uint8_t) ((info->ssrc >> 16) & 0xFF);
    buffer[6] = (uint8_t) ((info->ssrc >> 8) & 0xFF);
    buffer[7] = (uint8_t) ((info->ssrc) & 0xFF);
    buffer[8] = (uint8_t) ((hb >> 24) & 0xFF);
    buffer[9] = (uint8_t) ((hb >> 16) & 0xFF);
    buffer[10] = (uint8_t) ((hb >> 8) & 0xFF);
    buffer[11] = (uint8_t) ((hb) & 0xFF);
    buffer[12] = (uint8_t) ((lb >> 24) & 0xFF);
    buffer[13] = (uint8_t) ((lb >> 16) & 0xFF);
    buffer[14] = (uint8_t) ((lb >> 8) & 0xFF);
    buffer[15] = (uint8_t) ((lb) & 0xFF);
    buffer[16] = (uint8_t) ((info->rtpts >> 24) & 0xFF);
    buffer[17] = (uint8_t) ((info->rtpts >> 16) & 0xFF);
    buffer[18] = (uint8_t) ((info->rtpts >> 8) & 0xFF);
    buffer[19] = (uint8_t) ((info->rtpts) & 0xFF);
    buffer[20] = (uint8_t) ((info->packet_count >> 24) & 0xFF);
    buffer[21] = (uint8_t) ((info->packet_count >> 16) & 0xFF);
    buffer[22] = (uint8_t) ((info->packet_count >> 8) & 0xFF);
    buffer[23] = (uint8_t) ((info->packet_count) & 0xFF);
    buffer[24] = (uint8_t) ((info->octet_count >> 24) & 0xFF);
    buffer[25] = (uint8_t) ((info->octet_count >> 16) & 0xFF);
    buffer[26] = (uint8_t) ((info->octet_count >> 8) & 0xFF);
    buffer[27] = (uint8_t) ((info->octet_count) & 0xFF);

    rtcp_send_message(info, buffer, PACKET_LENGTH);
}

//////////////// public /////////////////////


struct rtcp_socket_info *rtcp_socket_open(const uint8_t *host, uint16_t port) {
    struct rtcp_socket_info *info = (struct rtcp_socket_info *) calloc(1, sizeof(struct rtcp_socket_info));
    if (info == NULL) {
        LOGE("@@@ rtcp_socket_open: Failed to calloc a struct.");
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
        rtcp_socket_close(info);
        return NULL;
    }

    info->last_send_time = time(NULL);

    return info;
}


void rtcp_socket_set_ttl(struct rtcp_socket_info *info, uint8_t ttl) {
    info->ttl = ttl;

    if (setsockopt(info->sd, IPPROTO_IP, IP_MULTICAST_TTL, (char *)&ttl, sizeof(ttl)) != 0) {
        LOGE("@@@ rtcp_socket_open: Failed to setsockopt IP_MULTICAST_TTL.");
        rtcp_socket_close(info);
        return;
    }
}


void rtcp_socket_set_ssrc(struct rtcp_socket_info *info, uint32_t ssrc) {
    info->ssrc = ssrc;
    info->packet_count = 0;
    info->octet_count = 0;
}


void rtcp_socket_update(struct rtcp_socket_info *info, uint32_t packet_length) {
    info->packet_count++;
    info->octet_count += packet_length;

    time_t now = time(NULL);
    double d = difftime(now, info->last_send_time);
    if (d >= 5) {
        rtcp_socket_send_report(info);
        info->last_send_time = now;
    }
}


void rtcp_socket_close(struct rtcp_socket_info *info) {
    if (info && info->sd) {
        close(info->sd);
    }
    SAFE_FREE(info);
}
