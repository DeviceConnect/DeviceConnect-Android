package org.deviceconnect.android.manager.plugin;


public class MessagingException extends Exception {

    private final Reason mReason;

    MessagingException(final Throwable cause, final Reason reason) {
        super(cause);
        mReason = reason;
    }

    MessagingException(final Reason reason) {
        mReason = reason;
    }

    public Reason getReason() {
        return mReason;
    }

    public enum Reason {
        NOT_ENABLED,
        CONNECTION_SUSPENDED,
        NOT_CONNECTED
    }
}
