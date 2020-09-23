package org.deviceconnect.android.deviceplugin.midi.core;

import java.nio.ByteBuffer;

/**
 * MIDI メッセージ.
 */
public abstract class MidiMessage {

    /**
     * チャンネルの最大個数.
     */
    public static final int CHANNEL_MAX_COUNT = 16;

    public abstract void append(ByteBuffer buffer);

}
