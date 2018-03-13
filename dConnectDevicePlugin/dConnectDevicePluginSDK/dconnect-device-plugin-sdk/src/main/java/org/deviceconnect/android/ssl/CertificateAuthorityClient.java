/*
 CertificateAuthorityClient.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.ssl;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import org.bouncycastle.asn1.pkcs.CertificationRequest;
import org.deviceconnect.android.BuildConfig;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * ローカル認証局へAIDL経由でアクセスする機能を提供する.
 *
 * <p>
 * ローカル認証局に対して証明書要求を送信する場合は、
 * {@link #executeCertificateRequest(CertificationRequest, CertificateRequestCallback)}
 * を実行する. 実行すると、指定したAndroidサービスとのバインド後、証明書要求が送信される.
 * </p>
 *
 * <p>
 * {@link CertificateRequestCallback} から証明書要求を取得した後は、
 * かならず {@link #dispose()} によってバインドを解除すること.
 * 解除しない場合は、メモリリークの原因となる.
 * </p>
 *
 * @author NTT DOCOMO, INC.
 */
class CertificateAuthorityClient {

    private final Context mContext;

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder binder) {
            synchronized (mLock) {
                mLocalCA = ICertificateAuthority.Stub.asInterface(binder);
                mLock.notifyAll();
            }
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            synchronized (mLock) {
                mLocalCA = null;
            }
        }
    };

    private final ComponentName mLocalCAName;

    private ICertificateAuthority mLocalCA;

    private final Object mLock = new Object();

    private final Logger mLogger = Logger.getLogger("LocalCA");

    CertificateAuthorityClient(final Context context, final ComponentName name) {
        mContext = context;
        mLocalCAName = name;
    }

    private boolean bindLocalCA() {
        Intent intent = new Intent().setComponent(mLocalCAName);
        return mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private ICertificateAuthority fetchLocalCA(final CertificateRequestCallback callback)
        throws InterruptedException {

        if (BuildConfig.DEBUG) {
            mLogger.info("Binding Local CA service...");
        }

        if(!bindLocalCA()) {
            mLogger.severe("Local CA service (" + mLocalCAName + ") is not available.");
            callback.onError();
            return null;
        }

        if (BuildConfig.DEBUG) {
            mLogger.info("Waiting Local CA service connection...");
        }

        synchronized (mLock) {
            if (mLocalCA == null) {
                mLock.wait(5000);
            }
        }

        if (BuildConfig.DEBUG) {
            mLogger.info("Checking Local CA service connection...");
        }

        ICertificateAuthority localCA = mLocalCA;
        if (localCA == null) {
            mLogger.log(Level.SEVERE, "Failed to bind local CA service.");
            callback.onError();
            return null;
        }
        return localCA;
    }

    private Certificate decodeX509Certificate(final byte[] data) throws CertificateException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        return factory.generateCertificate(new ByteArrayInputStream(data));
    }

    void dispose() {
        if (mLocalCA != null) {
            mContext.unbindService(mConnection);
        }
    }

    void executeCertificateRequest(final CertificationRequest request,
                                   final CertificateRequestCallback callback) {
        try {
            ICertificateAuthority localCA = fetchLocalCA(callback);
            if (localCA == null) {
                return;
            }
            byte[] cert = localCA.requestCertificate(request.getEncoded());
            if (cert == null) {
                mLogger.log(Level.SEVERE, "end-point certificate is null.");
                callback.onError();
                return;
            }
            byte[] rootCert = localCA.getRootCertificate();
            if (rootCert == null) {
                mLogger.log(Level.SEVERE, "root certificate is null.");
                callback.onError();
                return;
            }
            callback.onCreate(decodeX509Certificate(cert), decodeX509Certificate(rootCert));
        } catch (InterruptedException e) {
            mLogger.log(Level.SEVERE, "Failed to generate server certificate.", e);
            callback.onError();
        } catch (RemoteException e) {
            mLogger.log(Level.SEVERE, "Failed to generate server certificate.", e);
            callback.onError();
        } catch (IOException e) {
            mLogger.log(Level.SEVERE, "Failed to generate server certificate.", e);
            callback.onError();
        } catch (CertificateException e) {
            mLogger.log(Level.SEVERE, "Failed to generate server certificate.", e);
            callback.onError();
        }
    }

}
