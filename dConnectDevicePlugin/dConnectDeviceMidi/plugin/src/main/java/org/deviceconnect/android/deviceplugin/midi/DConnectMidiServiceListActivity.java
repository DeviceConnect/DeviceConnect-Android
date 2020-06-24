package org.deviceconnect.android.deviceplugin.midi;

import android.app.Activity;

import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.android.ui.activity.DConnectServiceListActivity;

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
        return null;
    }

    @Override
    protected void onItemClick(final DConnectService service) {
        if (service instanceof DConnectMidiService) {
            DConnectMidiService midiService = (DConnectMidiService) service;

        }
    }
}