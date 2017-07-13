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
import android.content.pm.ComponentInfo;
import android.graphics.drawable.Drawable;

import org.deviceconnect.android.manager.util.VersionName;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import static org.deviceconnect.android.manager.plugin.DevicePluginState.FOUND;

/**
 * デバイスプラグイン.
 * @author NTT DOCOMO, INC.
 */
public class DevicePlugin {
    /** デバイスプラグインを定義するコンポーネントの情報. */
    private ComponentInfo mPluginComponent;
    /** デバイスプラグインのバージョン名. */
    private String mVersionName;
    /** プラグインID. */
    private String mPluginId;
    /** デバイスプラグイン名. */
    private String mDeviceName;
    /* プラグインアイコン. */
    private Drawable mPluginIcon;
    /** Class name of service for restart. */
    private String mStartServiceClassName;
    /* プラグインSDKバージョン名. */
    private VersionName mPluginSdkVersionName;
    /**
     * サポートしているプロファイルを格納する.
     */
    private List<String> mSupports = new ArrayList<String>();

    private ConnectionType mConnectionType;

    private Connection mConnection;

    private DevicePluginState mState = DevicePluginState.FOUND;

    private final Logger mLogger = Logger.getLogger("dconnect.manager");

    /**
     * プラグインを宣言するコンポーネントを取得する.
     * @return プラグインを宣言するコンポーネント
     */
    public ComponentInfo getPluginComponent() {
        return mPluginComponent;
    }

    /**
     * プラグインを宣言するコンポーネントを設定する.
     * @param component プラグインを宣言するコンポーネント
     */
    void setPluginComponent(final ComponentInfo component) {
        mPluginComponent = component;
    }

    /**
     * デバイスプラグインのパッケージ名を取得する.
     * @return パッケージ名
     */
    public String getPackageName() {
        return mPluginComponent.packageName;
    }

    /**
     * デバイスプラグインのバージョン名を取得する.
     * @return バージョン名
     */
    public String getVersionName() {
        return mVersionName;
    }

    /**
     * デバイスプラグインのバージョン名を設定する.
     * @param versionName バージョン名
     */
    public void setVersionName(final String versionName) {
        mVersionName = versionName;
    }

    /**
     * デバイスプラグインのクラス名を取得する.
     * @return クラス名
     */
    public String getClassName() {
        return mPluginComponent.name;
    }
    /**
     * デバイスプラグインIDを取得する.
     * @return デバイスプラグインID
     */
    public String getPluginId() {
        return mPluginId;
    }
    /**
     * デバイスプラグインIDを設定する.
     * @param pluginId デバイスプラグインID
     */
    public void setPluginId(final String pluginId) {
        this.mPluginId = pluginId;
    }
    /**
     * デバイスプラグイン名を取得する.
     * @return デバイスプラグイン名
     */
    public String getDeviceName() {
        return mDeviceName;
    }
    /**
     * デバイスプラグイン名を設定する.
     * @param deviceName デバイスプラグイン名
     */
    public void setDeviceName(final String deviceName) {
        mDeviceName = deviceName;
    }
    /**
     * ComponentNameを取得する.
     * @return ComponentNameのインスタンス
     */
    public ComponentName getComponentName() {
        return new ComponentName(getPackageName(), getClassName());
    }
    
    /**
     * Get a class name of service for restart.
     * @return class name or null if there are no service for restart
     */
    public String getStartServiceClassName() {
        return mStartServiceClassName;
    }
    /**
     * Set a class name of service for restart.
     * @param className class name
     */
    public void setStartServiceClassName(final String className) {
        this.mStartServiceClassName = className;
    }
    /**
     * サポートするプロファイルを追加する.
     * @param profileName プロファイル名
     */
    public void addProfile(final String profileName) {
        mSupports.add(profileName);
    }
    /**
     * サポートするプロファイルを設定する.
     * @param profiles プロファイル名一覧
     */
    public void setSupportProfiles(final List<String> profiles) {
        mSupports = profiles;
    }
    /**
     * デバイスプラグインがサポートするプロファイルの一覧を取得する.
     * @return サポートするプロファイルの一覧
     */
    public List<String> getSupportProfiles() {
        return mSupports;
    }

    public boolean supportsProfile(final String profileName) {
        if (mSupports == null) {
            return false;
        }
        for (String support : mSupports) {
            if (support.equalsIgnoreCase(profileName)) { // MEMO パスの大文字小文字無視
                return true;
            }
        }
        return false;
    }

    /**
     * デバイスプラグインSDKのバージョンを設定する.
     * @param pluginSdkVersionName デバイスプラグインSDKのバージョン
     */
    public void setPluginSdkVersionName(final VersionName pluginSdkVersionName) {
        mPluginSdkVersionName = pluginSdkVersionName;
    }

    /**
     * デバイスプラグインSDKのバージョンを取得する.
     * @return デバイスプラグインSDKのバージョン
     */
    public VersionName getPluginSdkVersionName() {
        return mPluginSdkVersionName;
    }

    /**
     * デバイスプラグインのアイコンデータを設定する.
     * @param icon デバイスプラグインのアイコンデータ
     */
    public void setPluginIcon(final Drawable icon) {
        mPluginIcon = icon;
    }

    /**
     * デバイスプラグインのアイコンデータを取得する.
     * @return デバイスプラグインのアイコンデータ
     */
    public Drawable getPluginIcon() {
        return mPluginIcon;
    }

    public ConnectionType getConnectionType() {
        return mConnectionType;
    }

    void setConnectionType(ConnectionType connectionType) {
        mConnectionType = connectionType;
    }

    void setConnection(final Connection connection) {
        mConnection = connection;
    }

    public DevicePluginState getState() {
        return mState;
    }

    private void setState(final DevicePluginState state) {
        mState = state;
    }

    public synchronized void enable() {
        switch (getState()) {
            case FOUND:
            case DISABLED:
                setState(DevicePluginState.ENABLED);
                try {
                    mConnection.connect();
                    mLogger.info("Connected to the plug-in: " + getPackageName());
                } catch (ConnectingException e) {
                    mLogger.warning("Failed to connect to the plug-in: " + getPackageName());
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    public synchronized void send(final Intent message) throws MessagingException {
        if (DevicePluginState.ENABLED != getState()) {
            throw new MessagingException(MessagingException.Reason.NOT_ENABLED);
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
}
