/*
 FPLUGRequest.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.fplug.fplug;

/**
 * This class is information of requests for F-PLUG.
 *
 * @author NTT DOCOMO, INC.
 */
public class FPLUGRequest {

    enum REQUEST_TYPE {
        INIT,
        CANCEL_PAIRING,
        WATT_HOUR,
        TEMPERATURE,
        HUMIDITY,
        ILLUMINANCE,
        REALTIME_WATT,
        PAST_WATT,
        PAST_VALUES,
        SET_DATE,
        LED_ON,
        LED_OFF
    }

    private REQUEST_TYPE type;
    private FPLUGRequestCallback callback;
    private Object value;

    public FPLUGRequest(REQUEST_TYPE type, FPLUGRequestCallback callback) {
        this.type = type;
        this.callback = callback;
    }

    public REQUEST_TYPE getType() {
        return type;
    }

    public void setType(REQUEST_TYPE type) {
        this.type = type;
    }

    public FPLUGRequestCallback getCallback() {
        return callback;
    }

    public void setCallback(FPLUGRequestCallback callback) {
        this.callback = callback;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
