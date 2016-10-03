/*
 ServiceDiscoveryProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.android.service.DConnectServiceListener;
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
        ServiceDiscoveryProfileConstants, DConnectServiceListener {

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
     * Status Change Event API (PUT).
     */
    private final DConnectApi mPutStatusChangeApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_SERVICE_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.addEvent(request);
            switch (error) {
                case NONE:
                    setResult(response, DConnectMessage.RESULT_OK);
                    break;
                case INVALID_PARAMETER:
                    MessageUtils.setInvalidRequestParameterError(response);
                    break;
                default:
                    MessageUtils.setUnknownError(response);
                    break;
            }
            return true;
        }
    };

    /**
     * Status Change Event API (DELETE).
     */
    private final DConnectApi mDeleteStatusChangeApi = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_SERVICE_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.removeEvent(request);
            switch (error) {
                case NONE:
                    setResult(response, DConnectMessage.RESULT_OK);
                    break;
                case INVALID_PARAMETER:
                    MessageUtils.setInvalidRequestParameterError(response);
                    break;
                default:
                    MessageUtils.setUnknownError(response);
                    break;
            }
            return true;
        }
    };

    /**
     * 指定されたサービスプロバイダーをもつSystemプロファイルを生成する.
     * 
     * @param provider サービスプロバイダー
     */
    public ServiceDiscoveryProfile(final DConnectServiceProvider provider) {
        mProvider = provider;
        if (mProvider != null) {
            mProvider.addServiceListener(this);
        }
        addApi(mServiceDiscoveryApi);
        addApi(mPutStatusChangeApi);
        addApi(mDeleteStatusChangeApi);
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

    @Override
    public void onServiceAdded(final DConnectService service) {
        sendStatusChangeEvent(service, true);
    }

    @Override
    public void onServiceRemoved(final DConnectService service) {
        sendStatusChangeEvent(service, false);
    }

    @Override
    public void onStatusChange(final DConnectService service) {
        sendStatusChangeEvent(service, true);
    }

    private void sendStatusChangeEvent(final DConnectService service, final boolean exists) {
        List<Event> events = EventManager.INSTANCE.getEventList(
            PROFILE_NAME,
            ATTRIBUTE_ON_SERVICE_CHANGE
        );
        if (events.size() == 0) {
            return;
        }

        Bundle eventBundle = new Bundle();
        Bundle networkServiceBundle = createServiceBundle(service);
        setState(networkServiceBundle, exists);
        eventBundle.putParcelable(PARAM_NETWORK_SERVICE, networkServiceBundle);
        // NOTE: Service DiscoveryプロファイルのリクエストにはサービスIDが付加されないので、
        // イベントマネージャのイベント管理テーブルにも保存されない。
        // よって、イベント送信時に、下記のようにサービスIDを明示的に設定する必要がある。
        eventBundle.putString(PARAM_SERVICE_ID, service.getId());
        for (Event event : events) {
            sendEvent(event, eventBundle);
        }
    }

    private Bundle createServiceBundle(final DConnectService service) {
        Bundle serviceBundle = new Bundle();
        setId(serviceBundle, service.getId());
        setName(serviceBundle, service.getName());
        setType(serviceBundle, service.getNetworkType());
        setOnline(serviceBundle, service.isOnline());
        setConfig(serviceBundle, service.getConfig());
        setScopes(serviceBundle, service);
        return serviceBundle;
    }

    protected void appendServiceList(final Intent response) {
        List<Bundle> serviceBundles = new ArrayList<Bundle>();
        for (DConnectService service : mProvider.getServiceList()) {
            serviceBundles.add(createServiceBundle(service));
        }
        setServices(response, serviceBundles);
        setResult(response, DConnectMessage.RESULT_OK);
    }

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
