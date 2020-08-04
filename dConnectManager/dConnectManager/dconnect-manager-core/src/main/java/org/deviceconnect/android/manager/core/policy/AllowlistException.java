/*
 AllowlistException.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.core.policy;

/**
 * Exception of allowlist management.
 * 
 * @author NTT DOCOMO, INC.
 */
public class AllowlistException extends Exception {

    /**
     * Default serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     * @param message the detail message for this exception
     */
    AllowlistException(final String message) {
        super(message);
    }

    /**
     * Constructor.
     * @param message the detail message for this exception
     * @param throwable the cause of this exception
     */
    AllowlistException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
