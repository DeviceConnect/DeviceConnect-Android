/*
 ArduinoUno.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.fabo.param;

import static org.deviceconnect.android.deviceplugin.fabo.param.FaBoShield.Mode.ANALOG;
import static org.deviceconnect.android.deviceplugin.fabo.param.FaBoShield.Mode.GPIO_OUT;

/**
 * FaBoShieldのPin情報.
 */
public class FaBoShield {
    // pin modes
    public static final byte PIN_MODE_GPIO_IN = (byte) 0x00;
    public static final byte PIN_MODE_GPIO_OUT = (byte) 0x01;
    public static final byte PIN_MODE_ANALOG = (byte) 0x02;
    public static final byte PIN_MODE_PWM = (byte) 0x03;
    public static final byte PIN_MODE_SERVO = (byte) 0x04;

    // PORT0
    public final static int BIT_D0 =  (int)0x0001;  // 00000000 00000001
    public final static int BIT_D1 =  (int)0x0002;  // 00000000 00000010
    public final static int BIT_D2 =  (int)0x0004;  // 00000000 00000100
    public final static int BIT_D3 =  (int)0x0008;  // 00000000 00001000
    public final static int BIT_D4 =  (int)0x0010;  // 00000000 00010000
    public final static int BIT_D5 =  (int)0x0020;  // 00000000 00100000
    public final static int BIT_D6 =  (int)0x0040;  // 00000000 01000000
    public final static int BIT_D7 =  (int)0x0100;  // 00000001 00000000

    // PORT1
    public final static int BIT_D8 =  (int)0x0001;  // 00000000 00000001
    public final static int BIT_D9 =  (int)0x0002;  // 00000000 00000010
    public final static int BIT_D10 = (int)0x0004;  // 00000000 00000100
    public final static int BIT_D11 = (int)0x0008;  // 00000000 00001000
    public final static int BIT_D12 = (int)0x0010;  // 00000000 00010000
    public final static int BIT_D13 = (int)0x0020;  // 00000000 00100000
    public final static int BIT_A0 =  (int)0x0040;  // 00000000 01000000
    public final static int BIT_A1 =  (int)0x0100;  // 00000001 00000000

    // PORT2
    public final static int BIT_A2 =  (int)0x0001;  // 00000000 00000001
    public final static int BIT_A3 =  (int)0x0002;  // 00000000 00000010
    public final static int BIT_A4 =  (int)0x0004;  // 00000000 00000100
    public final static int BIT_A5 =  (int)0x0008;  // 00000000 00001000

    // PORT0
    public final static int PORT_D0 =  0;
    public final static int PORT_D1 =  0;
    public final static int PORT_D2 =  0;
    public final static int PORT_D3 =  0;
    public final static int PORT_D4 =  0;
    public final static int PORT_D5 =  0;
    public final static int PORT_D6 =  0;
    public final static int PORT_D7 =  0;

    // PORT1
    public final static int PORT_D8 =  1;
    public final static int PORT_D9 =  1;
    public final static int PORT_D10 = 1;
    public final static int PORT_D11 = 1;
    public final static int PORT_D12 = 1;
    public final static int PORT_D13 = 1;
    public final static int PORT_A0 =  1;
    public final static int PORT_A1 =  1;

    // PORT2
    public final static int PORT_A2 =  2;
    public final static int PORT_A3 =  2;
    public final static int PORT_A4 =  2;
    public final static int PORT_A5 =  2;

    // PIN NO
    public final static int PIN_NO_D0 =  0;
    public final static int PIN_NO_D1 =  1;
    public final static int PIN_NO_D2 =  2;
    public final static int PIN_NO_D3 =  3;
    public final static int PIN_NO_D4 =  4;
    public final static int PIN_NO_D5 =  5;
    public final static int PIN_NO_D6 =  6;
    public final static int PIN_NO_D7 =  7;
    public final static int PIN_NO_D8 =  8;
    public final static int PIN_NO_D9 =  9;
    public final static int PIN_NO_D10 = 10;
    public final static int PIN_NO_D11 = 11;
    public final static int PIN_NO_D12 = 12;
    public final static int PIN_NO_D13 = 13;
    public final static int PIN_NO_A0 =  14;
    public final static int PIN_NO_A1 =  15;
    public final static int PIN_NO_A2 =  16;
    public final static int PIN_NO_A3 =  17;
    public final static int PIN_NO_A4 =  18;
    public final static int PIN_NO_A5 =  19;

    /**
     * ピンの設定レベル.
     */
    public enum Level {
        /**
         * HIGHレベル.
         */
        HIGH(1, "HIGH"),

        /**
         * LOWレベル.
         */
        LOW(0, "LOW");

        private int mValue;
        private String mName;

        Level(final int value, final String name) {
            mValue = value;
            mName = name;
        }

        public int getValue() {
            return mValue;
        }

        public String getName() {
            return mName;
        }

        public static Level getLevel(final int value) {
            for (Level level : values()) {
                if (level.getValue() == value) {
                    return level;
                }
            }
            return null;
        }
    }

    /**
     * ピンのモード.
     */
    public enum Mode {
        GPIO_IN(PIN_MODE_GPIO_IN, "GPIOIN"),
        GPIO_OUT(PIN_MODE_GPIO_OUT, "GPIOOUT"),
        ANALOG(PIN_MODE_ANALOG, "ANALOG"),
        PWM(PIN_MODE_PWM, "PWM"),
        SERVO(PIN_MODE_SERVO, "SERVO");

        private int mValue;
        private String mName;

        Mode(final int value, final String name) {
            mValue = value;
            mName = name;
        }

        public int getValue() {
            return mValue;
        }

        public String getName() {
            return mName;
        }

        public static Mode getMode(final int value) {
            for (Mode mode : values()) {
                if (mode.getValue() == value) {
                    return mode;
                }
            }
            return null;
        }
    }

    /**
     * ピン情報の構造体.
     */
    public enum Pin {
        PIN_D0(PIN_NO_D0, PORT_D0, BIT_D0, GPIO_OUT, "0", "D0"),
        PIN_D1(PIN_NO_D1, PORT_D1, BIT_D1, GPIO_OUT, "1", "D1"),
        PIN_D2(PIN_NO_D2, PORT_D2, BIT_D2, GPIO_OUT, "2", "D2"),
        PIN_D3(PIN_NO_D3, PORT_D3, BIT_D3, GPIO_OUT, "3", "D3"),
        PIN_D4(PIN_NO_D4, PORT_D4, BIT_D4, GPIO_OUT, "4", "D4"),
        PIN_D5(PIN_NO_D5, PORT_D5, BIT_D5, GPIO_OUT, "5", "D5"),
        PIN_D6(PIN_NO_D6, PORT_D6, BIT_D6, GPIO_OUT, "6", "D6"),
        PIN_D7(PIN_NO_D7, PORT_D7, BIT_D7, GPIO_OUT, "7", "D7"),
        PIN_D8(PIN_NO_D8, PORT_D8, BIT_D8, GPIO_OUT, "8", "D8"),
        PIN_D9(PIN_NO_D9, PORT_D9, BIT_D9, GPIO_OUT, "9", "D9"),
        PIN_D10(PIN_NO_D10, PORT_D10, BIT_D10, GPIO_OUT, "10", "D10"),
        PIN_D11(PIN_NO_D11, PORT_D11, BIT_D11, GPIO_OUT, "11", "D11"),
        PIN_D12(PIN_NO_D12, PORT_D12, BIT_D12, GPIO_OUT, "12", "D12"),
        PIN_D13(PIN_NO_D13, PORT_D13, BIT_D13, GPIO_OUT, "13", "D13"),
        PIN_A0(PIN_NO_A0, PORT_A0, BIT_A0, ANALOG, "14", "A0"),
        PIN_A1(PIN_NO_A1, PORT_A1, BIT_A1, ANALOG, "15", "A1"),
        PIN_A2(PIN_NO_A2, PORT_A2, BIT_A2, ANALOG, "16", "A2"),
        PIN_A3(PIN_NO_A3, PORT_A3, BIT_A3, ANALOG, "17", "A3"),
        PIN_A4(PIN_NO_A4, PORT_A4, BIT_A4, ANALOG, "18", "A4"),
        PIN_A5(PIN_NO_A5, PORT_A5, BIT_A5, ANALOG, "19", "A5");

        private final int mPinNumber;
        private final int mPort;
        private final int mBit;
        private final String[] mPinNames;
        private Mode mMode;
        private int mValue;

        Pin(final int pinNumber, final int port, final int bit, final Mode mode, final String... pinNames) {
            mPinNumber = pinNumber;
            mPort = port;
            mBit = bit;
            mMode = mode;
            mPinNames = pinNames;
        }

        /**
         * ピン番号を取得します.
         * @return ピン番号
         */
        public int getPinNumber() {
            return mPinNumber;
        }

        /**
         * 値を保持するための配列番号を取得します.
         * @return
         */
        public int getPort() {
            return mPort;
        }

        /**
         * デジタル値を格納するビットを取得します.
         * @return デジタル値を格納するビット
         */
        public int getBit() {
            return mBit;
        }

        /**
         * ピンの名前を取得します.
         * @return ピンの名前
         */
        public String[] getPinNames() {
            return mPinNames;
        }

        /**
         * ピンのモードを取得します.
         * @return ピンのモード
         */
        public Mode getMode() {
            return mMode;
        }

        /**
         * ピンのモードを設定します.
         * @param mode ピンのモード
         */
        public void setMode(final Mode mode) {
            mMode = mode;
        }

        /**
         * ピンの値を取得します.
         * @return ピンの値
         */
        public int getValue() {
            return mValue;
        }

        /**
         * ピンの値を設定します.
         * @param value ピンの値
         */
        public void setValue(final int value) {
            mValue = value;
        }

        /**
         * ピン番号からPinのインスタンスを取得します.
         * @param number ピン番号
         * @return Pinのインスタンス
         */
        public static Pin getPin(final int number) {
            for (Pin pin : values()) {
                if (pin.getPinNumber() == number) {
                    return pin;
                }
            }
            return null;
        }

        /**
         * ピンの名前からPinのインスタンスを取得します.
         * @param pinName ピン名
         * @return Pinのインスタンス
         */
        public static Pin getPin(final String pinName) {
            for (Pin pin : values()) {
                if (pin.getPinNames()[1].equalsIgnoreCase(pinName)) {
                    return pin;
                }
            }
            return null;
        }
    }
}
