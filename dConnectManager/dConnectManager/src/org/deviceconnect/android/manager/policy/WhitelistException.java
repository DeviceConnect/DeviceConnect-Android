/*
 WhitelistException.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.policy;

/**
 * Exception of whitelist management.
 * 
 * @author NTT DOCOMO, INC.
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
