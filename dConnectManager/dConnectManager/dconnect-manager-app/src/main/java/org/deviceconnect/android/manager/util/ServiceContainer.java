package org.deviceconnect.android.manager.util;

import android.graphics.drawable.Drawable;

import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;

/**
 * サービスの情報を格納するコンテナ.
 */
public class ServiceContainer {
    /**
     * サービスID.
     */
    private String mId;

    /**
     * サービス名.
     */
    private String mName;

    /**
     * ネットワークタイプ.
     */
    private ServiceDiscoveryProfileConstants.NetworkType mNetworkType;

    /**
     * オンライン状態.
     */
    private boolean online;

    /**
     * サポートプロファイル.
     */
    private String[] supports;

    /**
     * アイコン.
     */
    private Drawable mDrawable;

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public ServiceDiscoveryProfileConstants.NetworkType getNetworkType() {
        return mNetworkType;
    }

    public void setNetworkType(ServiceDiscoveryProfileConstants.NetworkType networkType) {
        mNetworkType = networkType;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String[] getSupports() {
        return supports;
    }

    public void setSupports(String[] supports) {
        this.supports = supports;
    }

    public Drawable getDrawable() {
        return mDrawable;
    }

    public void setDrawable(Drawable drawable) {
        mDrawable = drawable;
    }

    @Override
    public String toString() {
        return "{\n    id=" + getId() + "\n    name=" + getName() + "\n    " + getNetworkType() + "\n}";
    }
}
