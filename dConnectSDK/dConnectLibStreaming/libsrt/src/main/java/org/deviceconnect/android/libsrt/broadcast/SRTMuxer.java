package org.deviceconnect.android.libsrt.broadcast;

import android.net.Uri;

import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;
import org.deviceconnect.android.libsrt.SRTSocketException;
import org.deviceconnect.android.libsrt.SRTStats;
import org.deviceconnect.android.libsrt.util.Mpeg2TsMuxer;
import org.deviceconnect.android.libsrt.util.SRTSocketThread;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class SRTMuxer extends Mpeg2TsMuxer {
    /**
     * 接続先の URI.
     */
    private String mBroadcastURI;

    /**
     * 接続先のアドレス.
     */
    private String mAddress;

    /**
     * 接続先のポート番号.
     */
    private int mPort;

    /**
     * SRTSocket 通信を管理するスレッド.
     */
    private SRTSocketThread mSRTSocketThread;

    /**
     * イベントを通知するためのリスナー.
     */
    private OnEventListener mOnEventListener;

    /**
     * コンストラクタ.
     * @param broadcastURI 接続先の URI
     */
    public SRTMuxer(String broadcastURI) {
        mBroadcastURI = broadcastURI;

        Uri uri = Uri.parse(broadcastURI);
        if (!"srt".equals(uri.getScheme())) {
            throw new IllegalArgumentException("broadcastURI scheme is not srt.");
        }

        mAddress = uri.getHost();
        mPort = uri.getPort();
    }

    /**
     * 接続先の URI を取得します.
     *
     * @return 接続先の URI
     */
    public String getBroadcastURI() {
        return mBroadcastURI;
    }

    /**
     * イベントを通知するためのリスナーを設定します.
     *
     * @param listener リスナー
     */
    public void setOnEventListener(OnEventListener listener) {
        mOnEventListener = listener;
    }

    /**
     * SRT サーバに接続されているか確認します.
     *
     * @return SRT サーバに接続されている場合はtrue、それ以外はfalse
     */
    private boolean isConnected() {
        return mSRTSocketThread != null && mSRTSocketThread.isConnected();
    }

    @Override
    public boolean onPrepare(VideoQuality videoQuality, AudioQuality audioQuality) {
        if (mSRTSocketThread != null) {
            return false;
        }

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean result = new AtomicBoolean(false);

        mSRTSocketThread = new SRTSocketThread(mAddress, mPort);
        mSRTSocketThread.setOnEventListener(new SRTSocketThread.OnEventListener() {
            @Override
            public void onConnected() {
                result.set(true);
                latch.countDown();
            }

            @Override
            public void onnErrorConnecting(Exception e) {
                result.set(false);
                latch.countDown();
            }

            @Override
            public void onDisconnected() {
                if (mOnEventListener != null) {
                    mOnEventListener.onDisconnected();
                }
            }

            @Override
            public void onReceived(byte[] data, int dataLength) {
                // TODO 受信データ
            }

            @Override
            public void onError(Exception e) {
                // TODO エラー受信データ
            }

            @Override
            public void onStats(SRTStats stats) {
            }
        });

        // SRT の配信準備が完了してから、エンコード処理を行わないと処理が進まないので、
        // ここで、配信準備が完了するのを待ちます。
        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // ignore.
        }

        if (!result.get()) {
            return false;
        }

        if (mOnEventListener != null) {
            mOnEventListener.onConnected();
        }

        boolean prepareResult = super.onPrepare(videoQuality, audioQuality);
        if (!prepareResult) {
            // prepare に失敗した場合にはスレッドを停止しておく
            mSRTSocketThread.stop();
            mSRTSocketThread = null;
        }
        return prepareResult;
    }

    @Override
    public void onReleased() {
        super.onReleased();

        if (mSRTSocketThread != null) {
            mSRTSocketThread.stop();
            mSRTSocketThread = null;
        }
    }

    @Override
    public void sendPacket(byte[] data, int offset, int length) {
        try {
            mSRTSocketThread.sendData(data, offset, length);
        } catch (SRTSocketException e) {
            // TODO エラー処理
        }
    }

    /**
     * RtmpMuxer のイベントを通知するリスナー.
     */
    public interface OnEventListener {
        /**
         * RTMP サーバに接続されたことを通知します.
         */
        void onConnected();

        /**
         * RTMP サーバから切断されたことを通知します.
         */
        void onDisconnected();

        /**
         * RTMP サーバからの通信ビットレートを通知します.
         *
         * @param bitrate ビットレート
         */
        void onNewBitrate(long bitrate);
    }
}
