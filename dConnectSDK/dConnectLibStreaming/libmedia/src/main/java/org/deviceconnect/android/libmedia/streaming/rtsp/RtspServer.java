package org.deviceconnect.android.libmedia.streaming.rtsp;

import android.util.Log;

import org.deviceconnect.android.libmedia.BuildConfig;
import org.deviceconnect.android.libmedia.streaming.MediaEncoderException;
import org.deviceconnect.android.libmedia.streaming.rtp.RtpSocket;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.MediaStream;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.RtspSession;
import org.deviceconnect.android.libmedia.streaming.util.IpAddressManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RtspServer {
    /**
     * デバッグフラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * デバッグ用タグ.
     */
    private static final String TAG = "RTSP-SERVER";

    /**
     * RTSP のセッションを管理するクラス.
     */
    private RtspSession mRtspSession;

    /**
     * サーバ名.
     */
    private String mServerName;

    /**
     * サーバのポート番号.
     */
    private int mServerPort = 10000;

    /**
     * サーバのソケットを処理するスレッド.
     */
    private ServerSocketThread mServerThread;

    /**
     * サーバに接続されたクライアントの処理を行うスレッドを格納するリスト.
     */
    private final List<ClientSocketThread> mClientSocketThreads = new ArrayList<>();

    /**
     * コールバック.
     */
    private Callback mCallback;

    /**
     * エラーが発生フラグ.
     *
     * RtspSession 内部でエラーが発生した場合に、このフラグは true になります。
     */
    private boolean mErrorFlag;

    /**
     * RTSP のエンコードを行うセッションを取得します.
     *
     * <p>
     * エンコードが開始されていない場合には null を返却します。
     * </p>
     *
     * @return RtspSession
     */
    public RtspSession getRtspSession() {
        return mRtspSession;
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
     * コールバックを設定します.
     *
     * @param callback コールバック
     */
    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    /**
     * RTSP サーバを開始します.
     *
     * @throws IOException サーバの開始に失敗した場合に発生
     */
    public void start() throws IOException {
        if (mServerThread != null) {
            if (DEBUG) {
                Log.w(TAG, "RtspServer is already started.");
            }
            return;
        }

        synchronized (mClientSocketThreads) {
            mClientSocketThreads.clear();
        }

        mServerThread = new ServerSocketThread(mServerPort);
        mServerThread.start();
    }

    /**
     * RTSP サーバを停止します.
     */
    public void stop() {
        if (mServerThread != null) {
            mServerThread.terminate();
            mServerThread = null;
        }
    }

    /**
     * サーバソケットの処理を行うスレッド.
     */
    private class ServerSocketThread extends Thread {
        /**
         * サーバソケット.
         */
        private ServerSocket mServerSocket;

        /**
         * コンストラクタ.
         *
         * @param port ポート番号
         * @throws IOException ソケットを開くのに失敗した場合に発生
         */
        ServerSocketThread(int port) throws IOException {
            mServerSocket = new ServerSocket(port);
            mServerSocket.setReuseAddress(true);
            setName("RTSP-SERVER-SOCKET");
        }

        /**
         * サーバソケットの処理を停止します.
         */
        void terminate() {
            if (mServerSocket != null) {
                try {
                    mServerSocket.close();
                } catch (IOException e) {
                    // ignore.
                }
                mServerSocket = null;
            }

            interrupt();

            try {
                join(200);
            } catch (InterruptedException e) {
                // ignore.
            }

            synchronized (mClientSocketThreads) {
                for (ClientSocketThread thread : mClientSocketThreads) {
                    thread.terminate();
                }
                mClientSocketThreads.clear();
            }
        }

        @Override
        public void run() {
            if (DEBUG) {
                IpAddressManager ipAddressManager = new IpAddressManager();
                ipAddressManager.storeIPAddress();

                Log.d(TAG, "Rtsp Server started.");
                Log.d(TAG, "  IP: " + ipAddressManager.getWifiIPv4Address());
                Log.d(TAG, "  PORT: " + mServerPort);
            }

            mErrorFlag = false;
            try {
                while (!isInterrupted()) {
                    new ClientSocketThread(mServerSocket.accept()).start();
                }
            } catch (SocketException e) {
                // ignore.
            } catch (Exception e) {
                if (DEBUG) {
                    Log.e(TAG, "", e);
                }
            }

            if (DEBUG) {
                Log.d(TAG, "Rtsp Server stopped.");
            }
        }
    }

    /**
     * RTSP サーバに接続されたクライアント用のスレッドを追加します.
     *
     * @param thread 追加するクライアント用のスレッド
     */
    private void addClientSocketThread(ClientSocketThread thread) {
        synchronized (mClientSocketThreads) {
            if (mClientSocketThreads.isEmpty()) {
               createSession();
            }
            mClientSocketThreads.add(thread);
        }
    }

    /**
     * RTSP サーバから切断されたクライアント用のスレッドを削除します.
     *
     * @param thread 削除するクライアント用のスレッド
     */
    private void removeClientSocketThread(ClientSocketThread thread) {
        synchronized (mClientSocketThreads) {
            mClientSocketThreads.remove(thread);
            if (mClientSocketThreads.isEmpty()) {
                releaseSession();
            }
        }
    }

    /**
     * RTP を管理するためのセッションを作成します.
     */
    private void createSession() {
        if (mRtspSession != null) {
            if (DEBUG) {
                Log.e(TAG, "RtspSession is already created.");
            }
            releaseSession();
        }
        mRtspSession = new RtspSession();
        mRtspSession.setOnEventListener(mEventListener);
        mCallback.createSession(mRtspSession);
        mRtspSession.configure();
    }

    /**
     * RTP を管理するセッションを破棄します.
     */
    private void releaseSession() {
        if (mRtspSession != null) {
            mRtspSession.stop();
            mCallback.releaseSession(mRtspSession);
            mRtspSession = null;
        }
    }

    /**
     * クライアント Socket を全て閉じます.
     */
    private void closeAllClientSocket() {
        synchronized (mClientSocketThreads) {
            for (ClientSocketThread t : mClientSocketThreads) {
                t.terminate();
            }
        }
    }

    /**
     * RtspSession からのイベントを受信するリスナー.
     */
    private final RtspSession.OnEventListener mEventListener = new RtspSession.OnEventListener() {
        @Override
        public void onStarted() {
            if (DEBUG) {
                Log.d(TAG, "MediaStreamer started.");
            }
        }

        @Override
        public void onStopped() {
            if (DEBUG) {
                Log.d(TAG, "MediaStreamer stopped.");
            }
        }

        @Override
        public void onError(MediaEncoderException e) {
            if (DEBUG) {
                Log.e(TAG, "Error occurred on MediaStreamer.", e);
            }

            mErrorFlag = true;
            closeAllClientSocket();
        }
    };

    /**
     * クライアントとの接続を行うスレッド.
     */
    private class ClientSocketThread extends Thread {
        /**
         * クライアントと接続しているソケット.
         */
        private final Socket mClientSocket;

        /**
         * クライアントから送られてくるデータを読み込むためのストリーム.
         */
        private final BufferedReader mInput;

        /**
         * クライアントにデータを送信するためのストリーム.
         */
        private final OutputStream mOutput;

        /**
         * 接続された UDP のソケットのリスト.
         */
        private final List<RtpSocket> mRtpSockets = new ArrayList<>();

        /**
         * コンストラクタ.
         * @param socket クライアントと接続しているソケット
         * @throws IOException ソケットからストリームを取得するのに失敗した場合に発生
         */
        ClientSocketThread(Socket socket) throws IOException {
            mClientSocket = socket;
            mClientSocket.setReuseAddress(true);
            mClientSocket.setKeepAlive(true);
            mInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            mOutput = socket.getOutputStream();
            setName("RTSP-CLIENT-SOCKET");
        }

        /**
         * スレッドの停止処理を行います.
         */
        private void terminate() {
            try {
                mClientSocket.close();
            } catch (IOException e) {
                // ignore.
            }

            interrupt();

            try {
                join(200);
            } catch (InterruptedException e) {
                // ignore.
            }
        }

        @Override
        public void run() {
            if (DEBUG) {
                Log.d(TAG, "Rtsp Client Socket started.");
                Log.d(TAG, "  Client Socket: " + mClientSocket.getInetAddress());
            }

            try {
                // 事前にエラーがあった場合には、RtspSession を作成し直すために
                // 他の Socket が閉じて、RtspSession が削除されるのを待ちます。
                while (mErrorFlag) {
                    synchronized (mClientSocketThreads) {
                        if (mClientSocketThreads.isEmpty()) {
                            mErrorFlag = false;
                            break;
                        }
                    }
                    Thread.sleep(50);
                }

                addClientSocketThread(this);

                while (!isInterrupted()) {
                    RtspRequest request = null;
                    RtspResponse response = null;

                    try {
                        request = RtspRequestParser.parse(mInput);
                    } catch (RtspRequestParserException e) {
                        if (DEBUG) {
                            Log.w(TAG, "RTSP Request is error.", e);
                        }
                        response = createResponse(null, e.getStatus());
                    }

                    if (request != null) {
                        try {
                            response = processRequest(request);
                        } catch (Exception e) {
                            response = createResponse(request, RtspResponse.Status.STATUS_INTERNAL_SERVER_ERROR);
                        }
                    }

                    if (response != null) {
                        response.send(mOutput);
                    }
                }
            } catch (Exception e) {
                if (DEBUG) {
                    Log.w(TAG, "client: " + mClientSocket.getInetAddress(), e);
                }
            } finally {
                for (RtpSocket socket : mRtpSockets) {
                    for (MediaStream stream : mRtspSession.getStreams()) {
                        stream.removeRtpSocket(socket);
                    }
                }
                mRtpSockets.clear();

                try {
                    mClientSocket.close();
                } catch (IOException e) {
                    // ignore.
                }

                removeClientSocketThread(this);
            }

            if (DEBUG) {
                Log.d(TAG, "Rtsp Client Socket stopped.");
            }
        }

        /**
         * クライアントのアドレスを取得します.
         *
         * @return クライアントのアドレス
         */
        private String getClientAddress() {
            return mClientSocket.getLocalAddress().getHostAddress() + ":" + mClientSocket.getLocalPort();
        }

        /**
         * RTSP の URL を取得します.
         *
         * @return RTSP の URL
         */
        private String getRtspUrl() {
            return "rtsp://" + mClientSocket.getLocalAddress().getHostAddress() + ":" + mClientSocket.getLocalPort();
        }

        /**
         * 指定されたアドレスがマルチキャスト用のアドレスか確認します.
         *
         * @param address 確認するアドレス
         * @return マルチキャスト用のアドレスの場合はtrue、それ以外はfalse
         * @throws UnknownHostException ホスト名が解決できなかった場合に発生
         */
        private boolean isMulticastAddress(String address) throws UnknownHostException {
            return InetAddress.getByName(address).isMulticastAddress();
        }

        /**
         * セッションの SDP を作成します.
         *
         * @return セッションの SDP
         */
        private String createSessionDescription() {
            String remote = mClientSocket.getInetAddress().getHostAddress();
            String local = mClientSocket.getLocalAddress().getHostAddress();
            return mRtspSession.createSessionDescription(remote, local);
        }

        /**
         * DESCRIBE リクエストの処理を行います.
         *
         * @param request リクエスト
         * @param response レスポンス
         */
        private void processDescribe(RtspRequest request, RtspResponse response) {
            if (mRtspSession == null) {
                response.setStatus(RtspResponse.Status.STATUS_INTERNAL_SERVER_ERROR);
            } else {
                response.setContent(createSessionDescription());
                response.addAttribute("Content-Base", getClientAddress());
                response.addAttribute("Content-Type", "application/sdp");
                response.setStatus(RtspResponse.Status.STATUS_OK);
            }
        }

        /**
         * SETUP リクエストの処理を行います.
         *
         * @param request リクエスト
         * @param response レスポンス
         * @throws IOException セッションの管理に失敗した場合に発生
         */
        private void processSetup(RtspRequest request, RtspResponse response) throws IOException {
            String trackId = RtspRequestParser.parseTrackID(request);
            if (trackId == null) {
                response.setStatus(RtspResponse.Status.STATUS_BAD_REQUEST);
                return;
            }

            MediaStream stream = mRtspSession.getStream(trackId);
            if (stream == null) {
                response.setStatus(RtspResponse.Status.STATUS_BAD_REQUEST);
                return;
            }

            int[] clientPort = RtspRequestParser.parseClientPort(request);
            if (clientPort == null) {
                clientPort = new int[]{
                        stream.getDestinationPort(),
                        stream.getDestinationPort() + 1
                };
            }

            RtpSocket rtpSocket = new RtpSocket(mClientSocket.getInetAddress(), clientPort[0], clientPort[1]);

            mRtpSockets.add(rtpSocket);
            stream.addRtpSocket(rtpSocket);

            int[] serverPort = rtpSocket.getLocalPorts();
            int ssrc = rtpSocket.getSsrc();

            String destination = mClientSocket.getInetAddress().getHostAddress();

            Map<String, String> transports = new LinkedHashMap<>();
            transports.put("RTP/AVP/UDP", "");
            transports.put((isMulticastAddress(destination)) ? "multicast" : "unicast", "");
            transports.put("destination", destination);
            transports.put("client_port", clientPort[0] + "-" + clientPort[1]);
            transports.put("server_port", serverPort[0] + "-" + serverPort[1]);
            transports.put("ssrc", Integer.toHexString(ssrc));
            transports.put("mode", "play");

            StringBuilder builder = new StringBuilder();
            for (Map.Entry<String, String> entry : transports.entrySet()) {
                if (builder.length() > 0) {
                    builder.append(";");
                }
                builder.append(entry.getKey());
                String value = entry.getValue();
                if (value != null && !value.isEmpty()) {
                    builder.append("=").append(value);
                }
            }

            response.addAttribute("Transport", builder.toString());
            response.addAttribute("Session", stream.getSession());
            response.addAttribute("Cache-Control", "no-cache");
            response.setStatus(RtspResponse.Status.STATUS_OK);
        }

        /**
         * PLAY リクエストの処理を行います.
         *
         * @param request リクエスト
         * @param response レスポンス
         */
        private void processPlay(RtspRequest request, RtspResponse response) {
            StringBuilder rtpInfo = new StringBuilder();
            for (MediaStream mediaStream : mRtspSession.getStreams()) {
                if (rtpInfo.length() > 0) {
                    rtpInfo.append(",");
                }
                rtpInfo.append("url=").append(getRtspUrl()).append("/trackID=")
                        .append(mediaStream.getTrackId()).append(";seq=0");
            }

            String session = request.getHeader("session");
            if (session != null) {
                // TODO ストリームごとに開始の処理は行う必要がある
            }

            mRtspSession.start();

            response.addAttribute("RTP-Info", rtpInfo.toString());
            response.addAttribute("Session", session);
            response.setStatus(RtspResponse.Status.STATUS_OK);
        }

        /**
         * OPTIONS リクエストの処理を行います.
         *
         * @param request リクエスト
         * @param response レスポンス
         */
        private void processOptions(RtspRequest request, RtspResponse response) {
            response.addAttribute("Public", "DESCRIBE,SETUP,TEARDOWN,PLAY,PAUSE");
            response.setStatus(RtspResponse.Status.STATUS_OK);
        }

        /**
         * リクエストの処理を行います.
         *
         * @param request リクエスト
         * @return レスポンス
         * @throws IOException 処理に失敗した場合に発生
         */
        private RtspResponse processRequest(RtspRequest request) throws IOException {
            RtspResponse response = createResponse(request);

            switch (request.getMethod()) {
                case DESCRIBE:
                    processDescribe(request, response);
                    break;
                case OPTIONS:
                    processOptions(request, response);
                    break;
                case SETUP:
                    processSetup(request, response);
                    break;
                case PLAY:
                    processPlay(request, response);
                    break;
                case PAUSE:
                case TEARDOWN:
                    response.setStatus(RtspResponse.Status.STATUS_OK);
                    break;
                default:
                    response.setStatus(RtspResponse.Status.STATUS_BAD_REQUEST);
                    break;
            }

            return response;
        }

        /**
         * リクエストに対応したレスポンスを作成します.
         *
         * @param request リクエスト
         * @return レスポンス
         */
        private RtspResponse createResponse(RtspRequest request) {
            RtspResponse response = new RtspResponse();
            response.setServerName(mServerName);
            if (request != null) {
                Integer cseq = request.getCSeq();
                if (cseq != null) {
                    response.setSequenceId(cseq);
                }
            }
            return response;
        }

        /**
         * 指定されたステータスコードのレスポンスを作成します.
         *
         * @param request リクエスト
         * @param status ステータスコード
         * @return レスポンス
         */
        private RtspResponse createResponse(RtspRequest request, RtspResponse.Status status) {
            RtspResponse response = createResponse(request);
            response.setStatus(status);
            return response;
        }
    }

    /**
     * RTSP サーバに接続があった時に RtspSession を作成
     */
    public interface Callback {
        /**
         * RtspSession を作成時に呼び出します.
         * <p>
         * この RtspSession に MediaStream を設定します。
         * </p>
         * @param session セッション
         */
        void createSession(RtspSession session);

        /**
         * RtspSession が破棄された時に呼び出します.
         *
         * @param session セッション
         */
        void releaseSession(RtspSession session);
    }
}
