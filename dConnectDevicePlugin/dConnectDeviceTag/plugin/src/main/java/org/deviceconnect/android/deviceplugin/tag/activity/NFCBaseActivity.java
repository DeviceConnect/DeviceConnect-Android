/*
 NFCBaseActivity.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.tag.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.Settings;

import org.deviceconnect.android.deviceplugin.tag.R;
import org.deviceconnect.android.deviceplugin.tag.TagMessageService;
import org.deviceconnect.android.deviceplugin.tag.services.TagConstants;
import org.deviceconnect.android.deviceplugin.tag.services.TagService;

/**
 * NFC を操作するための Activity.
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class NFCBaseActivity extends BindServiceActivity implements TagConstants {
    /**
     * NFC 管理クラス.
     */
    private NfcAdapter mNfcAdapter;

    /**
     * 動作中フラグ.
     */
    private boolean mRunning;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mNfcAdapter == null) {
            openNotSupportNfc();
        } else if (!mNfcAdapter.isEnabled()) {
            openDisableNfc();
        } else {
            Bundle opts = new Bundle();
            opts.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 1000);

            int flags = NfcAdapter.FLAG_READER_NFC_A;
            flags |= NfcAdapter.FLAG_READER_NFC_B;
            flags |= NfcAdapter.FLAG_READER_NFC_F;
            flags |= NfcAdapter.FLAG_READER_NFC_V;

            mNfcAdapter.enableReaderMode(this, mReaderCallback, flags, opts);

            mRunning = true;
        }
    }

    @Override
    protected void onPause() {
        // NFCが動作中に終了された時に、まだレスポンスを返却していない場合にはエラーを返しておく
        if (mRunning && !isReturnedResponse()) {
            onNotResponse();
        }
        super.onPause();
    }


    @Override
    protected void onServiceConnected() {
        Tag tag = getTagFromNfc();
        if (tag != null) {
            onFoundNFC(tag);
        }
    }

    @Override
    protected TagService getTagService() {
        TagMessageService service = getBoundService();
        return service == null ? null : service.getNFCService();
    }

    /**
     * 接触しているNFCを発見した時に呼び出されます.
     *
     * @param tag 接触しているNFC
     */
    protected abstract void onFoundNFC(final Tag tag);

    /**
     * NFCが無効になっている場合に呼び出されます.
     */
    protected abstract void onDisableNFC();

    /**
     * レスポンスを返却していない場合に呼び出されます.
     */
    protected abstract void onNotResponse();

    /**
     * NFC がサポートされていないことを通知するダイアログを表示します.
     */
    private void openNotSupportNfc() {
        postTagReaderActivityResult(TagConstants.RESULT_NOT_SUPPORT, null);
    }

    /**
     * NFC が無効になっていることを通知するダイアログを表示します.
     */
    private void openDisableNfc() {
        runOnUiThread(() -> new AlertDialog.Builder(NFCBaseActivity.this)
                .setTitle(R.string.activity_nfc_setting_error_disable_nfc_title)
                .setMessage(R.string.activity_nfc_setting_error_disable_nfc_message)
                .setPositiveButton(R.string.activity_nfc_setting_error_btn_ok, (dialogInterface, i) -> {
                    openNfcSetting();
                    dialogInterface.dismiss();
                })
                .setNegativeButton(R.string.activity_nfc_setting_error_btn_cancel, (dialogInterface, i) -> {
                    onDisableNFC();
                    finish();
                })
                .show());
    }

    /**
     * NFC 設定画面を表示します.
     * <p>
     * Android 端末側の設定画面からしか NFC は有効・無効を設定できない。
     * </p>
     */
    private void openNfcSetting() {
        Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
        startActivity(intent);
    }

    /**
     * 起動用 Intent から NFC タグを取得します.
     *
     * @return NFCタグ
     */
    Tag getTagFromNfc() {
        Intent intent = getIntent();
        if (intent != null) {
            return intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        } else {
            return null;
        }
    }

    /**
     * NFC読み込み受信用コールバック.
     */
    private final NfcAdapter.ReaderCallback mReaderCallback = this::onFoundNFC;
}
