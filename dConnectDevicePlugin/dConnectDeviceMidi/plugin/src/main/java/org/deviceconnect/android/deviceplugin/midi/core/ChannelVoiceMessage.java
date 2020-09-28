/*
 ChannelVoiceMessage.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.midi.core;

/**
 * コントロール・ボイス・メッセージ.
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class ChannelVoiceMessage extends MidiMessage {

    private static final int MASK_MESSAGE_TYPE = 0b11110000;

    private static final int MASK_CHANNEL_NUMBER = 0b00001111;

    int mMessageType;

    int mChannelNumber;

    /**
     * コンストラクタ.
     * @param message MIDI メッセージのバイト配列
     * @param offset MIDI メッセージの先頭位置
     * @param length MIDI メッセージの長さ
     */
    protected ChannelVoiceMessage(final byte[] message, final int offset, final int length) {
        mMessageType = message[offset] & MASK_MESSAGE_TYPE;
        mChannelNumber = message[offset] & MASK_CHANNEL_NUMBER;
    }

    protected ChannelVoiceMessage() {}

    public byte getStatusByte() {
        return (byte) ((mMessageType << 4) | mChannelNumber & 0x0F);
    }

    public int getMessageType() {
        return mMessageType;
    }

    public int getChannelNumber() {
        return mChannelNumber;
    }

    static class Builder {
        int mChannelNumber;
    }
}
