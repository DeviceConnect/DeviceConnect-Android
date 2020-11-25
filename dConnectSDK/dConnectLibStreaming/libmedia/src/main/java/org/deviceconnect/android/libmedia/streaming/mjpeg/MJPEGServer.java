package org.deviceconnect.android.libmedia.streaming.mjpeg;

import android.util.Log;

import org.deviceconnect.android.libmedia.BuildConfig;
import org.deviceconnect.android.libmedia.streaming.util.MixedReplaceMediaServer;

import java.io.IOException;
import java.net.Socket;

import javax.net.ssl.SSLContext;

public class MJPEGServer {
    /**
     * デバッグフラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * デバッグ用タグ.
     */
    private static final String TAG = "MJPEG";

    /**
     * サーバ名.
     */
    private String mServerName = "MJPEG";

    /**
     * サーバのポート番号.
     */
    private int mServerPort = 20000;

    /**
     * サーバのパス.
     */
    private String mServerPath = "mjpeg";

    /**
     * サーバ.
     */
    private MixedReplaceMediaServer mMixedReplaceMediaServer;

    /**
     * MJPEGエンコーダ.
     */
    private MJPEGEncoder mMJPEGEncoder;

    /**
     * MJPEG サーバのイベントを通知するコールバック.
     */
    private Callback mCallback;
    /**
     * SSL Context.
     */
    private SSLContext mSSLContext;

    /**
     * MJPEG サーバへのイベントを通知するコールバックを設定します.
     *
     * @param callback コールバック
     */
    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    /**
     * サーバのポート番号を設定します.
     *
     * @param serverPort サーバのポート番号
     */
    public void setServerPort(int serverPort) {
        mServerPort = serverPort;
    }

    /**
     * サーバ名を設定します.
     *
     * @param serverName サーバ名
     */
    public void setServerName(String serverName) {
        mServerName = serverName;
    }

    /**
     * サーバのパスを設定します.
     *
     * @param serverPath サーバのパス
     */
    public void setServerPath(String serverPath) {
        mServerPath = serverPath;
    }

    /**
     * MJPEG サーバへの URI を取得します.
     * <p>
     * {@link #start()} がされていない場合には null を返却します。
     * </p>
     * @return MJPEG サーバへの URI
     */
    public String getUri() {
        if (mMixedReplaceMediaServer == null) {
            return null;
        }
        return mMixedReplaceMediaServer.getUrl();
    }

    /**
     * MJPEG のエンコーダを取得します.
     *
     * @return MJPEG のエンコーダ
     */
    public MJPEGEncoder getMJPEGEncoder() {
        return mMJPEGEncoder;
    }

    public void setSSLContext(final SSLContext sslContext) {
        mSSLContext = sslContext;
    }
    /**
     * MJPEG のエンコーダを再起動します.
     *
     * <p>
     * エンコーダが設定されていない場合には何も処理を行いません。
     * </p>
     */
    public void restartEncoder() {
        if (mMJPEGEncoder != null) {
            mMJPEGEncoder.stop();
            mMJPEGEncoder.start();
        }
    }

    /**
     * MJPEG サーバを開始します.
     */
    public synchronized void start() throws IOException {
        if (mMixedReplaceMediaServer != null) {
            if (DEBUG) {
                Log.w(TAG, "MixedReplaceMediaServer is already started.");
            }
            return;
        }

        mMixedReplaceMediaServer = new MixedReplaceMediaServer();
        mMixedReplaceMediaServer.setSSLContext(mSSLContext);
        mMixedReplaceMediaServer.setPort(mServerPort);
        mMixedReplaceMediaServer.setServerName(mServerName);
        mMixedReplaceMediaServer.setPath(mServerPath);
        mMixedReplaceMediaServer.setCallback(new MixedReplaceMediaServer.Callback() {
            @Override
            public boolean onAccept(Socket socket) {
                synchronized (MJPEGServer.this) {
                    boolean result = false;
                    if (mCallback != null) {
                        result = mCallback.onAccept(socket);
                        if (result) {
                            try {
                                startMJPEGEncoder();
                            } catch (Exception e) {
                                stopMJPEGEncoder();
                                return false;
                            }
                        }
                    }
                    return result && mMJPEGEncoder != null;
                }
            }

            @Override
            public void onClosed(Socket socket) {
                synchronized (MJPEGServer.this) {
                    if (mCallback != null) {
                        mCallback.onClosed(socket);
                        if (mMixedReplaceMediaServer == null || mMixedReplaceMediaServer.isEmptyConnection()) {
                            stopMJPEGEncoder();
                        }
                    }
                }
            }
        });
        String url = mMixedReplaceMediaServer.start();
        if (url == null) {
            throw new IOException("Failed to start a MJPEGServer.");
        }

        if (DEBUG) {
            Log.i(TAG, "MixedReplaceMediaServer is started.");
            Log.i(TAG, "  port: " + mServerPort);
        }
    }

    /**
     * MJPEG サーバを停止します.
     */
    public synchronized void stop() {
        if (mCallback != null) {
            stopMJPEGEncoder();
        }

        if (mMixedReplaceMediaServer != null) {
            mMixedReplaceMediaServer.stop();
            mMixedReplaceMediaServer = null;
        }

        if (DEBUG) {
            Log.i(TAG, "MixedReplaceMediaServer is stopped.");
        }
    }

    private synchronized void startMJPEGEncoder() throws MJPEGEncoderException {
        if (mMJPEGEncoder != null) {
            return;
        }

        mMJPEGEncoder = mCallback.createMJPEGEncoder();
        if (mMJPEGEncoder != null) {
            mMJPEGEncoder.setCallback((byte[] jpeg) ->
                mMixedReplaceMediaServer.offerMedia(jpeg));
            mMJPEGEncoder.start();
        } else {
            throw new MJPEGEncoderException("Failed to create MJPEG Encoder.");
        }
    }

    private synchronized void stopMJPEGEncoder() {
        if (mMJPEGEncoder != null) {
            try {
                mMJPEGEncoder.stop();
            } catch (Exception e) {
                // ignore.
            }

            try {
                mCallback.releaseMJPEGEncoder(mMJPEGEncoder);
            } catch (Exception e) {
                // ignore.
            }
            mMJPEGEncoder = null;
        }
    }

    /**
     * MJPEG サーバへの接続・切断などのイベントがあったことを通知します.
     */
    public interface Callback {
        /**
         * MJPEG サーバへの接続要求を通知します.
         *
         * @param socket 接続要求してきたソケット
         * @return 接続を許可する場合はtrue、それ以外はfalse
         */
        boolean onAccept(Socket socket);

        /**
         * MJPEGサーバからソケットが切断されたことを通知します.
         *
         * @param socket 切断されたソケット
         */
        void onClosed(Socket socket);

        /**
         * MJPEG エンコーダの初期化を行います.
         *
         * @return MJPEG エンコーダ
         */
        MJPEGEncoder createMJPEGEncoder() throws MJPEGEncoderException;

        /**
         * MJPEG エンコーダの後始末を行います.
         *
         * @param encoder 後始末を行う MJPEG エンコーダ
         */
        void releaseMJPEGEncoder(MJPEGEncoder encoder);
    }
}
