package org.deviceconnect.android.ssl;


interface ICertificateAuthority {

    byte[] requestCertificate(in byte[] certificateRequest);

    byte[] getRootCertificate();
}
