/*
 DeviceData
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.data;

/**
 * This class is information of  HeartRate's device.
 * @author NTT DOCOMO, INC.
 */
public class TargetDeviceData {
    /** Target device's product name. */
    private String mProductName;
    /** Target device's manufacture name. */
    private String mManufactureName;
    /** Target device's model number. */
    private String mModelNumber;
    /** Target device's firmware revision. */
    private String mFirmwareRevision;
    /** Target device's serial number. */
    private String mSerialNumber;
    /** Target device's software revision. */
    private String mSoftwareRevision;
    /** Target device's hardware revision. */
    private String mHardwareRevision;
    /** Target device's part number. */
    private String mPartNumber;
    /** Target device's protocolRevision. */
    private String mProtocolRevision;
    /** Target device's system id. */
    private String mSystemId;
    /** Target device's battery level.*/
    private String mBatteryLevel;

    /**
     * Get Target device's product name.
     * @return Target device's product name
     */
    public String getProductName() {
        return mProductName;
    }

    /**
     * Set Target device's product name.
     * @param productName Target device's product name
     */
    public void setProductName(final String productName) {
        mProductName = productName;
    }

    /**
     * Get Target device's manufacture name.
     * @return Target device's manufacture name
     */
    public String getManufactureName() {
        return mManufactureName;
    }

    /**
     * Set Target device's manufacture name.
     * @param manufactureName Target device's manufacture name
     */
    public void setManufactureName(final String manufactureName) {
        mManufactureName = manufactureName;
    }

    /**
     * Get Target device's model number.
     * @return Target device's model number
     */
    public String getModelNumber() {
        return mModelNumber;
    }

    /**
     * Set Target device's model number
     * @param modelNumber Target device's model number
     */
    public void setModelNumber(final String modelNumber) {
        mModelNumber = modelNumber;
    }

    /**
     * Get Target device's firmware revision.
     * @return Target device's firmware revision
     */
    public String getFirmwareRevision() {
        return mFirmwareRevision;
    }

    /**
     * Set Target device's firmware revision.
     * @param firmwareRevision Target device's firmware revision
     */
    public void setFirmwareRevision(final String firmwareRevision) {
        mFirmwareRevision = firmwareRevision;
    }

    /**
     * Get Target device's serial number.
     * @return Target device's serial number
     */
    public String getSerialNumber() {
        return mSerialNumber;
    }

    /**
     * Set Target device's serial number.
     * @param serialNumber Target device's serial number
     */
    public void setSerialNumber(final String serialNumber) {
        mSerialNumber = serialNumber;
    }

    /**
     * Get Target device's software revision.
     * @return Target device's software revision
     */
    public String getSoftwareRevision() {
        return mSoftwareRevision;
    }

    /**
     * Set Target device's software revision.
     * @param softwareRevision Target device's software revision
     */
    public void setSoftwareRevision(final String softwareRevision) {
        mSoftwareRevision = softwareRevision;
    }

    /**
     * Get Target device's hardware revision.
     * @return Target device's hardware revision
     */
    public String getHardwareRevision() {
        return mHardwareRevision;
    }

    /**
     * Set Target device's hardware revision.
     * @param hardwareRevision Target device's hardware revision
     */
    public void setHardwareRevision(final String hardwareRevision) {
        mHardwareRevision = hardwareRevision;
    }

    /**
     * Get Target device's part number.
     * @return Target device's part number
     */
    public String getPartNumber() {
        return mPartNumber;
    }

    /**
     * Set Target device's part number.
     * @param partNumber Target device's part number
     */
    public void setPartNumber(final String partNumber) {
        mPartNumber = partNumber;
    }

    /**
     * Get Target device's protocol revision.
     * @return Target device's protocol revision
     */
    public String getProtocolRevision() {
        return mProtocolRevision;
    }

    /**
     * Set Target device's protocol revision.
     * @param protocolRevision Target device's protocol revision
     */
    public void setProtocolRevision(final String protocolRevision) {
        mProtocolRevision = protocolRevision;
    }

    /**
     * Get Target device's system id.
     * @return Target device's system id
     */
    public String getSystemId() {
        return mSystemId;
    }

    /**
     * Set Target device's system id.
     * @param systemId Target device's system id
     */
    public void setSystemId(final String systemId) {
        mSystemId = systemId;
    }

    /**
     * Get Target device's battery level.
     * @return Target device's battery level
     */
    public String getBatteryLevel() {
        return mBatteryLevel;
    }

    /**
     * Set Target device's battery level.
     * @param batteryLevel Target device's battery level
     */
    public void setBatteryLevel(final String batteryLevel) {
        mBatteryLevel = batteryLevel;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"productName\": " + mProductName + ", ");
        builder.append("\"manufactureName\": " + mManufactureName + ", ");
        builder.append("\"modelNumber\": " + mModelNumber + ", ");
        builder.append("\"firmwareRevision\": " + mFirmwareRevision + ", ");
        builder.append("\"serialNumber\": " + mSerialNumber + ", ");
        builder.append("\"softwareRevision\": " + mSoftwareRevision + ", ");
        builder.append("\"hardwareRevision\": " + mHardwareRevision + ", ");
        builder.append("\"partNumber\": " + mPartNumber + ", ");
        builder.append("\"protocolRevision\": " + mProtocolRevision + ", ");
        builder.append("\"systemId\": " + mSystemId + ", ");
        builder.append("\"batteryLevel\": " + mBatteryLevel +  "} ");
        return builder.toString();
    }
}
