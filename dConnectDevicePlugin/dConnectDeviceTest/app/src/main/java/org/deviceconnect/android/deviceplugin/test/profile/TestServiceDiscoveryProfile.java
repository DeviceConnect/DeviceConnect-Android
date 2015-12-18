/*
 TestServiceDiscoveryProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.test.profile;

import java.util.ArrayList;
import java.util.List;

import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.message.DConnectMessage;

import android.content.Intent;
import android.os.Bundle;

/**
 * JUnit用テストデバイスプラグイン、ServiceDiscoveryプロファイル.
 * @author NTT DOCOMO, INC.
 */
public class TestServiceDiscoveryProfile extends ServiceDiscoveryProfile {

    /**
     * コンストラクタ.
     * 
     * @param provider プロファイルプロバイダ
     */
    public TestServiceDiscoveryProfile(final DConnectProfileProvider provider) {
        super(provider);
    }

    /**
     * テスト用サービスID.
     */
    public static final String SERVICE_ID = "test_service_id";

    /**
     * 特殊文字を含むテスト用サービスID.
     */
    public static final String SERVICE_ID_SPECIAL_CHARACTERS = "!#$'()-~¥@[;+:*],._/=?&%^|`\"{}<>";

    /**
     * テスト用デバイス名: {@value} .
     */
    public static final String DEVICE_NAME = "Test Success Device";

    /**
     * テスト用デバイス名: {@value} .
     */
    public static final String DEVICE_NAME_SPECIAL_CHARACTERS = "Test Service ID Special Characters";

    /**
     * テスト用デバイスタイプ.
     */
    public static final String DEVICE_TYPE = "TEST";

    /**
     * テスト用オンライン状態.
     */
    public static final boolean DEVICE_ONLINE = true;

    /**
     * テスト用コンフィグ.
     */
    public static final String DEVICE_CONFIG = "test config";

    /**
     * セッションキーが空の場合のエラーを作成する.
     * @param response レスポンスを格納するIntent
     */
    private void createEmptySessionKey(final Intent response) {
        MessageUtils.setInvalidRequestParameterError(response);
    }

    @Override
    protected boolean onGetServices(final Intent request, final Intent response) {
        List<Bundle> services = new ArrayList<Bundle>();

        // 典型的なサービス
        Bundle service = new Bundle();
        setId(service, SERVICE_ID);
        setName(service, DEVICE_NAME);
        setType(service, DEVICE_TYPE);
        setOnline(service, DEVICE_ONLINE);
        setConfig(service, DEVICE_CONFIG);
        setScopes(service, getProfileProvider());
        services.add(service);

        // サービスIDが特殊なサービス
        service = new Bundle();
        setId(service, SERVICE_ID_SPECIAL_CHARACTERS);
        setName(service, DEVICE_NAME_SPECIAL_CHARACTERS);
        setType(service, DEVICE_TYPE);
        setOnline(service, DEVICE_ONLINE);
        setConfig(service, DEVICE_CONFIG);
        setScopes(service, getProfileProvider());
        services.add(service);

        setResult(response, DConnectMessage.RESULT_OK);
        setServices(response, services);
        
        return true;
    }

    @Override
    protected boolean onPutOnServiceChange(final Intent request, final Intent response,
                                            final String serviceId, final String sessionKey) {
        
        if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);

            Intent message = MessageUtils.createEventIntent();
            setSessionKey(message, sessionKey);
            setServiceID(message, serviceId);
            setProfile(message, getProfileName());
            setAttribute(message, ATTRIBUTE_ON_SERVICE_CHANGE);
            
            Bundle service = new Bundle();
            setId(service, SERVICE_ID);
            setName(service, DEVICE_NAME);
            setType(service, DEVICE_TYPE);
            setOnline(service, DEVICE_ONLINE);
            setConfig(service, DEVICE_CONFIG);
            
            setNetworkService(message, service);
            
            Util.sendBroadcast(getContext(), message);
        }
        
        return true;
    }

    @Override
    protected boolean onDeleteOnServiceChange(final Intent request, final Intent response,
                                                final String serviceId, final String sessionKey) {
        if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }
}
