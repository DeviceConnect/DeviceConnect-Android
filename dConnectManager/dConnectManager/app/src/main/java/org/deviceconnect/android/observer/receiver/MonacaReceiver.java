package org.deviceconnect.android.observer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;

import org.deviceconnect.android.manager.DConnectLaunchActivity;
import org.deviceconnect.android.manager.IDConnectService;

/**
 * Created by Sotheara on 1/21/16.
 */
public class MonacaReceiver extends BroadcastReceiver {

    public static final String MONACA_START = "org.deviceconnect.android.intent.action.observer.MONACA_START";
    public static final String MONACA_STOP = "org.deviceconnect.android.intent.action.observer.MONACA_STOP";
    public static final String MONACA = "monaca";

    @Override
    public void onReceive(Context context, Intent intent) {

        IDConnectService mDConnectService = DConnectLaunchActivity.sDConnectService;

        if (mDConnectService != null) {
            try {
                if (intent.getAction().equals(MONACA_START)) {
                    mDConnectService.start();
                }
                if (intent.getAction().equals(MONACA_STOP)) {
                    mDConnectService.stop();
                }
            } catch (RemoteException e) {
            }
        }
        else {
            Intent i = new Intent();
            i.setClassName(context.getPackageName(), "org.deviceconnect.android.manager.DConnectLaunchActivity");
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra(MONACA, intent.getAction());
            context.startActivity(i);
        }
    }

}