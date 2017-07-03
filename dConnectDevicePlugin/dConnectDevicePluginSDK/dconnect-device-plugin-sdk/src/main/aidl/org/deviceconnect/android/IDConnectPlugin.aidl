// IDConnectPlugin.aidl
package org.deviceconnect.android;

import android.content.Intent;
import org.deviceconnect.android.IDConnectCallback;

interface IDConnectPlugin {

    void registerCallback(in IDConnectCallback callback);

    void sendRequest(in Intent request);

    ParcelFileDescriptor readFileDescriptor(in String fileId);
}