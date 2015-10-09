// IDConnectWebService.aidl
package org.deviceconnect.android.manager;

// Declare any non-default types here with import statements

interface IDConnectWebService {
    void start();
    void stop();
    boolean isRunning();
}
