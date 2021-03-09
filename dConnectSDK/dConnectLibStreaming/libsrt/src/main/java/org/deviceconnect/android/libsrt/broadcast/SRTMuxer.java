package org.deviceconnect.android.libsrt.broadcast;

import android.net.Uri;

import org.deviceconnect.android.libmedia.streaming.MediaEncoderException;
import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;
import org.deviceconnect.android.libsrt.SRT;
import org.deviceconnect.android.libsrt.SRTSocket;
import org.deviceconnect.android.libsrt.SRTSocketException;
import org.deviceconnect.android.libsrt.util.Mpeg2TsMuxer;

public class SRTMuxer extends Mpeg2TsMuxer {
    /**
     * 接続先の URI.
     */
    private final String mBroadcastURI;

    /**
     * 接続先のアドレス.
     */
    private final String mAddress;

    /**
     * 接続先のポート番号.
     */
    private final int mPort;

    /**
     * ソケット.
     */
    private SRTSocket mSocket;

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
        return mSocket != null && mSocket.isConnected();
    }

    @Override
    public boolean onPrepare(VideoQuality videoQuality, AudioQuality audioQuality) {
        if (mSocket != null) {
            return false;
        }

        try {
            mSocket = new SRTSocket();
            mSocket.setOption(SRT.SRTO_SENDER, true);
            mSocket.connect(mAddress, mPort);
        } catch (SRTSocketException e) {
            if (mOnEventListener != null) {
                mOnEventListener.onError(new MediaEncoderException(e));
            }
            return false;
        }

        if (mOnEventListener != null) {
            mOnEventListener.onConnected();
        }

        boolean prepareResult = super.onPrepare(videoQuality, audioQuality);
        if (!prepareResult) {
            // prepare に失敗した場合にはソケットを閉じておく
            mSocket.close();
            mSocket = null;
        }
        return prepareResult;
    }

    @Override
    public void onReleased() {
        super.onReleased();

        if (mSocket != null) {
            mSocket.close();
            mSocket = null;
        }
    }

    @Override
    public void sendPacket(byte[] data, int offset, int length) {
        try {
            mSocket.send(data, offset,length);
        } catch (SRTSocketException e) {
            if (mOnEventListener != null) {
                mOnEventListener.onError(new MediaEncoderException(e));
            }
        }
    }

    /**
     * SRTMuxer のイベントを通知するリスナー.
     */
    public interface OnEventListener {
        /**
         * SRT サーバに接続されたことを通知します.
         */
        void onConnected();

        /**
         * SRT サーバから切断されたことを通知します.
         */
        void onDisconnected();

        /**
         * SRT サーバからの通信ビットレートを通知します.
         *
         * @param bitrate ビットレート
         */
        void onNewBitrate(long bitrate);

        /**
         * SRT サーバとの接続でエラーが発生したことを通知します.
         *
         * @param e エラー原因の例外
         */
        void onError(MediaEncoderException e);
    }
}
