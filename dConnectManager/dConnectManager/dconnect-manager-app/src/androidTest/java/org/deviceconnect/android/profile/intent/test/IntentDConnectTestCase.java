/*
 IntentDConnectTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.intent.test;

import android.content.Context;

import org.deviceconnect.android.test.DConnectTestCase;
import org.deviceconnect.message.DConnectSDKFactory;
import org.junit.After;
import org.junit.Before;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;


/**
 * Intent用のテストケース.
 * @author NTT DOCOMO, INC.
 */
public class IntentDConnectTestCase extends DConnectTestCase {

    private Context getTargetContext() {
        return getInstrumentation().getTargetContext().getApplicationContext();
    }

    @Before
    public void setUp() throws Exception {
        mDConnectSDK = DConnectSDKFactory.create(getTargetContext(), DConnectSDKFactory.Type.INTENT);
        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        mDConnectSDK = null;
        super.tearDown();
    }
}
