/*
 * Copyright (C) 2012 Google Inc.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * URL：https://github.com/google/googletv-android-samples
 * クラス：com.example.google.tv.anymotelibrary.connection.KeyStoreManager
 * 変更者：NTT DOCOMO, INC.
 */

package org.deviceconnect.server.nanohttpd.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.logging.Logger;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.deviceconnect.server.nanohttpd.BuildConfig;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.google.fix.PRNGFixes;
import com.google.polo.ssl.SslUtil;

/**
 * キーストア・マネージャー. クライアントとサーバの証明書を管理する.
 */
public final class KeyStoreManager {

    /** タグ. */
    private static final String LOG_TAG = "KeyStoreManager";

    /**
     * キーストアのファイル名.
     */
    private static final String KEYSTORE_FILENAME = "keystore.bks";

    /**
     * キーストアのパスワード.
     * 
     * TODO パスワード文字列について要検証.
     */
    private static final char[] KEYSTORE_PASSWORD = "sjdlf'%Rli\"SHglk29ugsld??AfjL+D-".toCharArray();

    /**
     * Alias for the remote controller (local) identity in the {@link KeyStore}.
     */
    private static final String LOCAL_IDENTITY_ALIAS = "Device Connect-remote";

    /**
     * Alias pattern for Device Connect server identities in the {@link KeyStore}.
     */
    private static final String REMOTE_IDENTITY_ALIAS_PATTERN = "Device Connect-server-%X";

    /** Context. */
    private Context mContext;
    /** Key Managers. */
    private KeyManager[] mKeyManagers;
    /** Trust Managers. */
    private TrustManager[] mTrustManagers;
    /** Key Store. */
    private KeyStore mKeyStore;

    /** ロガー. */
    private final Logger mLogger = Logger.getLogger("dconnect.manager");

    /**
     * Verify if local certificate is available.
     * 
     * @return true, if certificate is available.
     */
    private boolean hasLocalIdentityAlias() {
        try {
            if (!mKeyStore.containsAlias(LOCAL_IDENTITY_ALIAS)) {
                if (BuildConfig.DEBUG) {
                    Log.e(LOG_TAG, "Key store missing identity for " + LOCAL_IDENTITY_ALIAS);
                }
                return false;
            }
        } catch (KeyStoreException e) {
            if (BuildConfig.DEBUG) {
                Log.e(LOG_TAG, "Key store exception occurred", e);
            }
            return false;
        }
        return true;
    }

    /**
     * Loads or otherwise creates keys for application. This call may take
     * substantial amount of time to complete.
     * 
     * @param context Context of the Application
     * @param fromFile Generate the key store from the file
     * @throws GeneralSecurityException General Security Exception
     */
    public void initialize(final Context context, final boolean fromFile) throws GeneralSecurityException {
        // Java Cryptography Architectureの乱数種に関するセキュリティ問題への対処.
        PRNGFixes.apply();

        mContext = context;
        if (fromFile) {
            loadKeyStore();
            if (hasLocalIdentityAlias()) {
                generateAppCertificate();
            }
        } else {
            createKeyStore();
            generateAppCertificate();
        }
        collectKeyManagers();
        collectTrustManagers();
    }

    /**
     * Create application-specific certificate that will be used to authenticate
     * user.
     */
    @SuppressLint("TrulyRandom")
    private void generateAppCertificate() {
        clearKeyStore();
        try {
            if (BuildConfig.DEBUG) {
                Log.v(LOG_TAG, "Generating key pair ...");
            }
            KeyPairGenerator kg = KeyPairGenerator.getInstance("RSA");
            KeyPair keyPair = kg.generateKeyPair();
            if (BuildConfig.DEBUG) {
                Log.v(LOG_TAG, "Generating certificate ...");
            }
            String commonName = getCertificateName(getUniqueId());
            X509Certificate cert = SslUtil.generateX509V3Certificate(keyPair, commonName);
            Certificate[] chain = {cert};
            if (BuildConfig.DEBUG) {
                Log.v(LOG_TAG, "Adding key to keystore  ...");
            }
            mKeyStore.setKeyEntry(LOCAL_IDENTITY_ALIAS, keyPair.getPrivate(), null, chain);

            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "Key added!");
            }
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Unable to create identity KeyStore", e);
        }
    }

    /**
     * KeyStoreをファイルから読み取る. 失敗した場合は生成する.
     */
    public void loadKeyStore() {
        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        } catch (KeyStoreException e) {
            throw new IllegalStateException("Unable to get default instance of KeyStore", e);
        }
        try {
            FileInputStream fis = mContext.openFileInput(KEYSTORE_FILENAME);
            keyStore.load(fis, KEYSTORE_PASSWORD);
        } catch (IOException e) {
            if (BuildConfig.DEBUG) {
                Log.v(LOG_TAG, "Unable open keystore file", e);
            }
            keyStore = null;
        } catch (GeneralSecurityException e) {
            if (BuildConfig.DEBUG) {
                Log.v(LOG_TAG, "Unable open keystore file", e);
            }
            keyStore = null;
        }

        /*
         * No keys found: generate.
         */
        if (keyStore == null) {
            try {
                createKeyStore();
                return;
            } catch (GeneralSecurityException e) {
                throw new IllegalStateException("Unable to create identity KeyStore", e);
            }
        }

        mKeyStore = keyStore;
    }

    /**
     * 新たにKeyStoreを生成する.
     * @throws GeneralSecurityException General Security Exception
     */
    public void createKeyStore() throws GeneralSecurityException {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        try {
            keyStore.load(null, KEYSTORE_PASSWORD);
        } catch (IOException e) {
            throw new GeneralSecurityException("Unable to create empty keyStore", e);
        }

        mKeyStore = keyStore;
    }

    /**
     * キーストアをファイルに出力する.
     */
    public synchronized void storeKeyStore() {
        try {
            FileOutputStream fos = mContext.openFileOutput(KEYSTORE_FILENAME, Context.MODE_PRIVATE);
            mKeyStore.store(fos, KEYSTORE_PASSWORD);
            fos.close();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to store keyStore", e);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Unable to store keyStore", e);
        }
    }

    /**
     * Returns the name that should be used in a new certificate.
     * <p>
     * The format is:
     * "CN=Device Connect-server/PRODUCT/DEVICE/MODEL/unique identifier"
     * @param id ID
     * @return Certificate Name
     */
    private static String getCertificateName(final String id) {
        return "CN=Device Connect-server/" + Build.PRODUCT + "/" + Build.DEVICE + "/" + Build.MODEL + "/" + id;
    }

    /**
     * @return key managers loaded for this service.
     */
    public KeyManager[] getKeyManagers() {
        return mKeyManagers;
    }

    /**
     * Collect Key Manager.
     * 
     * @throws GeneralSecurityException General Security Exception
     */
    private synchronized void collectKeyManagers() throws GeneralSecurityException {
        if (mKeyStore == null) {
            throw new NullPointerException("null mKeyStore");
        }
        KeyManagerFactory factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        factory.init(mKeyStore, "".toCharArray());
        mKeyManagers = factory.getKeyManagers();
    }

    /**
     * @return trust managers loaded for this service.
     */
    public TrustManager[] getTrustManagers() {
        return mTrustManagers;
    }

    /**
     * Collect Trust Managers.
     * @throws GeneralSecurityException General Security Exception
     */
    private synchronized void collectTrustManagers() throws GeneralSecurityException {
        // Build a new set of TrustManagers based on the KeyStore.
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(mKeyStore);
        mTrustManagers = tmf.getTrustManagers();
    }

    /**
     * Stores the remote device certificate in keystore.
     * 
     * @param peerCert Peer Certificate
     */
    synchronized void storeCertificate(final Certificate peerCert) {
        try {
            String alias = String.format(KeyStoreManager.REMOTE_IDENTITY_ALIAS_PATTERN, peerCert.hashCode());
            if (mKeyStore.containsAlias(alias)) {
                if (BuildConfig.DEBUG) {
                    Log.w(LOG_TAG, "Deleting existing entry for " + alias);
                }
                mKeyStore.deleteEntry(alias);
            }
            if (BuildConfig.DEBUG) {
                Log.i(LOG_TAG, "Adding cert to keystore: " + alias);
            }
            mKeyStore.setCertificateEntry(alias, peerCert);

            try {
                collectTrustManagers();
            } catch (GeneralSecurityException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
        } catch (KeyStoreException e) {
            if (BuildConfig.DEBUG) {
                Log.e(LOG_TAG, "Storing cert failed", e);
            }
        }
    }

    /**
     * Clear Key Store.
     */
    private void clearKeyStore() {
        try {
            for (Enumeration<String> e = mKeyStore.aliases(); e.hasMoreElements();) {
                final String alias = e.nextElement();
                if (BuildConfig.DEBUG) {
                    Log.v(LOG_TAG, "Deleting alias: " + alias);
                }
                mKeyStore.deleteEntry(alias);
            }
        } catch (KeyStoreException e) {
            if (BuildConfig.DEBUG) {
                Log.e(LOG_TAG, "Clearing certificates failed", e);
            }
        }
    }

    /**
     * Get Unique ID.
     * @return Unique ID
     */
    private String getUniqueId() {
        String id = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        // null ANDROID_ID is possible on emulator
        return id != null ? id : "emulator";
    }

    /**
     * 証明書を読み込みFactoryクラスを生成する.
     * 
     * @return 読み込み成功時はSSLServerSocketFactoryを、その他はnullを返す.
     */
    public SSLServerSocketFactory getServerSocketFactory() {
        SSLServerSocketFactory retval = null;
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");

            sslContext.init(mKeyManagers, mTrustManagers, new SecureRandom());
            retval = sslContext.getServerSocketFactory();
        } catch (NoSuchAlgorithmException e) {
            mLogger.warning("NoSuchAlgorithmException in the"
                    + " DConnectServerNanoHttpd#createServerSocketFactory() method. "
                    + e.toString());
        } catch (KeyManagementException e) {
            mLogger.warning("KeyManagementException in the DConnectServerNanoHttpd#createServerSocketFactory() method. "
                    + e.toString());
        }
        return retval;
    }
}
