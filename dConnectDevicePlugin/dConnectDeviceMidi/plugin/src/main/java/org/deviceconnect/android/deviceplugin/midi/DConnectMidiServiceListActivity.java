package org.deviceconnect.android.deviceplugin.midi;

import android.app.Activity;
import android.content.Intent;

import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.android.ui.activity.DConnectServiceListActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * MIDI プラグインのサービス一覧画面.
 */
public class DConnectMidiServiceListActivity extends DConnectServiceListActivity {

    @Override
    protected Class<? extends DConnectMessageService> getMessageServiceClass() {
        return MidiMessageService.class;
    }

    @Override
    protected Class<? extends Activity> getSettingManualActivityClass() {
        return DConnectMidiBleSettingsActivity.class;
    }

    @Override
    protected boolean enablesItemClick() {
        return true;
    }

    @Override
    protected void onItemClick(final DConnectService service) {
        if (service instanceof DConnectMidiDeviceService) {
            DConnectMidiDeviceService midiService = (DConnectMidiDeviceService) service;
            ServiceInfo serviceInfo = midiService.getServiceInfo();
            serviceInfo.setServiceName(midiService.getName());
            serviceInfo.setProfileNameList(getProfileNameList(midiService));

            Intent intent = new Intent(this, DConnectMidiServiceDetailActivity.class);
            intent.putExtra(DConnectMidiServiceDetailActivity.EXTRA_SERVICE_INFO, serviceInfo);
            startActivity(intent);
        }
    }

    private List<String> getProfileNameList(final DConnectService service) {
        List<String> result = new ArrayList<>();
        for (DConnectProfile profile : service.getProfileList()) {
            result.add(profile.getProfileName());
        }
        return result;
    }
}