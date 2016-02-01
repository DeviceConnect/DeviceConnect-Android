package org.deviceconnect.android.deviceplugin.theta.core.osc;


import org.json.JSONException;
import org.json.JSONObject;

public class OscState {

    private static final String PARAM_BATTERY_LEVEL = "batteryLevel";

    private static final String PARAM_CAPTURE_STATUS = "_captureStatus";

    private double mBatteryLevel;

    private String mCaptureStatus;

    private OscState() {
    }

    public static OscState parse(final JSONObject state) throws JSONException {
        OscState result = new OscState();
        result.mBatteryLevel = state.getDouble(PARAM_BATTERY_LEVEL);
        result.mCaptureStatus = state.getString(PARAM_CAPTURE_STATUS);
        return result;
    }

    public double getBatteryLevel() {
        return mBatteryLevel;
    }

    public String getCaptureStatus() {
        return mCaptureStatus;
    }
}
