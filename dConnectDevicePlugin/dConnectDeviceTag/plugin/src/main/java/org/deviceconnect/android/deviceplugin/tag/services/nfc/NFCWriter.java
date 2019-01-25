/*
 NFCWriter.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.tag.services.nfc;

import android.annotation.TargetApi;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Build;

import java.io.IOException;

/**
 * NFC タグにデータを書き込むクラス.
 *
 * @author NTT DOCOMO, INC.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class NFCWriter {
    /**
     * NFC に URI のデータを書き込みます.
     *
     * @param tag 書き込み先のNFCタグ
     * @param uri 書き込むURI
     * @throws IOException 書き込みに失敗した場合に発生
     * @throws FormatException NFCがNdef以外のフォーマットの場合に発生
     */
    public void writeUri(final Tag tag, final String uri) throws IOException, FormatException {
        Ndef ndef = Ndef.get(tag);
        if (ndef == null) {
            throw new FormatException("");
        }

        try {
            NdefRecord extRecord = NdefRecord.createUri(uri);
            NdefMessage msg = new NdefMessage(new NdefRecord[]{extRecord});
            ndef.connect();
            ndef.writeNdefMessage(msg);
        } finally {
            ndef.close();
        }
    }

    /**
     * NFC にテキストのデータを書き込みます.
     *
     * @param tag 書き込み先のNFCタグ
     * @param languageCode 言語コード
     * @param text 書き込むテキスト
     * @throws IOException 書き込みに失敗した場合に発生
     * @throws FormatException NFCがNdef以外のフォーマットの場合に発生
     */
    public void writeText(final Tag tag, final String languageCode, final String text) throws IOException, FormatException {
        Ndef ndef = Ndef.get(tag);
        if (ndef == null) {
            throw new FormatException("");
        }

        try {
            NdefRecord extRecord = NdefRecord.createTextRecord(languageCode, text);
            NdefMessage msg = new NdefMessage(new NdefRecord[]{extRecord});
            ndef.connect();
            ndef.writeNdefMessage(msg);
        } finally {
            ndef.close();
        }
    }

    /**
     * NFC にMimeType付きのデータを書き込みます.
     *
     * @param tag 書き込み先のNFCタグ
     * @param mimeType 言語コード
     * @param mimeData 書き込むデータ
     * @throws IOException 書き込みに失敗した場合に発生
     * @throws FormatException NFCがNdef以外のフォーマットの場合に発生
     */
    public void writeMimeType(final Tag tag, final String mimeType, final byte[] mimeData) throws IOException, FormatException {
        Ndef ndef = Ndef.get(tag);
        if (ndef == null) {
            throw new FormatException("");
        }

        try {
            NdefRecord extRecord = NdefRecord.createMime(mimeType, mimeData);
            NdefMessage msg = new NdefMessage(new NdefRecord[]{extRecord});
            ndef.connect();
            ndef.writeNdefMessage(msg);
        } finally {
            ndef.close();
        }
    }
}
