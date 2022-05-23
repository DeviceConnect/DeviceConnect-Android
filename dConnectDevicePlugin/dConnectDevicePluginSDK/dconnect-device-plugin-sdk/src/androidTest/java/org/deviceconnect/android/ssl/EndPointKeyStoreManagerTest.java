package org.deviceconnect.android.ssl;

import android.content.ComponentName;
import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.deviceconnect.android.logger.AndroidHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


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
    public void testRequestKeyStore() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        String keyStoreFile = "keystore.p12";
        ComponentName authorityName = new ComponentName("org.deviceconnect.android.test",
                "org.deviceconnect.android.ssl.TestCertificateAuthorityService");

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<KeyStore> result = new AtomicReference<>();
        final AtomicReference<KeyStoreError> keyStoreError = new AtomicReference<>();
        final String keyStorePassword = "0000";
        final String alias = context.getPackageName();

        EndPointKeyStoreManager mgr = new EndPointKeyStoreManager(context, keyStoreFile, keyStorePassword, alias, authorityName);
        mgr.clean();
        mgr.requestKeyStore("0.0.0.0", new KeyStoreCallback() {
            @Override
            public void onSuccess(final KeyStore keyStore, final Certificate cert, final Certificate rootCert) {
                result.set(keyStore);
                latch.countDown();
            }

            @Override
            public void onError(final KeyStoreError error) {
                keyStoreError.set(error);
                latch.countDown();
            }
        });

        if (latch.getCount() > 0) {
            latch.await(20, TimeUnit.SECONDS);
        }

        assertNull("ERROR: " + keyStoreError, keyStoreError.get());

        KeyStore keyStore = result.get();
        assertNotNull(keyStore);
        Certificate[] chain = keyStore.getCertificateChain(alias);
        assertNotNull(chain);
        assertEquals(2, chain.length);
    }
}
