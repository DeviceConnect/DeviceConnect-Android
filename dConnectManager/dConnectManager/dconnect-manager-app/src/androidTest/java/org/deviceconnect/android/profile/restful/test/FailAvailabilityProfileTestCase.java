/*
 FailAvailabilityProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectResponseMessage;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Availabilityプロファイルの異常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class FailAvailabilityProfileTestCase extends RESTfulDConnectTestCase {

    @Override
    protected boolean isLocalOAuth() {
        return false;
    }

    /**
     * メソッドにPOSTを指定してサーバ起動確認テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /availability
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetAvailabilityInvalidMethodPost() {
        String uri = "http://localhost:4035/gotapi/availability";

        DConnectResponseMessage response = mDConnectSDK.post(uri, null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
    }

    /**
     * メソッドにPUTを指定してサーバ起動確認テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /availability
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetAvailabilityInvalidMethodPut() {
        String uri = "http://localhost:4035/gotapi/availability";

        DConnectResponseMessage response = mDConnectSDK.put(uri, null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
    }

    /**
     * メソッドにDELETEを指定してサーバ起動確認テストを行う.
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /availability
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetAvailabilityInvalidMethodDelete() {
        String uri = "http://localhost:4035/gotapi/availability";

        DConnectResponseMessage response = mDConnectSDK.delete(uri);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
    }

}
