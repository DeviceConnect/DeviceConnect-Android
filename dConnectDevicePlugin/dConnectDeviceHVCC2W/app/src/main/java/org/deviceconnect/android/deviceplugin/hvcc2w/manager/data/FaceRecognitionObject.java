/*
 FaceRecognitionObject
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvcc2w.manager.data;

/**
 * HVC Face Recognition Object.
 * @author NTT DOCOMO, INC.
 */

public interface FaceRecognitionObject {

    /**
     * Get Name.
     */
    String getName();
    /**
     * Set Name.
     * @param name name
     */
    void setName(final String name);
    /**
     * Set User ID.
     */
    void setDeviceId(final String deviceId);
    /**
     * Get Device ID.
     */
    String getDeviceId();
    /**
     * Get User ID.
     */
    int getUserId();
    /**
     * Set User ID.
     * @param userId User ID
     */
    void setUserId(final int userId);
    /**
     * Get Data ID.
     */
    int getDataId();
    /**
     * Set Data ID.
     * @param dataId Data ID
     */
    void setDataId(final int dataId);
}
