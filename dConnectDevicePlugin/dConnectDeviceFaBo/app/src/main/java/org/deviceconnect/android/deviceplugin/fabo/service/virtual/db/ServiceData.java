package org.deviceconnect.android.deviceplugin.fabo.service.virtual.db;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * 仮装サービスデータ.
 */
public class ServiceData implements Parcelable {
    /**
     * 仮装サービスのID.
     */
    private String mServiceId;

    /**
     * 仮装サービスの名前.
     */
    private String mName;

    /**
     * 仮装サービスが保持するプロファイルデータ.
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
     * @param profileData 追加するプロファイル
     */
    public void addProfileData(final ProfileData profileData) {
        mProfileDataList.add(profileData);
        profileData.setServiceId(mServiceId);
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
