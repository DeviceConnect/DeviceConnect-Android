package org.deviceconnect.android.libsrt;

import java.io.IOException;

public class SRTSocketException extends IOException {

    private final int mError;

    SRTSocketException(final int error) {
        super();
        mError = error;
    }

    public int getError() {
        return mError;
    }
}
