/*
 BaseSettingActivity.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.setting;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import androidx.appcompat.app.AppCompatActivity;

import org.deviceconnect.android.manager.DConnectApplication;
import org.deviceconnect.android.manager.DConnectService;
import org.deviceconnect.android.manager.R;
import org.deviceconnect.android.manager.core.DConnectSettings;
import org.deviceconnect.android.manager.core.WebSocketInfoManager;
import org.deviceconnect.android.manager.core.plugin.DevicePluginManager;
import org.deviceconnect.android.manager.core.plugin.MessagingException;

import java.util.concurrent.atomic.AtomicBoolean;


/**
 * 設定画面のベースクラス.
 *
 * <p>
 * 画面起動時にマネージャ本体とのバインドし、マネージャ本体を制御できるようにする.
 * </p>
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class BaseSettingActivity extends AppCompatActivity {

    /**
     * マネージャ本体を操作するクラス.
     */
    private DConnectService mDConnectService;

    /**
     * 前回起動時に保存していたインスタンス.
     */
    private Bundle mSavedInstance;

    /**
     * マネージャのサーバー起動完了を待機するスレッド.
     *
     * マネージャ本体のサービスとバインドした際にスレッドのインスタンスを作成する.
     * マネージャのサーバー起動完了確認、予期しないバインド切断、または本画面非表示の際に破棄する.
     */
    private Thread mManagerMonitorThread;

    /**
     * マネージャ監視用ロックオブジェクト.
     */
    private final Object mManagerMonitorLock = new Object();

    /**
     * プログレスバー用のダイアログ.
     */
    private ProgressDialogFragment mDialog;

    private boolean mActivityVisible;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSavedInstance = savedInstanceState;
        bindManager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mActivityVisible = true;
    }

    @Override
    protected void onPause() {
        mActivityVisible = false;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        dismissStartingManagerDialog();
        stopManagerMonitor();
        unbindManager();
        super.onDestroy();
    }

    protected boolean hasSavedInstance() {
        return mSavedInstance != null;
    }

    /**
     * マネージャ本体とのバインド待ち状態になったことを通知.
     */
    protected void onManagerBinding() {
        // NOP.
    }

    /**
     * マネージャ本体とのバインドが不可能な状態であることを通知.
     */
    protected void onCannotManagerBonded() {
        // NOP.
    }

    /**
     * マネージャ本体とバインドしたことを通知.
     * @param dConnectService マネージャ本体
     */
    protected void onManagerBonded(final DConnectService dConnectService) {
        // NOP.
    }

    /**
     * マネージャの起動状態を通知.
     * @param dConnectService マネージャ本体
     * @param isRunning サーバ起動フラグ
     */
    protected void onManagerDetected(final DConnectService dConnectService, final boolean isRunning) {
        // NOP.
    }

    /**
     * マネージャ本体とのバインドが切断されたことを通知.
     */
    protected void onManagerLost() {
        // NOP.
    }

    /**
     * DConnectService との接続状態を取得します.
     *
     * @return DConnectService との接続状態
     */
    protected boolean isBonded() {
        return mDConnectService != null;
    }

    /**
     * Device Connec tManager の動作状況を取得します.
     *
     * @return 動作している場合はtrue、それ以外はfalse
     */
    protected boolean isDConnectServiceRunning() {
        return mDConnectService != null && mDConnectService.isRunning();
    }

    /**
     * DConnectServiceと接続するためのクラス.
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            DConnectService manager = ((DConnectService.LocalBinder) service).getDConnectService();
            mDConnectService = manager;
            onManagerBonded(manager);
            startManagerMonitor(manager);
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            mDConnectService = null;
            onManagerLost();
            stopManagerMonitor();
        }
    };

    /**
     * Manager の起動を監視するスレッドを開始します.
     * <p>
     * Manager の起動が確認できたら、{@link #onManagerDetected(DConnectService, boolean)} を呼び出します。
     * </p>
     * @param dConnectService DConnectServiceのインスタンス
     */
    private void startManagerMonitor(final DConnectService dConnectService) {
        synchronized (mManagerMonitorLock) {
            if (mManagerMonitorThread == null) {
                mManagerMonitorThread = new Thread(() -> {
                    try {
                        onManagerDetected(dConnectService, waitForManagerStartup(dConnectService));
                    } catch (InterruptedException e) {
                        // NOP.
                    } finally {
                        mManagerMonitorThread = null;
                    }
                });
                mManagerMonitorThread.start();
            }
        }
    }

    /**
     * Manager の起動を監視するスレッドを停止します.
     */
    private void stopManagerMonitor() {
        synchronized (mManagerMonitorLock) {
            if (mManagerMonitorThread != null) {
                mManagerMonitorThread.interrupt();
            }
        }
    }

    /**
     * Device Connect Manager を開始します.
     * <p>
     * callback への通知は UI スレッドで行います。
     * </p>
     * @param callback 開始結果を通知するコールバック
     */
    public void startManager(Callback<Boolean> callback) {
        if (mDConnectService == null) {
            callback.on(false);
            return;
        }

        // DConnectService を起動しておかないと bind が切れた時にサービスが止まってしまう
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), DConnectService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }

        mDConnectService.startInternal();

        new Thread(() -> {
            if (callback !=  null) {
                AtomicBoolean running = new AtomicBoolean();
                try {
                    running.set(waitForManagerStartup(mDConnectService));
                } catch (InterruptedException e) {
                    running.set(false);
                }
                runOnUiThread(() -> callback.on(running.get()));
            }
        }).start();
    }

    /**
     * Device Connect Manager を停止します.
     */
    public void stopManager() {
        if (mDConnectService == null) {
            return;
        }

        mDConnectService.stopInternal();

        // サービスを停止しておく
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), DConnectService.class);
        stopService(intent);
    }

    /**
     * レスポンスを返却するコールバック.
     *
     * @param <T>
     */
    public interface Callback<T> {
        void on(T result);
    }

    /**
     * マネージャの起動設定がONの場合は、サーバー起動完了するまで、スレッドをブロックする、それ以外の場合は、即座に処理を返す.
     * <p>
     * UI スレッドからは呼び出さないようにしてください。
     * </p>
     * @param manager マネージャ本体のサービス
     * @return 起動完了を確認した場合は<code>true</code>、それ以外の場合は<code>false</code>
     * @throws InterruptedException キャンセルされた場合
     */
    boolean waitForManagerStartup(final DConnectService manager) throws InterruptedException {
        DConnectApplication application = (DConnectApplication) getApplication();
        DConnectSettings settings = application.getSettings();
        while (!manager.isRunning() && settings.isManagerStartFlag()) {
            Thread.sleep(100);
        }
        return manager.isRunning();
    }

    /**
     * DConnectService にバインドします.
     */
    private synchronized void bindManager() {
        if (isBonded()) {
            return;
        }

        Intent bindIntent = new Intent(getApplicationContext(), DConnectService.class);
        boolean canBind = bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        if (canBind) {
            onManagerBinding();
        } else {
            onCannotManagerBonded();
        }
    }

    /**
     * DConnectService からアンバインドします.
     */
    private synchronized void unbindManager() {
        if (!isBonded()) {
            return;
        }
        unbindService(mServiceConnection);
        mDConnectService = null;
    }

    /**
     * WebSocket 情報管理クラスを取得します.
     *
     * @return  WebSocket 情報管理クラス
     */
    protected WebSocketInfoManager getWebSocketInfoManager() {
        if (mDConnectService == null) {
            return null;
        }
        return mDConnectService.getWebSocketInfoManager();
    }

    /**
     * プラグイン管理クラスを取得します.
     *
     * @return プラグイン管理クラス
     */
    protected DevicePluginManager getPluginManager() {
        if (mDConnectService == null) {
            return null;
        }
        return mDConnectService.getPluginManager();
    }

    /**
     * SSL の有効無効設定を取得します.
     *
     * @return 有効の場合はtrue、それ以外はfalse
     */
    protected Boolean isSSL() {
        DConnectApplication application = (DConnectApplication) getApplication();
        DConnectSettings settings = application.getSettings();
        if (settings == null) {
            return null;
        }
        return settings.isSSL();
    }

    /**
     * DConnectService のインスタンスを取得します.
     * <p>
     * バインドできていない場合は null を返却します。
     * </p>
     * @return DConnectService のインスタンス
     */
    public DConnectService getManagerService() {
        return mDConnectService;
    }

    /**
     * Device Connect Manager 起動中ダイアログを表示します.
     */
    public void showStaringManagerDialog() {
        showProgressDialog(R.string.activity_service_list_launch_manager_message);
    }

    /**
     * Device Connect Manager 起動中ダイアログを閉じます.
     */
    public void dismissStartingManagerDialog() {
        dismissProgressDialog();
    }

    /**
     * サービス検索中のダイアログを表示します.
     */
    public void showSearchingService() {
        showProgressDialog(R.string.activity_service_list_search_service);
    }

    /**
     * サービス検索中のダイアログを閉じます.
     */
    public void dismissSearchingService() {
        dismissProgressDialog();
    }

    /**
     * プログレスバーダイアログを表示します.
     *
     * @param resId プログレスバーに表示する文字列のリソースID
     */
    private void showProgressDialog(final int resId) {
        runOnUiThread(() -> {
            if (mActivityVisible) {
                if (mDialog != null) {
                    mDialog.dismiss();
                }
                mDialog = ProgressDialogFragment.create(getString(resId));
                mDialog.show(getFragmentManager(), "progress-dialog");
            }
        });
    }

    /**
     * プログレスバーを閉じます.
     */
    private void dismissProgressDialog() {
        runOnUiThread(() -> {
            if (mActivityVisible) {
                try {
                    mDialog.dismiss();
                } catch (Exception e) {
                    // ignore.
                }
                mDialog = null;
            }
        });
    }

    /**
     * エラーメッセージを表示します.
     *
     * @param errorMessage エラーメッセージ
     */
    public void showErrorMessage(String errorMessage) {
        if (errorMessage == null) {
            errorMessage = "null";
        }

        String title = getString(R.string.activity_service_list_error_message_title);
        String message = getString(R.string.activity_service_list_error_message_message, errorMessage);
        String positive = getString(R.string.activity_service_list_error_message_ok);
        AlertDialogFragment dialog = AlertDialogFragment.create("error-message", title, message, positive);
        dialog.show(getFragmentManager(), "error-message");
    }

    /**
     * エラーメッセージを表示します.
     *
     * @param e エラーの例外
     */
    public void showMessagingErrorDialog(final MessagingException e) {
        runOnUiThread(() -> {
            String errorMessage = findErrorMessage(e);

            Bundle args = new Bundle();
            args.putString(ErrorDialogFragment.EXTRA_MESSAGE, errorMessage);
            ErrorDialogFragment f = new ErrorDialogFragment();
            f.setArguments(args);
            f.show(getSupportFragmentManager(), "error");
        });
    }

    /**
     * 例外からエラーメッセージを取得します.
     *
     * @param e エラー原因の例外
     * @return エラーメッセージ
     */
    private String findErrorMessage(final MessagingException e) {
        switch (e.getReason()) {
            case NOT_ENABLED:
                return getString(R.string.dconnect_error_plugin_not_enabled);
            case CONNECTION_SUSPENDED:
                return getString(R.string.dconnect_error_plugin_suspended);
            default:
                return getString(R.string.dconnect_error_plugin_not_connected);
        }
    }
}
