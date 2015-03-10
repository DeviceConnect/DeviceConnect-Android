/*
 NormalAvailabilityProfileTestCase.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.intent.test;


/**
 * Availabilityプロファイルの正常系テスト.
 * @author NTT DOCOMO, INC.
 */
public class NormalAvailabilityProfileTestCase extends IntentDConnectTestCase {
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
     * 【Intent通信】
     * Action: GET
     * Profile: availability
     * Interface: なし
     * Attribute: なし
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
