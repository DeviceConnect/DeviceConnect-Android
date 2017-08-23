/*
 KeyboardCode.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hogp.util;

/**
 * キーボードのキーコードを扱うユーティリティクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public final class KeyboardCode {
    /**
     * モディファイアーキー無し.
     */
    public static final int MODIFIER_KEY_NONE = 0;

    /**
     * CTRLキー.
     */
    public static final int MODIFIER_KEY_CTRL = 1;

    /**
     * SHIFTキー.
     */
    public static final int MODIFIER_KEY_SHIFT = 2;

    /**
     * ALTキー（オプションキー）.
     */
    public static final int MODIFIER_KEY_ALT = 4;

    /**
     * GUIキー（Windowsキー・コマンドキー）.
     */
    public static final int MODIFIER_KEY_GUI = 8;

    public static final int KEY_F1 = 0x3a;
    public static final int KEY_F2 = 0x3b;
    public static final int KEY_F3 = 0x3c;
    public static final int KEY_F4 = 0x3d;
    public static final int KEY_F5 = 0x3e;
    public static final int KEY_F6 = 0x3f;
    public static final int KEY_F7 = 0x40;
    public static final int KEY_F8 = 0x41;
    public static final int KEY_F9 = 0x42;
    public static final int KEY_F10 = 0x43;
    public static final int KEY_F11 = 0x44;
    public static final int KEY_F12 = 0x45;

    public static final int KEY_PRINT_SCREEN = 0x46;
    public static final int KEY_SCROLL_LOCK = 0x47;
    public static final int KEY_CAPS_LOCK = 0x39;
    public static final int KEY_NUM_LOCK = 0x53;
    public static final int KEY_INSERT = 0x49;
    public static final int KEY_HOME = 0x4a;
    public static final int KEY_PAGE_UP = 0x4b;
    public static final int KEY_PAGE_DOWN = 0x4e;

    /**
     * 右矢印キー.
     */
    public static final byte KEY_RIGHT_ARROW = 0x4f;

    /**
     * 左矢印キー.
     */
    public static final byte KEY_LEFT_ARROW = 0x50;

    /**
     * 下矢印キー.
     */
    public static final byte KEY_DOWN_ARROW = 0x51;

    /**
     * 上矢印キー.
     */
    public static final byte KEY_UP_ARROW = 0x52;

    /**
     * エンターキー.
     */
    public static final byte KEY_ENTER = 0x28;

    /**
     * エスケープキー.
     */
    public static final byte KEY_ESC = 0x29;

    /**
     * 削除キー.
     */
    public static final byte KEY_DEL = 0x2A;

    private KeyboardCode() {}

    /**
     * 指定された文字からモディファイアーキーを取得します.
     *
     * @param aChar String contains one character
     * @return modifier code
     */
    public static byte modifier(final String aChar) {
        switch (aChar) {
            case "A":
            case "B":
            case "C":
            case "D":
            case "E":
            case "F":
            case "G":
            case "H":
            case "I":
            case "J":
            case "K":
            case "L":
            case "M":
            case "N":
            case "O":
            case "P":
            case "Q":
            case "R":
            case "S":
            case "T":
            case "U":
            case "V":
            case "W":
            case "X":
            case "Y":
            case "Z":
            case "!":
            case "@":
            case "#":
            case "$":
            case "%":
            case "^":
            case "&":
            case "*":
            case "(":
            case ")":
            case "_":
            case "+":
            case "{":
            case "}":
            case "|":
            case ":":
            case "\"":
            case "~":
            case "<":
            case ">":
            case "?":
                return MODIFIER_KEY_SHIFT;
            default:
                return 0;
        }
    }

    /**
     * 指定された文字からキーコードを取得します.
     *
     * @param aChar String contains one character
     * @return keyCode
     */
    public static byte keyCode(final String aChar) {
        switch (aChar) {
            case "A":
            case "a":
                return 0x04;
            case "B":
            case "b":
                return 0x05;
            case "C":
            case "c":
                return 0x06;
            case "D":
            case "d":
                return 0x07;
            case "E":
            case "e":
                return 0x08;
            case "F":
            case "f":
                return 0x09;
            case "G":
            case "g":
                return 0x0a;
            case "H":
            case "h":
                return 0x0b;
            case "I":
            case "i":
                return 0x0c;
            case "J":
            case "j":
                return 0x0d;
            case "K":
            case "k":
                return 0x0e;
            case "L":
            case "l":
                return 0x0f;
            case "M":
            case "m":
                return 0x10;
            case "N":
            case "n":
                return 0x11;
            case "O":
            case "o":
                return 0x12;
            case "P":
            case "p":
                return 0x13;
            case "Q":
            case "q":
                return 0x14;
            case "R":
            case "r":
                return 0x15;
            case "S":
            case "s":
                return 0x16;
            case "T":
            case "t":
                return 0x17;
            case "U":
            case "u":
                return 0x18;
            case "V":
            case "v":
                return 0x19;
            case "W":
            case "w":
                return 0x1a;
            case "X":
            case "x":
                return 0x1b;
            case "Y":
            case "y":
                return 0x1c;
            case "Z":
            case "z":
                return 0x1d;
            case "!":
            case "1":
                return 0x1e;
            case "@":
            case "2":
                return 0x1f;
            case "#":
            case "3":
                return 0x20;
            case "$":
            case "4":
                return 0x21;
            case "%":
            case "5":
                return 0x22;
            case "^":
            case "6":
                return 0x23;
            case "&":
            case "7":
                return 0x24;
            case "*":
            case "8":
                return 0x25;
            case "(":
            case "9":
                return 0x26;
            case ")":
            case "0":
                return 0x27;
            case "\n": // LF
                return 0x28;
            case "\b": // BS
                return 0x2a;
            case "\t": // TAB
                return 0x2b;
            case " ":
                return 0x2c;
            case "_":
            case "-":
                return 0x2d;
            case "+":
            case "=":
                return 0x2e;
            case "{":
            case "[":
                return 0x2f;
            case "}":
            case "]":
                return 0x30;
            case "|":
            case "\\":
                return 0x31;
            case ":":
            case ";":
                return 0x33;
            case "\"":
            case "'":
                return 0x34;
            case "~":
            case "`":
                return 0x35;
            case "<":
            case ",":
                return 0x36;
            case ">":
            case ".":
                return 0x37;
            case "?":
            case "/":
                return 0x38;
            default:
                return 0;
        }
    }
}
