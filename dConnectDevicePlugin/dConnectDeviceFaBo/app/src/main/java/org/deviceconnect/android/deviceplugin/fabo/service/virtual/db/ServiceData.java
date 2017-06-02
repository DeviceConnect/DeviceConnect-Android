package org.deviceconnect.android.deviceplugin.fabo.service.virtual.db;

import java.util.ArrayList;
import java.util.List;

public class ServiceData {
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
     * サービスIDを取得します.
     * @return
     */
    public String getServiceId() {
        return mServiceId;
    }

    public void setServiceId(final String serviceId) {
        mServiceId = serviceId;
    }

    public String getName() {
        return mName;
    }

    public void setName(final String name) {
        mName = name;
    }

    public List<ProfileData> getProfileDataList() {
        return mProfileDataList;
    }

    public void setProfileDataList(final List<ProfileData> profileDataList) {
        mProfileDataList = profileDataList;
    }

    public void addProfileData(final ProfileData profileData) {
        mProfileDataList.add(profileData);
    }
}
