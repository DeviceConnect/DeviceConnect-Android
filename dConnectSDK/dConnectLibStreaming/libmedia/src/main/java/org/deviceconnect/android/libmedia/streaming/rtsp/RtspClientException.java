package org.deviceconnect.android.libmedia.streaming.rtsp;

public class RtspClientException extends RuntimeException {
    private RtspResponse.Status mStatus;

    public RtspClientException(String message, RtspResponse.Status status) {
        super(message);
        mStatus = status;
    }

    public RtspClientException(String message, Throwable cause, RtspResponse.Status status) {
        super(message, cause);
        mStatus = status;
    }

    public RtspClientException(Throwable cause, RtspResponse.Status status) {
        super(cause);
        mStatus = status;
    }

    public RtspResponse.Status getStatus() {
        return mStatus;
    }
}
