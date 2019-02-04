/*
 RecorderException.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.camera;

class RecorderException extends Exception {

    static final int REASON_FATAL = 0;
    static final int REASON_NOT_ALLOWED = 1;
    static final int REASON_DISCONNECTED = 2;
    static final int REASON_DISABLED = 3;
    static final int REASON_IN_USE = 4;
    static final int REASON_TOO_MANY = 5;

    private final int mReason;

    RecorderException(final int reason) {
        this(reason, getDefaultMessage(reason), null);
    }

    RecorderException(final int reason, final Exception cause) {
        this(reason, cause.getMessage(), cause);
    }

    RecorderException(final int reason, final String message, final Exception cause) {
        super(message, cause);
        mReason = reason;
    }

    public int getReason() {
        return mReason;
    }

    private static String getDefaultMessage(final int reason) {
        switch (reason) {
            case REASON_FATAL:
                return "Fatal error";
            case REASON_NOT_ALLOWED:
                return "The specified camera is not allowed";
            case REASON_DISCONNECTED:
                return "The specified camera is disconnected";
            case REASON_DISABLED:
                return "The specified camera is disabled";
            case REASON_IN_USE:
                return "The specified camera is in use";
            case REASON_TOO_MANY:
            default:
                return "Too many cameras in use";
        }
    }
}