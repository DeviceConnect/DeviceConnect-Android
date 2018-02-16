package org.deviceconnect.android.ssl;


import java.security.cert.Certificate;

interface CertificateRequestCallback {

    void onCreate(Certificate certificate);

    void onError();

}
