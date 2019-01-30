/*
 NFCWriterActivity.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.tag.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.tag.R;
import org.deviceconnect.android.deviceplugin.tag.services.TagConstants;
import org.deviceconnect.android.deviceplugin.tag.services.nfc.exception.NFCWriteException;
import org.deviceconnect.android.deviceplugin.tag.services.nfc.NFCWriter;

/**
 * NFC を書き込むための Activity.
 *
 * @author NTT DOCOMO, INC.
 */
public class NFCWriterActivity extends NFCBaseActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_writer);
    }

    @Override
    protected void onFoundNFC(final Tag tag) {
        openWriteTag(tag);
    }

    @Override
    protected void onDisableNFC() {
        postTagWriterActivityResult(TagConstants.RESULT_DISABLED);
    }

    @Override
    protected void onNotResponse() {
        postTagWriterActivityResult(TagConstants.RESULT_FAILED);
    }

    /**
     * タグに書き込むタイプを取得します.
     *
     * @return タグに書き込むタイプ
     */
    private String getTagType() {
        String tagType = null;
        Intent intent = getIntent();
        if (intent != null) {
            tagType = intent.getStringExtra(TagConstants.EXTRA_TAG_TYPE);
        }
        if (tagType == null) {
            tagType = TagConstants.TYPE_TEXT;
        }
        return tagType;
    }

    /**
     * タグに書き込むテキストの言語コードを取得します.
     *
     * @return タグに書き込むテキストの言語コード
     */
    private String getLanguageCode() {
        String languageCode = null;
        Intent intent = getIntent();
        if (intent != null) {
            languageCode = intent.getStringExtra(TagConstants.EXTRA_LANGUAGE_CODE);
        }
        if (languageCode == null) {
            languageCode = "en";
        }
        return languageCode;
    }

    /**
     * タグに書き込むマイムタイプを取得します.
     *
     * @return タグに書き込むマイムタイプ
     */
    private String getMimeType() {
        String mimeType = null;
        Intent intent = getIntent();
        if (intent != null) {
            mimeType = intent.getStringExtra(TagConstants.EXTRA_MIME_TYPE);
        }
        if (mimeType == null) {
            mimeType = "text/plain";
        }
        return mimeType;
    }

    /**
     * タグに書き込むデータを取得します.
     *
     * @return タグに書き込むデータ
     */
    private byte[] getMimeData() {
        byte[] mimeData = null;
        Intent intent = getIntent();
        if (intent != null) {
            mimeData = intent.getByteArrayExtra(TagConstants.EXTRA_MIME_DATA);
        }
        if (mimeData == null) {
            mimeData = "".getBytes();
        }
        return mimeData;
    }

    /**
     * タグに書き込むデータを取得します.
     *
     * @return タグに書き込むデータ
     */
    private String getTagData() {
        Intent intent = getIntent();
        if (intent != null) {
            return intent.getStringExtra(TagConstants.EXTRA_TAG_DATA);
        }
        return null;
    }

    /**
     * NFC に書き込むを確認するダイアログを表示します.
     *
     * @param tag 書き込み先のNFCタグ
     */
    private void openWriteTag(final Tag tag) {
        runOnUiThread(() -> new AlertDialog.Builder(NFCWriterActivity.this)
                .setTitle(R.string.activity_nfc_write_dialog_title)
                .setMessage(R.string.activity_nfc_write_dialog_message)
                .setPositiveButton(R.string.activity_nfc_setting_error_btn_ok, (dialogInterface, i) -> {
                    try {
                        writeTag(tag);
                        postTagWriterActivityResult(TagConstants.RESULT_SUCCESS);
                    } catch (NFCWriteException e) {
                        switch (e.getCode()) {
                            default:
                            case IO_ERROR:
                                postTagWriterActivityResult(TagConstants.RESULT_FAILED);
                                break;
                            case INVALID_FORMAT:
                                postTagWriterActivityResult(TagConstants.RESULT_INVALID_FORMAT);
                                break;
                            case NOT_WRITABLE:
                                postTagWriterActivityResult(TagConstants.RESULT_NOT_WRIATEBLE);
                                break;
                        }
                    }
                })
                .setNegativeButton(R.string.activity_nfc_setting_error_btn_cancel, (dialogInterface, i) -> finish())
                .show());
    }

    /**
     * タグに書き込みます.
     *
     * @param tag 書き込み先のNFCタグ
     * @throws NFCWriteException 書き込みに失敗した場合に発生
     */
    private void writeTag(final Tag tag) throws NFCWriteException {
        switch (getTagType()) {
            default:
            case TagConstants.TYPE_TEXT:
                new NFCWriter().writeText(tag, getLanguageCode(), getTagData());
                break;
            case TagConstants.TYPE_URI:
                new NFCWriter().writeUri(tag, getTagData());
                break;
            case TagConstants.TYPE_MIME:
                new NFCWriter().writeMimeType(tag, getMimeType(), getMimeData());
                break;
        }
    }
}
