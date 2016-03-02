/*
 LinkingDevice.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking;

public class LinkingDevice {

    private String displayName;
    private String name;
    private String bdAddress;
    private Integer modelId;
    private Integer uniqueId;
    private byte[] illumination;
    private byte[] vibration;
    private Object sensor;
    private boolean isConnected;
    private int feature;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBdAddress() {
        return bdAddress;
    }

    public void setBdAddress(String bdAddress) {
        this.bdAddress = bdAddress;
    }

    public Integer getModelId() {
        return modelId;
    }

    public void setModelId(Integer modelId) {
        this.modelId = modelId;
    }

    public Integer getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(Integer uniqueId) {
        this.uniqueId = uniqueId;
    }

    public byte[] getIllumination() {
        return illumination;
    }

    public void setIllumination(byte[] illumination) {
        this.illumination = illumination;
    }

    public byte[] getVibration() {
        return vibration;
    }

    public void setVibration(byte[] vibration) {
        this.vibration = vibration;
    }

    public Object getSensor() {
        return sensor;
    }

    public void setSensor(Object sensor) {
        this.sensor = sensor;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setIsConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    public int getFeature() {
        return feature;
    }

    public void setFeature(int feature) {
        this.feature = feature;
    }

}
