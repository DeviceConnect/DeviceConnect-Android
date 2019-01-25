/*
 NFCReader.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.tag.services.nfc;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;

import org.deviceconnect.android.deviceplugin.tag.services.TagInfo;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NFC タグを読み込むためのクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public class NFCReader {

    /**
     * RTD URI の Identifier code の定義.
     */
    private static final String[] IDENTIFIER_CODE = {
            "",
            "http://wwww.",
            "https://wwww.",
            "http://",
            "https://",
            "tel:",
            "mailto:",
            "ftp://anonymous:anonymous@",
            "ftp://ftp.",
            "ftps://",
            "sftp://",
            "smb://",
            "nfs://",
            "ftp://",
            "dav://",
            "news:",
            "telnet://",
            "imap:",
            "rtsp://",
            "urn:",
            "pop:",
            "sip:",
            "sips:",
            "rftp:",
            "btspp://",
            "btl2cap://",
            "btgoep://",
            "tcpobex://",
            "irdaobex://",
            "file://",
            "urn:epc:id:",
            "urn:epc:tag:",
            "urn:epc:pat:",
            "urn:pec:raw:",
            "urn:epc:",
            "urn:nfc:"
    };

    /**
     * タグ情報を読み込みます.
     *
     * @param tag タグ
     * @return タグ情報
     */
    public TagInfo readTag(final Tag tag) throws  FormatException {
        TagInfo tagInfo = new TagInfo();
        tagInfo.setTagId(NFCService.tagToString(tag));
        readNdef(tag, tagInfo);
        return tagInfo;
    }

    /**
     * Ndef の情報を読み込みます.
     *
     * @param tag タグ
     * @param tagInfo 情報を格納するクラス
     * @throws FormatException NFCタグのフォーマットが不正な場合に発生
     */
    private void readNdef(final Tag tag, final TagInfo tagInfo) throws FormatException {
        try {
            Ndef ndef = Ndef.get(tag);
            ndef.connect();
            readNdefMessage(ndef.getNdefMessage(), tagInfo.getList());
        } catch (IOException e) {
            // ignore.
        }
    }

    /**
     * NdefMessage の情報を指定したリストに格納します.
     *
     * @param message NFCに格納されたメッセージ
     * @param tagList 情報を格納するリスト
     */
    private void readNdefMessage(final NdefMessage message, final List<Map<String, Object>> tagList) {
        if (message != null && message.getRecords() != null) {
            for (NdefRecord record : message.getRecords()) {
                tagList.add(readRecord(record));
            }
        }
    }

    /**
     * NdefRecord の情報を読み込みます.
     *
     * @param record NdefRecord
     * @return NdefRecordの情報
     */
    private Map<String, Object> readRecord(final NdefRecord record) {
        Map<String, Object> recordInfo = new HashMap<>();

        // TODO 各タイプに合わせてフォーマットを実装

        byte[] type = record.getType();
        byte[] payload = record.getPayload();

        switch (record.getTnf()) {
            case NdefRecord.TNF_EMPTY:
                recordInfo.put("tnf", "NFC Forum empty");
                recordInfo.put("type", new String(type));
                break;
            case NdefRecord.TNF_WELL_KNOWN:
                recordInfo.put("tnf", "NFC Forum well-known type");
                recordInfo.put("type", new String(type));
                if (Arrays.equals(NdefRecord.RTD_TEXT, type)) {
                    String charsetName = ((payload[0] & 0x80) == 0) ? "UTF-8" : "UTF-16";
                    int languageCodeLength = payload[0] & 0x3F;
                    int textOffset = 1 + languageCodeLength;
                    int textLength = payload.length - 1 - languageCodeLength;
                    try {
                        recordInfo.put("languageCode", new String(payload, 1, languageCodeLength, charsetName));
                        recordInfo.put("text", new String(payload, textOffset, textLength, charsetName));
                    } catch (UnsupportedEncodingException e) {
                        // ignore.
                    }
                } else if (Arrays.equals(NdefRecord.RTD_URI, type)) {
                    String protocol = getIdentifierCode(payload[0]);
                    try {
                        recordInfo.put("uri", protocol + new String(payload, 1, payload.length - 1, "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        // ignore.
                    }
                } else if (Arrays.equals(NdefRecord.RTD_SMART_POSTER, type)) {
                    recordInfo.put("poster", new String(payload));
                }
                break;
            case NdefRecord.TNF_ABSOLUTE_URI:
                recordInfo.put("tnf", "NFC Forum absolute uri");
                recordInfo.put("type", new String(type));
                recordInfo.put("uri", new String(payload));
                break;
            case NdefRecord.TNF_EXTERNAL_TYPE:
                recordInfo.put("tnf", "NFC Forum external type");
                recordInfo.put("type", new String(type));
                recordInfo.put("external", new String(payload));
                break;
            case NdefRecord.TNF_MIME_MEDIA:
                recordInfo.put("tnf", "NFC Forum mime media");
                recordInfo.put("type", new String(type));
                recordInfo.put("mimeData", new String(payload));
                break;
            case NdefRecord.TNF_UNCHANGED:
                recordInfo.put("tnf", "NFC Forum unchanged");
                recordInfo.put("type", new String(type));
                break;
            case NdefRecord.TNF_UNKNOWN:
                recordInfo.put("tnf", "NFC Forum unknown");
                recordInfo.put("type", new String(type));
                break;
        }

        return recordInfo;
    }

    /**
     * Identifier Code の文字列を取得します.
     *
     * @param code コード
     * @return Identifier Codeの文字列
     */
    private String getIdentifierCode(int code) {
        if (code < 0 || IDENTIFIER_CODE.length <= code) {
            return "";
        }
        return IDENTIFIER_CODE[code];
    }
}
