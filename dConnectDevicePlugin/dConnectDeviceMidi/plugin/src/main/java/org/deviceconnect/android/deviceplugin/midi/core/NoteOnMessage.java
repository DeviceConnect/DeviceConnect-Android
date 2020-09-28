/*
 NoteOnMessage.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.midi.core;

import java.nio.ByteBuffer;

/**
 * ノート・オン・メッセージ.
 *
 * @author NTT DOCOMO, INC.
 */
public class NoteOnMessage extends ChannelVoiceMessage implements NoteMessage {

    public static final int MESSAGE_TYPE = 0b1001;

    private static final int MASK_NOTE_NUMBER = 0x7F;

    private static final int MASK_VELOCITY = 0x7F;

    private int mNoteNumber;

    private int mVelocity;

    /**
     * コンストラクタ.
     * @param message MIDI メッセージのバイト配列
     * @param offset MIDI メッセージの先頭位置
     * @param length MIDI メッセージの長さ
     */
    NoteOnMessage(final byte[] message, final int offset, final int length) {
        super(message, offset, length);
        mNoteNumber = message[offset + 1] & MASK_NOTE_NUMBER;
        mVelocity = message[offset + 2] & MASK_VELOCITY;
    }

    private NoteOnMessage() {}

    public int getNoteNumber() {
        return mNoteNumber;
    }

    public int getVelocity() {
        return mVelocity;
    }

    @Override
    public void append(final ByteBuffer buffer) {
        buffer.put(getStatusByte());
        buffer.put((byte) (mNoteNumber & 0x7F));
        buffer.put((byte) (mVelocity & 0x7F));
    }

    public static class Builder extends ChannelVoiceMessage.Builder {

        private int mNoteNumber;

        private int mVelocity;

        public Builder setNoteNumber(final int noteNumber) {
            mNoteNumber = noteNumber;
            return this;
        }

        public Builder setVelocity(final int velocity) {
            mVelocity = velocity;
            return this;
        }

        public Builder setChannelNumber(final int channelNumber) {
            mChannelNumber = channelNumber;
            return this;
        }

        public NoteOnMessage build() {
            NoteOnMessage m = new NoteOnMessage();
            m.mMessageType = MESSAGE_TYPE;
            m.mChannelNumber = mChannelNumber;
            m.mNoteNumber = mNoteNumber;
            m.mVelocity = mVelocity;
            return m;
        }
    }


}
