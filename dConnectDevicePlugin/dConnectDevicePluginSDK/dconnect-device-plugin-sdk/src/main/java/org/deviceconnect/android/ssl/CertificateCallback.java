package org.deviceconnect.android.ssl;


import java.security.cert.Certificate;

interface CertificateCallback {

    void onCreate(Certificate certificate);

    void onError();

}
