/*
 DConnectSDKFactoryTest.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.message;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

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
    @Test(expected = NullPointerException.class)
    public void create_context_null() {
        DConnectSDKFactory.create(null, DConnectSDKFactory.Type.HTTP);
    }

    /**
     * typeにnullを設定して、DConnectSDKのインスタンスを作成する。
     * <pre>
     * 【期待する動作】
     * ・NullPointerExceptionが派生すること。
     * </pre>
     */
    @Test(expected = NullPointerException.class)
    public void create_type_null() {
        DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), null);
    }
}
