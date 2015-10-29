package org.deviceconnect.android.deviceplugin.theta.core;


public class ThetaDeviceException extends Exception {

    private static final int UNKNOWN = 0;

    private static final int NOT_FOUND_THETA = 1;

    private static final int BAD_REQUEST = 2;

    private static final int TIMEOUT = 3;

    private static final int NOT_FOUND_OBJECT = 4;

    private static final int NOT_SUPPORTED_FEATURE = 5;

    private final int mReason;

    public int getReason() {
        return mReason;
    }

    ThetaDeviceException(final int reason) {
        super();
        mReason = reason;
    }

}
