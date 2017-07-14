package org.deviceconnect.android.deviceplugin.hogp.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.hogp.BuildConfig;
import org.deviceconnect.android.deviceplugin.hogp.HOGPMessageService;
import org.deviceconnect.android.deviceplugin.hogp.server.AbstractHOGPServer;
import org.deviceconnect.android.message.DConnectMessageService;

/**
 * HOGPMessageServiceとバインド処理を行うActivity.
 */
public class HOGPBaseActivity extends Activity {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "HOGP";
    private boolean mIsBound;

    /**
     * バインドしたHOGPMessageServiceのインスタンス.
     */
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

    /**
     * HOGPMessageServiceとバインドします.
     */
    private void bindService() {
        Intent intent = new Intent(this, HOGPMessageService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    /**
     * HOGPMessageServiceとアンバインドします.
     * <p>
     * バインドされていない場合には何もしません。
     * </p>
     */
    private void unbindService() {
        if (mIsBound) {
            unbindService(mConnection);
        }
    }

    /**
     * HOGPMessageServiceとバインドした時のコネクション.
     */
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
            mHOGPMessageService = null;
            mIsBound = false;
            HOGPBaseActivity.this.onServiceDisconnected();
        }
    };

    /**
     * バインドされているHOGPMessageServiceを取得します.
     * <p>
     * バインドされていない場合にはnullを返却します。
     * </p>
     * @return HOGPMessageService
     */
    HOGPMessageService getHOGPMessageService() {
        return mHOGPMessageService;
    }

    /**
     * HOGPサーバのインスタンスを取得します.
     * <p>
     * HOGPMessageServiceにバインドできていない場合や起動していない場合はnullを返却します。
     * </p>
     * @return HOGPサーバ
     */
    AbstractHOGPServer getHOGPServer() {
        if (mHOGPMessageService != null) {
            return mHOGPMessageService.getHOGPServer();
        }
        return null;
    }

    /**
     * HOGPMessageServiceとバインドされた時に呼び出します.
     */
    void onServiceConnected() {
    }

    /**
     * HOGPMessageServiceとアンバインドされた時に呼び出します.
     */
    void onServiceDisconnected() {
    }
}
