/*
 PeerConfig.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.webrtc.core;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

/**
 * PeerConfig.
 *
 * @author NTT DOCOMO, INC.
 */
public final class PeerConfig implements Parcelable {

    /**
     * Defined of api key.
     *
     * Constant Value: {@value}
     */
    private static final String EXTRA_API_KEY = "apiKey";

    /**
     * Defined of domain.
     *
     * Constant Value: {@value}
     */
    private static final String EXTRA_DOMAIN = "domain";

    /**
     * Api key of skyway.
     */
    private String mApiKey;

    /**
     * domain.
     */
    private String mDomain;

    /**
     * Constructor.
     * @param config string of JSON
     * @throws IllegalArgumentException if the string is invalid format
     */
    public PeerConfig(final String config) throws IllegalArgumentException {
        String apiKey, domain;
        try {
            JSONObject obj = new JSONObject(config);
            apiKey = obj.optString(EXTRA_API_KEY);
            domain = obj.optString(EXTRA_DOMAIN);
        } catch (Exception e) {
            throw new IllegalArgumentException("config format is invalid.");
        }

        if ((apiKey == null || apiKey.isEmpty()) || (domain == null || domain.isEmpty())) {
            throw new IllegalArgumentException("apiKey or domain is not set.");
        } else {
            mApiKey = apiKey;
            mDomain = domain;
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null) {
            if (obj instanceof PeerConfig) {
                PeerConfig other = (PeerConfig) obj;
                // Note: no null checks, because mApiKey and mDomain can never be null.
                return mApiKey.equals(other.mApiKey) && mDomain.equals(other.mDomain);
            } else {
                return false;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return mApiKey.hashCode() + mDomain.hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel out, final int flags) {
        out.writeString(mApiKey);
        out.writeString(mDomain);
    }

    public PeerConfig(final Parcel in) {
        mApiKey = in.readString();
        mDomain = in.readString();
    }

    public static final Parcelable.Creator<PeerConfig> CREATOR = new Parcelable.Creator<PeerConfig>() {
        @Override
        public PeerConfig createFromParcel(final Parcel in) {
            return new PeerConfig(in);
        }
        @Override
        public PeerConfig[] newArray(final int size) {
            return new PeerConfig[size];
        }
    };

    /**
     * Retrieve a api key.
     * @return api key
     */
    public String getApiKey() {
        return mApiKey;
    }

    /**
     * Retrieve a domain.
     * @return domain
     */
    public String getDomain() {
        return mDomain;
    }

    @Override
    public String toString() {
        return mApiKey + "_" + mDomain;
    }
}
