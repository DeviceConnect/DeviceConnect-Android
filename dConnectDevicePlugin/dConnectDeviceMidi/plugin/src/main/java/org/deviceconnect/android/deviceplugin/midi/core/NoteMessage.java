package org.deviceconnect.android.deviceplugin.midi.core;

/**
 * ノートを制御するメッセージのインターフェース.
 */
public interface NoteMessage {

    int getChannelNumber();

    int getNoteNumber();

}
