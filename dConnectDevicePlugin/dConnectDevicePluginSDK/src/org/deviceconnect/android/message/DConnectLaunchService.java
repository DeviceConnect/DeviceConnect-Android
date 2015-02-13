package org.deviceconnect.android.message;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Service for start device plug-in.
 * @author NTT DOCOMO, INC.
 */
public class DConnectLaunchService extends Service {
    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }
}
