package org.deviceconnect.android.deviceplugin.midi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

/**
 * MIDI プラグインのサービス詳細情報画面.
 */
public class DConnectMidiServiceDetailActivity extends Activity {

    static final String EXTRA_SERVICE_INFO = "serviceInfo";

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.service_detail_activity);

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        ServiceInfo serviceInfo = intent.getParcelableExtra(EXTRA_SERVICE_INFO);
        if (serviceInfo == null) {
            finish();
            return;
        }

        setTitle(serviceInfo.getServiceName());

        CustomTableRow protocolVersionRow = findViewById(R.id.service_detail_item_title_protocol_version);
        protocolVersionRow.setItemContent(serviceInfo.getProtocolVersion());

        CustomTableRow directionRow = findViewById(R.id.service_detail_item_title_direction);
        directionRow.setItemContent(getDirectionName(serviceInfo));

        CustomTableRow deviceNameRow = findViewById(R.id.service_detail_item_title_device_name);
        deviceNameRow.setItemContent(serviceInfo.getProductName());

        CustomTableRow manufacturerNameRow = findViewById(R.id.service_detail_item_title_manufacturer_name);
        manufacturerNameRow.setItemContent(serviceInfo.getManufacturerName());

        CustomTableRow profileListRow = findViewById(R.id.service_detail_item_title_device_connect_profile_list);
        profileListRow.setItemContent(getProfileNameList(serviceInfo));
    }

    private String getDirectionName(final ServiceInfo serviceInfo) {
        switch (serviceInfo.getPortType()) {
            case ServiceInfo.PORT_TYPE_INPUT:
                return getString(R.string.input);
            case ServiceInfo.PORT_TYPE_OUTPUT:
                return getString(R.string.output);
            default:
                return getString(R.string.unknown);
        }
    }

    private String getProfileNameList(final ServiceInfo serviceInfo) {
        StringBuilder result = new StringBuilder();
        for (String profileName : serviceInfo.getProfileNameList()) {
            result.append(profileName);
            result.append("\n");
        }
        return result.toString();
    }
}
