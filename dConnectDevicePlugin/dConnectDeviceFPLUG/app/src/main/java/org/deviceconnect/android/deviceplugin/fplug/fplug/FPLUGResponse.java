/*
 FPLUGResponse.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.fplug.fplug;

import java.util.List;

/**
 * This class is information of responses for F-PLUG.
 *
 * @author NTT DOCOMO, INC.
 */
public class FPLUGResponse {

    private String address;
    private List<WattHour> wattHourList;
    private double temperature;
    private int humidity;
    private int illuminance;
    private double realtimeWatt;
    private List<PastValues> pastValuesList;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<WattHour> getWattHourList() {
        return wattHourList;
    }

    public void setWattHourList(List<WattHour> wattHourList) {
        this.wattHourList = wattHourList;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public int getIlluminance() {
        return illuminance;
    }

    public void setIlluminance(int illuminance) {
        this.illuminance = illuminance;
    }

    public double getRealtimeWatt() {
        return realtimeWatt;
    }

    public void setRealtimeWatt(double realtimeWatt) {
        this.realtimeWatt = realtimeWatt;
    }

    public List<PastValues> getPastValuesList() {
        return pastValuesList;
    }

    public void setPastValuesList(List<PastValues> pastValuesList) {
        this.pastValuesList = pastValuesList;
    }
}
