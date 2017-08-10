/*
 DevicePluginXmlProfileLocale.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.localoauth;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * DevicePlugin.xmlのプロファイルロケール情報.
 * @author NTT DOCOMO, INC.
 */
public class DevicePluginXmlProfileLocale implements Parcelable {

    /** ロケール文字列("ja","en"). */
    protected String mLocale;
    
    /** ローカライズされたプロファイル名. */
    protected String mName;
    
    /** ローカライズされたDescription. */
    protected String mDescription;
    
    /**
     * コンストラクタ(ロケール文字列指定).
     * @param locale    ロケール文字列("ja","en")
     */
    public DevicePluginXmlProfileLocale(final String locale) {
        mLocale = locale;
    }

    /**
     * ロケール文字列を返す.
     * @return locale ロケール文字列
     */
    public String getLocale() {
        return mLocale;
    }
    
    /**
     * ロケール文字列を設定.
     * @param locale ロケール文字列
     */
    public void setLocale(final String locale) {
        mLocale = locale;
    }
    
    /**
     * ローカライズされたプロファイル名を返す.
     * @return ローカライズされたプロファイル名
     */
    public String getName() {
        return mName;
    }
    
    /**
     * ローカライズされたプロファイル名を設定.
     * @param name ローカライズされたプロファイル名
     */
    public void setName(final String name) {
        mName = name;
    }
    
    /**
     * ローカライズされたDescriptionを設定.
     * @return ローカライズされたDescription
     */
    public String getDescription() {
        return mDescription;
    }
    
    /**
     * ローカライズされたDescriptionを設定.
     * @param description ローカライズされたDescription
     */
    public void setDescription(final String description) {
        mDescription = description;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mLocale);
        dest.writeString(this.mName);
        dest.writeString(this.mDescription);
    }

    protected DevicePluginXmlProfileLocale(Parcel in) {
        this.mLocale = in.readString();
        this.mName = in.readString();
        this.mDescription = in.readString();
    }

    public static final Creator<DevicePluginXmlProfileLocale> CREATOR = new Creator<DevicePluginXmlProfileLocale>() {
        @Override
        public DevicePluginXmlProfileLocale createFromParcel(Parcel source) {
            return new DevicePluginXmlProfileLocale(source);
        }

        @Override
        public DevicePluginXmlProfileLocale[] newArray(int size) {
            return new DevicePluginXmlProfileLocale[size];
        }
    };
}
