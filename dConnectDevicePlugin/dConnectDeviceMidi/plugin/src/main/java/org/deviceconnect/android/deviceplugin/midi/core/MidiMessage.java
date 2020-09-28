/*
 MidiMessage.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.midi.core;

import java.nio.ByteBuffer;

/**
 * MIDI メッセージ.
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class MidiMessage {

    /**
     * チャンネルの最大個数.
     */
    public static final int CHANNEL_MAX_COUNT = 16;

    public abstract void append(ByteBuffer buffer);

}
