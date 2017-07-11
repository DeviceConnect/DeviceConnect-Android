package org.deviceconnect.android.deviceplugin.hogp.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.hogp.BuildConfig;
import org.deviceconnect.android.deviceplugin.hogp.HOGPMessageService;
import org.deviceconnect.android.message.DConnectMessageService;

public class HOGPBaseActivity extends Activity {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "HOGP";
    private boolean mIsBound;

    private HOGPMessageService mHOGPMessageService;

    @Override
    protected void onResume() {
        super.onResume();
        bindService();
    }

    @Override
    protected void onPause() {
        unbindService();
        super.onPause();
    }

    private void bindService() {
        Intent intent = new Intent(this, HOGPMessageService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    private void unbindService() {
        if (mIsBound) {
            unbindService(mConnection);
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            if (DEBUG) {
                Log.i(TAG, "onServiceConnected " + name);
            }
            mIsBound = true;
            mHOGPMessageService = (HOGPMessageService) ((DConnectMessageService.LocalBinder) service).getMessageService();
            HOGPBaseActivity.this.onServiceConnected();
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            if (DEBUG) {
                Log.i(TAG, "onServiceDisconnected " + name);
            }
            HOGPBaseActivity.this.onServiceDisconnected();
        }
    };

    HOGPMessageService getHOGPMessageService() {
        return mHOGPMessageService;
    }

    void onServiceConnected() {
    }

    void onServiceDisconnected() {
    }
}
