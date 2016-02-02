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

    /**
     * The HMAC manager.
     */
    private HmacManager mHmacManager;

    /**
     * The logger.
     */
    protected final Logger mLogger = Logger.getLogger("dconnect.manager");

    /**
     * The button to launch or stop Device Connect Manager.
     */
    private Button mLaunchOrStopButton;

    /**
     * The text view to show a prompt message.
     */
    private TextView mMessageView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dconnect_launcher);
        mHmacManager = new HmacManager(this);

        mMessageView = (TextView) findViewById(R.id.text_manager_launcher_message);
        mLaunchOrStopButton = (Button) findViewById(R.id.button_manager_launcher_launch_or_stop);
        Button cancelButton = (Button) findViewById(R.id.button_manager_launcher_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                finish();
            }
        });

        Intent i1 = new Intent();
        i1.setClass(this, DConnectService.class);
        startService(i1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        if (intent != null && isSchemeForLaunch(intent.getScheme())) {
            String key = intent.getStringExtra(IntentDConnectMessage.EXTRA_KEY);
            String origin = intent.getStringExtra(IntentDConnectMessage.EXTRA_ORIGIN);
            mLogger.info("Requested to update HMAC key from intent: origin=" + origin + ", key=" + key);

            if (key == null || origin == null) {
                mLogger.warning("Origin or key is missing.");
                Uri uri = intent.getData();
                if (uri != null) {
                    key = uri.getQueryParameter(IntentDConnectMessage.EXTRA_KEY);
                    origin = uri.getQueryParameter(IntentDConnectMessage.EXTRA_ORIGIN);
                    mLogger.info("Requested to update HMAC key from URI: origin=" + origin + ", key=" + key);
                }
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

        Intent bindIntent = new Intent(IDConnectService.class.getName());
        bindIntent.setPackage(getPackageName());
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        unbindService(mServiceConnection);
        super.onPause();
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

    /**
     * Toggles the text on views.
     *
     * @param isLaunched <code>true</code> if Device Connect Manager is running, otherwise <code>false</code>
     */
    private void toggleButton(final boolean isLaunched) {
        if (isLaunched) {
            mMessageView.setText(R.string.activity_launch_message_stop);
            mLaunchOrStopButton.setText(R.string.activity_launch_button_stop);
            mLaunchOrStopButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if (mDConnectService != null) {
                        try {
                            mDConnectService.stop();
                        } catch (RemoteException e) {
                            // do nothing
                            mLogger.warning("Failed to stop service");
                        }
                    }
                    finish();
                }
            });
        } else {
            mMessageView.setText(R.string.activity_launch_message_launch);
            mLaunchOrStopButton.setText(R.string.activity_launch_button_launch);
            mLaunchOrStopButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if (mDConnectService != null) {
                        try {
                            mDConnectService.start();
                        } catch (RemoteException e) {
                            // do nothing
                            mLogger.warning("Failed to start service");
                        }
                    }
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
     * DConnectServiceを操作するクラス.
     */
    private IDConnectService mDConnectService;

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
                    try {
                        boolean running = mDConnectService.isRunning();
                        toggleButton(running);
                    } catch (RemoteException e) {
                        mLogger.warning("Failed to get service");
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
