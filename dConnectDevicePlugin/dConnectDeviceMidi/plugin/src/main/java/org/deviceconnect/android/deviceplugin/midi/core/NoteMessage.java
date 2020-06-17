package org.deviceconnect.android.deviceplugin.midi.core;

import java.nio.ByteBuffer;

/**
 * ノートを制御するメッセージ.
 */
public interface NoteMessage {

    int getChannelNumber();

    int getNoteNumber();

}
