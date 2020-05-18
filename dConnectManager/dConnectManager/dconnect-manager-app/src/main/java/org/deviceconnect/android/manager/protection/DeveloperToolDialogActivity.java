package org.deviceconnect.android.manager.protection;

import android.app.Activity;
import android.content.Intent;
import android.provider.Settings;

import org.deviceconnect.android.manager.R;
import org.deviceconnect.android.manager.util.SimpleDialogFragment;

/**
 * 開発者向けオプション画面を開くことの確認ダイアログを表示するアクティビティ.
 */
public class DeveloperToolDialogActivity extends Activity implements SimpleDialogFragment.Callback {

    /**
     * 確認ダイアンログのタグを定義.
     */
    private static final String TAG_DIALOG = "developer-tool-dialog";

    @Override
    protected void onResume() {
        super.onResume();
        showDialog();
    }

    @Override
    public void onDialogPositiveButtonClicked(final SimpleDialogFragment dialog) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onDialogNegativeButtonClicked(final SimpleDialogFragment dialog) {
        finish();
    }

    private void showDialog() {
        SimpleDialogFragment f = new SimpleDialogFragment.Builder()
                .setTitle(getString(R.string.copy_protection_usb_debug_setting_prompt_dialog_title))
                .setMessage(getString(R.string.copy_protection_usb_debug_setting_prompt_dialog_message))
                .setPositive(getString(R.string.copy_protection_usb_debug_setting_prompt_dialog_positive))
                .setNegative(getString(R.string.copy_protection_usb_debug_setting_prompt_dialog_negative))
                .setCancelable(false)
                .create();
        f.show(getFragmentManager(), TAG_DIALOG);
    }
}
