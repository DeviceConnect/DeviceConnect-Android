// IDConnectService.aidl
package org.deviceconnect.android.manager;

// Declare any non-default types here with import statements

interface IDConnectService {
    void start();
    void stop();
    boolean isRunning();
}
