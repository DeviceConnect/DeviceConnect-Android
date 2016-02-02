package org.deviceconnect.android.deviceplugin.theta.core;


public class ThetaDeviceException extends Exception {

    public static final int UNKNOWN = 0;

    public static final int NOT_FOUND_THETA = 1;

    public static final int BAD_REQUEST = 2;

    public static final int TIMEOUT = 3;

    public static final int NOT_FOUND_OBJECT = 4;

    public static final int NOT_SUPPORTED_FEATURE = 5;

    public static final int INVALID_RESPONSE = 6;

    public static final int IO_ERROR = 7;

    public static final int FORBIDDEN = 8;

    public static final int UNAVAILABLE = 9;

    public static final int OUT_OF_MEMORY = 10;

    public static final int NOT_FOUND_RECORDER = 11;

    private final int mReason;

    public int getReason() {
        return mReason;
    }

    ThetaDeviceException(final int reason) {
        super();
        mReason = reason;
    }

    ThetaDeviceException(final int reason, final Throwable e) {
        super(e);
        mReason = reason;
    }

    ThetaDeviceException(final int reason, final String message) {
        super(message);
        mReason = reason;
    }

}
