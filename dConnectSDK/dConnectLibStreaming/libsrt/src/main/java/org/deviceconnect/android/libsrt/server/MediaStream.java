package org.deviceconnect.android.libsrt.server;

import android.util.Log;

import org.deviceconnect.android.libmedia.streaming.mpeg2ts.H264TsSegmenter;
import org.deviceconnect.android.libsrt.SRTSocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public abstract class MediaStream {
    /**
     * 送信先のソケットのリスト.
     */
    private final List<SRTSocket> mSRTSocketList = new ArrayList<>();

    /**
     * H264 のセグメントに分割するkクラス.
     */
    private H264TsSegmenter mH264TsSegmenter;

    /**
     * データを一時的に格納するバッファ.
     */
    private ByteBuffer mByteBuffer = ByteBuffer.allocate(4096);

    private final H264TsSegmenter.BufferListener mBufferListener = (byte[] result) -> {
        try {
            final int max = 188 * 7;
            if (result.length > max) {
                for (int offset = 0; offset < result.length; offset += max) {
                    final int length;
                    if (result.length - offset < max) {
                        length = result.length - offset;
                    } else {
                        length = max;
                    }
                    byte[] data = new byte[length];
                    System.arraycopy(result, offset, data, 0, length);
                    sendPacket(data);
                }
            } else {
                sendPacket(result);
            }
        } catch (Exception e) {
            Log.e("ABC", "Failed to send packet", e);
        }
    };

    public void configure() {
        mH264TsSegmenter = new H264TsSegmenter();
        mH264TsSegmenter.setBufferListener(mBufferListener);
        mH264TsSegmenter.initialize(0, 0,0, 30);
    }

    public void release() {
        // TODO 後始末
    }

    public void writePacket(byte[] packet, int packetLength, long pts) {
        if (mByteBuffer.capacity() < packetLength) {
            mByteBuffer = ByteBuffer.allocate(packetLength);
        }
        mByteBuffer.clear();
        mByteBuffer.put(packet, 0, packetLength);
        mByteBuffer.flip();

        mH264TsSegmenter.generatePackets(mByteBuffer, pts / 1000L);
    }

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
    private void sendPacket(byte[] data) {
        synchronized (mSRTSocketList) {
            for (SRTSocket socket : mSRTSocketList) {
                try {
                    socket.send(data, data.length);
                } catch (IOException e) {
                    socket.close();
                }
            }
        }
    }
}
