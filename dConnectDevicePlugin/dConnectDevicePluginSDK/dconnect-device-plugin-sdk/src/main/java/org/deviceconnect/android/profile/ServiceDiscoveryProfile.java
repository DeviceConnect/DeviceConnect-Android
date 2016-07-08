/*
 ServiceDiscoveryProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.android.service.DConnectServiceProvider;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Service Discovery プロファイル.
 * 
 * <p>
 * Device Connectサービス検索機能を提供するAPI.<br>
 * </p>
 * @author NTT DOCOMO, INC.
 */
public class ServiceDiscoveryProfile extends DConnectProfile implements
        ServiceDiscoveryProfileConstants {

    /**
     * プロファイルプロバイダー.
     */
    private final DConnectServiceProvider mProvider;

    /**
     * Service Discovery API.
     */
    private final DConnectApi mServiceDiscoveryApi = new GetApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            appendServiceList(response);
            return true;
        }
    };

    /**
     * 指定されたサービスプロバイダーをもつSystemプロファイルを生成する.
     * 
     * @param provider サービスプロバイダー
     */
    public ServiceDiscoveryProfile(final DConnectServiceProvider provider) {
        this.mProvider = provider;
        addApi(mServiceDiscoveryApi);
    }

    /**
     * サービスプロバイダーを取得する.
     * 
     * @return サービスプロバイダー
     */
    protected DConnectServiceProvider getServiceProvider() {
        return mProvider;
    }

    @Override
    public final String getProfileName() {
        return PROFILE_NAME;
    }

    protected void appendServiceList(final Intent response) {
        List<Bundle> serviceBundles = new ArrayList<Bundle>();
        for (DConnectService service : mProvider.getServiceList()) {
            Bundle serviceBundle = new Bundle();
            setId(serviceBundle, service.getId());
            setName(serviceBundle, service.getName());
            setType(serviceBundle, service.getNetworkType());
            setOnline(serviceBundle, service.isOnline());
            setConfig(serviceBundle, service.getConfig());
            setScopes(serviceBundle, service);
            serviceBundles.add(serviceBundle);
        }
        setServices(response, serviceBundles);
        setResult(response, DConnectMessage.RESULT_OK);
    }

    // TODO Status Change Event APIの実装.

    // ------------------------------------
    // レスポンスセッターメソッド群
    // ------------------------------------

    /**
     * レスポンスにデバイス一覧を設定する.
     * 
     * @param response レスポンスパラメータ
     * @param services デバイス一覧
     */
    public static void setServices(final Intent response, final Bundle[] services) {
        response.putExtra(PARAM_SERVICES, services);
    }

    /**
     * レスポンスにデバイス一覧を設定する.
     * 
     * @param response レスポンスパラメータ
     * @param services デバイス一覧
     */
    public static void setServices(final Intent response, final List<Bundle> services) {
        setServices(response, services.toArray(new Bundle[services.size()]));
    }

    /**
     * メッセージにデバイス情報を設定する.
     * 
     * @param message メッセージパラメータ
     * @param networkService デバイス情報
     */
    public static void setNetworkService(final Intent message, final Bundle networkService) {
        message.putExtra(PARAM_NETWORK_SERVICE, networkService);
    }

    /**
     * サービスIDを設定する.
     * 
     * @param service デバイスパラメータ
     * @param id サービスID
     */
    public static void setId(final Bundle service, final String id) {
        service.putString(PARAM_ID, id);
    }

    /**
     * デバイス名を設定する.
     * 
     * @param service デバイスパラメータ
     * @param name デバイス名
     */
    public static void setName(final Bundle service, final String name) {
        service.putString(PARAM_NAME, name);
    }

    /**
     * デバイスのネットワークタイプを設定する.
     * 
     * @param service デバイスパラメータ
     * @param type デバイスのネットワークタイプ
     *            <ul>
     *            <li>{@link NetworkType#WIFI}</li>
     *            <li>{@link NetworkType#BLE}</li>
     *            <li>{@link NetworkType#NFC}</li>
     *            <li>{@link NetworkType#BLUETOOTH}</li>
     *            </ul>
     */
    public static void setType(final Bundle service, final NetworkType type) {
        setType(service, type.getValue());
    }
    
    /**
     * デバイスのネットワークタイプを設定する.
     * 
     * @param service デバイスパラメータ
     * @param type デバイスのネットワークタイプ
     */
    public static void setType(final Bundle service, final String type) {
        service.putString(PARAM_TYPE, type);
    }

    /**
     * デバイスのオンライン状態を設定する.
     * 
     * @param service デバイスパラメータ
     * @param online オンライン: true、 オフライン: false
     */
    public static void setOnline(final Bundle service, final boolean online) {
        service.putBoolean(PARAM_ONLINE, online);
    }

    /**
     * デバイスの設定情報を設定する.
     * 
     * @param service デバイスパラメータ
     * @param config 設定情報文字列
     */
    public static void setConfig(final Bundle service, final String config) {
        service.putString(PARAM_CONFIG, config);
    }

    /**
     * デバイスの接続状態を設定する.
     * 
     * @param service デバイスパラメータ
     * @param state 接続 : true、未接続 : false
     */
    public static void setState(final Bundle service, final boolean state) {
        service.putBoolean(PARAM_STATE, state);
    }

    /**
     * デバイスプラグインのサポートするプロファイル一覧を設定する.
     * 
     * @param service デバイスパラメータ
     */
    public static void setScopes(final Bundle serviceBundle, final DConnectService service) {
        ArrayList<String> scopes = new ArrayList<String>();
        List<DConnectProfile> profileList = service.getProfileList();
        for (DConnectProfile profile : profileList) {
            scopes.add(profile.getProfileName());
        }
        serviceBundle.putStringArray(PARAM_SCOPES, scopes.toArray(new String[scopes.size()]));
    }

}
