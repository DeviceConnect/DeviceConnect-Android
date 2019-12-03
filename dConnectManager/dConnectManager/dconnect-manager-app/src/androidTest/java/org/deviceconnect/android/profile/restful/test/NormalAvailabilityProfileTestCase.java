/*
 NormalAvailabilityProfileTestCase.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectResponseMessage;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Availabilityプロファイルの正常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class NormalAvailabilityProfileTestCase extends RESTfulDConnectTestCase {

    @Override
    protected boolean isLocalOAuth() {
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
    @Test
    public void testGetAvailability() throws Exception {
        String uri = "http://localhost:4035/gotapi/availability";

        DConnectResponseMessage response = mDConnectSDK.get(uri);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }
}
