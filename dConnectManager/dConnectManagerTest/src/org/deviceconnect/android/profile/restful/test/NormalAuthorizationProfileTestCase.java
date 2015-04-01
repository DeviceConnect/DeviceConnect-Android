/*
 NormalAuthorizationProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import org.deviceconnect.profile.BatteryProfileConstants;
import org.deviceconnect.profile.ConnectProfileConstants;
import org.deviceconnect.profile.DeviceOrientationProfileConstants;
import org.deviceconnect.profile.NotificationProfileConstants;

/**
 * Authorizationプロファイルの正常系テスト.
 * @author NTT DOCOMO, INC.
 */
public class NormalAuthorizationProfileTestCase extends RESTfulDConnectTestCase {

    /**
     * コンストラクタ.
     * @param string テストタグ
     */
    public NormalAuthorizationProfileTestCase(final String string) {
        super(string);
    }

    @Override
    protected boolean isLocalOAuth() {
        return false;
    }

    @Override
    protected boolean isSearchDevices() {
        return false;
    }

    @Override
    protected String getOrigin() {
        return "abc";
    }

    /**
     * クライアント作成テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /authorization/grant
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・clientIdにstring型の値が返ること。
     * </pre>
     */
    public void testCreateClient() {
        String clientId = createClient();
        assertNotNull(clientId);
    }

    /**
     * クライアント作成済みのパッケージについてクライアントを作成し直すテストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /authorization/grant
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・異なるclientIdが返ること。
     * </pre>
     */
    public void testCreateClientOverwrite() {
        String clientId = createClient();
        assertNotNull(clientId);
        String newClientId = createClient();
        assertNotNull(newClientId);
        assertFalse(newClientId.equals(clientId));
    }

    /**
     * アクセストークン取得テストを行う.
     * 1つのスコープを指定する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /authorization/accesstoken?
     *           clientId=xxxx&scope=xxxx&applicationName=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・クライアント作成に成功すること。
     * ・アクセストークン取得に成功すること。
     * </pre>
     */
    public void testRequestAccessToken() {
        String clientId = createClient();
        assertNotNull(clientId);

        String accessToken = requestAccessToken(clientId,
                new String[] {NotificationProfileConstants.PROFILE_NAME});
        assertNotNull(accessToken);
    }

    /**
     * アクセストークン取得テストを行う.
     * 複数のスコープを指定する.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /authorization/accesstoken?
     *           clientId=xxxx&scope=xxxx&applicationName=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・クライアント作成に成功すること。
     * ・アクセストークン取得に成功すること。
     * </pre>
     */
    public void testRequestAccessTokenMultiScope() {
        String clientId = createClient();
        assertNotNull(clientId);
        
        String accessToken = requestAccessToken(clientId, new String[] {
                BatteryProfileConstants.PROFILE_NAME,
                ConnectProfileConstants.PROFILE_NAME,
                DeviceOrientationProfileConstants.PROFILE_NAME
        });
        assertNotNull(accessToken);
    }
}
