package org.deviceconnect.android.deviceplugin.theta.core;


public class ThetaDeviceException extends Exception {

    public static final int UNKNOWN = 0;

    public static final int NOT_FOUND_THETA = 1;

    public static final int BAD_REQUEST = 2;

    public static final int TIMEOUT = 3;

    public static final int NOT_FOUND_OBJECT = 4;

    public static final int NOT_SUPPORTED_FEATURE = 5;

    private final int mReason;

    public int getReason() {
        return mReason;
    }

    ThetaDeviceException(final int reason) {
        super();
        mReason = reason;
    }

}
