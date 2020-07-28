package org.deviceconnect.android.deviceplugin.midi;

import org.deviceconnect.android.deviceplugin.midi.core.MidiMessage;

import java.io.IOException;

public interface MidiMessageSender {
    void send(int port, MidiMessage message) throws IOException;
}
