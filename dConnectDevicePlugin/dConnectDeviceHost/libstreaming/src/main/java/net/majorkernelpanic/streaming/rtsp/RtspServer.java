package net.majorkernelpanic.streaming.rtsp;


public interface RtspServer {

    enum Error {
        BIND_FAILED,
        START_FAILED,
        STOP_FAILED
    }

    /** Port used by default. */
    int DEFAULT_PORT = 8086;

    /** Streaming started. */
    int MESSAGE_STREAMING_STARTED = 0X00;

    /** Streaming stopped. */
    int MESSAGE_STREAMING_STOPPED = 0X01;

    /** Be careful: those callbacks won't necessarily be called from the ui thread ! */
    interface CallbackListener {

        /** Called when an error occurs. */
        void onError(RtspServer server, Exception cause, Error error);

        /** Called when streaming starts/stops. */
        void onMessage(RtspServer server, int message);
    }

    void addCallbackListener(CallbackListener listener);

    void removeCallbackListener(CallbackListener listener);

    int getPort();

    void setPort(int port);

    String getName();

    void start();

    void stop();
}
