/*
 MidiSystemProfile.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.midi.profiles;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.midi.DConnectMidiSettingsListActivity;
import org.deviceconnect.android.profile.SystemProfile;

/**
 * System プロファイルの実装.
 *
 * @author NTT DOCOMO, INC.
 */
public class MidiSystemProfile extends SystemProfile {
    @Override
    protected Class<? extends Activity> getSettingPageActivity(final Intent request, final Bundle param) {
        return DConnectMidiSettingsListActivity.class;
    }
}