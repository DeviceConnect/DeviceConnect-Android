package org.deviceconnect.android.libsrt.server;

import org.deviceconnect.android.libmedia.streaming.IMediaMuxer;
import org.deviceconnect.android.libsrt.SRTSocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class SRTMuxer implements IMediaMuxer {
    /**
     * 送信先のソケットのリスト.
     */
    private final List<SRTSocket> mSRTSocketList = new ArrayList<>();

    /**
     * 送信先のソケットを追加します.
     *
     * @param socket 追加するソケット
     */
    public void addSRTClientSocket(SRTSocket socket) {
        synchronized (mSRTSocketList) {
            mSRTSocketList.add(socket);
        }
    }

    /**
     * 送信先のソケットを削除します.
     *
     * @param socket 削除するソケット
     */
    public void removeSRTClientSocket(SRTSocket socket) {
        synchronized (mSRTSocketList) {
            mSRTSocketList.remove(socket);
        }
    }

    /**
     * 各ソケットにデータを送信します.
     *
     * @param data 送信するデータ
     */
    public void sendPacket(byte[] data) {
        sendPacket(data, 0, data.length);
    }

    /**
     * 各ソケットにデータを送信します.
     *
     * @param data 送信するデータ
     */
    public void sendPacket(byte[] data, int offset, int length) {
        synchronized (mSRTSocketList) {
            for (SRTSocket socket : mSRTSocketList) {
                try {
                    socket.send(data, offset, length);
                } catch (IOException e) {
                    socket.close();
                }
            }
        }
    }
}
