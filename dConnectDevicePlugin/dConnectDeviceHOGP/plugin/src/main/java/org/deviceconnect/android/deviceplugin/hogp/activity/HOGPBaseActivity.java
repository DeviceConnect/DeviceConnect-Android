/*
 HOGPBaseActivity.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hogp.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import org.deviceconnect.android.deviceplugin.hogp.BuildConfig;
import org.deviceconnect.android.deviceplugin.hogp.HOGPMessageService;
import org.deviceconnect.android.deviceplugin.hogp.server.AbstractHOGPServer;
import org.deviceconnect.android.message.DConnectMessageService;

/**
 * HOGPMessageServiceとバインド処理を行うActivity.
 *
 * @author NTT DOCOMO, INC.
 */
public class HOGPBaseActivity extends Activity {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "HOGP";

    /**
     * サービスとのバインド状態を管理するフラグ.
     * <p>
     * バインドされている場合はtrue。
     * </p>
     */
    private boolean mIsBound;

    /**
     * バインドしたHOGPMessageServiceのインスタンス.
     */
    private HOGPMessageService mHOGPMessageService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindService();
    }

    @Override
    protected void onDestroy() {
        unbindService();
        super.onDestroy();
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

            onServiceDisconnected();
            mHOGPMessageService = null;
            mIsBound = false;
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
            HOGPBaseActivity.this.onServiceDisconnected();
            mHOGPMessageService = null;
            mIsBound = false;
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
