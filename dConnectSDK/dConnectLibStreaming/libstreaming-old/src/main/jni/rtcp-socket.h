#ifndef RTCP_SERVER_H
#define RTCP_SERVER_H

#include <sys/socket.h>
#include <arpa/inet.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <errno.h>
#include <time.h>


#ifdef __cplusplus
extern "C" {
#endif


struct rtcp_socket_info {
    int sd;
    uint32_t packet_count;
    uint32_t octet_count;
    uint32_t ssrc;
    uint64_t rtpts;
    uint64_t ntpts;
    uint8_t ttl;
    uint16_t port;
    time_t last_send_time;
    struct sockaddr_in addr;
};


struct rtcp_socket_info *rtcp_socket_open(const uint8_t *host, uint16_t port);

void rtcp_socket_set_ttl(struct rtcp_socket_info *info, uint8_t ttl);

void rtcp_socket_set_ssrc(struct rtcp_socket_info *info, uint32_t ssrc);

void rtcp_socket_update(struct rtcp_socket_info *info, uint32_t packet_length);

void rtcp_socket_close(struct rtcp_socket_info *info);


#ifdef __cplusplus
}
#endif
#endif //RTCP_SERVER_H
