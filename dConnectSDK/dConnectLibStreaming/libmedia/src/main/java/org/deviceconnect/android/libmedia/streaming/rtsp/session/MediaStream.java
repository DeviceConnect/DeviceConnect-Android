package org.deviceconnect.android.libmedia.streaming.rtsp.session;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.deviceconnect.android.libmedia.streaming.rtp.RtpPacketize;
import org.deviceconnect.android.libmedia.streaming.rtp.RtpSocket;
import org.deviceconnect.android.libmedia.streaming.sdp.MediaDescription;

public abstract class MediaStream {
    /**
     * 送信先のパケットを管理.
     */
    private final Map<RtpSocket, RtpPacketize> mRtpPacketizes = new HashMap<>();

    /**
     * トラック ID.
     */
    private String mTrackId;

    /**
     * 送信先のポート番号.
     */
    private int mDestinationPort;

    /**
     * セッション名.
     */
    private String mSession = createSession();

    /**
     * セッション名を作成します.
     *
     * @return セッション名
     */
    private String createSession() {
        return UUID.randomUUID().toString();
    }

    /**
     * セッション名を取得します.
     *
     * @return セッション名
     */
    public String getSession() {
        return mSession;
    }

    /**
     * MediaStream の破棄を行います.
     */
    public void release() {
        synchronized (mRtpPacketizes) {
            for (RtpSocket socket : mRtpPacketizes.keySet()) {
                socket.close();
            }
            mRtpPacketizes.clear();
        }
    }

    /**
     * パケットにデータを書き込みます.
     *
     * @param data 書き込むデータ
     * @param dataLength 書き込むデータサイズ
     * @param pts プレゼンテーションタイム
     */
    public void writePacket(byte[] data, int dataLength, long pts) {
        synchronized (mRtpPacketizes) {
            for (RtpPacketize packet : mRtpPacketizes.values()) {
                try {
                    packet.write(data, dataLength, pts);
                } catch (Exception e) {
                    // ignore.
                }
            }
        }
    }

    /**
     * 送信先のポート番号を取得します.
     *
     * @return 送信先のポート番号
     */
    public int getDestinationPort() {
        return mDestinationPort;
    }

    /**
     * 送信先のポート番号を設定します.
     *
     * @param destinationPort 送信先のポート番号
     */
    public void setDestinationPort(int destinationPort) {
        mDestinationPort = destinationPort;
    }

    /**
     * トラック ID を取得します.
     *
     * @return トラック ID
     */
    public String getTrackId() {
        return mTrackId;
    }

    /**
     * トラック ID を設定します.
     *
     * @param trackId トラック ID
     */
    public void setTrackId(String trackId) {
        mTrackId = trackId;
    }

    /**
     * 送信先の RtpSocket を追加します.
     *
     * @param socket 追加する RTP 用のソケット
     */
    public void addRtpSocket(RtpSocket socket) {
        RtpPacketize packet = createRtpPacketize();
        packet.setCallback(socket);
        packet.setSsrc(socket.getSsrc());

        socket.open();

        synchronized (mRtpPacketizes) {
            mRtpPacketizes.put(socket, packet);
        }
    }

    /**
     * 送信先の RtpSocket を削除します.
     *
     * @param socket 削除する RTP 用のソケット
     */
    public void removeRtpSocket(RtpSocket socket) {
        socket.close();

        synchronized (mRtpPacketizes) {
            mRtpPacketizes.remove(socket);
        }
    }

    /**
     * 送信するデータをパケット化するクラスを作成します.
     *
     * @return 送信するデータをパケット化するクラス
     */
    public abstract RtpPacketize createRtpPacketize();

    /**
     * エンコードするための情報を取得する処理を行います.
     */
    public abstract void configure();

    /**
     * メディアストリームの SDP 情報を取得します.
     *
     * @return メディアストリームの SDP 情報
     */
    public abstract MediaDescription getMediaDescription();
}
