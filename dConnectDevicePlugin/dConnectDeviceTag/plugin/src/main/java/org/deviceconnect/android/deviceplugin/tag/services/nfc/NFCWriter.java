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

import org.deviceconnect.android.deviceplugin.tag.services.nfc.exception.NFCWriteException;

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
     * @throws NFCWriteException 書き込みに失敗した場合に発生
     */
    public void writeUri(final Tag tag, final String uri) throws NFCWriteException {
        Ndef ndef = Ndef.get(tag);
        if (ndef == null) {
            throw new NFCWriteException(NFCWriteException.ErrorCode.INVALID_FORMAT, "Ndef is not supported.");
        }

        if (!ndef.isWritable()) {
            throw new NFCWriteException(NFCWriteException.ErrorCode.NOT_WRITABLE, "Ndef is not writable.");
        }

        try {
            NdefRecord extRecord = NdefRecord.createUri(uri);
            NdefMessage msg = new NdefMessage(new NdefRecord[]{extRecord});
            ndef.connect();
            ndef.writeNdefMessage(msg);
        } catch (IOException e) {
            throw new NFCWriteException(NFCWriteException.ErrorCode.IO_ERROR, "IO/Error", e);
        } catch (FormatException e) {
            throw new NFCWriteException(NFCWriteException.ErrorCode.INVALID_FORMAT, "Invalid format.", e);
        } finally {
            try {
                ndef.close();
            } catch (IOException e) {
                // ignore.
            }
        }
    }

    /**
     * NFC にテキストのデータを書き込みます.
     *
     * @param tag 書き込み先のNFCタグ
     * @param languageCode 言語コード
     * @param text 書き込むテキスト
     * @throws NFCWriteException 書き込みに失敗した場合に発生
     */
    public void writeText(final Tag tag, final String languageCode, final String text) throws NFCWriteException {
        Ndef ndef = Ndef.get(tag);
        if (ndef == null) {
            throw new NFCWriteException(NFCWriteException.ErrorCode.INVALID_FORMAT, "Ndef is not supported.");
        }

        if (!ndef.isWritable()) {
            throw new NFCWriteException(NFCWriteException.ErrorCode.NOT_WRITABLE, "Ndef is not writable.");
        }

        try {
            NdefRecord extRecord = NdefRecord.createTextRecord(languageCode, text);
            NdefMessage msg = new NdefMessage(new NdefRecord[]{extRecord});
            ndef.connect();
            ndef.writeNdefMessage(msg);
        } catch (IOException e) {
            throw new NFCWriteException(NFCWriteException.ErrorCode.IO_ERROR, "IO/Error", e);
        } catch (FormatException e) {
            throw new NFCWriteException(NFCWriteException.ErrorCode.INVALID_FORMAT, "Invalid format.", e);
        } finally {
            try {
                ndef.close();
            } catch (IOException e) {
                // ignore.
            }
        }
    }

    /**
     * NFC にMimeType付きのデータを書き込みます.
     *
     * @param tag 書き込み先のNFCタグ
     * @param mimeType 言語コード
     * @param mimeData 書き込むデータ
     * @throws NFCWriteException 書き込みに失敗した場合に発生
     */
    public void writeMimeType(final Tag tag, final String mimeType, final byte[] mimeData) throws NFCWriteException {
        Ndef ndef = Ndef.get(tag);
        if (ndef == null) {
            throw new NFCWriteException(NFCWriteException.ErrorCode.INVALID_FORMAT, "Ndef is not supported.");
        }

        if (!ndef.isWritable()) {
            throw new NFCWriteException(NFCWriteException.ErrorCode.NOT_WRITABLE, "Ndef is not writable.");
        }

        try {
            NdefRecord extRecord = NdefRecord.createMime(mimeType, mimeData);
            NdefMessage msg = new NdefMessage(new NdefRecord[]{extRecord});
            ndef.connect();
            ndef.writeNdefMessage(msg);
        } catch (IOException e) {
            throw new NFCWriteException(NFCWriteException.ErrorCode.IO_ERROR, "IO/Error", e);
        } catch (FormatException e) {
            throw new NFCWriteException(NFCWriteException.ErrorCode.INVALID_FORMAT, "Invalid format.", e);
        } finally {
            try {
                ndef.close();
            } catch (IOException e) {
                // ignore.
            }
        }
    }
}
