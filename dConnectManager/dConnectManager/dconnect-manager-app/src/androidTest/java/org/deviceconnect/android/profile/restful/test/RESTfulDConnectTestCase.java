/*
 RESTfulDConnectTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import androidx.test.platform.app.InstrumentationRegistry;

import org.deviceconnect.android.test.DConnectTestCase;
import org.deviceconnect.message.DConnectSDKFactory;

/**
 * RESTful API用テストケース.
 * @author NTT DOCOMO, INC.
 */
public class RESTfulDConnectTestCase extends DConnectTestCase {
    @Override
    public void setUp() throws Exception {
        mDConnectSDK = DConnectSDKFactory.create(InstrumentationRegistry.getContext(), DConnectSDKFactory.Type.HTTP);
        mDConnectSDK.setOrigin(getOrigin());
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        mDConnectSDK = null;
    }
}
