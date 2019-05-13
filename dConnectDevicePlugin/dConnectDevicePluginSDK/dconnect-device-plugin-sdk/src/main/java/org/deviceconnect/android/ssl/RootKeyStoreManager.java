/*
 RootKeyStoreManager.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.ssl;


import android.content.Context;

import com.google.fix.PRNGFixes;

import org.deviceconnect.android.BuildConfig;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.x500.X500Principal;

/**
 * ルート証明書用キーストア管理クラス.
 *
 * <p>
 * キーストアを外部から要求された時、証明書が未生成だった場合は、ルート証明書を自身によって発行・永続化する.
 *
 * 証明書の発行は、数秒かかる場合があるため、別スレッド上で処理される.
 *
 * NOTE: 本クラスの保持する証明書はただ1つ.
 * </p>
 *
 * @author NTT DOCOMO, INC.
 */
class RootKeyStoreManager extends AbstractKeyStoreManager implements KeyStoreManager {

    /**
     * 証明書発行スレッド.
     */
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    /**
     * ロガー.
     */
    private final Logger mLogger = Logger.getLogger("LocalCA");

    /**
     * 証明書のサブジェクト名.
     */
    private final String mSubjectName;

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     * @param subjectName 証明書のサブジェクト名
     * @param keyStoreFileName キーストアのファイル名
     */
    RootKeyStoreManager(final Context context,
                        final String subjectName,
                        final String keyStoreFileName) {
        super(context, keyStoreFileName);
        mSubjectName = subjectName;

        // Java Cryptography Architectureの乱数種に関するセキュリティ問題への対処.
        PRNGFixes.apply();
    }

    @Override
    public void requestKeyStore(final String ipAddress, final KeyStoreCallback callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Certificate cert = null;
                try {
                    cert = mKeyStore.getCertificate(ipAddress);
                    if (cert != null) {
                        callback.onSuccess(mKeyStore, cert, cert);
                        return;
                    }
                    if (BuildConfig.DEBUG) {
                        mLogger.info("Generating self-signed server certificate...");
                    }
                    cert = generateSelfSignedCertificate();
                    if (BuildConfig.DEBUG) {
                        mLogger.info("Generated self-signed server certificate...");
                    }
                } catch (KeyStoreException e) {
                    mLogger.log(Level.SEVERE, "Failed to generate self-signed server certificate.", e);
                    callback.onError(KeyStoreError.BROKEN_KEYSTORE);
                    return;
                } catch (GeneralSecurityException e) {
                    mLogger.log(Level.SEVERE, "Failed to generate self-signed server certificate.", e);
                    callback.onError(KeyStoreError.UNSUPPORTED_KEYSTORE_FORMAT);
                    return;
                }

                try {
                    saveKeyStore();
                    callback.onSuccess(mKeyStore, cert, cert);
                } catch (Exception e) {
                    mLogger.log(Level.SEVERE, "Failed to save self-signed server certificate.", e);
                    callback.onError(KeyStoreError.FAILED_BACKUP_KEYSTORE);
                }
            }
        });
    }

    /**
     * 自己署名証明書を生成する.
     *
     * @throws GeneralSecurityException 生成に失敗した場合
     * @return 自己署名証明書
     */
    private Certificate generateSelfSignedCertificate() throws GeneralSecurityException {
        KeyPairGenerator kg = KeyPairGenerator.getInstance("RSA");
        KeyPair keyPair = kg.generateKeyPair();
        X500Principal subject = new X500Principal("CN=" + mSubjectName);
        Certificate cert = generateX509V3Certificate(keyPair, subject, subject, null,true);
        Certificate[] chain = {cert};
        mKeyStore.setKeyEntry(mSubjectName, keyPair.getPrivate(), null, chain);
        return cert;
    }
}
