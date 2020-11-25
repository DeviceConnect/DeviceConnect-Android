package org.deviceconnect.android.libmedia.streaming.mjpeg;

/**
 * MJPEGEncoderの処理中に発生したエラーを扱う.
 */
public class MJPEGEncoderException extends RuntimeException {
    public MJPEGEncoderException(String message) {
        super(message);
    }

    public MJPEGEncoderException(String message, Throwable cause) {
        super(message, cause);
    }

    public MJPEGEncoderException(Throwable cause) {
        super(cause);
    }
}
