/*
 org.deviceconnect.android.deviceplugin.linking
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.setting;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.linking.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LinkingBeaconActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "extraId";

    private final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss", Locale.JAPAN);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon);

        Button clearBtn = (Button) findViewById(R.id.activity_beacon_clear_btn);
        if (clearBtn != null) {
            clearBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearTextView();
                }
            });
        }

        setData();
    }

    private void setData() {
        setExtraId("12345");
        setTimeStamp(new Date().getTime());
        setLowBattery(false);
        setBatteryLevel(34.5f);
        setTemperature(30);
        setAtmosphericPressure(1023.6f);
        setHumidity(60.0f);
        setDistance(1);
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

    private void setLowBattery(boolean lowBattery) {
        TextView view = (TextView) findViewById(R.id.activity_beacon_battery_low);
        if (view != null) {
            view.setText(String.valueOf(lowBattery));
        }
    }

    private void setBatteryLevel(float level) {
        TextView view = (TextView) findViewById(R.id.activity_beacon_battery_level);
        if (view != null) {
            view.setText(getString(R.string.activity_beacon_unit_percent, level));
        }
    }

    private void setAtmosphericPressure(float atmosphericPressure) {
        TextView view = (TextView) findViewById(R.id.activity_beacon_atmospheric_pressure);
        if (view != null) {
            view.setText(getString(R.string.activity_beacon_unit_hectopascal, atmosphericPressure));
        }
    }

    private void setTemperature(float temperature) {
        TextView view = (TextView) findViewById(R.id.activity_beacon_temperature);
        if (view != null) {
            view.setText(getString(R.string.activity_beacon_unit_c, temperature));
        }
    }

    private void setHumidity(float humidity) {
        TextView view = (TextView) findViewById(R.id.activity_beacon_humidity);
        if (view != null) {
            view.setText(getString(R.string.activity_beacon_unit_percent, humidity));
        }
    }

    private void setDistance(int distance) {
        TextView view = (TextView) findViewById(R.id.activity_beacon_distance);
        if (view != null) {
            view.setText(String.valueOf(distance));
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
