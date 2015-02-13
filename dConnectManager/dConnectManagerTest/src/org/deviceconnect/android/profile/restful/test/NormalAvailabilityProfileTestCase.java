/*
 NormalAvailabilityProfileTestCase.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;


/**
 * Availabilityプロファイルの正常系テスト.
 * @author NTT DOCOMO, INC.
 */
public class NormalAvailabilityProfileTestCase extends RESTfulDConnectTestCase {
    /**
     * コンストラクタ.
     * @param string テストタグ
     */
    public NormalAvailabilityProfileTestCase(final String string) {
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

    /**
     * サーバ起動確認テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /availability
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    public void testGetAvailability() {
        assertTrue(isManagerAvailable());
    }

}
