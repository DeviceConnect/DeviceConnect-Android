package org.deviceconnect.android.libmedia.streaming.rtsp;

public class RtspRequestParserException extends Exception {

    private RtspResponse.Status mStatus;

    public RtspRequestParserException(RtspResponse.Status status) {
        mStatus = status;
    }

    public RtspResponse.Status getStatus() {
        return mStatus;
    }
}
