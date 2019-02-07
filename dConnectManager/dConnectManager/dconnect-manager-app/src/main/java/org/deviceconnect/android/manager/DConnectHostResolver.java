package org.deviceconnect.android.manager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

import org.deviceconnect.android.manager.core.util.DConnectUtil;
import org.deviceconnect.android.manager.setting.AlertDialogFragment;

import java.util.logging.Logger;

public class DConnectHostResolver extends AppCompatActivity implements AlertDialogFragment.OnAlertDialogListener {

    /**
     * Webサーバ起動確認ダイアログのタグを定義します.
     */
    private static final String TAG_WEB_SERVER = "WebServer";

    private final Logger mLogger = Logger.getLogger("dconnect.manager");

    private DConnectWebService mWebService;

    private String mHost;

    private int mPort;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            mWebService = ((DConnectWebService.LocalBinder) service).getDConnectWebService();
            mHost = DConnectUtil.getIPAddress(getApplicationContext());
            mPort = mWebService.getPort();
            if (mWebService.isRunning()) {
                showWebBrowserAndFinish(getIntent());
            } else {
                showConfirmDialog();
            }
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLogger.info("DConnectHostResolver.onResume");

        Intent service = new Intent(this, DConnectWebService.class);
        bindService(service, mServiceConnection, Context.BIND_AUTO_CREATE);
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
            DConnectWebService webService = mWebService;
            if (webService != null) {
                startWebServer(webService);
                showWebBrowserAndFinish(getIntent());
            }
        }
    }

    private void startWebServer(DConnectWebService webService) {
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
