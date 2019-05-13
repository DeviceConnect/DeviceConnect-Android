package org.deviceconnect.android.ssl;

import android.content.ComponentName;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.deviceconnect.android.logger.AndroidHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


@RunWith(AndroidJUnit4.class)
public class EndPointKeyStoreManagerTest {

    @Before
    public void setup() {
        Logger logger = Logger.getLogger("LocalCA");
        AndroidHandler handler = new AndroidHandler(logger.getName());
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
    }

    @Test
    public void testRequestKeyStore() {
        Context context = InstrumentationRegistry.getContext();
        String keyStoreFile = "keystore.p12";
        ComponentName authorityName = new ComponentName("org.deviceconnect.android.test",
                "org.deviceconnect.android.ssl.TestCertificateAuthorityService");

        final CountDownLatch latch = new CountDownLatch(1);
        final KeyStore[] result = new KeyStore[1];

        KeyStoreManager mgr = new EndPointKeyStoreManager(context, keyStoreFile, context.getPackageName(), authorityName);
        mgr.requestKeyStore("0.0.0.0", new KeyStoreCallback() {
            @Override
            public void onSuccess(final KeyStore keyStore, final Certificate cert, final Certificate rootCert) {
                result[0] = keyStore;
                latch.countDown();
            }

            @Override
            public void onError(final KeyStoreError error) {
                latch.countDown();
            }
        });
        try {
            if (latch.getCount() > 0) {
                latch.await(20, TimeUnit.SECONDS);
            }
            Assert.assertNotNull(result[0]);
        } catch (InterruptedException e) {
            Assert.assertTrue(false);
        }
    }

}
