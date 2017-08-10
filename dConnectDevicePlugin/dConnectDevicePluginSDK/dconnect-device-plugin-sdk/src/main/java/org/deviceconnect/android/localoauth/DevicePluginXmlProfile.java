/*
 DevicePluginXmlProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.localoauth;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

/**
 * DevicePlugin.xmlのProfile設定値.
 * @author NTT DOCOMO, INC.
 */
public class DevicePluginXmlProfile implements Parcelable {
    
    /** プロファイル. */
    protected final String mProfile;
    
    /** 有効期限(秒). */
    protected final long mExpirePeriod;
    
    /** ロケール別プロファイル情報. */
    protected final Map<String, DevicePluginXmlProfileLocale> mProfileLocales;

    /** プロファイル定義ディレクトリのパス. nullの場合は assets/api と同じ扱いとする. */
    private String mSpecPath;
    
    /**
     * コンストラクタ.
     * @param profile プロファイル名
     * @param expirePeriod 有効期限(秒)
     */
    public DevicePluginXmlProfile(final String profile, final long expirePeriod) {
        mProfile = profile;
        mExpirePeriod = expirePeriod;
        mProfileLocales = new HashMap<String, DevicePluginXmlProfileLocale>();
    }
    
    /**
     * プロファイル名を返す.
     * @return プロファイル名
     */
    public String getProfile() {
        return mProfile;
    }
    
    /**
     * 有効期限(秒)を返す.
     * @return 有効期限(秒)
     */
    public long getExpirePeriod() {
        return mExpirePeriod;
    }

    /**
     * ローカライズされたプロファイル名を設定する.
     * @param lang  ロケール文字列
     * @param name  ローカライズされたプロファイル名
     */
    public void putName(final String lang, final String name) {
        DevicePluginXmlProfileLocale locale = mProfileLocales.get(lang);
        if (locale != null) {
            locale.setName(name);
        } else {
            locale = new DevicePluginXmlProfileLocale(lang);  
            locale.setName(name);
            mProfileLocales.put(lang, locale);
        }
    }

    /**
     * ローカライズされたプロファイル名を設定する.
     * @param lang  ロケール文字列
     * @param description  ローカライズされたDescription
     */
    public void putDescription(final String lang, final String description) {
        DevicePluginXmlProfileLocale locale = mProfileLocales.get(lang);
        if (locale != null) {
            locale.setDescription(description);
        } else {
            locale = new DevicePluginXmlProfileLocale(lang);  
            locale.setDescription(description);
            mProfileLocales.put(lang, locale);
        }
    }

    /**
     * Locales配列を返す.
     * @return Locales配列 
     */
    public Map<String, DevicePluginXmlProfileLocale> getXmlProfileLocales() {
        return mProfileLocales;
    }

    /**
     * プロファイル定義ディレクトリのパスを設定する.
     * @param path プロファイル定義ディレクトリのパス
     */
    public void setSpecPath(final String path) {
        mSpecPath = path;
    }

    /**
     * プロファイル定義ディレクトリのパスを取得する.
     * @return プロファイル定義ディレクトリのパス
     */
    public String getSpecPath() {
        return mSpecPath;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mProfile);
        dest.writeLong(this.mExpirePeriod);
        dest.writeInt(this.mProfileLocales.size());
        for (Map.Entry<String, DevicePluginXmlProfileLocale> entry : this.mProfileLocales.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeParcelable(entry.getValue(), flags);
        }
        dest.writeString(this.mSpecPath);
    }

    protected DevicePluginXmlProfile(Parcel in) {
        this.mProfile = in.readString();
        this.mExpirePeriod = in.readLong();
        int mProfileLocalesSize = in.readInt();
        this.mProfileLocales = new HashMap<String, DevicePluginXmlProfileLocale>(mProfileLocalesSize);
        for (int i = 0; i < mProfileLocalesSize; i++) {
            String key = in.readString();
            DevicePluginXmlProfileLocale value = in.readParcelable(DevicePluginXmlProfileLocale.class.getClassLoader());
            this.mProfileLocales.put(key, value);
        }
        this.mSpecPath = in.readString();
    }

    public static final Creator<DevicePluginXmlProfile> CREATOR = new Creator<DevicePluginXmlProfile>() {
        @Override
        public DevicePluginXmlProfile createFromParcel(Parcel source) {
            return new DevicePluginXmlProfile(source);
        }

        @Override
        public DevicePluginXmlProfile[] newArray(int size) {
            return new DevicePluginXmlProfile[size];
        }
    };
}
