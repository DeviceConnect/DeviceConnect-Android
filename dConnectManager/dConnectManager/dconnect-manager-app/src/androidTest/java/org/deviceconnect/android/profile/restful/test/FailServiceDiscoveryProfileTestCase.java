/*
 FailServiceDiscoveryProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectMessage.ErrorCode;
import org.deviceconnect.message.DConnectResponseMessage;
import org.deviceconnect.message.DConnectSDK;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


/**
 * Network Service Discovery プロファイルの異常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class FailServiceDiscoveryProfileTestCase extends RESTfulDConnectTestCase {
    /**
     * POSTメソッドでサービスの探索を行う.
     * 
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /serviceDiscovery
     * </pre>
     * 
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetServices001() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ServiceDiscoveryProfileConstants.PROFILE_NAME);

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.UNKNOWN_ATTRIBUTE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * PUTメソッドでサービスの探索を行う.
     * 
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /serviceDiscovery
     * </pre>
     * 
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetServices002() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ServiceDiscoveryProfileConstants.PROFILE_NAME);

        DConnectResponseMessage response = mDConnectSDK.put(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.UNKNOWN_ATTRIBUTE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * DELETEメソッドでサービスの探索を行う.
     * 
     * <pre>
     * 【HTTP通信】
     * Method: DELETE
     * Path: /serviceDiscovery
     * </pre>
     * 
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testGetServices003() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ServiceDiscoveryProfileConstants.PROFILE_NAME);

        DConnectResponseMessage response = mDConnectSDK.delete(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.UNKNOWN_ATTRIBUTE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * serviceIdを指定してサービスの探索を行う.
     * 
     * <pre>
     * 【HTTP通信】
     * Method: GET
     * Path: /serviceDiscovery?serviceId=xxxx
     * </pre>
     * 
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・servicesに少なくとも1つ以上のサービスが発見されること。
     * </pre>
     */
    @Test
    public void testGetServices004() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ServiceDiscoveryProfileConstants.PROFILE_NAME);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));

        List services = response.getList(ServiceDiscoveryProfile.PARAM_SERVICES);
        assertThat(services, is(notNullValue()));
        assertThat(services.size(), is(greaterThan(0)));
        for (Object obj : services) {
            DConnectMessage service = (DConnectMessage) obj;
            String id = service.getString("id");
            String name = service.getString("name");
            assertThat(id, is(notNullValue()));
            assertThat(name, is(notNullValue()));
        }
    }

    /**
     * POSTメソッドでサービス追加イベント登録を行う.
     * 
     * <pre>
     * 【HTTP通信】
     * Method: POST
     * Path: /serviceDiscovery/onServiceChange
     * </pre>
     * 
     * <pre>
     * 【期待する動作】
     * ・resultに1が返ってくること。
     * </pre>
     */
    @Test
    public void testOnServiceChangeInvalidMethodPost() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(ServiceDiscoveryProfileConstants.PROFILE_NAME);
        builder.setAttribute(ServiceDiscoveryProfileConstants.ATTRIBUTE_ON_SERVICE_CHANGE);

        DConnectResponseMessage response = mDConnectSDK.post(builder.build(), null);
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(ErrorCode.UNKNOWN_ATTRIBUTE.getCode()));
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }
}
