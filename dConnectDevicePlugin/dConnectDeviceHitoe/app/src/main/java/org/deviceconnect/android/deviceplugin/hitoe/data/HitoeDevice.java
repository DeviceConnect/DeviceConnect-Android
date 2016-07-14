/*
 HitoeDevice
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.data;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is information of a hitoe device.
 * @author NTT DOCOMO, INC.
 */
public class HitoeDevice {
    /**  device type. */
    private String mType;
    /** device name. */
    private String mName;
    /** device id. */
    private String mId = "-1";
    /** device connect mode. */
    private String mConnectMode;
    /** device pin code. */
    private String mPin;
    /** device register flag. */
    private boolean mRegisterFlag;
    /** memory setting. */
    private String mMemorySetting;
    /** available raw datalist. */
    private List<String> mAvailableRawDataList = new ArrayList<>();
    /** available ba datalist. */
    private List<String> mAvailableBaDataList = new ArrayList<>();
    /** available ex datalist. */
    private List<String> mAvailableExDataList = new ArrayList<>();

    /** session id. */
    private String mSessionId;
    /** Raw connection id. */
    private String mRawConnectionId;
    /** Ba connection id. */
    private String mBaConnectionId;
    /** Ex connection id. */
    private String mExConnectionId;
    /** ex connection list. */
    private List<String> mExConnectionList = new ArrayList<>();

    /** Response id. */
    private int mResponseId;

    /**
     * Constructor.
     * @param raw raw data
     */
    public HitoeDevice(final String raw) {
        setData(raw);
        String[] dataList = HitoeConstants.AVAILABLE_EX_DATA_STR.split("\n");
        for (int i = 0; i < dataList.length; i++) {
            mAvailableExDataList.add(dataList[i]);
        }
    }
    /**
     * Get Device type.
     * @return Device type
     */
    public String getType() {
        return mType;
    }

    /**
     * Set Device Type.
     * @param type Device Type
     */
    public void setType(final String type) {
        mType = type;
    }

    /**
     * Get device id.
     * @return device id
     */
    public String getId() {
        return mId;
    }

    /**
     * Set device id.
     * @param id device id
     */
    public void setId(final String id) {
        mId = id;
    }

    /**
     * Get device Name.
     * @return device name
     */
    public String getName() {
        return mName;
    }

    /**
     * Set device name.
     * @param name device name
     */
    public void setName(final String name) {
        mName = name;
    }

    /**
     * Get pin code.
     * @return pin code
     */
    public String getPinCode() {
        return mPin;
    }

    /**
     * Set pin code.
     * @param pin pin code
     */
    public void setPinCode(final String pin) {
        mPin = pin;
    }

    /**
     * Get connect mode.
     * @return connect mode
     */
    public String getConnectMode() { return mConnectMode; }

    /**
     * Set connect mdoe.
     * @param connectMode connect mode
     */
    public void setConnectMode(final String connectMode) { mConnectMode = connectMode; }

    /**
     * Is register flag.
     * @return register flag
     */
    public boolean isRegisterFlag() {
        return mRegisterFlag;
    }

    /**
     * Set register flag.
     * @param registerFlag register flag
     */
    public void setRegisterFlag(final boolean registerFlag) {
        mRegisterFlag = registerFlag;
    }

    /**
     * Get Memory setting.
     * @return memory setting
     */
    public String getMemorySetting() {
        return mMemorySetting;
    }

    /**
     * Set memory setting.
     * @param memorySetting memory setting
     */
    public void setMemorySetting(final String memorySetting) {
        mMemorySetting = memorySetting;
    }

    /**
     * Get available raw data list.
     * @return available raw data list
     */
    public List<String> getAvailableRawDataList() {
        return mAvailableRawDataList;
    }

    /**
     * Set available raw data list.
     * @param availableRawDataList available raw data list
     */
    public void setAvailableRawDataList(final List<String> availableRawDataList) {
        mAvailableRawDataList = availableRawDataList;
    }

    /**
     * Get Available ba data list.
     * @return available ba data list
     */
    public List<String> getAvailableBaDataList() {
        return mAvailableBaDataList;
    }

    /**
     * Set available ba data list.
     * @param availableBaDataList available ba data list
     */
    public void setAvailableBaDataList(final List<String> availableBaDataList) {
        mAvailableBaDataList = availableBaDataList;
    }

    /**
     * Get available Ex data list.
     * @return available ex data list
     */
    public List<String> getAvailableExDataList() {
        return mAvailableExDataList;
    }

    /**
     * Set available ex data list.
     * @param availableExDataList available ex data list
     */
    public void setAvailableExDataList(final List<String> availableExDataList) {
        mAvailableExDataList = availableExDataList;
    }

    /**
     * Get session id.
     * @return session id
     */
    public String getSessionId() {
        return mSessionId;
    }

    /**
     * Set session id.
     * @param sessionId session id
     */
    public void setSessionId(final String sessionId) {
        mSessionId = sessionId;
    }

    /**
     * Get raw connection id.
     * @return Raw connection id
     */
    public String getRawConnectionId() {
        return mRawConnectionId;
    }

    /**
     * Set raw connection id.
     * @param rawConnectionId raw connection id
     */
    public void setRawConnectionId(final String rawConnectionId) {
        mRawConnectionId = rawConnectionId;
    }

    /**
     * Get ba connection id.
     * @return ba connection id
     */
    public String getBaConnectionId() {
        return mBaConnectionId;
    }

    /**
     * Set ba connection id.
     * @param baConnectionId ba connection id
     */
    public void setBaConnectionId(final String baConnectionId) {
        mBaConnectionId = baConnectionId;
    }

    /**
     * Get ex connection id.
     * @return ex connection id
     */
    public String getExConnectionId() {
        return mExConnectionId;
    }

    /**
     * Set ex connection id.
     * @param exConnectionId ex connection id
     */
    public void setExConnectionId(final String exConnectionId) {
        mExConnectionId = exConnectionId;
    }

    /**
     * Get Ex connection list.
     * @return ex connection list
     */
    public List<String> getExConnectionList() {
        return mExConnectionList;
    }

    /**
     * Set ex connection list.
     * @param exConnectionList ex connection list
     */
    public void setExConnectionList(final ArrayList<String> exConnectionList) {
        this.mExConnectionList = exConnectionList;
    }
    /**
     * Get Response id.
     * @return response id
     */
    public int getResponseId() {
        return mResponseId;
    }

    /**
     * Set Response id.
     * @param responseId response id
     */
    public void setResponseId(final int responseId) {
        mResponseId = responseId;
    }



    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"type\": ").append(mType).append(", ");
        builder.append("\"name\": ").append(mName).append(", ");
        builder.append("\"id\": ").append(mId).append(", ");
        builder.append("\"address\": ").append(mPin).append(", ");
        builder.append("\"connectMode\": ").append(mConnectMode).append(", ");
        builder.append("\"registerFlag\": ").append(mRegisterFlag).append("} ");
        return builder.toString();
    }

    /**
     * Set Data.
     * @param val raw data
     */
    public void setData(final String val) {

        if (val == null) {

            return;
        }

        String[] list = val.split(HitoeConstants.COMMA, -1);
        this.mType = list[0];
        this.mName = list[1];
        this.mId = list[2];
        this.mConnectMode = list[3];
        if (list[4].equals("memory_setting")) {
            this.mMemorySetting = list[4];
        }
    }


    /**
     *  Set available data.
     *  @param availableData available data
     */
    public void setAvailableData(final String availableData) {

        String[] dataList = availableData.split(HitoeConstants.BR);
        for (int i = 0; i < dataList.length; i++) {
            if (dataList[i].startsWith(HitoeConstants.RAW_DATA_PREFFIX)) {

                if (!mAvailableRawDataList.contains(dataList[i])) {

                    mAvailableRawDataList.add(dataList[i]);
                }
            } else if (dataList[i].startsWith(HitoeConstants.BA_DATA_PREFFIX)) {

                if (!mAvailableBaDataList.contains(dataList[i])) {

                    mAvailableBaDataList.add(dataList[i]);
                }
            } else if (dataList[i].startsWith(HitoeConstants.EX_DATA_PREFFIX)) {

                if (!mAvailableExDataList.contains(dataList[i])) {

                    mAvailableExDataList.add(dataList[i]);
                }
            }
        }
    }

    /**
     * Set Connection Id.
     * @param connectionId connection id
     */
    public void setConnectionId(final String connectionId) {

        if (connectionId.startsWith(HitoeConstants.RAW_CONNECTION_PREFFIX)) {

            mRawConnectionId = connectionId;
        } else if (connectionId.startsWith(HitoeConstants.BA_CONNECTION_PREFFIX)) {

            mBaConnectionId = connectionId;
        } else if (connectionId.startsWith(HitoeConstants.EX_CONNECTION_PREFFIX)) {
            getExConnectionList().add(connectionId);
        }
    }


    /**
     *  Remove connection id.
     *  @param connectionId connection id
     */
    public void removeConnectionId(final String connectionId) {

        if (mRawConnectionId != null && mRawConnectionId.equals(connectionId)) {

            mRawConnectionId = null;
        } else if (mBaConnectionId != null && mBaConnectionId.equals(connectionId)) {

            mBaConnectionId = null;
        } else if (mExConnectionList != null && mExConnectionList.contains(connectionId)) {

            mExConnectionList.remove(connectionId);
        }
    }
}
