/*
 DConnectLaunchActivity.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import org.deviceconnect.android.manager.hmac.HmacManager;
import org.deviceconnect.android.manager.setting.SettingActivity;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.logging.Logger;

/**
 * Device Connect Manager Launch Activity.
 *
 * @author NTT DOCOMO, INC.
 */
public class DConnectLaunchActivity extends Activity {

    /**
     * The names of URI scheme for launching Device Connect Manager.
     */
    private static final String[] SCHEMES_LAUNCH = {"dconnect", "gotapi"};

    private static final String HOST_START = "start";

    private static final String HOST_STOP = "stop";

    private static final String PATH_ROOT = "/";

    private static final String PATH_ACTIVITY = PATH_ROOT + "activity";

    private static final String PATH_SERVER = PATH_ROOT + "server";

    private static final int RESULT_ERROR = Activity.RESULT_FIRST_USER;

    /**
     * The HMAC manager.
     */
    private HmacManager mHmacManager;

    /**
     * DConnectServiceを操作するクラス.
     */
    private IDConnectService mDConnectService;

    /**
     * The logger.
     */
    protected final Logger mLogger = Logger.getLogger("dconnect.manager");

    private DConnectSettings mSettings = DConnectSettings.getInstance();

    private Runnable mBehavior;

    /** マネージャ本体のサービスがBindされているかどうか. */
    private boolean mIsBind = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().hide();

        mHmacManager = new HmacManager(this);
        mSettings.load(this);
        processRequest(getIntent());
    }

    private boolean allowExternalStartAndStop() {
        return mSettings.allowExternalStartAndStop();
    }

    private void processRequest(final Intent intent) {
        if (intent != null && isSchemeForLaunch(intent.getScheme())) {
            updateHMACKey(intent);

            Uri uri = intent.getData();
            String host = uri.getHost();
            String path = uri.getPath();
            if (HOST_START.equals(host)) {
                preventAutoStop();
                if (!allowExternalStartAndStop() || PATH_ROOT.equals(path) || PATH_ACTIVITY.equals(path)) {
                    mBehavior = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (!mDConnectService.isRunning()) {
                                    displayActivity();
                                } else {
                                    finish();
                                }
                            } catch (RemoteException e) {
                                finish();
                            }
                        }
                    };
                } else if (PATH_SERVER.equals(path)) {
                    mBehavior = new Runnable() {
                        @Override
                        public void run() {
                            startManager();
                            onActivityResult(0, RESULT_OK, null);
                            finish();
                        }
                    };
                } else {
                    finish();
                    return;
                }
            } else if (HOST_STOP.equals(host)) {
                if (!allowExternalStartAndStop() || PATH_ROOT.equals(path) || PATH_ACTIVITY.equals(path)) {
                    mBehavior = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (mDConnectService.isRunning()) {
                                    displayActivity();
                                } else {
                                    finish();
                                }
                            } catch (RemoteException e) {
                                finish();
                            }
                        }
                    };
                } else if (PATH_SERVER.equals(path)) {
                    mBehavior = new Runnable() {
                        @Override
                        public void run() {
                            boolean canStop = !existsConnectedWebSocket();
                            int result;
                            if (canStop) {
                                stopManager();
                                result = RESULT_OK;
                            } else {
                                mLogger.warning("Cannot stop Device Connect Manager automatically.");
                                result = RESULT_ERROR;
                            }
                            onActivityResult(0, result, null);
                            finish();
                        }
                    };
                } else {
                    finish();
                    return;
                }
            }
            bindManagerService();
        } else {
            finish();
        }
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        setIntent(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        onActivityResult(0, RESULT_CANCELED, null);
        if (mIsBind) {
            unbindService(mServiceConnection);
            mIsBind = false;
        }
        finish();
    }

    /**
     * Checks whether the specified scheme is for launching the Manager or not.
     *
     * @param receivedScheme the name of custom URI scheme received from an app.
     * @return <code>true</code> if the specified scheme is for launching the Manager, otherwise <code>false</code>
     */
    private boolean isSchemeForLaunch(final String receivedScheme) {
        for (String scheme : SCHEMES_LAUNCH) {
            if (scheme.equals(receivedScheme)) {
                return true;
            }
        }
        return false;
    }

    private void updateHMACKey(final Intent intent) {
        Uri uri = intent.getData();
        String key = intent.getStringExtra(IntentDConnectMessage.EXTRA_KEY);
        String origin = intent.getStringExtra(IntentDConnectMessage.EXTRA_ORIGIN);
        mLogger.info("Requested to update HMAC key from intent: origin=" + origin + ", key=" + key);

        if (key == null || origin == null) {
            mLogger.warning("Origin or key is missing.");
            key = uri.getQueryParameter(IntentDConnectMessage.EXTRA_KEY);
            origin = uri.getQueryParameter(IntentDConnectMessage.EXTRA_ORIGIN);
            mLogger.info("Requested to update HMAC key from URI: origin=" + origin + ", key=" + key);
        }

        try {
            if (origin != null) {
                origin = URLDecoder.decode(origin, "UTF-8");
                if (key != null && !TextUtils.isEmpty(origin)) {
                    mHmacManager.updateKey(origin, key);
                }
            }
        } catch (UnsupportedEncodingException e) {
            // nothing to do.
            mLogger.warning("Failed to decode origin=" + origin);
        }
    }

    private synchronized void bindManagerService() {
        Intent bindIntent = new Intent(IDConnectService.class.getName());
        bindIntent.setPackage(getPackageName());
        mIsBind = bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void preventAutoStop() {
        Intent targetIntent = new Intent();
        targetIntent.setClass(getApplicationContext(), DConnectService.class);
        startService(targetIntent);
    }

    private void startManager() {
        if (mDConnectService != null) {
            try {
                mDConnectService.start();
            } catch (RemoteException e) {
                // do nothing
                mLogger.warning("Failed to start service");
            }
        }
    }

    private void stopManager() {
        if (mDConnectService != null) {
            try {
                mDConnectService.stop();
            } catch (RemoteException e) {
                // do nothing
                mLogger.warning("Failed to stop service");
            }
        }
    }

    private void displayActivity() {
        getActionBar().show();
        setContentView(R.layout.activity_dconnect_launcher);
        setTheme(R.style.AppTheme);
        View root = findViewById(R.id.launcher_root);
        root.setVisibility(View.VISIBLE);

        Button cancelButton = (Button) findViewById(R.id.button_manager_launcher_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                onActivityResult(0, RESULT_OK, null);
                finish();
            }
        });

        try {
            toggleButton(mDConnectService.isRunning());
        } catch (RemoteException e) {
            mLogger.warning("Failed to get service");
        }
    }

    private boolean existsConnectedWebSocket() {
        WebSocketInfoManager mgr = ((DConnectApplication) getApplication()).getWebSocketInfoManager();
        return mgr.getWebSocketInfos().size() > 0;
    }

    /**
     * Toggles the text on views.
     *
     * @param isLaunched <code>true</code> if Device Connect Manager is running, otherwise <code>false</code>
     */
    private void toggleButton(final boolean isLaunched) {
        TextView messageView = (TextView) findViewById(R.id.text_manager_launcher_message);
        Button launchOrStopButton = (Button) findViewById(R.id.button_manager_launcher_launch_or_stop);
        if (messageView == null || launchOrStopButton == null) {
            return;
        }
        if (isLaunched) {
            messageView.setText(R.string.activity_launch_message_stop);
            launchOrStopButton.setText(R.string.activity_launch_button_stop);
            launchOrStopButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    stopManager();
                    onActivityResult(0, RESULT_OK, null);
                    finish();
                }
            });
        } else {
            messageView.setText(R.string.activity_launch_message_launch);
            launchOrStopButton.setText(R.string.activity_launch_button_launch);
            launchOrStopButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    startManager();
                    onActivityResult(0, RESULT_OK, null);
                    finish();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_dconnect_launcher, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.dconnect_launcher_menu_item_settings) {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
        } else if (id == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    /**
     * DConnectServiceと接続するためのクラス.
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            mDConnectService = (IDConnectService) service;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mBehavior != null) {
                        mBehavior.run();
                    }
                }
            });
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            mDConnectService = null;
        }
    };
}
