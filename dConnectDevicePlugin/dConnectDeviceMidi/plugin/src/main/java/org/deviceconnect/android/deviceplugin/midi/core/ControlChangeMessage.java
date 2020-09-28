/*
 ControlChangeMessage.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.midi.core;

import java.nio.ByteBuffer;

/**
 * コントロール・チェンジ・メッセージ.
 *
 * @author NTT DOCOMO, INC.
 */
public class ControlChangeMessage extends ChannelVoiceMessage {

    public static final int MESSAGE_TYPE = 0b1011;

    private static final int MASK_CONTROL_NUMBER = 0x7F;

    private static final int MASK_CONTROL_VALUE = 0x7F;

    /**
     * コントロール番号.
     */
    private int mControlNumber;

    /**
     * コントロール値.
     */
    private int mControlValue;

    /**
     * コンストラクタ.
     * @param message MIDI メッセージのバイト配列
     * @param offset MIDI メッセージの先頭位置
     * @param length MIDI メッセージの長さ
     */
    public ControlChangeMessage(final byte[] message, final int offset, final int length) {
        super(message, offset, length);
        mControlNumber = message[offset + 1] & MASK_CONTROL_NUMBER;
        mControlValue = message[offset + 2] & MASK_CONTROL_VALUE;
    }

    private ControlChangeMessage() {}

    @Override
    public void append(final ByteBuffer buffer) {
        buffer.put(getStatusByte());
        buffer.put((byte) (mControlNumber & 0x7F));
        buffer.put((byte) (mControlValue & 0x7F));
    }

    public int getControlNumber() {
        return mControlNumber;
    }

    public int getControlValue() {
        return mControlValue;
    }

    public static class Builder extends ChannelVoiceMessage.Builder {

        /**
         * コントロール番号.
         */
        private int mControlNumber;

        /**
         * コントロール値.
         */
        private int mControlValue;

        public Builder setControlNumber(final int controlNumber) {
            mControlNumber = controlNumber;
            return this;
        }

        public Builder setControlValue(final int controlValue) {
            mControlValue = controlValue;
            return this;
        }

        public Builder setChannelNumber(final int channelNumber) {
            mChannelNumber = channelNumber;
            return this;
        }

        public ControlChangeMessage build() {
            ControlChangeMessage m = new ControlChangeMessage();
            m.mMessageType = MESSAGE_TYPE;
            m.mChannelNumber = mChannelNumber;
            m.mControlNumber = mControlNumber;
            m.mControlValue = mControlValue;
            return m;
        }
    }
}
