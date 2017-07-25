package org.deviceconnect.android.deviceplugin.fabo.service.virtual.db;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * 仮想サービスデータ.
 */
public class ServiceData implements Parcelable {
    /**
     * 仮想サービスのID.
     */
    private String mServiceId;

    /**
     * 仮想サービスの名前.
     */
    private String mName;

    /**
     * 仮想サービスが保持するプロファイルデータ.
     */
    private List<ProfileData> mProfileDataList = new ArrayList<>();

    /**
     * コンストラクタ.
     */
    public ServiceData() {
    }

    /**
     * Parcelable用のコンストラクタ.
     * @param parcel Parcel
     */
    private ServiceData(final Parcel parcel) {
        mServiceId = parcel.readString();
        mName = parcel.readString();
        parcel.readList(mProfileDataList, ServiceData.class.getClassLoader());
    }

    /**
     * サービスIDを取得します.
     * @return サービスID
     */
    public String getServiceId() {
        return mServiceId;
    }

    /**
     * サービスIDを設定します.
     * @param serviceId サービスID
     */
    public void setServiceId(final String serviceId) {
        mServiceId = serviceId;
        for (ProfileData p : mProfileDataList) {
            p.setServiceId(serviceId);
        }
    }

    /**
     * サービスの名前を取得します.
     * @return サービスの名前
     */
    public String getName() {
        return mName;
    }

    /**
     * サービスの名前を設定します.
     * @param name サービスの名前
     */
    public void setName(final String name) {
        mName = name;
    }

    /**
     * サービスが持つプロファイルのリストを取得します.
     * @return プロファイルのリスト
     */
    public List<ProfileData> getProfileDataList() {
        return mProfileDataList;
    }

    public ProfileData getProfileData(final ProfileData.Type type) {
        for (ProfileData p : mProfileDataList) {
            if (p.getType() == type) {
                return p;
            }
        }
        return null;
    }

    /**
     * サービスが持つプロファイルのリストを設定します.
     * @param profileDataList プロファイルのリスト
     */
    public void setProfileDataList(final List<ProfileData> profileDataList) {
        mProfileDataList = profileDataList;
        for (ProfileData p : mProfileDataList) {
            p.setServiceId(mServiceId);
        }
    }

    /**
     * サービスが持つプロファイルのリストに追加します.
     * <p>
     * 既に同じプロファイルを持っている場合には、新しいプロファイルに置き換えます。
     * </p>
     * @param profileData 追加するプロファイル
     */
    public void addProfileData(final ProfileData profileData) {
        if (mProfileDataList.contains(profileData)) {
            for (int i = 0; i < mProfileDataList.size(); i++) {
                if (mProfileDataList.get(i).equals(profileData)) {
                    mProfileDataList.set(i, profileData);
                    break;
                }
            }
        } else {
            mProfileDataList.add(profileData);
        }
        profileData.setServiceId(mServiceId);
    }

    /**
     * 削除します.
     * @param profileData
     */
    public void removeProfileData(final ProfileData profileData) {
        mProfileDataList.remove(profileData);
    }

    /**
     * 仮想サービスが使用しているピンの一覧を取得します.
     * @return ピンの一覧
     */
    public List<Integer> getUsePins() {
        List<Integer> pins = new ArrayList<>();
        for (ProfileData p : mProfileDataList) {
            for (Integer i : p.getPinList()) {
                pins.add(i);
            }
        }
        return pins;
    }

    /**
     * 指定されたピン番号が使用されているか確認を行います.
     * @param pinNumber 確認を行うピン番号
     * @return 使用されている場合にはtrue、それ以外はfalse
     */
    public boolean usedPin(final int pinNumber) {
        for (ProfileData p : mProfileDataList) {
            for (Integer i : p.getPinList()) {
                if (pinNumber == i) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel parcel, final int flags) {
        parcel.writeString(mServiceId);
        parcel.writeString(mName);
        parcel.writeList(mProfileDataList);
    }

    public static final Creator<ServiceData> CREATOR = new Creator<ServiceData>() {
        @Override
        public ServiceData createFromParcel(final Parcel source) {
            return new ServiceData(source);
        }
        @Override
        public ServiceData[] newArray(final int size) {
            return new ServiceData[size];
        }
    };
}
