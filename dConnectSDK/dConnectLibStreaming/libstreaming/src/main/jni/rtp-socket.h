#ifndef RTP_SERVER_H
#define RTP_SERVER_H

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


/**
 * パケットのタイプを定義します.
 */
typedef enum _rtp_packet_type {
    RTP_PACKET_H264 = 1,
    RTP_PACKET_MJPEG = 2
} rtp_packet_type;


struct rtcp_socket_info;


/**
 * RTPメッセージ用の構造体.
 */
struct rtp_message {
    /**
     * パケットのタイプ.
     */
    rtp_packet_type packet_type;

    /**
     * パケットのデータ.
     */
    uint8_t *data;

    /**
     * パケットのデータサイズ.
     */
    uint32_t length;

    /**
     * パケットのタイムスタンプ.
     */
    uint64_t timestamp;
};


/**
 * RTP用の構造体.
 */
struct rtp_socket_info {
    /**
     * ペイロード・タイプ(PT).
     */
    uint8_t payload_type;

    /**
     * シーケンス番号.
     */
    uint32_t sequence_number;

    /**
     * 同期送信元(SSRC)識別子.
     */
    uint32_t ssrc;

    /**
     * タイムスタンプ.
     */
    uint64_t timestamp;

    /**
     *
     */
    uint64_t clock;

    uint8_t count;
    uint8_t header[5];

    /**
     * UDPソケットのディスクリプタ.
     */
    int sd;

    /**
     * Time To Live.
     */
    uint8_t ttl;

    /**
     * UDPソケットのポート番号.
     */
    uint16_t port;

    /**
     * UDPソケットのアドレス.
     */
    struct sockaddr_in addr;

    /**
     * RTCP用の構造体.
     */
    struct rtcp_socket_info *rtcp_info;
};


struct rtp_socket_info *rtp_socket_open(const uint8_t *host, uint16_t port);

void rtp_socket_set_ttl(struct rtp_socket_info *info, uint8_t ttl);

void rtp_socket_set_ssrc(struct rtp_socket_info *info, uint32_t ssrc);

void rtp_socket_send_message(struct rtp_socket_info *info, struct rtp_message *message);

void rtp_socket_close(struct rtp_socket_info *info);

ssize_t rtp_send_message(struct rtp_socket_info *info, uint8_t *message, uint32_t length);

void rtp_socket_set_header(struct rtp_socket_info *info, uint8_t *buffer);

void rtp_socket_mark_next_packet(uint8_t *buffer);

uint32_t rtp_compute_ts(struct rtp_socket_info *info, uint64_t pts);


#ifdef __cplusplus
}
#endif
#endif //RTP_SERVER_H
