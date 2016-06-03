package org.deviceconnect.android.profile;

public interface TemperatureProfileConstants {
    String PROFILE_NAME = "temperature";

    String PARAM_TEMPERATURE = "temperature";
    String PARAM_TYPE = "type";
    String PARAM_TIME_STAMP = "timeStamp";
    String PARAM_TIME_STAMP_STRING = "timeStampString";

    enum TemperatureType {
        TYPE_CELSIUS(1),
        TYPE_FAHRENHEIT(2);

        private int mValue;

        private TemperatureType(int value) {
            mValue = value;
        }

        public int getValue() {
            return mValue;
        }
    }
}
