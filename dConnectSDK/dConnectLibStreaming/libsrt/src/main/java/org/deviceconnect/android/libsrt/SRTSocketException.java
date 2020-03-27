package org.deviceconnect.android.libsrt;

import java.io.IOException;

public class SRTSocketException extends IOException {

    private int mError;

    public SRTSocketException(int error) {
        mError = error;
    }

    public SRTSocketException(String message, int error) {
        super(message);
        mError = error;
    }

    public SRTSocketException(String message, Throwable cause, int error) {
        super(message, cause);
        mError = error;
    }

    public SRTSocketException(Throwable cause, int error) {
        super(cause);
        mError = error;
    }

    public int getError() {
        return mError;
    }
}
