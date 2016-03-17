/*
 FaBoDeviceProvider.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.fabo.param;

/**
 * Firmata version 3.2のCommand.
 *
 */
public class FirmataV32 {

    // Firmata Buadrate
    public static final int BAUDRATE =                57600;

    // message command bytes (128-255/0x80-0xFF)
    public static final byte DIGITAL_MESSAGE =        (byte)0x90; // send data for a digital port (collection of 8 pins)
    public static final byte ANALOG_MESSAGE =         (byte)0xE0; // send data for an analog pin (or PWM)
    public static final byte REPORT_ANALOG =          (byte)0xC0; // enable analog input by pin #
    public static final byte REPORT_DIGITAL =         (byte)0xD0; // enable digital input by port pair
    //
    public static final byte SET_PIN_MODE =           (byte)0xF4; // set a pin to INPUT/OUTPUT/PWM/etc
    public static final byte SET_DIGITAL_PIN_VALUE =  (byte)0xF5; // set value of an individual digital pin

    public static final byte PIN_MODE_GPIO_IN =       (byte)0x00;
    public static final byte PIN_MODE_GPIO_OUT =      (byte)0x01;
    public static final byte PIN_MODE_ANALOG =        (byte)0x02;
    public static final byte PIN_MODE_PWM =           (byte)0x03;
    public static final byte PIN_MODE_SERVO =         (byte)0x04;

    public static final byte REPORT_VERSION =         (byte)0xF9; // report protocol version
    public static final byte SYSTEM_RESET =           (byte)0xFF; // reset from MIDI
    //
    public static final byte START_SYSEX =            (byte)0xF0; // start a MIDI Sysex message
    public static final byte END_SYSEX =              (byte)0xF7; // end a MIDI Sysex message

    // Param
    public static final byte DISABLE =                (byte)0x00;
    public static final byte ENABLE =                 (byte)0x01;
}

