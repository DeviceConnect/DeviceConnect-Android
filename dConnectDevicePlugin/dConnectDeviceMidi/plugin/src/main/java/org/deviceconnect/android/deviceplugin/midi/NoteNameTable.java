/*
 NoteNameTable.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.midi;

/**
 * 音名と MIDI ノート番号の対応表.
 *
 * 音名の仕様は国際式に基づくものとする.
 *
 * @author NTT DOCOMO, INC.
 */
public class NoteNameTable {

    /**
     * 音名の個数.
     */
    private static final int NUM = 128;

    /**
     * 音名のリスト. キーは MIDI ノート番号とする.
     */
    private static final String[] NOTE_NAME_LIST = new String[NUM];
    static {
        String[] baseNames = {
                "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
        };
        final int offset = -1;
        for (int noteNum = 0; noteNum < NUM; noteNum++) {
            String octave = String.valueOf((noteNum / baseNames.length) + offset);
            String noteName = baseNames[noteNum % baseNames.length] + octave;
            NOTE_NAME_LIST[noteNum] = noteName;
        }
    }

    /**
     * 指定した MIDI ノート番号に対応する音名を取得する.
     *
     * @param number MIDI ノート番号
     * @return 音名. 見つからない場合は <code>null</code>
     */
    public static String numberToName(final int number) {
        if (number < 0 || number >= NUM) {
            return null;
        }
        return NOTE_NAME_LIST[number];
    }

    /**
     * 指定した音名に対応する MIDI ノート番号を取得する.
     *
     * @param name 音名
     * @return MIDI ノート番号. 見つからない場合は <code>null</code>
     */
    public static Integer nameToNumber(final String name) {
        for (int noteNum = 0; noteNum < NUM; noteNum++) {
            if (NOTE_NAME_LIST[noteNum].equals(name)) {
                return noteNum;
            }
        }
        return null;
    }
}
