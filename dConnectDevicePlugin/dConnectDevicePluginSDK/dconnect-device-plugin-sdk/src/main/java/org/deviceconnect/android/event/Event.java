/*
 Event.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.event;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * イベントデータクラス.
 * 
 *
 * @author NTT DOCOMO, INC.
 */
public class Event implements Serializable {

    /** 
     * シリアルバージョン.
     */
    private static final long serialVersionUID = 2249802839155396087L;
    
    /** 
     * プロファイル.
     */
    private String mProfile;
    
    /** 
     * インターフェース.
     */
    private String mInterface;
    
    /** 
     * 属性.
     */
    private String mAttribute;
    
    /**
     * サービスID.
     */
    private String mServiceId;
    
    /**
     * アクセストークン.
     */
    private String mAccessToken;
    
    /** 
     * オリジン.
     */
    private String mOrigin;
    
    /**
     * レシーバーのパッケージ名.
     */
    private String mReceiverName;
    
    /** 
     * 登録日.
     */
    private Timestamp mCreateDate;
    
    /** 
     * 更新日.
     */
    private Timestamp mUpdateDate;

    /**
     * プロファイルを取得する.
     * 
     * @return プロファイル名
     */
    public String getProfile() {
        return mProfile;
    }

    /**
     * プロファイルを設定する.
     * 
     * @param profile プロファイル
     */
    public void setProfile(final String profile) {
        this.mProfile = profile;
    }

    /**
     * 属性を取得する.
     * 
     * @return 属性名
     */
    public String getAttribute() {
        return mAttribute;
    }

    /**
     * 属性を設定する.
     * 
     * @param attribute 属性.
     */
    public void setAttribute(final String attribute) {
        this.mAttribute = attribute;
    }

    /**
     * サービスIDを取得する.
     * 
     * @return サービスID
     */
    public String getServiceId() {
        return mServiceId;
    }

    /**
     * サービスIDを設定する.
     * 
     * @param serviceId サービスID
     */
    public void setServiceId(final String serviceId) {
        this.mServiceId = serviceId;
    }

    /**
     * アクセストークンを取得する.
     * 
     * @return アクセストークン
     */
    public String getAccessToken() {
        return mAccessToken;
    }

    /**
     * アクセストークンを設定する.
     * 
     * 
     * @param accessToken アクセストークン
     */
    public void setAccessToken(final String accessToken) {
        this.mAccessToken = accessToken;
    }

    /**
     * インターフェースを取得する.
     * 
     * @return インターフェース
     */
    public String getInterface() {
        return mInterface;
    }

    /**
     * インターフェースを設定する.
     * 
     * @param inter インターフェース
     */
    public void setInterface(final String inter) {
        this.mInterface = inter;
    }
    
    /**
     * オリジンを取得する.
     * 
     * @return オリジン
     */
    public String getOrigin() {
        return mOrigin;
    }
    
    /**
     * オリジンを設定する.
     * 
     * @param origin オリジン
     */
    public void setOrigin(final String origin) {
        this.mOrigin = origin;
    }
    
    /**
     * レシーバーのパッケージ名を返す.
     * @return レシーバーのパッケージ名
     */
    public String getReceiverName() {
        return mReceiverName;
    }
    
    /**
     * レシーバーのパッケージ名を設定します.
     * 
     * @param receiverName レシーバーのパッケージ名
     */
    public void setReceiverName(final String receiverName) {
        mReceiverName = receiverName;
    }
    
    /**
     * 登録日を取得する.
     * 
     * @return 登録日
     */
    public Timestamp getCreateDate() {
        return mCreateDate;
    }
    
    /**
     * 登録日を設定する.
     * 
     * @param createDate 登録日
     */
    public void setCreateDate(final Timestamp createDate) {
        mCreateDate = createDate;
    }
    
    /**
     * 更新日を取得する.
     * 
     * @return 登録日
     */
    public Timestamp getUpdateDate() {
        return mUpdateDate;
    }
    
    /**
     * 更新日を設定する.
     * 
     * @param updateDate 更新日
     */
    public void setUpdateDate(final Timestamp updateDate) {
        mUpdateDate = updateDate;
    }
    
    @Override
    public String toString() {
        StringBuilder to = new StringBuilder();
        to.append("[profile = ");
        to.append(mProfile);
        to.append(", interface = ");
        to.append(mInterface);
        to.append(", attribute = ");
        to.append(mAttribute);
        to.append(", serviceId = ");
        to.append(mServiceId);
        to.append(", sessionKey = ");
        to.append(mOrigin);
        to.append(", receiverName = ");
        to.append(mReceiverName);
        to.append(", accessToken = ");
        to.append(mAccessToken);
        to.append(", createDate = ");
        if (mCreateDate != null) {
            to.append(mCreateDate.toString());
        } else {
            to.append("null");
        }
        to.append(", updateDate = ");
        if (mUpdateDate != null) {
            to.append(mUpdateDate.toString());
        } else {
            to.append("null");
        }
        to.append("]");
        return to.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        if (mProfile != null ? !mProfile.equals(event.mProfile) : event.mProfile != null)
            return false;
        if (mInterface != null ? !mInterface.equals(event.mInterface) : event.mInterface != null)
            return false;
        if (mAttribute != null ? !mAttribute.equals(event.mAttribute) : event.mAttribute != null)
            return false;
        if (mServiceId != null ? !mServiceId.equals(event.mServiceId) : event.mServiceId != null)
            return false;
        if (mAccessToken != null ? !mAccessToken.equals(event.mAccessToken) : event.mAccessToken != null)
            return false;
        if (mOrigin != null ? !mOrigin.equals(event.mOrigin) : event.mOrigin != null) return false;
        if (mReceiverName != null ? !mReceiverName.equals(event.mReceiverName) : event.mReceiverName != null)
            return false;
        if (mCreateDate != null ? !mCreateDate.equals(event.mCreateDate) : event.mCreateDate != null)
            return false;
        return mUpdateDate != null ? mUpdateDate.equals(event.mUpdateDate) : event.mUpdateDate == null;

    }

    @Override
    public int hashCode() {
        int result = mProfile != null ? mProfile.hashCode() : 0;
        result = 31 * result + (mInterface != null ? mInterface.hashCode() : 0);
        result = 31 * result + (mAttribute != null ? mAttribute.hashCode() : 0);
        result = 31 * result + (mServiceId != null ? mServiceId.hashCode() : 0);
        result = 31 * result + (mAccessToken != null ? mAccessToken.hashCode() : 0);
        result = 31 * result + (mOrigin != null ? mOrigin.hashCode() : 0);
        result = 31 * result + (mReceiverName != null ? mReceiverName.hashCode() : 0);
        result = 31 * result + (mCreateDate != null ? mCreateDate.hashCode() : 0);
        result = 31 * result + (mUpdateDate != null ? mUpdateDate.hashCode() : 0);
        return result;
    }
}
