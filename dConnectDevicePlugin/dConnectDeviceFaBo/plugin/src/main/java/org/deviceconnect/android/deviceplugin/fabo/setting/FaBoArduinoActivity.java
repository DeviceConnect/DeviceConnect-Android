package org.deviceconnect.android.deviceplugin.fabo.setting;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MenuItem;

import org.deviceconnect.android.deviceplugin.fabo.FaBoArduinoDeviceService;
import org.deviceconnect.android.deviceplugin.fabo.R;
import org.deviceconnect.android.deviceplugin.fabo.device.FaBoDeviceControl;
import org.deviceconnect.android.deviceplugin.fabo.setting.fragment.FaBoArduinoFragment;
import org.deviceconnect.android.deviceplugin.fabo.setting.fragment.FaBoConnectFragment;
import org.deviceconnect.android.deviceplugin.fabo.setting.fragment.FaBoFirmwareFragment;
import org.deviceconnect.android.message.DConnectMessageService;

public class FaBoArduinoActivity extends Activity {
    /**
     * プラグインのサービス.
     */
    private DConnectMessageService mMessageService;

    /**
     * サービスとのバインド状態.
     */
    private boolean mIsBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fabo_arduino);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_HOME);
            actionBar.setTitle(R.string.activity_setting_arduino_title);
        }

        Intent a = new Intent();
        a.setClass(this, FaBoArduinoDeviceService.class);
        startService(a);

        boolean finishFlag = true;
        Intent intent = getIntent();
        if (intent != null) {
            String page = intent.getStringExtra("page");
            if ("firmata".equals(page)) {
                showFirmwareFragment();
                return;
            } else if ("connect".equals(page)) {
                finishFlag = false;
            }
        }
        showConnectFragment(finishFlag);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindMessageService();
    }

    @Override
    protected void onPause() {
        unbindMessageService();
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Arduinoとの接続を監視するフラグメントを表示します.
     * @param flag 終了フラグ
     */
    private void showConnectFragment(final boolean flag) {
        Bundle args = new Bundle();
        args.putBoolean(FaBoConnectFragment.EXTRA_FINISH_FLAG, flag);

        FaBoConnectFragment fragment = new FaBoConnectFragment();
        fragment.setArguments(args);

        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    /**
     * ファームウェア更新用フラグメントを表示します.
     */
    private void showFirmwareFragment() {
        FaBoFirmwareFragment fragment = new FaBoFirmwareFragment();
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    /**
     * DConnectMessageServiceとバインドします.
     */
    private void bindMessageService() {
        if (!mIsBound) {
            Intent intent = new Intent(getApplicationContext(), FaBoArduinoDeviceService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    /**
     * DConnectMessageServiceとアンバインドします.
     */
    private void unbindMessageService() {
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    /**
     * バインドしたサービスを取得します.
     * @return バインドしたサービス
     */
    private FaBoArduinoDeviceService getService() {
        return (FaBoArduinoDeviceService) mMessageService;
    }

    /**
     * FaBoDeviceControlのインスタンスを取得します.
     * <p>
     * バインドしていなかったり、USBに接続出来ていない場合にはnullを返却します。
     * </p>
     * @return FaBoDeviceControlのインスタンス
     */
    public FaBoDeviceControl getFaBoDeviceControl() {
        FaBoArduinoDeviceService s = getService();
        if (s != null) {
            return s.getFaBoDeviceControl();
        }
        return null;
    }

    /**
     * サービスとバインドされたことをを通知します.
     */
    private void notifyBindService() {
        FragmentManager manager = getFragmentManager();
        Fragment f = manager.findFragmentById(R.id.fragment_container);
        if (f != null && f instanceof FaBoArduinoFragment) {
            ((FaBoArduinoFragment) f).onBindService();
        }

    }

    /**
     * サービスとアンバインドされたことを通知します.
     */
    private void notifyUnbindService() {
        FragmentManager manager = getFragmentManager();
        Fragment f = manager.findFragmentById(R.id.fragment_container);
        if (f != null && f instanceof FaBoArduinoFragment) {
            ((FaBoArduinoFragment) f).onUnbindService();
        }
    }

    /**
     * DConnectMessageServiceとのバインドを状態のイベントを受け取るクラス.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMessageService = ((DConnectMessageService.LocalBinder) service).getMessageService();
                    mIsBound = true;
                    notifyBindService();
                }
            });
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            notifyUnbindService();
            mIsBound = false;
            mMessageService = null;
        }
    };
}
