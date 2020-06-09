package org.deviceconnect.android.deviceplugin.midi;

import android.media.midi.MidiDevice;

public class MidiDeviceWrapper {

    private final MidiDevice mMidiDevice;

    public MidiDeviceWrapper(final MidiDevice midiDevice) {
        mMidiDevice = midiDevice;
    }
}
