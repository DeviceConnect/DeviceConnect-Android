/*
 HvcDetectListener.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.comm;

import omron.HVC.HVC_PRM;
import omron.HVC.HVC_RES;

/**
 * HVC Detect Listener.
 * 
 * @author NTT DOCOMO, INC.
 */
public interface HvcDetectListener {
    
    /**
     * Device Connected.
     */
    void onConnected();
    
    /**
     * Send Parameter finished.
     * @param hvcPrm send HVC parameter.
     */
    void onPostSetParam(final HVC_PRM hvcPrm);
    /**
     * Detect finished.
     * @param result result
     */
    void onDetectFinished(final HVC_RES result);
    /**
     * Disconnected.
     */
    void onDisconnected();
    
    /**
     * Error.
     * @param status error status.
     */
    void onConnectError(final int status);
    /**
     * Set parameter error.
     * @param status error status.
     */
    void onSetParamError(final int status);
    /**
     * Request error.
     * @param status error status.
     */
    void onRequestDetectError(int status);
    /**
     * Detect error.
     * @param status error status.
     */
    void onDetectError(final int status);
}
