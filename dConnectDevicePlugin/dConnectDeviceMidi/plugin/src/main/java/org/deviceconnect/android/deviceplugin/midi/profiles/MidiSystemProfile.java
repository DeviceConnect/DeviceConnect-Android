package org.deviceconnect.android.deviceplugin.midi.profiles;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.midi.MidiSettingActivity;
import org.deviceconnect.android.profile.SystemProfile;


public class MidiSystemProfile extends SystemProfile {
    @Override
    protected Class<? extends Activity> getSettingPageActivity(final Intent request, final Bundle param) {
        // TODO 設定画面が不要な場合、null を返却してください.
        return MidiSettingActivity.class;
    }
}