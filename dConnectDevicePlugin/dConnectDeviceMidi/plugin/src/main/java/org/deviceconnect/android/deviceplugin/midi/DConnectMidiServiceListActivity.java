/*
 DConnectMidiServiceListActivity.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.midi;

import android.app.Activity;
import android.content.Intent;

import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.android.ui.activity.DConnectServiceListActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * MIDI プラグインのサービス一覧画面.
 *
 * @author NTT DOCOMO, INC.
 */
public class DConnectMidiServiceListActivity extends DConnectServiceListActivity {

    private final Logger mLogger = Logger.getLogger("midi-plugin");

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

    @Override
    public void onServiceAdded(DConnectService service) {
        mLogger.info("*** onServiceAdded: name = " + service.getName());
        super.onServiceAdded(service);
    }

    @Override
    public void onServiceRemoved(DConnectService service) {
        mLogger.info("*** onServiceRemoved: name = " + service.getName());
        super.onServiceRemoved(service);
    }

    @Override
    public void onStatusChange(DConnectService service) {
        mLogger.info("*** onStatusChange: name = " + service.getName() + " isOnline = " + service.isOnline());
        super.onStatusChange(service);
    }
}