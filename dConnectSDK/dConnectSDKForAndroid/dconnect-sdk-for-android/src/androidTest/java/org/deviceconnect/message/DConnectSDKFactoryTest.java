package org.deviceconnect.message;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class DConnectSDKFactoryTest {

    @Test
    public void create_context_null() {
        try {
            DConnectSDK sdk = DConnectSDKFactory.create(null, DConnectSDKFactory.Type.HTTP);
            fail("No NullPointerException occurred.");
        } catch (NullPointerException e) {
            // テスト成功
        }
    }

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
