/*
 SmartDevice.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.uiapp.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Device Connect スマートサービス.
 */
public class DCService implements Parcelable {

    /**
     * サービス名.
     */
    private String mName;

    /**
     * サービス種別.
     */
    private String mType;

    /**
     * サービスID.
     */
    private String mId;

    /**
     * オンラインフラグ.
     */
    private boolean mOnline;

    /**
     * サービスリスト.
     */
    private List<DCProfile> mServiceList = new ArrayList<>();

    /**
     * コンストラクタ.
     * @param id サービスID
     * @param name サービス名
     */
    public DCService(final String id, final String name) {
        setId(id);
        setName(name);
    }

    /**
     * Parcelableコンストラクタ.
     * @param in 入力
     */
    private DCService(final Parcel in) {
        setName(in.readString());
        setType(in.readString());
        setId(in.readString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(mName);
        dest.writeString(mType);
        dest.writeString(mId);
    }

    @Override
    public String toString() {
        return mName;
    }

    /**
     * サービスIDを設定する.
     * @param id サービスID
     */
    public void setId(final String id) {
        mId = id;
    }

    /**
     * サービス名を設定する.
     * @param name サービス名
     */
    public void setName(final String name) {
        mName = name;
    }

    /**
     * サービス種別を設定する.
     * @param type サービス種別
     */
    public void setType(final String type) {
        mType = type;
    }

    /**
     * サービス名.
     * @return サービス名
     */
    public String getName() {
        return mName;
    }

    /**
     * サービス種別を取得する.
     * @return サービス種別
     */
    public String getType() {
        return mType;
    }

    /**
     * サービスIDを取得する.
     * @return サービスID
     */
    public String getId() {
        return mId;
    }

    /**
     * サービスの接続状態を取得する.
     * @return サービスの接続状態
     */
    public boolean isOnline() {
        return mOnline;
    }

    /**
     * サービスの接続状態を設定する.
     * @param online サービスの接続状態
     */
    public void setOnline(boolean online) {
        mOnline = online;
    }

    /**
     * サービスリストを取得する.
     * @return サービスリスト
     */
    public List<DCProfile> getServiceList() {
        return mServiceList;
    }

    /**
     * サービスを追加する.
     * @param service サービス
     */
    public void addService(final DCProfile service) {
        mServiceList.add(service);
    }

    /**
     * サービスを削除する.
     * @param service サービス
     */
    public void removeService(final DCProfile service) {
        mServiceList.remove(service);
    }

    /**
     * Parcelableクリエイター.
     */
    public static final Parcelable.Creator<DCService> CREATOR = new Parcelable.Creator<DCService>() {
        @Override
        public DCService createFromParcel(final Parcel in) {
            return new DCService(in);
        }
        @Override
        public DCService[] newArray(final int size) {
            return new DCService[size];
        }
    };
}
