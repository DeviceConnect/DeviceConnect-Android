/*
 DevicePluginXml.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.localoauth;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

/**
 * DevicePlugin.xmlのルート要素.
 *
 * @author NTT DOCOMO, INC.
 */
public class DevicePluginXml implements Parcelable {

    /** API仕様定義ファイルを格納するディレクトリへのパス. */
    String mSpecPath;

    /** プラグインのサポートするプロファイルリストの宣言. */
    Map<String, DevicePluginXmlProfile> mSupportedProfiles;

    /**
     * Android SDKによって割り当てられた XML リソースID.
     * 同一プラグインに対する設定かどうかの判定に使用する.
     */
    private final int mResourceId;

    /**
     * コンストラクタ.
     *
     * @param resId XMLリソースID
     */
    DevicePluginXml(final int resId) {
        mResourceId = resId;
    }

    /**
     * コンストラクタ.
     * @deprecated 別のコンストラクタ DevicePluginXml(int) を使用することを推奨
     */
    public DevicePluginXml() {
        this(-1);
    }

    /**
     * XMLリソースIDを取得する.
     * @return XMLリソースID
     */
    public int getResourceId() {
        return mResourceId;
    }

    /**
     * 同一プラグインに対する設定かどうかを返す.
     * @param plugin プラグイン情報
     * @return 同一プラグインに対する設定である場合は<code>true</code>、そうでない場合は<code>false</code>
     */
    public boolean isSamePlugin(final DevicePluginXml plugin) {
        return getResourceId() == plugin.getResourceId();
    }

    /**
     * API仕様定義ファイルを格納するディレクトリへのパスを取得する.
     * @return API仕様定義ファイルを格納するディレクトリへのパス
     */
    public String getSpecPath() {
        return mSpecPath;
    }

    /**
     * プラグインのサポートするプロファイルリストの宣言を取得する.
     * @return プラグインのサポートするプロファイルリストの宣言
     */
    public Map<String, DevicePluginXmlProfile> getSupportedProfiles() {
        return mSupportedProfiles;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mSpecPath);
        dest.writeInt(this.mSupportedProfiles.size());
        for (Map.Entry<String, DevicePluginXmlProfile> entry : this.mSupportedProfiles.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeParcelable(entry.getValue(), flags);
        }
        dest.writeInt(this.mResourceId);
    }

    protected DevicePluginXml(Parcel in) {
        this.mSpecPath = in.readString();
        int mSupportedProfilesSize = in.readInt();
        this.mSupportedProfiles = new HashMap<String, DevicePluginXmlProfile>(mSupportedProfilesSize);
        for (int i = 0; i < mSupportedProfilesSize; i++) {
            String key = in.readString();
            DevicePluginXmlProfile value = in.readParcelable(DevicePluginXmlProfile.class.getClassLoader());
            this.mSupportedProfiles.put(key, value);
        }
        this.mResourceId = in.readInt();
    }

    public static final Creator<DevicePluginXml> CREATOR = new Creator<DevicePluginXml>() {
        @Override
        public DevicePluginXml createFromParcel(Parcel source) {
            return new DevicePluginXml(source);
        }

        @Override
        public DevicePluginXml[] newArray(int size) {
            return new DevicePluginXml[size];
        }
    };
}
