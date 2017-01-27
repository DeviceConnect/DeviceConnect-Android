/*
 DConnectService.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.service;


import android.content.Context;
import android.content.Intent;

import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceInformationProfile;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Device Connect APIサービス.
 *
 * @author NTT DOCOMO, INC.
 */
public class DConnectService implements DConnectProfileProvider, ServiceDiscoveryProfileConstants {

    /**
     * サービスID.
     */
    private final String mId;

    /**
     * サポートするプロファイル一覧.
     */
    private final Map<String, DConnectProfile> mProfiles = new HashMap<String, DConnectProfile>();

    /**
     * サービス名.
     */
    private String mName;

    /**
     * サービスタイプ.
     */
    private String mType;

    /**
     * オンラインフラグ.
     */
    private boolean mIsOnline;

    /**
     * サービスのコンフィグ.
     */
    private String mConfig;

    /**
     * コンテキスト.
     */
    private Context mContext;

    /**
     * ステータス更新通知リスナー.
     */
    private OnStatusChangeListener mStatusListener;

    /**
     * コンストラクタ.
     * @param id サービスID
     * @throws NullPointerException idに<code>null</code>が指定された場合
     */
    public DConnectService(final String id) {
        if (id == null) {
            throw new NullPointerException("id is null.");
        }
        mId = id;
        addProfile(new ServiceInformationProfile());
    }

    /**
     * サービスIDを取得する.
     * @return サービスID
     */
    public String getId() {
        return mId;
    }

    /**
     * サービス名を設定する.
     *
     * @param name サービス名
     */
    public void setName(final String name) {
        mName = name;
    }

    /**
     * サービス名を取得する.
     *
     * @return サービス名.
     */
    public String getName() {
        return mName;
    }

    /**
     * サービスのネットワークタイプを設定する.
     *
     * @param type ネットワークタイプ
     */
    public void setNetworkType(final NetworkType type) {
        mType = type.getValue();
    }

    /**
     * サービスのネットワークタイプを設定する.
     * <p>
     * {@link org.deviceconnect.profile.ServiceDiscoveryProfileConstants.NetworkType NetworkType}
     * に定義されていないタイプの場合には、このメソッドを使用して独自のネットワークタイプを設定することができる。
     * </p>
     * @param type ネットワークタイプ
     */
    public void setNetworkType(final String type) {
        mType = type;
    }

    /**
     * サービスのネットワークタイプを取得する.
     * @return ネットワークタイプ
     */
    public String getNetworkType() {
        return mType;
    }

    /**
     * ネットワークの状態を設定する.
     *
     * @param isOnline オンラインの場合はtrue、オフラインの場合はfalse
     */
    public void setOnline(final boolean isOnline) {
        mIsOnline = isOnline;

        if (mStatusListener != null) {
            mStatusListener.onStatusChange(this);
        }
    }

    /**
     * ネットワークの状態を取得する.
     * @return オンラインの場合はtrue、オフラインの場合はfalse
     */
    public boolean isOnline() {
        return mIsOnline;
    }

    /**
     * サービスのコンフィグを取得する.
     * <p>
     * コンフィグ情報が存在しない場合には{@code null}を返却する。
     * </p>
     * @return サービスのコンフィグ
     */
    public String getConfig() {
        return mConfig;
    }

    /**
     * サービスのコンフィグを設定する.
     * <p>
     * コンフィグ情報が存在しない場合には、{@code null}を設定する。<br>
     * デフォルトは、{@code null}が設定されている。
     * </p>
     * @param config コンフィグ情報
     */
    public void setConfig(final String config) {
        mConfig = config;
    }

    /**
     * コンテキストを設定する.
     * <p>
     * {@link DConnectServiceManager}に追加されるときにコンテキストが設定される。
     * </p>
     * @param context コンテキスト
     */
    void setContext(final Context context) {
        mContext = context;
    }

    /**
     * コンテキストを取得する.
     * @return
     */
    public Context getContext() {
        return mContext;
    }

    @Override
    public List<DConnectProfile> getProfileList() {
        List<DConnectProfile> list = new ArrayList<DConnectProfile>();
        for (DConnectProfile profile : mProfiles.values()) {
            list.add(profile);
        }
        return list;
    }

    @Override
    public DConnectProfile getProfile(final String name) {
        if (name == null) {
            return null;
        }
        return mProfiles.get(name.toLowerCase());
    }

    @Override
    public void addProfile(final DConnectProfile profile) {
        if (profile == null) {
            return;
        }
        profile.setService(this);
        mProfiles.put(profile.getProfileName().toLowerCase(), profile);
    }

    @Override
    public void removeProfile(final DConnectProfile profile) {
        if (profile == null) {
            return;
        }
        mProfiles.remove(profile.getProfileName().toLowerCase());
    }

    /**
     * サービスに命令が通知されたときに呼び出されるメソッド.
     * <p>
     * このメソッドの中でサービスに登録されている各プロファイルに命令を振り分ける。<br>
     * 各プロファイルでは、requestに対するレスポンスをresponseに格納する。
     * </p>
     * <p>
     * レスポンスにtrueが返却した場合には、Plugin SDKは、responseをDevice Connect Managerに返却する。<br>
     * falseの場合には、Plugin SDKは、responseをDevice Connect Managerに返却しません。プラグイン側で、
     * {@link org.deviceconnect.android.message.DConnectMessageService#sendResponse(Intent)}を用いて
     * レスポンスを返却する必要があります。
     * </p>
     * @param request リクエスト
     * @param response レスポンス
     * @return 同期的にレスポンスを返却する場合にはtrue、それ以外はfalse
     */
    public boolean onRequest(final Intent request, final Intent response) {
        DConnectProfile profile = getProfile(DConnectProfile.getProfile(request));
        if (profile == null) {
            MessageUtils.setNotSupportProfileError(response);
            return true;
        }
        return profile.onRequest(request, response);
    }

    /**
     * ステータス更新通知リスナーを設定する.
     *
     * @param listener リスナー
     */
    void setOnStatusChangeListener(final OnStatusChangeListener listener) {
        mStatusListener = listener;
    }

    /**
     * ステータス更新通知リスナー.
     *
     * @author NTT DOCOMO, INC.
     */
    interface OnStatusChangeListener {
        /**
         * ステータスが変更されたサービスを通知する.
         *
         * @param service ステータスが変更されたサービス
         */
        void onStatusChange(DConnectService service);
    }
}
