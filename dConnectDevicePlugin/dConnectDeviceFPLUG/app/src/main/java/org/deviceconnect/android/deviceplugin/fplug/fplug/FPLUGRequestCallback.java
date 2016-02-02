/*
 FPLUGRequestCallback.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.fplug.fplug;

/**
 * This class is callback for receives result of request to F-PLUG.
 *
 * @author NTT DOCOMO, INC.
 */
public interface FPLUGRequestCallback {
    void onSuccess(FPLUGResponse response);

    void onError(String message);

    void onTimeout();
}
