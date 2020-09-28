/*
 MidiMessageParser.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.midi.core;

/**
 * MIDI メッセージ解析クラス.
 *
 * 与えられたバイト配列を MIDI メッセージとして解析し、
 * その結果を {@link MidiMessage} オブジェクトで返す.
 *
 * @author NTT DOCOMO, INC.
 */
public class MidiMessageParser {

    /**
     * 指定した MIDI メッセージを解析し、その結果を {@link MidiMessage} オブジェクトで返す.
     * @param message MIDI メッセージのバイト配列
     * @param offset MIDI メッセージの先頭位置
     * @param length MIDI メッセージの長さ
     * @return {@link MidiMessage} オブジェクト
     */
    public MidiMessage parse(final byte[] message, final int offset, final int length) {
        if (message == null) {
            throw new IllegalArgumentException("message is null.");
        }
        if (message.length < 1) {
            throw new IllegalArgumentException("length of message is too short.");
        }
        int type = (message[offset] & 0xF0) >> 4;
        switch (type) {
            case NoteOffMessage.MESSAGE_TYPE:
                return new NoteOffMessage(message, offset, length);
            case NoteOnMessage.MESSAGE_TYPE:
                return new NoteOnMessage(message, offset, length);
            case ControlChangeMessage.MESSAGE_TYPE:
                return new ControlChangeMessage(message, offset, length);
            default:
                return null;
        }
    }

}
