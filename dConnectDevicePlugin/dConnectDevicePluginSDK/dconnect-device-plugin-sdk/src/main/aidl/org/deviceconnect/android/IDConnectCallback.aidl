// IDConnectCallback.aidl
package org.deviceconnect.android;

import android.content.Intent;

interface IDConnectCallback {

    void sendMessage(in Intent message);
}