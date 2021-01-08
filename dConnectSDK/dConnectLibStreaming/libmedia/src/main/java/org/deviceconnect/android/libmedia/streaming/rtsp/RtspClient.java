package org.deviceconnect.android.libmedia.streaming.rtsp;

import android.net.Uri;
import android.util.Log;

import org.deviceconnect.android.libmedia.BuildConfig;
import org.deviceconnect.android.libmedia.streaming.rtp.RtpReceiver;
import org.deviceconnect.android.libmedia.streaming.sdp.Attribute;
import org.deviceconnect.android.libmedia.streaming.sdp.MediaDescription;
import org.deviceconnect.android.libmedia.streaming.sdp.SessionDescription;
import org.deviceconnect.android.libmedia.streaming.sdp.SessionDescriptionParser;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RtspClient {
    /**
     * デバッグフラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * デバッグ用タグ.
     */
    private static final String TAG = "RTSP-CLIENT";

    /**
     * RTSP サーバとのセッションを維持するためのスレッド.
     */
    private SessionThread mSessionThread;

    /**
     * RTSP サーバの URL.
     */
    private final String mRtspServerUrl;

    /**
     * RTSP サーバに送信するリクエストに付加するユーザエージェント.
     */
    private String mUserAgent = "RTSP-CLIENT";

    /**
     * 受信した RTP パケットを通知するコールバック.
     */
    private OnEventListener mOnEventListener;

    /**
     * RTSP サーバとの接続タイムアウト.
     */
    private int mConnectionTimeout = 10 * 1000;

    /**
     * 指定ポート番号リスト.
     */
    private final List<Integer> mRtpPortList = new ArrayList<>();

    /**
     * 使用ポート番号リスト.
     */
    private final List<Integer> mUsePortList = new ArrayList<>();

    /**
     * コンストラクタ.
     * @param url RTSP サーバの URL
     */
    public RtspClient(String url) {
        this(url, new ArrayList<>());
    }

    /**
     * コンストラクタ.
     * @param url RTSP サーバの URL
     * @param rtpPortList RTP/RTCPに指定するUDPポート番号一覧
     */
    public RtspClient(String url, List<Integer> rtpPortList) {
        if (url == null) {
            throw new IllegalArgumentException("url is null.");
        }

        if (rtpPortList == null) {
            throw new IllegalArgumentException("setPortList is null.");
        }

        for (int port : rtpPortList) {
            if (port <= 1024) {
                throw new IllegalArgumentException("rtpPortList is invalid port number. (Must be greater than 1025.)");
            }
        }

        mRtspServerUrl = url;
        mRtpPortList.addAll(rtpPortList);
    }

    /**
     * 受信した RTP パケットを通知するリスナーを設定します.
     *
     * @param listener リスナー
     */
    public void setOnEventListener(OnEventListener listener) {
        mOnEventListener = listener;
    }

    /**
     * RTSP サーバとの接続タイムアウト時間を設定します.
     *
     * 単位: ミリ秒
     *
     * デフォルトでは、10000 (10秒) が設定されています。
     *
     * @param connectionTimeout 接続タイムアウト時間
     */
    public void setConnectionTimeout(int connectionTimeout) {
        mConnectionTimeout = connectionTimeout;
    }

    /**
     * ユーザエージェントを設定します.
     *
     * @param userAgent ユーザエージェント
     */
    public void setUserAgent(String userAgent) {
        mUserAgent = userAgent;
    }

    /**
     * RTSP サーバとの接続を開始します.
     * <p>
     * 既に接続されている場合は、何も処理を行いません。
     * </p>
     */
    public synchronized void start() {
        if (mSessionThread != null) {
            if (DEBUG) {
                Log.w(TAG, "RtspClient is already running.");
            }
            return;
        }

        mSessionThread = new SessionThread();
        mSessionThread.setName("RTSP-CLIENT");
        mSessionThread.start();
    }

    /**
     * RTSP サーバとの接続を切断します.
     */
    public synchronized void stop() {
        if (mSessionThread != null) {
            mSessionThread.terminate();
            mSessionThread = null;
        }
    }

    /**
     * RTSP クライアントの動作状態を確認します.
     *
     * @return 動作中の場合はtrue、それ以外はfalse
     */
    public synchronized boolean isRunning() {
        return mSessionThread != null;
    }

    /**
     * 受信したデータサイズを取得します.
     *
     * @return 受信したデータサイズ
     */
    public long getReceivedSize() {
        return mSessionThread != null ? mSessionThread.getReceivedSize() : 0;
    }

    /**
     * 受信したデータの BPS を取得します.
     *
     * @return 受信したデータの BPS
     */
    public long getBPS() {
        return mSessionThread != null ? mSessionThread.getBPS() : 0;
    }

    private void postOnError(RtspClientException e) {
        if (mOnEventListener != null) {
            mOnEventListener.onError(e);
        }
    }

    private void postOnSdpReceived(SessionDescription sdp) {
        if (mOnEventListener != null) {
            mOnEventListener.onSdpReceived(sdp);
        }
    }

    private void postOnRtpReceived(MediaDescription md, byte[] data, int dataLength) {
        if (mOnEventListener != null) {
            mOnEventListener.onRtpReceived(md, data, dataLength);
        }
    }

    private void postOnRtcpReceived(MediaDescription md, byte[] data, int dataLength) {
        if (mOnEventListener != null) {
            mOnEventListener.onRtcpReceived(md, data, dataLength);
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

    /**
     * RTSP サーバとの接続を維持するためのスレッド.
     */
    private class SessionThread extends Thread {
        /**
         * 停止フラグ.
         */
        private boolean mStopFlag;

        /**
         * RTSP サーバとの接続を行うソケット.
         */
        private Socket mSocket;

        /**
         * RTSP サーバからデータを読み込むためのリーダー.
         */
        private BufferedReader mReader;

        /**
         * RTSP サーバにデータを書き込むライター.
         */
        private OutputStream mWriter;

        /**
         * シーケンス番号.
         */
        private int mCseq = 1;

        /**
         * セッション名.
         */
        private String mSession;

        /**
         * RTSP サーバのベース URL.
         */
        private String mContentBase;

        /**
         * RTSP サーバとのセッションを確認する通信を行う間隔.
         */
        private int mInterval = 30 * 1000;

        /**
         * SDP.
         */
        private SessionDescription mSessionDescription;

        /**
         * RTP を受信するためのレシーバ.
         */
        private final List<RtpReceiver> mRtpReceivers = new ArrayList<>();

        /**
         * クライアント側のポート番号.
         */
        private int[] mClientPorts;

        /**
         * サーバ側のポート番号.
         */
        private int[] mServerPorts;

        /**
         * インターバルを設定します.
         *
         * @param interval インターバル
         */
        private void setInterval(int interval) {
            mInterval = interval;
        }

        /**
         * スレッドの停止処理を行います.
         */
        private void terminate() {
            mStopFlag = true;

            interrupt();

            try {
                join(500);
            } catch (InterruptedException e) {
                // ignore.
            }
        }

        @Override
        public void run() {
            try {
                Uri uri = Uri.parse(mRtspServerUrl);

                if (DEBUG) {
                    Log.d(TAG, "RTSP CLIENT START");
                    Log.d(TAG, "  HOST: " + uri.getHost());
                    Log.d(TAG, "  PORT: " + uri.getPort());
                }

                InetSocketAddress endpoint= new InetSocketAddress(uri.getHost(), uri.getPort());

                mSocket = new Socket();
                mSocket.setKeepAlive(true);
                mSocket.setReuseAddress(true);
                mSocket.connect(endpoint, mConnectionTimeout);
                mReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                mWriter = new BufferedOutputStream(mSocket.getOutputStream());

                postOnConnected();

                processOptions();
                processDescribe();

                if (mSessionDescription != null) {
                    for (MediaDescription md : mSessionDescription.getMediaDescriptions()) {
                        processSetup(md);
                    }
                }

                processPlay();

                while (!mStopFlag) {
                    try {
                        Thread.sleep(mInterval);
                    } catch (InterruptedException e) {
                        break;
                    }

                    processOptions();
                }

                processTearDown();
            } catch (RtspClientException e) {
                if (!mStopFlag) {
                    postOnError(e);
                }
            } catch (Exception e) {
                if (!mStopFlag) {
                    postOnError(new RtspClientException(e, RtspResponse.Status.STATUS_UNKNOWN));
                }
            } finally {
                synchronized (mRtpReceivers) {
                    for (RtpReceiver receiver : mRtpReceivers) {
                        receiver.close();
                        // 使用ポート番号リストからポート番号削除
                        mUsePortList.remove((Integer) receiver.getRtpPort());
                        mUsePortList.remove((Integer) receiver.getRtcpPort());
                    }
                    mRtpReceivers.clear();
                }

                if (mSocket != null) {
                    try {
                        mSocket.close();
                    } catch (IOException e) {
                        // ignore.
                    }
                }

                postOnDisconnected();

                if (DEBUG) {
                    Log.d(TAG, "RTSP CLIENT END");
                }
            }
        }

        private long getReceivedSize() {
            long size = 0;
            synchronized (mRtpReceivers) {
                for (RtpReceiver receiver : mRtpReceivers) {
                    size += receiver.getReceivedSize();
                }
            }
            return size;
        }

        private long getBPS() {
            long bps = 0;
            synchronized (mRtpReceivers) {
                for (RtpReceiver receiver : mRtpReceivers) {
                    bps += receiver.getBPS();
                }
            }
            return bps;
        }

        /**
         * RTP を受信するためのレシーバを追加します.
         *
         * @param md メディア情報
         */
        private void addRtpReceiver(MediaDescription md, int rtpPort, int rtcpPort) {
            RtpReceiver receiver = new RtpReceiver(rtpPort, rtcpPort);
            receiver.setCallback(new RtpReceiver.Callback() {
                @Override
                public void onError(Exception e) {
                    postOnError(new RtspClientException(e, RtspResponse.Status.STATUS_UNKNOWN));
                }

                @Override
                public void onRtpReceived(byte[] data, int dataLength) {
                    postOnRtpReceived(md, data, dataLength);
                }

                @Override
                public void onRtcpReceived(byte[] data, int dataLength) {
                    postOnRtcpReceived(md, data, dataLength);
                }
            });
            receiver.open();

            synchronized (mRtpReceivers) {
                mRtpReceivers.add(receiver);
            }
        }

        /**
         * RTSP サーバからのレスポンスを解析します.
         *
         * @param input RTSP サーバからのレスポンスが格納されたリーダー.
         * @return RTSP レスポンス
         * @throws IOException レスポンスの取得に失敗した場合に発生
         */
        private RtspResponse parseRtspResponse(BufferedReader input) throws IOException {
            RtspResponse response = RtspResponseParser.parse(input);

            if (response.getStatus() != RtspResponse.Status.STATUS_OK) {
                throw new RtspClientException("", response.getStatus());
            }

            String session = response.getAttribute("Session");
            if (session != null) {
                mSession = session;
            }

            String contentBase = response.getAttribute("Content-Base");
            if (contentBase != null) {
                mContentBase = contentBase;
            }

            return response;
        }

        /**
         * OPTION リクエストを RTSP サーバに送信します.
         *
         * @throws IOException リクエスト送信・レスポンス受信に失敗した場合に発生
         */
        private void processOptions() throws IOException {
            RtspRequest request = new RtspRequest();
            request.setMethod(RtspRequest.Method.OPTIONS);
            request.setUri(mRtspServerUrl);
            request.setCSeq(mCseq++);
            request.addHeader("User-Agent", mUserAgent);
            if (mSession != null) {
                request.addHeader("Session", mSession);
            }

            request.send(mWriter);

            parseRtspResponse(mReader);
        }

        /**
         * DESCRIBE リクエストを RTSP サーバに送信します.
         *
         * @throws IOException リクエスト送信・レスポンス受信に失敗した場合に発生
         */
        private void processDescribe() throws IOException {
            RtspRequest request = new RtspRequest();
            request.setMethod(RtspRequest.Method.DESCRIBE);
            request.setUri(mRtspServerUrl);
            request.setCSeq(mCseq++);
            request.addHeader("User-Agent", mUserAgent);
            request.addHeader("Accept", "application/sdp");
            if (mSession != null) {
                request.addHeader("Session", mSession);
            }

            request.send(mWriter);

            RtspResponse response = parseRtspResponse(mReader);

            String content = response.getContent();
            if (content != null) {
                mSessionDescription = SessionDescriptionParser.parse(content);
                postOnSdpReceived(mSessionDescription);
            }
        }

        /**
         * Transport ヘッダーを作成します.
         *
         * @param md メディア情報
         * @param rtpPort RTPポート番号
         * @param rtspPort RTSPポート番号
         * @return Transport ヘッダー
         */
        private String createTransport(MediaDescription md, int rtpPort, int rtspPort) {
            return md.getProto() + ";unicast;client_port=" + rtpPort +"-" + rtspPort;
        }

        /**
         * MediaStream の制御を行う URL を取得します.
         *
         * @param md MediaDescription
         * @return MediaStream の制御を行う URL
         */
        private String getMediaStreamUrl(MediaDescription md) {
            String streamUrl = null;
            for (Attribute attribute : md.getAttributes()) {
                if (attribute.getField().equalsIgnoreCase("control")) {
                    streamUrl = attribute.getValue();
                }
            }

            if (streamUrl == null) {
                streamUrl = mRtspServerUrl;
            }

            if (!streamUrl.startsWith("rtsp://")) {
                streamUrl = mContentBase + "/" + streamUrl;
            }

            return streamUrl;
        }

        /**
         * SetPortListからPort番号を取得する
         *
         * SetPortList内のポート番号を使い切っている場合は
         * UsePortListの最大値+1を返す
         * @return ポート番号。SetPortListが空の場合は-1を返す
         */
        private int getPortNumber() {
            if (mRtpPortList.isEmpty()) {
                return -1;
            }
            for (int port : mRtpPortList) {
                if (!mUsePortList.contains(port)) {
                    mUsePortList.add(port);
                    return port;
                }
            }
            int port = Collections.max(mUsePortList) + 1;
            mUsePortList.add(port);
            return port;
        }

        /**
         * SETUP リクエストを RTSP サーバに送信します.
         *
         * @throws IOException リクエスト送信・レスポンス受信に失敗した場合に発生
         */
        private void processSetup(MediaDescription md) throws IOException {
            int rtpPort, rtspPort;
            // 指定UDPポート設定判定
            if (!mRtpPortList.isEmpty()) {
                rtpPort = getPortNumber();
                rtspPort = getPortNumber();
            } else {
                rtpPort = md.getPort();
                rtspPort = rtpPort + 1;
                if (rtpPort <= 1024 || rtspPort <= 1024) {
                    rtpPort = 1025;
                    rtspPort = 1026;
                }
                // 使用ポートリストに該当する番号が存在する場合、重複しないポート番号を設定する。
                if (mUsePortList.contains(rtpPort) || mUsePortList.contains(rtspPort)) {
                    rtpPort = getPortNumber();
                    rtspPort = getPortNumber();
                } else {
                    mUsePortList.add(rtpPort);
                    mUsePortList.add(rtspPort);
                }
            }

            // RTP の受信を開始
            addRtpReceiver(md, rtpPort, rtspPort);

            RtspRequest request = new RtspRequest();
            request.setMethod(RtspRequest.Method.SETUP);
            request.setUri(getMediaStreamUrl(md));
            request.setCSeq(mCseq++);
            request.addHeader("User-Agent", mUserAgent);
            request.addHeader("Transport", createTransport(md, rtpPort, rtspPort));
            if (mSession != null) {
                request.addHeader("Session", mSession);
            }

            request.send(mWriter);

            RtspResponse response = parseRtspResponse(mReader);

            mClientPorts = RtspResponseParser.parseClientPort(response);
            mServerPorts = RtspResponseParser.parseServerPort(response);

            if (DEBUG) {
                Log.d(TAG, "ClientPorts: " + Arrays.toString(mClientPorts));
                Log.d(TAG, "ServerPorts: " + Arrays.toString(mServerPorts));
            }
        }

        /**
         * PLAY リクエストを RTSP サーバに送信します.
         *
         * @throws IOException リクエスト送信・レスポンス受信に失敗した場合に発生
         */
        private void processPlay() throws IOException {
            RtspRequest request = new RtspRequest();
            request.setMethod(RtspRequest.Method.PLAY);
            request.setUri(mRtspServerUrl);
            request.setCSeq(mCseq++);
            request.addHeader("User-Agent", mUserAgent);
            if (mSession != null) {
                request.addHeader("Session", mSession);
            }

            request.send(mWriter);

            parseRtspResponse(mReader);
        }

        /**
         * TEARDOWN リクエストを RTSP サーバに送信します.
         *
         * @throws IOException リクエスト送信・レスポンス受信に失敗した場合に発生
         */
        private void processTearDown() throws IOException {
            RtspRequest request = new RtspRequest();
            request.setMethod(RtspRequest.Method.TEARDOWN);
            request.setUri(mRtspServerUrl);
            request.setCSeq(mCseq++);
            request.addHeader("User-Agent", mUserAgent);
            if (mSession != null) {
                request.addHeader("Session", mSession);
            }

            request.send(mWriter);

            parseRtspResponse(mReader);
        }
    }

    public interface OnEventListener {
        /**
         * 接続されたことを通知します.
         */
        void onConnected();

        /**
         * 切断されたことを通知します.
         */
        void onDisconnected();

        /**
         * RTSP サーバとの通信で発生したエラーを通知します.
         *
         * @param e エラー原因の例外
         */
        void onError(RtspClientException e);

        /**
         * RTSP サーバから送られてきた SDP を通知します.
         *
         * @param sdp SDP
         */
        void onSdpReceived(SessionDescription sdp);

        /**
         * 受信した RTP を通知します.
         *
         * @param md メディア情報
         * @param data RTP パケットデータ
         * @param dataLength RTP パケットデータサイズ
         */
        void onRtpReceived(MediaDescription md, byte[] data, int dataLength);

        /**
         * 受信した RTCP を通知します.
         *
         * @param md メディア情報
         * @param data RTCP パケットデータ
         * @param dataLength RTCP パケットデータサイズ
         */
        void onRtcpReceived(MediaDescription md, byte[] data, int dataLength);
    }
}
