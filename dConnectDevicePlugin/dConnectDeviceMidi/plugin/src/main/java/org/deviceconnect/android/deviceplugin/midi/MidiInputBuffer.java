package org.deviceconnect.android.deviceplugin.midi;

import android.media.midi.MidiInputPort;

import org.deviceconnect.android.deviceplugin.midi.core.MidiMessage;

import java.io.IOException;
import java.nio.ByteBuffer;

class MidiInputBuffer {

    private static final int BUFFER_SIZE = 1024;

    private final MidiInputPort mMidiInputPort;

    private final byte[] mMessageArray;

    private final ByteBuffer mMessageBuffer;

    MidiInputBuffer(final MidiInputPort midiInputPort) {
        mMidiInputPort = midiInputPort;
        mMessageArray = new byte[BUFFER_SIZE];
        mMessageBuffer = ByteBuffer.wrap(mMessageArray);
    }

    void send(final MidiMessage message) throws IOException {
        synchronized (mMessageBuffer) {
            mMessageBuffer.clear();
            message.append(mMessageBuffer);
            mMidiInputPort.send(mMessageArray, 0, mMessageBuffer.position());
        }
    }

    MidiInputPort getInputPort() {
        return mMidiInputPort;
    }

    void close() {
        try {
            mMidiInputPort.close();
        } catch (IOException ignored) {}
    }
}
