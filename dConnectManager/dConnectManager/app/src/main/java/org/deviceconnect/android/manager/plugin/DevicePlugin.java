/*
 DevicePlugin.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.plugin;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ComponentInfo;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import org.deviceconnect.android.manager.util.DConnectUtil;
import org.deviceconnect.android.manager.util.VersionName;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.deviceconnect.android.manager.plugin.DevicePluginState.DISABLED;
import static org.deviceconnect.android.manager.plugin.DevicePluginState.ENABLED;

/**
 * デバイスプラグイン.
 * @author NTT DOCOMO, INC.
 */
public class DevicePlugin {
    /** 接続試行回数. */
    private static final int MAX_CONNECTION_TRY = 5;
    /** 設定ファイル名のプレフィクス. */
    private static final String PREFIX_PREFERENCES = "plugin_preferences_";
    /** 設定キー: 有効状態. */
    private static final String KEY_ENABLED = "enabled";

    /** デバイスプラグイン情報. */
    private Info mInfo;
    /** デバイスプラグインを宣言するコンポーネントの情報. */
    private ComponentInfo mPluginComponent;
    /** 有効状態. */
    private DevicePluginState mState;
    /** プラグイン設定を永続化するオブジェクト. */
    private SharedPreferences mPreferences;
    /** 接続管理クラス. */
    private Connection mConnection;
    /** ロガー. */
    private final Logger mLogger = Logger.getLogger("dconnect.manager");

    /**
     * リソースを削除する.
     */
    synchronized void dispose() {
        mPreferences.edit().clear().apply();
    }

    /**
     * プラグインを宣言するコンポーネントを取得する.
     * @return プラグインを宣言するコンポーネント
     */
    public ComponentInfo getPluginComponent() {
        return mPluginComponent;
    }

    /**
     * デバイスプラグインのパッケージ名を取得する.
     * @return パッケージ名
     */
    public String getPackageName() {
        return mPluginComponent.packageName;
    }

    /**
     * デバイスプラグインのクラス名を取得する.
     * @return クラス名
     */
    public String getClassName() {
        return mPluginComponent.name;
    }

    /**
     * ComponentNameを取得する.
     * @return ComponentNameのインスタンス
     */
    public ComponentName getComponentName() {
        return new ComponentName(getPackageName(), getClassName());
    }

    /**
     * デバイスプラグインのバージョン名を取得する.
     * @return バージョン名
     */
    public String getVersionName() {
        return mInfo.mVersionName;
    }

    /**
     * デバイスプラグインIDを取得する.
     * @return デバイスプラグインID
     */
    public String getPluginId() {
        return mInfo.mPluginId;
    }

    /**
     * デバイスプラグイン名を取得する.
     * @return デバイスプラグイン名
     */
    public String getDeviceName() {
        return mInfo.mDeviceName;
    }
    
    /**
     * Get a class name of service for restart.
     * @return class name or null if there are no service for restart
     */
    public String getStartServiceClassName() {
        return mInfo.mStartServiceClassName;
    }

    /**
     * デバイスプラグインがサポートするプロファイルの一覧を取得する.
     * @return サポートするプロファイルの一覧
     */
    public List<String> getSupportProfiles() {
        return new ArrayList<>(mInfo.mSupports);
    }

    public boolean supportsProfile(final String profileName) {
        for (String support : mInfo.mSupports) {
            if (support.equalsIgnoreCase(profileName)) { // MEMO パスの大文字小文字無視
                return true;
            }
        }
        return false;
    }

    /**
     * デバイスプラグインSDKのバージョンを取得する.
     * @return デバイスプラグインSDKのバージョン
     */
    public VersionName getPluginSdkVersionName() {
        return mInfo.mPluginSdkVersionName;
    }

    /**
     * デバイスプラグインのアイコンデータのリソースIDを取得する.
     * @return デバイスプラグインのアイコンデータのリソースID
     */
    public Integer getPluginIconId() {
        return mInfo.mPluginIconId;
    }

    /**
     * デバイスプラグインのアイコンデータを取得する.
     * @param context コンテキスト
     * @return デバイスプラグインのアイコンデータ
     */
    public Drawable getPluginIcon(final Context context) {
        return DConnectUtil.loadPluginIcon(context, this);
    }

    public ConnectionType getConnectionType() {
        return mInfo.mConnectionType;
    }

    void setConnection(final Connection connection) {
        mConnection = connection;
    }

    public DevicePluginState getState() {
        return mState;
    }

    private void setState(final DevicePluginState state) {
        mState = state;

        boolean isEnabled = state == ENABLED;
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(KEY_ENABLED, isEnabled);
        editor.apply();
    }

    public boolean isEnabled() {
        return mState == ENABLED;
    }

    public synchronized void enable() {
        setState(DevicePluginState.ENABLED);
        apply();
    }

    public synchronized void disable() {
        setState(DevicePluginState.DISABLED);
        apply();
    }

    public synchronized void apply() {
        switch (getState()) {
            case ENABLED:
                if (mConnection.getState() == ConnectionState.DISCONNECTED) {
                    tryConnection();
                }
                break;
            case DISABLED:
                if (mConnection.getState() == ConnectionState.CONNECTED) {
                    mConnection.disconnect();
                    mLogger.info("Disconnected to the plug-in: " + getPackageName());
                }
                break;
            default:
                break;
        }
    }

    private boolean tryConnection() {
        for (int cnt = 0; cnt < MAX_CONNECTION_TRY; cnt++) {
            try {
                mConnection.connect();
                mLogger.info("Connected to the plug-in: " + getPackageName());
                return true;
            } catch (ConnectingException e) {
                mLogger.warning("Failed to connect to the plug-in: " + getPackageName());
            }
        }
        return false;
    }

    public void addConnectionStateListener(final ConnectionStateListener listener) {
        mConnection.addConnectionStateListener(listener);
    }

    public void removeConnectionStateListener(final ConnectionStateListener listener) {
        mConnection.removeConnectionStateListener(listener);
    }

    public synchronized void send(final Intent message) throws MessagingException {
        if (DevicePluginState.ENABLED != getState()) {
            throw new MessagingException(MessagingException.Reason.NOT_ENABLED);
        }
        switch (mConnection.getState()) {
            case SUSPENDED:
                if (!tryConnection()) {
                    throw new MessagingException(MessagingException.Reason.CONNECTION_SUSPENDED);
                }
                break;
            default:
                break;
        }
        mConnection.send(message);
    }

    @Override
    public String toString() {
        return "{\n" +
                "    DeviceName: " + getDeviceName() + "\n" +
                "    ServiceId: " + getPluginId() + "\n" +
                "    Package: " + getPackageName() + "\n" +
                "    Class: " + getClassName() + "\n" +
                "    Version: " + getVersionName() + "\n" +
                "}";
    }

    static class Builder {
        /** コンテキスト. */
        private final Context mContext;
        /** デバイスプラグイン情報. */
        private Info mInfo = new Info();
        /** デバイスプラグインを宣言するコンポーネントの情報. */
        private ComponentInfo mPluginComponent;

        public Builder(final Context context) {
            mContext = context;
        }

        Builder setStartServiceClassName(final String startServiceClassName) {
            mInfo.mStartServiceClassName = startServiceClassName;
            return this;
        }

        Builder setVersionName(final String versionName) {
            mInfo.mVersionName = versionName;
            return this;
        }

        Builder setPluginSdkVersionName(final VersionName pluginSdkVersionName) {
            mInfo.mPluginSdkVersionName = pluginSdkVersionName;
            return this;
        }

        Builder setPluginId(final String pluginId) {
            mInfo.mPluginId = pluginId;
            return this;
        }

        Builder setDeviceName(final String deviceName) {
            mInfo.mDeviceName = deviceName;
            return this;
        }

        Builder setPluginIconId(final Integer pluginIconId) {
            mInfo.mPluginIconId = pluginIconId;
            return this;
        }

        Builder setSupportedProfiles(final List<String> supportedProfiles) {
            mInfo.mSupports = supportedProfiles;
            return this;
        }

        Builder setConnectionType(final ConnectionType connectionType) {
            mInfo.mConnectionType = connectionType;
            return this;
        }

        Builder setPluginComponent(final ComponentInfo pluginComponent) {
            mPluginComponent = pluginComponent;
            return this;
        }

        DevicePlugin build() {
            String prefName = PREFIX_PREFERENCES + mInfo.getPluginId();
            SharedPreferences pref = mContext.getSharedPreferences(prefName, Context.MODE_PRIVATE);
            boolean isEnabled = pref.getBoolean(KEY_ENABLED, true);

            DevicePlugin plugin = new DevicePlugin();
            plugin.mInfo = mInfo;
            plugin.mPluginComponent = mPluginComponent;
            plugin.mPreferences = pref;
            plugin.mState = isEnabled ? ENABLED : DISABLED;
            return plugin;
        }
    }

    public static class Info implements Parcelable {
        /** Class name of service for restart. */
        private String mStartServiceClassName;
        /** デバイスプラグインのバージョン名. */
        private String mVersionName;
        /** プラグインSDKバージョン名. */
        private VersionName mPluginSdkVersionName;
        /** プラグインID. */
        private String mPluginId;
        /** デバイスプラグイン名. */
        private String mDeviceName;
        /** プラグインアイコン. */
        private Integer mPluginIconId;
        /** サポートしているプロファイルのリスト. */
        private List<String> mSupports;
        /** 接続タイプ. */
        private ConnectionType mConnectionType;

        public String getStartServiceClassName() {
            return mStartServiceClassName;
        }

        public String getVersionName() {
            return mVersionName;
        }

        public VersionName getPluginSdkVersionName() {
            return mPluginSdkVersionName;
        }

        public String getPluginId() {
            return mPluginId;
        }

        public String getDeviceName() {
            return mDeviceName;
        }

        public Integer getPluginIconId() {
            return mPluginIconId;
        }

        public List<String> getSupports() {
            return mSupports;
        }

        public ConnectionType getConnectionType() {
            return mConnectionType;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.mStartServiceClassName);
            dest.writeString(this.mVersionName);
            dest.writeParcelable(this.mPluginSdkVersionName, flags);
            dest.writeString(this.mPluginId);
            dest.writeString(this.mDeviceName);
            dest.writeValue(this.mPluginIconId);
            dest.writeStringList(this.mSupports);
            dest.writeInt(this.mConnectionType == null ? -1 : this.mConnectionType.ordinal());
        }

        Info() {
        }

        protected Info(Parcel in) {
            this.mStartServiceClassName = in.readString();
            this.mVersionName = in.readString();
            this.mPluginSdkVersionName = in.readParcelable(VersionName.class.getClassLoader());
            this.mPluginId = in.readString();
            this.mDeviceName = in.readString();
            this.mPluginIconId = (Integer) in.readValue(Integer.class.getClassLoader());
            this.mSupports = in.createStringArrayList();
            int tmpMConnectionType = in.readInt();
            this.mConnectionType = tmpMConnectionType == -1 ? null : ConnectionType.values()[tmpMConnectionType];
        }

        public static final Creator<Info> CREATOR = new Creator<Info>() {
            @Override
            public Info createFromParcel(Parcel source) {
                return new Info(source);
            }

            @Override
            public Info[] newArray(int size) {
                return new Info[size];
            }
        };
    }
}
