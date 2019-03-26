/*
 NFCReaderActivity.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.tag.activity;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.tag.R;
import org.deviceconnect.android.deviceplugin.tag.services.TagConstants;
import org.deviceconnect.android.deviceplugin.tag.services.TagInfo;
import org.deviceconnect.android.deviceplugin.tag.services.nfc.NFCReader;

/**
 * NFC を読み込むための Activity.
 *
 * @author NTT DOCOMO, INC.
 */
public class NFCReaderActivity extends NFCBaseActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isOnce()) {
            setContentView(R.layout.activity_nfc_reader);
        }
    }

    @Override
    protected void onDisableNFC() {
        postTagReaderActivityResult(TagConstants.RESULT_DISABLED, null);
    }

    @Override
    protected void onFoundNFC(final Tag tag) {
        try {
            TagInfo tagInfo = new NFCReader().readTag(tag);
            if (tagInfo != null) {
                postTagReaderActivityResult(TagConstants.RESULT_SUCCESS, tagInfo);
            }
        } catch (Exception e) {
            postTagReaderActivityResult(TagConstants.RESULT_FAILED, null);
        }
    }

    @Override
    protected void onNotResponse() {
        postTagReaderActivityResult(TagConstants.RESULT_FAILED, null);
    }

    /**
     * 回数設定を取得します.
     *
     * @return １回の場合はtrue、それ以外はfalse
     */
    private boolean isOnce() {
        Intent intent = getIntent();
        if (intent != null) {
            return intent.getBooleanExtra(EXTRA_ONCE, false);
        }
        return true;
    }
}
