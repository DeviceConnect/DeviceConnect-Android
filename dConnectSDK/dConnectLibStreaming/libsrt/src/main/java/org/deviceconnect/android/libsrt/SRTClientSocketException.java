package org.deviceconnect.android.libsrt;

import java.io.IOException;

public class SRTClientSocketException extends IOException {

    private final int mError;

    SRTClientSocketException(final int error) {
        super();
        mError = error;
    }

    public int getError() {
        return mError;
    }
}
