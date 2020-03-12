package org.deviceconnect.android.libsrt.client;

import android.util.Log;

import org.deviceconnect.android.libsrt.BuildConfig;
import org.deviceconnect.android.libsrt.SRTSocket;
import org.deviceconnect.android.libsrt.SRTSocketException;
import org.deviceconnect.android.libsrt.SRTStats;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * SRT サーバと通信するクラス.
 */
public class SRTClient {
    /**
     * 統計データをログ出力するインターバルのデフォルト値. 単位はミリ秒.
     */
    static final long DEFAULT_STATS_INTERVAL = 5000;

    /**
     * デバッグ用タグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * タグ.
     */
    private static final String TAG = "SRT-CLIENT";

    /**
     * 接続先のアドレス.
     */
    private String mAddress;

    /**
     * 接続先のポート番号.
     */
    private int mPort;

    /**
     * SRT サーバとの接続を監視するスレッド.
     */
    private SRTSessionThread mSRTSessionThread;

    /**
     * SRT クライアントのイベントを通知するリスナー.
     */
    private OnEventListener mOnEventListener;

    /**
     * 定期的に統計情報をログ出力するタイマー.
     */
    private Timer mStatsTimer;

    /**
     * 統計データをログ出力するインターバル. 単位はミリ秒.
     */
    private long mStatsInterval = DEFAULT_STATS_INTERVAL;

    /**
     * 統計データをログに出力フラグ.
     *
     * <p>
     * trueの場合は、ログを出力します。
     * </p>
     */
    private boolean mShowStats;

    /**
     * クライアントのソケットに設定するオプション.
     */
    private Map<Integer, Object> mCustomSocketOptions;

    /**
     * コンストラクタ.
     * @param address 接続先のアドレス
     * @param port 接続先のポート番号
     */
    public SRTClient(String address, int port) {
        mAddress = address;
        mPort = port;
    }

    /**
     * ソケットに反映するオプションを設定します.
     *
     * <p>
     * オプションはサーバ側のソケットがバインドされる前に設定されます.
     * </p>
     *
     * @param socketOptions ソケットに反映するオプションd
     */
    public void setSocketOptions(Map<Integer, Object> socketOptions) {
        mCustomSocketOptions = socketOptions;
    }

    /**
     * SRT クライアントのイベントを通知するリスナーを設定します.
     *
     * @param listener リスナー
     */
    public void setOnEventListener(OnEventListener listener) {
        mOnEventListener = listener;
    }

    /**
     * 通信を開始します.
     */
    public synchronized void start() {
        if (mSRTSessionThread != null) {
            return;
        }
        mSRTSessionThread = new SRTSessionThread();
        mSRTSessionThread.setName("SRT-CLIENT-THREAD");
        mSRTSessionThread.start();
    }

    /**
     * 通信を停止します.
     */
    public synchronized void stop() {
        if (mSRTSessionThread != null) {
            mSRTSessionThread.terminate();
            mSRTSessionThread = null;
        }
    }

    /**
     * SRTサーバに接続されているか確認します.
     *
     * @return SRT サーバに接続されている場合はtrue、それ以外はfalse
     */
    public synchronized boolean isConnected() {
        return mSRTSessionThread != null &&
                mSRTSessionThread.mSRTSocket != null &&
                mSRTSessionThread.mSRTSocket.isConnected();
    }

    /**
     * SRT ソケットにオプションを設定します.
     *
     * <p>
     * Binding が post のオプションのみ、ソケットが接続された後にもオプションを設定することができます。
     * </p>
     *
     * @param option オプション
     * @param value 値
     */
    public synchronized void setSocketOption(int option, Object value) {
        if (mSRTSessionThread != null) {
            try {
                mSRTSessionThread.mSRTSocket.setOption(option, value);
            } catch (SRTSocketException e) {
                // ignore.
            }
        }
    }

    /**
     * SRT 統計データの LogCat への表示設定を行います.
     *
     * @param showStats LogCat に表示する場合はtrue、それ以外はfalse
     */
    public synchronized void setShowStats(boolean showStats) {
        mShowStats = showStats;

        // 既にクライアントが開始されている場合は、タイマーの設定を行います。
        if (mSRTSessionThread != null) {
            if (showStats) {
                startStatsTimer();
            } else {
                stopStatsTimer();
            }
        }
    }

    /**
     * SRT 統計情報を取得するインターバルを設定します.
     *
     * <p>
     * {@link #setShowStats(boolean)} の前に実行すること.
     * </p>
     *
     * @param interval インターバル. 単位はミリ秒
     */
    public void setStatsInterval(long interval) {
        mStatsInterval = interval;
    }

    /**
     * 統計情報の取得を開始します.
     */
    private void startStatsTimer() {
        if (mStatsTimer != null) {
            return;
        }
        mStatsTimer = new Timer();
        mStatsTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mSRTSessionThread != null) {
                    SRTSocket socket = mSRTSessionThread.mSRTSocket;
                    if (socket != null) {
                        SRTStats srtStats = socket.getStats();
                        if (DEBUG) {
                            Log.d(TAG, srtStats.toString());
                        }
                        postStats(srtStats);
                    }
                }
            }
        }, mStatsInterval, mStatsInterval);
    }

    /**
     * 統計情報の取得を停止します.
     */
    private void stopStatsTimer() {
        if (mStatsTimer != null) {
            mStatsTimer.cancel();
            mStatsTimer = null;
        }
    }

    /**
     * SRT サーバとの通信を監視するスレッド.
     */
    private class SRTSessionThread extends Thread {
        /**
         * バッファサイズを定義.
         */
        private static final int BUFFER_SIZE = 1500;

        /**
         * SRT ソケット.
         */
        private SRTSocket mSRTSocket;

        /**
         * 停止フラグ.
         */
        private boolean mStopFlag;

        SRTSessionThread() {
            setName("SRT-CLIENT");
        }

        /**
         * スレッドの終了処理を行います.
         */
        void terminate() {
            if (mSRTSocket != null) {
                try {
                    mSRTSocket.close();
                } catch (Exception e) {
                    // ignore.
                }
                mSRTSocket = null;
            }

            mStopFlag = true;

            interrupt();

            try {
                join(200);
            } catch (InterruptedException e) {
                // ignore.
            }
        }

        @Override
        public void run() {
            try {
                mSRTSocket = new SRTSocket();

                if (mCustomSocketOptions != null) {
                    for (Map.Entry<Integer, Object> o : mCustomSocketOptions.entrySet()) {
                        mSRTSocket.setOption(o.getKey(), o.getValue());
                    }
                }

                mSRTSocket.connect(mAddress, mPort);
            } catch (Exception e) {
                postOnError(e);
                return;
            }
            if (mShowStats) {
                startStatsTimer();
            }

            postOnConnected();

            try {
                int len;
                byte[] buffer = new byte[BUFFER_SIZE];

                while (!mStopFlag) {
                    len = mSRTSocket.recv(buffer, BUFFER_SIZE);
                    if (len > 0) {
                        postOnRead(buffer, len);
                    }
                }
            } catch (Exception e) {
                if (!mStopFlag) {
                    postOnError(e);
                }
            } finally {
                stopStatsTimer();

                if (mSRTSocket != null) {
                    try {
                        mSRTSocket.close();
                    } catch (Exception e) {
                        // ignore.
                    }
                    mSRTSocket = null;
                }

                postOnDisconnected();
            }
        }
    }

    private void postOnRead(byte[] data, int dataLength) {
        if (mOnEventListener != null) {
            mOnEventListener.onReceived(data, dataLength);
        }
    }

    private void postOnConnected() {
        if (mOnEventListener != null) {
            mOnEventListener.onConnected();
        }
    }

    private void postOnDisconnected() {
        if (mOnEventListener != null) {
            mOnEventListener.onDisconnected();
        }
    }

    private void postOnError(Exception e) {
        if (mOnEventListener != null) {
            mOnEventListener.onError(e);
        }
    }

    private void postStats(SRTStats stats) {
        if (mOnEventListener != null) {
            mOnEventListener.onStats(stats);
        }
    }

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
         * SRT サーバから送られ的たデータを受け取ります.
         *
         * <p>
         * 引数に渡されるデータは、同じ変数を使い回すので、
         * リスナーで受け取った先でコピーして使用するようにしてください。
         * </p>
         *
         * @param data 受信したデータ
         * @param dataLength 受信したデータサイズ
         */
        void onReceived(byte[] data, int dataLength);

        /**
         * SRT サーバへの接続に失敗したことを通知します.
         *
         * @param e 失敗原因の例外
         */
        void onError(Exception e);

        /**
         * SRT サーバとの通信状況を通知します.
         *
         * @param stats 通信統計情報
         */
        void onStats(SRTStats stats);
    }
}
