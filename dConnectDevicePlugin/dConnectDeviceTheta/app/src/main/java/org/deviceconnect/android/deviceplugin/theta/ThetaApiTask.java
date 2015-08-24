/*
 ThetaApiTask
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta;

/**
 * Theta API Task.
 *
 * @author NTT DOCOMO, INC.
 */
public interface ThetaApiTask {

    /**
     * Start to execute an task.
     * <p>
     * This method must be invoked by {@link ThetaApiClient} internally.
     * </p>
     *
     * @param api THETA API
     */
    public abstract void run(final ThetaApi api);

}
