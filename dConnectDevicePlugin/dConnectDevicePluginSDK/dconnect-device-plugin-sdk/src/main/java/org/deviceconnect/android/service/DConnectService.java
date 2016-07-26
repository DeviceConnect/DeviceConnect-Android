package org.deviceconnect.android.service;


import android.content.Context;
import android.content.Intent;

import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceInformationProfile;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DConnectService implements DConnectProfileProvider, ServiceDiscoveryProfileConstants {

    /**
     * サービスID.
     */
    private final String mId;

    /**
     * サポートするプロファイル一覧.
     */
    private final Map<String, DConnectProfile> mProfiles = new HashMap<String, DConnectProfile>();

    private String mName;

    private String mType;

    private boolean mIsOnline;

    private String mConfig;

    private Context mContext;

    /**
     * コンストラクタ.
     * @param id サービスID
     */
    public DConnectService(final String id) {
        if (id == null) {
            throw new NullPointerException("id is null.");
        }
        mId = id;
        addProfile(new ServiceInformationProfile());
    }

    /**
     * サービスIDを取得する.
     * @return サービスID
     */
    public String getId() {
        return mId;
    }

    public void setName(final String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public void setNetworkType(final NetworkType type) {
        mType = type.getValue();
    }

    public void setNetworkType(final String type) {
        mType = type;
    }

    public String getNetworkType() {
        return mType;
    }

    public void setOnline(final boolean isOnline) {
        mIsOnline = isOnline;
    }

    public boolean isOnline() {
        return mIsOnline;
    }

    public String getConfig() {
        return mConfig;
    }

    public void setConfig(final String config) {
        mConfig = config;
    }

    void setContext(final Context context) {
        mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    public List<DConnectProfile> getProfileList() {
        List<DConnectProfile> list = new ArrayList<DConnectProfile>();
        for (DConnectProfile profile : mProfiles.values()) {
            list.add(profile);
        }
        return list;
    }

    @Override
    public DConnectProfile getProfile(final String name) {
        if (name == null) {
            return null;
        }
        return mProfiles.get(name.toLowerCase());
    }

    @Override
    public void addProfile(final DConnectProfile profile) {
        if (profile == null) {
            return;
        }
        profile.setService(this);
        mProfiles.put(profile.getProfileName().toLowerCase(), profile);
    }

    @Override
    public void removeProfile(final DConnectProfile profile) {
        if (profile == null) {
            return;
        }
        mProfiles.remove(profile.getProfileName().toLowerCase());
    }

    public boolean onRequest(final Intent request, final Intent response) {
        DConnectProfile profile = getProfile(DConnectProfile.getProfile(request));
        if (profile == null) {
            MessageUtils.setNotSupportProfileError(response);
            return true;
        }
        return profile.onRequest(request, response);
    }
}
