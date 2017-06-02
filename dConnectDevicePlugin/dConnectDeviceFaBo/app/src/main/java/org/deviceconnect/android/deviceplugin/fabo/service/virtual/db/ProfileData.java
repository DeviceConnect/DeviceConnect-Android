package org.deviceconnect.android.deviceplugin.fabo.service.virtual.db;


import java.util.ArrayList;
import java.util.List;

public class ProfileData {

    public enum Type {
        GPIO_LIGHT(1),
        GPIO_TEMPERATURE(2),
        GPIO_VIBRATION(3),
        I2C_ROBOT_DRIVE_CONTROLLER(100),
        I2C_MOUSE_DRIVE_CONTROLLER(101);

        private int mValue;

        Type(final int value) {
            mValue = value;
        }

        public int getValue() {
            return mValue;
        }

        public static Type getType(final int type) {
            for (Type t : values()) {
                if (t.getValue() == type) {
                    return t;
                }
            }
            return null;
        }
    }

    /**
     * プロファイルが属する仮装サービスのID.
     */
    private String mServiceId;

    /**
     * プロファイルタイプ.
     */
    private Type mType;

    /**
     * プロファイルが操作を行うピン一覧.
     */
    private List<Integer> mPinList = new ArrayList<>();

    /**
     * プロファイルが属するサービスIDを取得します.
     * @return サービスID
     */
    public String getServiceId() {
        return mServiceId;
    }

    /**
     * プロファイルが属するサービスIDを設定します.
     * @param serviceId サービスID
     */
    public void setServiceId(final String serviceId) {
        mServiceId = serviceId;
    }

    public Type getType() {
        return mType;
    }

    public void setType(Type type) {
        mType = type;
    }

    public List<Integer> getPinList() {
        return mPinList;
    }

    public void setPinList(final List<Integer> pinList) {
        mPinList = pinList;
    }

    public void addPin(final int pin) {
        mPinList.add(pin);
    }
}
