package org.deviceconnect.android.ssl;


import java.security.KeyStore;

public interface KeyStoreCallback {

    void onSuccess(KeyStore keyStore);

    void onError(KeyStoreError error);

}
