/*
 NoteMessage.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.midi.core;

/**
 * ノートを制御するメッセージのインターフェース.
 *
 * @author NTT DOCOMO, INC.
 */
public interface NoteMessage {

    int getChannelNumber();

    int getNoteNumber();

}
