/*
 TooManyPackagesException.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.plugin;

/**
 * Android OS側からアプリケーションの情報を取得できないことを示す例外.
 *
 * @author NTT DOCOMO, INC.
 */
public class PluginDetectionException extends Exception {

    private final Reason mReason;

    public PluginDetectionException(final Reason reason) {
        mReason = reason;
    }

    public PluginDetectionException(final Throwable throwable, final Reason reason) {
        super(throwable);
        mReason = reason;
    }

    public Reason getReason() {
        return mReason;
    }

    public enum Reason {
        TOO_MANY_PACKAGES,
        OTHER
    }
}
