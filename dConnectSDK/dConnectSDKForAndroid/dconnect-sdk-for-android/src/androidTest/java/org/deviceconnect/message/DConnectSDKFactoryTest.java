/*
 DConnectSDKFactoryTest.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.message;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.fail;

/**
 * DConnectSDKFactoryのテスト.
 *
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class DConnectSDKFactoryTest {

    /**
     * contextにnullを設定して、DConnectSDKのインスタンスを作成する。
     * <pre>
     * 【期待する動作】
     * ・NullPointerExceptionが派生すること。
     * </pre>
     */
    @Test
    public void create_context_null() {
        try {
            DConnectSDK sdk = DConnectSDKFactory.create(null, DConnectSDKFactory.Type.HTTP);
            fail("No NullPointerException occurred.");
        } catch (NullPointerException e) {
            // テスト成功
        }
    }

    /**
     * typeにnullを設定して、DConnectSDKのインスタンスを作成する。
     * <pre>
     * 【期待する動作】
     * ・NullPointerExceptionが派生すること。
     * </pre>
     */
    @Test
    public void create_type_null() {
        try {
            DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), null);
            fail("No NullPointerException occurred.");
        } catch (NullPointerException e) {
            // テスト成功
        }
    }
}
