/*
 FaceRecognitionDataModel
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvcc2w.manager.data;

/**
 *  HVC Face Recognition Data Model.
 * @author NTT DOCOMO, INC.
 */
public class FaceRecognitionDataModel implements FaceRecognitionObject {
    /** Name. */
    private String mName;
    /** User ID. */
    private int mUserId;
    /** Data ID. */
    private int mDataId;
    /** Device ID. */
    private String mDeviceId;
    /**
     * Constructor.
     * @param name Name
     * @param deviceId Device ID
     * @param userId User ID
     * @param dataId Data ID
     */
    public FaceRecognitionDataModel(final String name, final String deviceId, final int userId, final int dataId) {
        mName = name;
        mDeviceId = deviceId;
        mUserId = userId;
        mDataId = dataId;
    }
    @Override
    public String getDeviceId() {
        return mDeviceId;
    }

    @Override
    public void setDeviceId(final String deviceId) {
        mDeviceId = deviceId;
    }


    @Override
    public String getName() {
        return mName;
    }

    @Override
    public void setName(final String name) {
        mName = name;
    }

    @Override
    public int getUserId() {
        return mUserId;
    }

    @Override
    public void setUserId(final int userId) {
        mUserId = userId;
    }

    @Override
    public int getDataId() {
        return mDataId;
    }

    @Override
    public void setDataId(final int dataId) {
        mDataId = dataId;
    }
}
