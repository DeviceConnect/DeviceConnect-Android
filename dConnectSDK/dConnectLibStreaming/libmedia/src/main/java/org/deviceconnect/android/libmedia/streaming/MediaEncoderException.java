package org.deviceconnect.android.libmedia.streaming;

public class MediaEncoderException extends RuntimeException {
    public MediaEncoderException(String message) {
        super(message);
    }

    public MediaEncoderException(String message, Throwable cause) {
        super(message, cause);
    }

    public MediaEncoderException(Throwable cause) {
        super(cause);
    }
}
