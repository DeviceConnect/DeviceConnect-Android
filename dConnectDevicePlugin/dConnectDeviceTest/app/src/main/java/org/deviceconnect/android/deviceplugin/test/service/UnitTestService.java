/*
 UnitTestService.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.test.service;


import android.content.Intent;

import org.deviceconnect.android.deviceplugin.test.profile.unique.TestJSONConversionProfile;
import org.deviceconnect.android.deviceplugin.test.profile.unique.TestUniqueProfile;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.spec.DConnectApiSpec;
import org.deviceconnect.android.profile.spec.DConnectPluginSpec;
import org.deviceconnect.android.profile.spec.DConnectProfileSpec;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.impl.client.DefaultIntentClient;

import java.util.Map;

/**
 * ユニットテスト用サービス.
 * <p>
 * 各プロファイルの疎通テストに使用する.
 * </p>
 * @author NTT DOCOMO, INC.
 */
public class UnitTestService extends DConnectService {

    private static final long DEFAULT_DELAY = 500;

    public UnitTestService(final String id, final String name, final DConnectPluginSpec pluginSpec) {
        super(id);
        setName(name);
        setOnline(true);

        Map<String, DConnectProfileSpec> profileSpecs = pluginSpec.getProfileSpecs();
        for (Map.Entry<String, DConnectProfileSpec> entry : profileSpecs.entrySet()) {
            final String profileName = entry.getKey();
            final DConnectProfileSpec profileSpec = entry.getValue();
            final DConnectProfile profile = new DConnectProfile() {
                @Override
                public String getProfileName() {
                    return profileName;
                }
            };
            for (final DConnectApiSpec apiSpec : profileSpec.getApiSpecList()) {
                DConnectApi api = new DConnectApi() {
                    @Override
                    public Method getMethod() {
                        return apiSpec.getMethod();
                    }

                    @Override
                    public String getInterface() {
                        return apiSpec.getInterfaceName();
                    }

                    @Override
                    public String getAttribute() {
                        return apiSpec.getAttributeName();
                    }

                    @Override
                    public boolean onRequest(final Intent request, final Intent response) {
                        DConnectProfile.setResult(response, DConnectMessage.RESULT_OK);
                        if (apiSpec.getType() == Type.EVENT) {
                            // 空のイベントを送信.
                            String sessionKey = DConnectProfile.getSessionKey(request);
                            String serviceId = DConnectProfile.getServiceID(request);
                            Intent message = MessageUtils.createEventIntent();
                            DConnectProfile.setSessionKey(message, sessionKey);
                            DConnectProfile.setServiceID(message, serviceId);
                            DConnectProfile.setProfile(message, profileName);
                            DConnectProfile.setAttribute(message, getAttribute());
                            sendBroadcast(message);
                        }
                        return true;
                    }
                };
                profile.addApi(api);
            }
        }

        addProfile(new TestUniqueProfile());
        addProfile(new TestJSONConversionProfile());
    }

    /**
     * 指定したミリ秒後に別スレッドでインテントをブロードキャストする.
     *
     * @param intent インテント
     * @param delay 遅延設定 (単位はミリ秒)
     */
    public void sendBroadcast(final Intent intent, final long delay) {
        (new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(delay);
                    intent.setComponent(DefaultIntentClient.DEFAULT_MESSAGE_RECEIVER);
                    getContext().sendBroadcast(intent);
                } catch (InterruptedException e) {
                    // do nothing.
                }
            }
        })).start();
    }

    /**
     * {@value #DEFAULT_DELAY}ミリ秒後に別スレッドでインテントをブロードキャストする.
     *
     * @param intent インテント
     */
    public void sendBroadcast(final Intent intent) {
        sendBroadcast(intent, DEFAULT_DELAY);
    }
}
