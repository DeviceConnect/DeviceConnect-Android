/*
 PastValues.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.fplug.fplug;

/**
 * This class is information of values that stored in F-PLUG as environments attribute.
 * <p>
 * values is temperature, humidity and illuminance.
 * </p>
 *
 * @author NTT DOCOMO, INC.
 */
public class PastValues {

    private int hoursAgo;
    private double temperature;
    private int humidity;
    private int illuminance;

    public int getHoursAgo() {
        return hoursAgo;
    }

    public void setHoursAgo(int hoursAgo) {
        this.hoursAgo = hoursAgo;
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

}
