package org.deviceconnect.android.deviceplugin.midi.profiles;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.midi.DConnectMidiServiceListActivity;
import org.deviceconnect.android.profile.SystemProfile;


public class MidiSystemProfile extends SystemProfile {
    @Override
    protected Class<? extends Activity> getSettingPageActivity(final Intent request, final Bundle param) {
        return DConnectMidiServiceListActivity.class;
    }
}