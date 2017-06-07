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
     * 仮装サービスに登録するプロファイルのタイプ.
     */
    public enum Type {
        /**
         * GPIO用Lightプロファイル.
         */
        GPIO_LIGHT(1),

        /**
         * GPIO用Temperatureプロファイル.
         */
        GPIO_TEMPERATURE(2),

        /**
         * GPIO用Vibrationプロファイル.
         */
        GPIO_VIBRATION(3),

        /**
         * GPIO用Illuminanceプロファイル.
         */
        GPIO_ILLUMINANCE(4),

        /**
         * GPIO用KeyEventプロファイル.
         */
        GPIO_KEY_EVENT(5),

        /**
         * GPIO用Humidityプロファイル.
         */
        GPIO_HUMIDITY(6),

        /**
         * GPIO用Proximityプロファイル.
         */
        GPIO_PROXIMITY(7),

        /**
         * I2C用RobotCar用DriveControllerプロファイル.
         */
        I2C_ROBOT_DRIVE_CONTROLLER(100),

        /**
         * I2C用RobotCar(Mouse)用DriveControllerプロファイル.
         */
        I2C_MOUSE_DRIVE_CONTROLLER(101),

        /**
         * I2C用Temperatureプロファイル.
         */
        I2C_TEMPERATURE(102);

        /**
         * プロファイルのタイプ.
         */
        private int mValue;

        Type(final int value) {
            mValue = value;
        }

        public int getValue() {
            return mValue;
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
}
