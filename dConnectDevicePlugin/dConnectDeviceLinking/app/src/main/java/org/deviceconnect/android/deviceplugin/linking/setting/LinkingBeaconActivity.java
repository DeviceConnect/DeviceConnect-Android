/*
 LinkingBeaconActivity.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.setting;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.linking.BuildConfig;
import org.deviceconnect.android.deviceplugin.linking.LinkingApplication;
import org.deviceconnect.android.deviceplugin.linking.R;
import org.deviceconnect.android.deviceplugin.linking.beacon.LinkingBeaconManager;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.LinkingBeacon;
import org.deviceconnect.android.deviceplugin.linking.setting.fragment.dialog.ConfirmationDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class LinkingBeaconActivity extends AppCompatActivity implements LinkingBeaconManager.OnBeaconEventListener,
        LinkingBeaconManager.OnBeaconButtonEventListener, ConfirmationDialogFragment.OnDialogEventListener {

    private static final String TAG = "LinkingPlugIn";

    public static final String EXTRA_ID = "extraId";
    public static final String VENDOR_ID = "vendorId";

    private final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss", Locale.JAPAN);

    private LinkingBeacon mLinkingBeacon;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linking_beacon);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        if (intent != null) {
            Bundle args = intent.getExtras();
            if (args != null) {
                int extraId = args.getInt(EXTRA_ID);
                int vendorId = args.getInt(VENDOR_ID);
                LinkingApplication app = (LinkingApplication) getApplication();
                LinkingBeaconManager mgr = app.getLinkingBeaconManager();
                mLinkingBeacon = mgr.findBeacon(extraId, vendorId);
            }
        }

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "Linking Beacon: " + mLinkingBeacon);
        }

        Button clearBtn = (Button) findViewById(R.id.activity_beacon_clear_btn);
        if (clearBtn != null) {
            clearBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearTextView();
                }
            });
        }

        setBeaconData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LinkingApplication app = (LinkingApplication) getApplication();
        LinkingBeaconManager mgr = app.getLinkingBeaconManager();
        mgr.addOnBeaconEventListener(this);
        mgr.addOnBeaconButtonEventListener(this);
    }

    @Override
    protected void onPause() {
        LinkingApplication app = (LinkingApplication) getApplication();
        LinkingBeaconManager mgr = app.getLinkingBeaconManager();
        mgr.removeOnBeaconEventListener(this);
        mgr.removeOnBeaconButtonEventListener(this);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_beacon, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_setting:
                removeBeacon();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNotify(LinkingBeacon beacon) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setBeaconData();
            }
        });
    }

    @Override
    public void onClickButton(LinkingBeacon beacon, final int keyCode, long timeStamp) {
        if (beacon.equals(mLinkingBeacon)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addButton("buttonId:[" + keyCode + "]");
                }
            });
        }
    }

    @Override
    public void onPositiveClick() {
        LinkingApplication app = (LinkingApplication) getApplication();
        LinkingBeaconManager mgr = app.getLinkingBeaconManager();
        mgr.removeBeacon(mLinkingBeacon);
    }

    @Override
    public void onNegativeClick() {

    }

    private void removeBeacon() {
        String title = getString(R.string.activity_beacon_delete_dialog_title);
        String message = getString(R.string.activity_beacon_delete_dialog_message);
        String positive = getString(R.string.activity_beacon_delete_dialog_positive);
        String negative = getString(R.string.activity_beacon_delete_dialog_negative);
        ConfirmationDialogFragment fragment = ConfirmationDialogFragment.newInstance(title, message, positive, negative);
        fragment.show(getSupportFragmentManager(), "dialog");
    }

    private void setBeaconData() {
        if (mLinkingBeacon == null) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Linking beacon is not exist.");
            }
            return;
        }

        setExtraId(String.valueOf(mLinkingBeacon.getExtraId()));
        setTimeStamp(mLinkingBeacon.getTimeStamp());

        if (mLinkingBeacon.getBatteryData() != null) {
            setLowBattery(String.valueOf(mLinkingBeacon.getBatteryData().isLowBatteryFlag()));
            setBatteryLevel(getString(R.string.activity_beacon_unit_percent, mLinkingBeacon.getBatteryData().getLevel()));
        } else {
            setLowBattery("-");
            setBatteryLevel("-");
        }

        if (mLinkingBeacon.getTemperatureData() != null) {
            setTemperature(getString(R.string.activity_beacon_unit_c, mLinkingBeacon.getTemperatureData().getValue()));
        } else {
            setTemperature("-");
        }

        if (mLinkingBeacon.getAtmosphericPressureData() != null) {
            setAtmosphericPressure(getString(R.string.activity_beacon_unit_hectopascal, mLinkingBeacon.getAtmosphericPressureData().getValue()));
        } else {
            setAtmosphericPressure("-");
        }

        if (mLinkingBeacon.getHumidityData() != null) {
            setHumidity(getString(R.string.activity_beacon_unit_percent, mLinkingBeacon.getHumidityData().getValue()));
        } else {
            setHumidity("-");
        }

        if (mLinkingBeacon.getGattData() != null) {
            setDistance(getDistanceString(mLinkingBeacon.getGattData().getDistance()));
        } else {
            setDistance("-");
        }
    }

    private void clearTextView() {
        TextView view = (TextView) findViewById(R.id.activity_beacon_button_event);
        if (view != null) {
            view.setText("");
        }
    }

    private void setExtraId(String extraId) {
        TextView view = (TextView) findViewById(R.id.activity_beacon_extra_id);
        if (view != null) {
            view.setText(extraId);
        }
    }

    private void setTimeStamp(long timeStamp) {
        TextView view = (TextView) findViewById(R.id.activity_beacon_time_stamp);
        if (view != null) {
            view.setText(mDateFormat.format(timeStamp));
        }
    }

    private void setLowBattery(String lowBattery) {
        TextView view = (TextView) findViewById(R.id.activity_beacon_battery_low);
        if (view != null) {
            view.setText(lowBattery);
        }
    }

    private void setBatteryLevel(String level) {
        TextView view = (TextView) findViewById(R.id.activity_beacon_battery_level);
        if (view != null) {
            view.setText(level);
        }
    }

    private void setAtmosphericPressure(String atmosphericPressure) {
        TextView view = (TextView) findViewById(R.id.activity_beacon_atmospheric_pressure);
        if (view != null) {
            view.setText(atmosphericPressure);
        }
    }

    private void setTemperature(String temperature) {
        TextView view = (TextView) findViewById(R.id.activity_beacon_temperature);
        if (view != null) {
            view.setText(temperature);
        }
    }

    private void setHumidity(String humidity) {
        TextView view = (TextView) findViewById(R.id.activity_beacon_humidity);
        if (view != null) {
            view.setText(humidity);
        }
    }

    private void setDistance(String distance) {
        TextView view = (TextView) findViewById(R.id.activity_beacon_distance);
        if (view != null) {
            view.setText(distance);
        }
    }

    private String getDistanceString(int distance) {
        switch (distance) {
            case 1:
                return getString(R.string.activity_beacon_distance_close);
            case 2:
                return getString(R.string.activity_beacon_distance_little_close);
            case 3:
                return getString(R.string.activity_beacon_distance_far);
            default:
                return "-";
        }
    }

    private void addButton(String value) {
        TextView view = (TextView) findViewById(R.id.activity_beacon_button_event);
        if (view != null) {
            String text = (String) view.getText();
            text = value + "\n" + text;
            view.setText(text);
        }
    }
}
