// IDConnectCallback.aidl
package org.deviceconnect.android;

import android.content.Intent;

interface IDConnectCallback {

    void sendResponse(in Intent response);

    void sendEvent(in Intent event);
}