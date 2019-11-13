/*
 LinkingBeaconActivity.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.setting;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.linking.lib.BuildConfig;
import org.deviceconnect.android.deviceplugin.linking.LinkingApplication;
import org.deviceconnect.android.deviceplugin.linking.lib.R;
import org.deviceconnect.android.deviceplugin.linking.beacon.LinkingBeaconManager;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.LinkingBeacon;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class LinkingBeaconActivity extends AppCompatActivity implements LinkingBeaconManager.OnBeaconEventListener,
        LinkingBeaconManager.OnBeaconButtonEventListener {

    private static final String TAG = "LinkingPlugIn";

    public static final String EXTRA_ID = "extraId";
    public static final String VENDOR_ID = "vendorId";

    private final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss", Locale.JAPAN);

    private LinkingBeacon mLinkingBeacon;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linking_beacon);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.activity_beacon_title));
            actionBar.setElevation(0);
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
            Log.i(TAG, "LinkingBeaconActivity#onCreate");
            Log.i(TAG, "Beacon: " + mLinkingBeacon);
        }

        Button clearBtn = (Button) findViewById(R.id.activity_beacon_clear_btn);
        if (clearBtn != null) {
            clearBtn.setOnClickListener((v) -> {
                clearTextView();
            });
        }

        setBeaconData();

        if (mLinkingBeacon == null) {
            finish();
        }
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
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNotify(final LinkingBeacon beacon) {
        runOnUiThread(() -> {
            setBeaconData();
        });
    }

    @Override
    public void onClickButton(final LinkingBeacon beacon, final int keyCode, final long timeStamp) {
        if (beacon.equals(mLinkingBeacon)) {
            runOnUiThread(() -> {
                addButton("buttonId:[" + keyCode + "]");
            });
        }
    }

    private void setBeaconData() {
        if (mLinkingBeacon == null) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Linking beacon is not exist.");
            }
            return;
        }

        setVendorId(String.valueOf(mLinkingBeacon.getVendorId()));
        setExtraId(String.valueOf(mLinkingBeacon.getExtraId()));
        setVersion(String.valueOf(mLinkingBeacon.getVersion()));
        setTimeStamp(mLinkingBeacon.getTimeStamp());
        setStatus(mLinkingBeacon.isOnline() ? "Online" : "Offline");

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

        if (mLinkingBeacon.getRawData() != null) {
            setRawData(String.valueOf(mLinkingBeacon.getRawData().getValue()));
        } else {
            setRawData("-");
        }
    }

    private void clearTextView() {
        TextView view = findViewById(R.id.activity_beacon_button_event);
        if (view != null) {
            view.setText("");
        }
    }

    private void setVendorId(final String vendorId) {
        TextView view = findViewById(R.id.activity_beacon_vendor_id);
        if (view != null) {
            view.setText(vendorId);
        }
    }

    private void setExtraId(final String extraId) {
        TextView view = findViewById(R.id.activity_beacon_extra_id);
        if (view != null) {
            view.setText(extraId);
        }
    }

    private void setVersion(final String version) {
        TextView view = findViewById(R.id.activity_beacon_version);
        if (view != null) {
            view.setText(version);
        }
    }

    private void setStatus(final String status) {
        TextView view = findViewById(R.id.activity_beacon_status);
        if (view != null) {
            view.setText(status);
        }
    }

    private void setTimeStamp(final long timeStamp) {
        TextView view = findViewById(R.id.activity_beacon_time_stamp);
        if (view != null) {
            view.setText(mDateFormat.format(timeStamp));
        }
    }

    private void setLowBattery(final String lowBattery) {
        TextView view = findViewById(R.id.activity_beacon_battery_low);
        if (view != null) {
            view.setText(lowBattery);
        }
    }

    private void setBatteryLevel(final String level) {
        TextView view = findViewById(R.id.activity_beacon_battery_level);
        if (view != null) {
            view.setText(level);
        }
    }

    private void setAtmosphericPressure(final String atmosphericPressure) {
        TextView view = findViewById(R.id.activity_beacon_atmospheric_pressure);
        if (view != null) {
            view.setText(atmosphericPressure);
        }
    }

    private void setTemperature(final String temperature) {
        TextView view = findViewById(R.id.activity_beacon_temperature);
        if (view != null) {
            view.setText(temperature);
        }
    }

    private void setHumidity(final String humidity) {
        TextView view = findViewById(R.id.activity_beacon_humidity);
        if (view != null) {
            view.setText(humidity);
        }
    }

    private void setDistance(final String distance) {
        TextView view = findViewById(R.id.activity_beacon_distance);
        if (view != null) {
            view.setText(distance);
        }
    }

    private void setRawData(final String rawData) {
        TextView view = findViewById(R.id.activity_beacon_raw_data);
        if (view != null) {
            view.setText(rawData);
        }
    }

    private String getDistanceString(final int distance) {
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

    private void addButton(final String value) {
        TextView view = findViewById(R.id.activity_beacon_button_event);
        if (view != null) {
            String text = (String) view.getText();
            text = value + "\n" + text;
            view.setText(text);
        }
    }
}
