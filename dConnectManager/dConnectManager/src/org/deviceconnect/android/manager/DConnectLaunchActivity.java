/*
 DConnectLaunchActivity.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.logging.Logger;

import org.deviceconnect.android.manager.hmac.HmacManager;
import org.deviceconnect.android.manager.setting.SettingActivity;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Device Connect Manager Launch Activity.
 *
 * @author NTT DOCOMO, INC.
 */
public class DConnectLaunchActivity extends Activity {

    /** The names of URI scheme for launching Device Connect Manager. */
    private static final String[] SCHEMES_LAUNCH = {"dconnect", "gotapi"};

    /** The HMAC manager. */
    private HmacManager mHmacManager;

    /** The logger. */
    protected final Logger mLogger = Logger.getLogger("dconnect.manager");

    /** The button to launch or stop Device Connect Manager. */
    private Button mLaunchOrStopButton;

    /** The text view to show a prompt message. */
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
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        toggleButton(isDConnectServiceRunning());

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
                return;
            }
        }
    }

    /**
     * Checks whether the specified scheme is for launching the Manager or not.
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
                    stopService(new Intent(DConnectLaunchActivity.this, DConnectService.class));
                    finish();
                }
            });
        } else {
            mMessageView.setText(R.string.activity_launch_message_launch);
            mLaunchOrStopButton.setText(R.string.activity_launch_button_launch);
            mLaunchOrStopButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    startService(new Intent(DConnectLaunchActivity.this, DConnectService.class));
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
     * Checks whether {@link DConnectService} is running or not.
     * @return <code>true</code> if {@link DConnectService} is running, otherwise <code>false</code>.
     */
    private boolean isDConnectServiceRunning() {
        return isServiceRunning(this, DConnectService.class);
    }

    /**
     * Checks whether the specified service is running or not.
     * @param c an instance of {@link Context}
     * @param cls the class of the specified service.
     * @return <code>true</code> if the specified service is running, otherwise <code>false</code>.
     */
    private boolean isServiceRunning(final Context c, final Class<?> cls) {
        ActivityManager am = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningServiceInfo> runningService = am.getRunningServices(Integer.MAX_VALUE);
        for (RunningServiceInfo i : runningService) {
            if (cls.getName().equals(i.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
