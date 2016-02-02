/*
 WattHour.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.fplug.fplug;

/**
 * This class is information of watt values that stored in F-PLUG.
 *
 * @author NTT DOCOMO, INC.
 */
public class WattHour {

    private int hoursAgo;
    private boolean reliable;
    private int watt;

    public int getHoursAgo() {
        return hoursAgo;
    }

    public void setHoursAgo(int hoursAgo) {
        this.hoursAgo = hoursAgo;
    }

    public boolean isReliable() {
        return reliable;
    }

    public void setReliable(boolean reliable) {
        this.reliable = reliable;
    }

    public int getWatt() {
        return watt;
    }

    public void setWatt(int watt) {
        this.watt = watt;
    }
}
