package org.deviceconnect.android.deviceplugin.fabo.service.virtual.db;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * プロファイルデータ.
 */
public class ProfileData implements Parcelable {

    /**
     * カテゴリ.
     */
    public enum Category {
        /**
         * GPIOを表すカテゴリー.
         */
        GPIO("GPIO"),

        /**
         * I2Cを表すカテゴリー.
         */
        I2C("I2C");

        private String mValue;

        Category(final String value) {
            mValue = value;
        }

        public String getValue() {
            return mValue;
        }
    }

    /**
     * 仮想サービスに登録するプロファイルのタイプ.
     */
    public enum Type {
        /**
         * GPIO用Lightプロファイル.
         * <p>
         * Brick: #101
         * </p>
         */
        GPIO_LIGHT(1, "#101", Category.GPIO, "light"),

        /**
         * GPIO用Temperatureプロファイル.
         * <p>
         * Brick: #108
         * </p>
         */
        GPIO_TEMPERATURE(2, "#108", Category.GPIO, "temperature"),

        /**
         * GPIO用Vibrationプロファイル.
         * <p>
         * Brick: #105
         * </p>
         */
        GPIO_VIBRATION(3, "#105", Category.GPIO, "vibration"),

        /**
         * GPIO用Illuminanceプロファイル.
         * <p>
         * Brick: #109
         * </p>
         */
        GPIO_ILLUMINANCE(4, "#109", Category.GPIO, "illuminace"),

        /**
         * GPIO用KeyEventプロファイル.
         * <p>
         * Brick: #103
         * </p>
         */
        GPIO_KEY_EVENT(5, "#103", Category.GPIO, "keyEvent"),

        /**
         * GPIO用Humidityプロファイル.
         * <p>
         * Brick: #115
         * </p>
         */
        GPIO_HUMIDITY(6, "#115", Category.GPIO, "humidity"),

        /**
         * GPIO用Proximityプロファイル.
         * <p>
         * Brick: #1116
         * </p>
         */
        GPIO_PROXIMITY(7, "#116", Category.GPIO, "proximity"),

        /**
         * I2C用RobotCar用DriveControllerプロファイル.
         */
        I2C_ROBOT_DRIVE_CONTROLLER(100, null, Category.I2C, "driveController"),

        /**
         * I2C用RobotCar(Mouse)用DriveControllerプロファイル.
         */
        I2C_MOUSE_DRIVE_CONTROLLER(101, null, Category.I2C, "driveController"),

        /**
         * I2C用DeviceOrientation(3axis)プロファイル.
         * <p>
         * Brick: #201
         * </p>
         */
        I2C_3AXIS_DEVICE_ORIENTATION(102, "#201", Category.I2C, "deviceOrientation"),

        /**
         * I2C用Temperatureプロファイル.
         * <p>
         * Brick: #207
         * </p>
         */
        I2C_TEMPERATURE(103, "#207", Category.I2C, "temperature"),

        /**
         * I2C用Humidityプロファイル.
         * <p>
         * Brick: #208
         * </p>
         */
        I2C_HUMIDITY(104, "#208", Category.I2C, "humidity"),

        /**
         * I2C用Proximityプロファイル.
         * <p>
         * Brick: #205
         * </p>
         */
        I2C_PROXIMITY(105, "#205", Category.I2C, "proximity"),

        /**
         * I2C用Illuminanceプロファイル.
         * <p>
         * Brick: #217
         * </p>
         */
        I2C_ILLUMINANCE(106, "#217", Category.I2C, "illuminace"),

        /**
         * I2C用AtmosphericPressureプロファイル.
         * <p>
         * Brick: #204
         * </p>
         */
        I2C_ATMOSPHERIC_PRESSURE(107, "#204", Category.I2C, "atmosphericPressure"),

        /**
         * I2C用Proximityプロファイル.
         * <p>
         * Brick: #222
         * </p>
         */
        I2C_LIDARLITE_PROXIMITY(108, "#222", Category.I2C, "proximity");

        /**
         * プロファイルの種別.
         */
        private int mValue;

        /**
         * BrickのID.
         */
        private String mBrick;

        /**
         * カテゴリ-.
         */
        private Category mCategory;

        /**
         * プロファイル名.
         */
        private String mProfileName;

        Type(final int value, final String brick, final Category category, final String profileName) {
            mValue = value;
            mBrick = brick;
            mCategory = category;
            mProfileName = profileName;
        }

        /**
         * プロファイルの種別を取得します.
         * @return プロファイルの種別
         */
        public int getValue() {
            return mValue;
        }

        /**
         * プロファイルが対応するBrick番号を取得します.
         * @return Brickを識別するID
         */
        public String getBrick() {
            return mBrick;
        }

        /**
         * プロファイルのカテゴリーを取得します.
         * @return CATEGORY_GPIO もしくは CATEGORY_I2C
         */
        public Category getCategory() {
            return mCategory;
        }

        /**
         * プロファイル名を取得します.
         * @return プロファイル名
         */
        public String getProfileName() {
            return mProfileName;
        }

        /**
         * 指定された値からプロファイルのタイプを取得します.
         * @param type プロファイルのタイプ
         * @return プロファイルタイプ
         */
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
     * プロファイルが属する仮想サービスのID.
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
     * コンストラクタ.
     */
    public ProfileData() {
    }

    /**
     * Parcelable用のコンストラクタ.
     * @param parcel Parcel
     */
    private ProfileData(final Parcel parcel) {
        mType = Type.getType(parcel.readInt());
        mServiceId = parcel.readString();
        parcel.readList(mPinList, ProfileData.class.getClassLoader());
    }

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

    /**
     * プロファイルのタイプを取得します.
     * @return プロファイルのタイプ
     */
    public Type getType() {
        return mType;
    }

    /**
     * プロファイルのタイプを設定します.
     * @param type プロファイルのタイプ
     */
    public void setType(final Type type) {
        mType = type;
    }

    /**
     * プロファイルが操作するピンのリストを取得します.
     * @return プロファイルが操作するピンのリスト
     */
    public List<Integer> getPinList() {
        return mPinList;
    }

    /**
     * プロファイルが操作するピンのリストを設定します.
     * @param pinList プロファイルが操作するピンのリスト
     */
    public void setPinList(final List<Integer> pinList) {
        mPinList = pinList;
    }

    /**
     * プロファイルが操作するピンをリストに追加します.
     * @param pin 追加するピン
     */
    public void addPin(final int pin) {
        mPinList.add(pin);
    }

    public boolean usedPin(final int pin) {
        return mPinList.contains(pin);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel parcel, final int flags) {
        parcel.writeInt(mType.getValue());
        parcel.writeString(mServiceId);
        parcel.writeList(mPinList);
    }

    public static final Creator<ProfileData> CREATOR = new Creator<ProfileData>() {
        @Override
        public ProfileData createFromParcel(final Parcel source) {
            return new ProfileData(source);
        }
        @Override
        public ProfileData[] newArray(final int size) {
            return new ProfileData[size];
        }
    };

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + mType.getValue();
        result = prime * result + ((mServiceId == null) ? 0 : mServiceId.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ProfileData other = (ProfileData) obj;
        return mType == other.mType && mServiceId.equals(other.getServiceId());
    }
}
