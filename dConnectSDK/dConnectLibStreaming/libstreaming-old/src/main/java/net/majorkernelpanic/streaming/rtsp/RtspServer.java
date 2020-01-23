package net.majorkernelpanic.streaming.rtsp;


import net.majorkernelpanic.streaming.Session;

import java.net.Socket;

public interface RtspServer {

    enum Error {
        BIND_FAILED,
        START_FAILED,
        STOP_FAILED
    }

    enum Message {
        SERVER_STARTED,
        SERVER_STOPPED,
        STREAMING_STARTED,
        STREAMING_STOPPED
    }

    /** Port used by default. */
    int DEFAULT_PORT = 8086;

    /** Be careful: those callbacks won't necessarily be called from the ui thread ! */
    interface CallbackListener {

        /** Called when an error occurs. */
        void onError(RtspServer server, Exception cause, Error error);

        /** Called when streaming starts/stops. */
        void onMessage(RtspServer server, Message message);
    }

    interface Delegate {
        /**
         * セッション作成時に呼び出されるメソッド.
         * @param uri 接続先のURI
         * @param client 接続元のクライアント
         * @return 作成したセッション
         */
        Session generateSession(String uri, Socket client);

        /**
         * セッション削除時に呼び出されるメソッド.
         * @param session 削除されるセッション
         */
        void eraseSession(Session session);
    }

    void addCallbackListener(CallbackListener listener);

    void removeCallbackListener(CallbackListener listener);

    void setDelegate(Delegate delegate);

    int getPort();

    void setPort(int port);

    String getName();

    boolean start();

    void stop();
}
