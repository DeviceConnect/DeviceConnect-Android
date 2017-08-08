/*
 FirmataV32.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.fabo.device.arduino;

/**
 * Firmata version 3.2のCommand.
 *
 */
class FirmataV32 {

    // Firmata Buadrate
    public static final int BAUDRATE =                57600;

    // message command bytes (128-255/0x80-0xFF)

    public static final byte DIGITAL_MESSAGE =        (byte)0x90; // send data for a digital port (collection of 8 pins)
    public static final byte ANALOG_MESSAGE =         (byte)0xE0; // send data for an analog pin (or PWM)
    public static final byte REPORT_ANALOG =          (byte)0xC0; // enable analog input by pin #
    public static final byte REPORT_DIGITAL =         (byte)0xD0; // enable digital input by port pair

    public static final byte SET_PIN_MODE =           (byte)0xF4; // set a pin to INPUT/OUTPUT/PWM/etc
    public static final byte SET_DIGITAL_PIN_VALUE =  (byte)0xF5; // set value of an individual digital pin

    public static final byte REPORT_VERSION =         (byte)0xF9; // report protocol version
    public static final byte SYSTEM_RESET =           (byte)0xFF; // reset from MIDI

    public static final byte START_SYSEX =            (byte)0xF0; // start a MIDI Sysex message
    public static final byte END_SYSEX =              (byte)0xF7; // end a MIDI Sysex message

    // extended command set using sysex (0-127/0x00-0x7F)
    // 0x00-0x0F reserved for user-defined commands

    public static final byte SERIAL_DATA =             0x60; // communicate with serial devices, including other boards
    public static final byte ENCODER_DATA =            0x61; // reply with encoders current positions
    public static final byte SERVO_CONFIG =            0x70; // set max angle, minPulse, maxPulse, freq
    public static final byte STRING_DATA =             0x71; // a string message with 14-bits per char
    public static final byte STEPPER_DATA =            0x72; // control a stepper motor
    public static final byte ONEWIRE_DATA =            0x73; // send an OneWire read/write/reset/select/skip/search request
    public static final byte SHIFT_DATA =              0x75; // a bitstream to/from a shift register
    public static final byte I2C_REQUEST =             0x76; // send an I2C read/write request
    public static final byte I2C_REPLY =               0x77; // a reply to an I2C read request
    public static final byte I2C_CONFIG =              0x78; // config I2C settings such as delay times and power pins
    public static final byte REPORT_FIRMWARE =         0x79; // report name and version of the firmware
    public static final byte EXTENDED_ANALOG =         0x6F; // analog write (PWM, Servo, etc) to any pin
    public static final byte PIN_STATE_QUERY =         0x6D; // ask for a pin's current mode and value
    public static final byte PIN_STATE_RESPONSE =      0x6E; // reply with pin's current mode and value
    public static final byte CAPABILITY_QUERY =        0x6B; // ask for supported modes and resolution of all pins
    public static final byte CAPABILITY_RESPONSE =     0x6C; // reply with supported modes and resolution
    public static final byte ANALOG_MAPPING_QUERY =    0x69; // ask for mapping of analog to pin numbers
    public static final byte ANALOG_MAPPING_RESPONSE = 0x6A; // reply with mapping info
    public static final byte SAMPLING_INTERVAL =       0x7A; // set the poll rate of the main loop
    public static final byte SCHEDULER_DATA =          0x7B; // send a createtask/deletetask/addtotask/schedule/querytasks/querytask request to the scheduler
    public static final byte SYSEX_NON_REALTIME =      0x7E; // MIDI Reserved for non-realtime messages
    public static final byte SYSEX_REALTIME =          0x7F; // MIDI Reserved for realtime messages

    // Param
    public static final byte DISABLE =                (byte)0x00;
    public static final byte ENABLE =                 (byte)0x01;

    // I2C
    public static final byte I2C_WRITE = 0x00;
    public static final byte I2C_READ = 0x08;
    public static final byte I2C_READ_CONTINUOUSLY = 0x10;
    public static final byte I2C_STOP_READING = 0x18;
}

