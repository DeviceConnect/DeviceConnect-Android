/*
 ObserverReceiver.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.observer.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.Toast;

import org.deviceconnect.android.manager.IDConnectService;
import org.deviceconnect.android.observer.DConnectObservationService;

/**
 * 監視用のブロードキャストを受けるレシーバー.
 * 
 *
 * @author NTT DOCOMO, INC.
 */
public class ObserverReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (DConnectObservationService.ACTION_START.equals(intent.getAction())) {
            if (mDConnectService != null) {
                try {
                    mDConnectService.start();
                    Toast.makeText(context, "Server Started...", Toast.LENGTH_SHORT).show();
                } catch (RemoteException e) {
                }
            }
        }
        if (DConnectObservationService.ACTION_STOP.equals(intent.getAction())) {
            if (mDConnectService != null) {
                try {
                    mDConnectService.stop();
                    Toast.makeText(context, "Server Stopped...", Toast.LENGTH_SHORT).show();
                } catch (RemoteException e) {
                }
            }
        }
    }

    private IDConnectService mDConnectService;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            mDConnectService = (IDConnectService) service;
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            mDConnectService = null;
        }
    };
}
