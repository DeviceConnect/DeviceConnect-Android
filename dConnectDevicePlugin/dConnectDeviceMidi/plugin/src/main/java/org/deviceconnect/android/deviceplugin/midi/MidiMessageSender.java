/*
 MidiMessageSender.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.midi;

import org.deviceconnect.android.deviceplugin.midi.core.MidiMessage;

import java.io.IOException;

/**
 * MIDI メッセージ送信インターフェース.
 *
 * @author NTT DOCOMO, INC.
 */
public interface MidiMessageSender {
    void send(int port, MidiMessage message) throws IOException;
}
