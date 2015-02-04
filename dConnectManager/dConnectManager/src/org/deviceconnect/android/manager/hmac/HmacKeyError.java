package org.deviceconnect.android.manager.hmac;

/**
 * Errors on HMAC Key cache control.
 */
enum HmacKeyError {

    /**
     * No error occurred.
     */
    NONE,

    /**
     * An invalid parameter was specified.
     */
    INVALID_PARAMETER,

    /**
     * HMAC key was not found.
     */
    NOT_FOUND,

    /**
     * HMAC Key cache control was failed with some unknown error.
     */
    FAILED
}
