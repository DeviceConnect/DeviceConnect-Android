package org.deviceconnect.android.manager.policy;


/**
 * Exception of whitelist management.
 */
public class WhitelistException extends Exception {

    /**
     * Default serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     * @param message the detail message for this exception
     */
    WhitelistException(final String message) {
        super(message);
    }

    /**
     * Constructor.
     * @param message the detail message for this exception
     * @param throwable the cause of this exception
     */
    WhitelistException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
