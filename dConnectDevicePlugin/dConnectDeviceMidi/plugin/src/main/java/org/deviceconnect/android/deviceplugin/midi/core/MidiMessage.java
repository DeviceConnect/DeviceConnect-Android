package org.deviceconnect.android.deviceplugin.midi.core;

import java.nio.ByteBuffer;

/**
 * MIDI メッセージ.
 */
public abstract class MidiMessage {

    public abstract void append(ByteBuffer buffer);

}
