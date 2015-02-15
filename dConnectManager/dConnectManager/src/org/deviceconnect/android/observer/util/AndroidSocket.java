/*
 AndroidSocket.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.observer.util;


/**
 * Android上のSocket情報を持つクラス.
 * @author NTT DOCOMO, INC.
 */
public class AndroidSocket {
    /**
     * アプリ名.
     */
    private String mAppName;

    /**
     * ローカルIPアドレス.
     */
    private String mLocalAddress;

    /**
     * ローカルポート.
     */
    private int mLocalPort;

    /**
     * リモートIPアドレス.
     */
    private String mRemoteAddress;

    /**
     * リモートポート.
     */
    private int mRemotePort;

    /**
     * Socketの状態.
     */
    private SocketState mState;

    /**
     * Socketのタイプ.
     */
    private SocketType mType;

    /**
     * UID.
     */
    private int mUid;

    /**
     * コンストラクタ.
     */
    public AndroidSocket() { }

    /**
     * アプリ名を取得する.
     * @return アプリ名
     */
    public String getAppName() {
        return mAppName;
    }

    /**
     * ローカルIPアドレスを取得する.
     * @return ローカルIPアドレス
     */
    public String getLocalAddress() {
        return mLocalAddress;
    }

    /**
     * ローカルポートを取得する.
     * @return ローカルポート
     */
    public int getLocalPort() {
        return mLocalPort;
    }

    /**
     * リモートIPアドレスを取得する.
     * @return リモートIPアドレス
     */
    public String getRemoteAddress() {
        return mRemoteAddress;
    }

    /**
     * リモートポートを取得する.
     * @return リモートポート
     */
    public int getRemotePort() {
        return mRemotePort;
    }

    /**
     * Socketの状態を取得する.
     * @return Socketの状態
     */
    public SocketState getState() {
        return mState;
    }

    /**
     * Socketタイプを取得する.
     * @return Socketタイプ
     */
    public SocketType getType() {
        return mType;
    }

    /**
     * UIDを取得する.
     * @return UID
     */
    public int getUid() {
        return mUid;
    }

    /**
     * アプリ名を設定する.
     * @param name アプリ名
     */
    public void setAppName(final String name) {
        mAppName = name;
    }

    /**
     * ローカルIPアドレスを設定する.
     * @param ip ローカルIPアドレス 
     */
    public void setLocalAddress(final String ip) {
        mLocalAddress = ip;
    }

    /**
     * ローカルポートを設定する.
     * @param port ローカルポート
     */
    public void setLocalPort(final int port) {
        mLocalPort = port;
    }

    /**
     * リモートIPアドレスを設定する.
     * @param ip リモートIPアドレス
     */
    public void setRemoteAddress(final String ip) {
        mRemoteAddress = ip;
    }

    /**
     * リモートポートを設定する.
     * @param port リモートポート
     */
    public void setRemotePort(final int port) {
        mRemotePort = port;
    }

    /**
     * Socketの状態を設定する.
     * @param socketstate Socketの状態
     */
    public void setState(final SocketState socketstate) {
        mState = socketstate;
    }

    /**
     * Socketタイプを設定する.
     * @param sockettype Socketタイプ
     */
    public void setType(final SocketType sockettype) {
        mType = sockettype;
    }

    /**
     * UIDを設定する.
     * @param id UID
     */
    public void setUid(final int id) {
        mUid = id;
    }

    @Override
    public String toString() {
        return (new StringBuilder("*")).append(mLocalAddress).append(":")
                .append(mLocalPort).append(" ").append(mRemoteAddress).append(":")
                .append(mRemotePort).append(" ").append(mState.toString()).append(" ")
                .append(mUid).append("/").append(mAppName).toString();
    }


}
