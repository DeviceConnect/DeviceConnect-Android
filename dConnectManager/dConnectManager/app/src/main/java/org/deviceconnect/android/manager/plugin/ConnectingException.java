package org.deviceconnect.android.manager.plugin;


class ConnectingException extends Exception {

    ConnectingException(final Throwable cause) {
        super(cause);
    }

    ConnectingException(final String message) {
        super(message);
    }

}
