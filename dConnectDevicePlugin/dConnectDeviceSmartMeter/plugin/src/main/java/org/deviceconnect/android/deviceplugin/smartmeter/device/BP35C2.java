/*
 BP35C2.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.smartmeter.device;

/**
 * BP35C2.
 *
 * @author NTT DOCOMO, INC.
 */
public class BP35C2 {
    /** Tag. */
    private final static String TAG = "BP35C2";

    /** ボーレート設定値. */
    public enum Baudrate {
        BAUDRATE_115200(0, 115200),
        BAUDRATE_2400(1, 2400),
        BAUDRATE_4800(2, 4800),
        BAUDRATE_9600(3, 9600),
        BAUDRATE_19200(4, 19200),
        BAUDRATE_38400(5, 38400),
        BAUDRATE_57600(6, 57600);

        private final int index;
        private final int baudrate;
        Baudrate(final int index, final int baudrate) {
            this.index = index;
            this.baudrate = baudrate;
        }

        public int getIndex() {
            return this.index;
        }

        public int getBaudrate() {
            return this.baudrate;
        }
    }

    /** キャラクター間インターバル設定値. */
    private enum CharactorInterval {
        INTERVAL_NONE(0),
        INTERVAL_100USEC(1),
        INTERVAL_200USEC(2),
        INTERVAL_300USEC(3),
        INTERVAL_400USEC(4),
        INTERVAL_50USEC(5);

        private final int index;
        CharactorInterval(final int index) {
            this.index = index;
        }

        public int getIndex() {
            return this.index;
        }
    }

    /** フロー制御設定値. */
    private enum FlowControl {
        FLOW_CONTROL_OFF(0),
        FLOW_CONTROL_ON(1);

        private final int index;
        FlowControl(final int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    /** コマンド. */
    public enum Command {
        SET_CHANNEL("SKSREG S2 "),
        SET_PAN_ID("SKSREG S3 "),
        ECHOBACK_OFF("SKSREG SFE 0"),
        ECHOBACK_ON("SKSREG SFE 1"),
        VERSION("SKVER"),
        INFO("SKINFO"),
        SET_BID("SKSETRBID "),
        SET_BPWD("SKSETPWD "),
        DEVICE_SCAN("SKSCAN 2 FFFFFFFF 6 0"),
        JOIN("SKJOIN "),
        TERMINATE("SKTERM"),
        SEND_TO("SKSENDTO "),
        SKLL64("SKLL64 "),
        ROPT("ROPT"),
        WOPT("WOPT "),
        NONE(" "),
        ;

        private final String command;

        Command(final String command) {
            this.command = command;
        }

        public String getString() {
            return this.command;
        }
    }

    /** 応答. */
    public enum Result {
        INFO("EINFO"),
        OK("OK"),
        FAIL("FAIL"),
        EVENT_20("EVENT 20 "),
        EVENT_21("EVENT 21 "),
        EVENT_22("EVENT 22 "),
        EVENT_24("EVENT 24 "),
        EVENT_25("EVENT 25 "),
        EVENT_29("EVENT 29 "),
        RCV_UDP("ERXUDP "),
        ;

        private final String command;

        Result(final String command) {
            this.command = command;
        }

        public String getString() {
            return this.command;
        }
    }

    /** 改行コード. */
    public static String CRLF = "\r\n";
    /** 表示モード(バイナリー). */
    public static String DISP_BINARY = "00";
    /** 表示モード(アスキー). */
    public static String DISP_ASCII = "01";

    /** デバイス名称. */
    public static String DEVICE_NAME = "BP35C2";

    /** ボーレート */
    private static int baudrate = Baudrate.BAUDRATE_115200.getBaudrate();
    /** キャラクター間インターバル. */
    private static int charactorInterval = CharactorInterval.INTERVAL_NONE.getIndex();
    /** フロー制御. */
    private static int flowControl = FlowControl.FLOW_CONTROL_OFF.getIndex();

    /**
     * デバイス名称取得.
     * @return デバイス名称.
     */
    public static String getDeviceType() {
        return DEVICE_NAME;
    }

    /**
     * ボーレート取得.
     * @return ボーレート.
     */
    public int getBaudrate() {
        return baudrate;
    }

    /**
     * キャラクター間インターバル取得.
     * @return キャラクター間インターバル.
     */
    public static int getCharactorInterval() {
        return charactorInterval;
    }

    /**
     * フロー制御取得.
     * @return フロー制御.
     */
    public static int getFlowControl() {
        return flowControl;
    }

    /**
     * ボーレート設定.
     * @param baudrate ボーレート設定値.
     */
    public void setBaudrate(final Baudrate baudrate) {
        BP35C2.baudrate = baudrate.getBaudrate();
    }

    /**
     * キャラクター間インターバル設定.
     * @param charactorInterval キャラクター間インターバル設定値.
     */
    public static void setCharactorInterval(final CharactorInterval charactorInterval) {
        BP35C2.charactorInterval = charactorInterval.getIndex();
    }

    /**
     * フロー制御設定.
     * @param flowControl フロー制御設定値.
     */
    public static void setFlowControl(final FlowControl flowControl) {
        BP35C2.flowControl = flowControl.getIndex();
    }
}
