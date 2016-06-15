package org.deviceconnect.android.service;


import android.content.Intent;

import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.DConnectProfileProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DConnectService implements DConnectProfileProvider {

    /**
     * サービスID.
     */
    private final String mId;

    /**
     * サポートするプロファイル一覧.
     */
    private final Map<String, DConnectProfile> mProfiles = new HashMap<String, DConnectProfile>();

    /**
     * コンストラクタ.
     * @param id サービスID
     */
    public DConnectService(final String id) {
        mId = id;
    }

    /**
     * サービスIDを取得する.
     * @return サービスID
     */
    public String getId() {
        return mId;
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
        return profile.onRequest(request, response, this);
    }
}
