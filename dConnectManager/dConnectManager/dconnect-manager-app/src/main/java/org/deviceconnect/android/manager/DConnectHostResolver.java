package org.deviceconnect.android.manager;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Window;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.manager.core.util.DConnectUtil;
import org.deviceconnect.android.manager.setting.AlertDialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class DConnectHostResolver extends AppCompatActivity implements AlertDialogFragment.OnAlertDialogListener {

    /**
     * Webサーバ起動確認ダイアログのタグを定義します.
     */
    private static final String TAG_WEB_SERVER = "WebServer";

    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private final Logger mLogger = Logger.getLogger("dconnect.manager");

    private final Handler mPermissionHandler = new Handler(Looper.getMainLooper());

    private final List<Runnable> mPendingTaskList = new ArrayList<>();

    private DConnectWebService mWebService;

    private String mHost;

    private int mPort;

    private boolean mIsResumed;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            mWebService = ((DConnectWebService.LocalBinder) service).getDConnectWebService();
            mHost = DConnectUtil.getIPAddress(getApplicationContext());
            mPort = mWebService.getPort();

            // 最初にパーミッションを取得
            grantPermission(new PermissionUtility.PermissionRequestCallback() {
                @Override
                public void onSuccess() {
                    if (mWebService.isRunning()) {
                        showWebBrowserAndFinish(getIntent());
                    } else {
                        requestConfirmDialog();
                    }
                }

                @Override
                public void onFail(final @NonNull String deniedPermission) {
                    finish();
                }
            });
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            finish();
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        getSupportActionBar().hide();

        Intent service = new Intent(this, DConnectWebService.class);
        bindService(service, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        mIsResumed = false;
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsResumed = true;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        for (Runnable task : popTaskList()) {
            task.run();
        }
    }

    private List<Runnable> popTaskList() {
        synchronized (mPendingTaskList) {
            List<Runnable> taskList = new ArrayList<>(mPendingTaskList);
            mPendingTaskList.clear();
            return taskList;
        }
    }

    private boolean isResumed() {
        return mIsResumed;
    }

    private void finishAndUnbind() {
        unbindService(mServiceConnection);
        finish();
    }

    private void showWebBrowserAndFinish(final Intent intent) {
        Uri uri = intent.getData();
        Uri newUri = convertToHttp(uri);
        mLogger.info("Converted URI: " + newUri.toString());

        Intent newIntent = new Intent(Intent.ACTION_VIEW);
        newIntent.setData(newUri);
        startActivity(newIntent);

        finishAndUnbind();
    }

    private void requestConfirmDialog() {
        Runnable task = () -> {
            showConfirmDialog();
        };
        if (isResumed()) {
            task.run();
        } else {
            // NOTE: Activity復帰後にダイアログを表示しないと例外発生.
            mPendingTaskList.add(task);
        }
    }

    private void showConfirmDialog() {
        String title = getString(R.string.activity_settings_web_server_warning_title);
        String message = getString(R.string.activity_settings_web_server_warning_message);
        String positive = getString(R.string.activity_settings_web_server_warning_positive);
        String negative = getString(R.string.activity_settings_web_server_warning_negative);
        AlertDialogFragment dialog = AlertDialogFragment.create(TAG_WEB_SERVER,
                title, message, positive, negative);
        dialog.show(getFragmentManager(), TAG_WEB_SERVER);
    }

    private Uri convertToHttp(final Uri uri) {
        return new Uri.Builder()
                .scheme("http")
                .encodedAuthority(mHost + ":" + mPort)
                .path(uri.getPath())
                .build();
    }

    @Override
    public void onPositiveButton(final String tag) {
        if (TAG_WEB_SERVER.equals(tag)) {
            final DConnectWebService webService = mWebService;
            if (webService != null) {
                startWebServer(webService);
                showWebBrowserAndFinish(getIntent());
            }
        }
    }

    private void grantPermission(final PermissionUtility.PermissionRequestCallback callback) {
        PermissionUtility.requestPermissions(
                getApplicationContext(),
                mPermissionHandler,
                PERMISSIONS,
                callback);
    }

    private void startWebServer(final DConnectWebService webService) {
        Intent intent = new Intent();
        intent.setClass(this, DConnectWebService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        webService.startWebServer();
    }

    @Override
    public void onNegativeButton(final String tag) {
        if (TAG_WEB_SERVER.equals(tag)) {
            finishAndUnbind();
        }
    }
}
