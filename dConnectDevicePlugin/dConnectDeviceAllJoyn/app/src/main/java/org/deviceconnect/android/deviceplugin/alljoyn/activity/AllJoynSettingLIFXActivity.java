package org.deviceconnect.android.deviceplugin.alljoyn.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import org.deviceconnect.android.deviceplugin.alljoyn.R;

/**
 * Setting activity for LIFX.
 *
 * @author NTT DOCOMO, INC.
 */
public class AllJoynSettingLIFXActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aj_settings_lifx_01);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_HOME);
            actionBar.setTitle(getString(R.string.alljoyn_settings_lifx_01_textViewTopBar_text));
        }

        findViewById(R.id.imageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=com.lifx.lifx")));
                } catch (ActivityNotFoundException ex) {
                    // Simply launch Google Play instead.
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://")));
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
